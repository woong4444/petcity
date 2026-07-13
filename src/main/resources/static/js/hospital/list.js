document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("hospitalFilterForm");
    if (!form) return;

    const ajaxUrl = form.dataset.ajaxUrl || "/hospital/list/ajax";
    const pageUrl = form.dataset.pageUrl || "/hospital/list";

    const keywordInput = document.getElementById("mainKeywordInput");
    const pageInput = document.getElementById("pageInput");
    const sortInput = document.getElementById("sortInput");
    const openStatusInput = document.getElementById("openStatusInput");

    const seoulAll = document.getElementById("seoulAll");
    const serviceAll = document.getElementById("serviceAll");

    const districtChecks = form.querySelectorAll("input[name='districts']");
    const serviceChecks = form.querySelectorAll("input[name='serviceIds']");
    const animalRadios = form.querySelectorAll("input[name='animalId']");
    const subAnimalRadios = form.querySelectorAll("input[name='subAnimalId']");

    const resetButton = document.getElementById("resetButton");
    let abortController = null;

    let gpsWgsX = null;
    let gpsWgsY = null;
    let gpsAddressName = "위치 확인 중...";

    let activeTmX = null;
    let activeTmY = null;
    let activeAddressName = null;
    let isCustomLocation = false;

    let geocoder = null;

    if (typeof kakao !== 'undefined') {
        kakao.maps.load(function() {
            if (kakao.maps.services) {
                geocoder = new kakao.maps.services.Geocoder();
            }
            initLocation();
        });
    } else {
        initLocation();
    }

    function initLocation() {
        const savedLocStr = sessionStorage.getItem('petcity_loc_data');
        if (savedLocStr) {
            const savedLoc = JSON.parse(savedLocStr);
            gpsWgsX = savedLoc.gpsWgsX;
            gpsWgsY = savedLoc.gpsWgsY;
            gpsAddressName = savedLoc.gpsAddressName;

            activeTmX = savedLoc.activeTmX;
            activeTmY = savedLoc.activeTmY;
            activeAddressName = savedLoc.activeAddressName;
            isCustomLocation = savedLoc.isCustomLocation;

            updateLocationUI();
            loadHospitalList();
            return;
        }

        let isLocationInited = false;
        const timeoutId = setTimeout(() => {
            if (!isLocationInited) {
                setDefaultLocation();
            }
        }, 3000);

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                function(position) {
                    if (isLocationInited) return;
                    clearTimeout(timeoutId);
                    isLocationInited = true;
                    gpsWgsX = position.coords.longitude;
                    gpsWgsY = position.coords.latitude;
                    resolveAddressAndApply(gpsWgsX, gpsWgsY, true, false);
                },
                function(error) {
                    if (isLocationInited) return;
                    clearTimeout(timeoutId);
                    setDefaultLocation();
                },
                { timeout: 2500 }
            );
        } else {
            clearTimeout(timeoutId);
            setDefaultLocation();
        }

        function setDefaultLocation() {
            isLocationInited = true;
            gpsWgsX = 126.9786567;
            gpsWgsY = 37.566826;
            gpsAddressName = "기본 위치 (서울시청)";
            resolveAddressAndApply(gpsWgsX, gpsWgsY, true, false);
        }
    }

    function resolveAddressAndApply(lon, lat, doSearch, isCustom) {
        if (geocoder) {
            geocoder.coord2RegionCode(lon, lat, function(result, status) {
                let addressName = "주소 알 수 없음";
                if (status === kakao.maps.services.Status.OK) {
                    for(let i=0; i<result.length; i++) {
                        if(result[i].region_type === 'H') {
                            addressName = result[i].address_name;
                            break;
                        }
                    }
                }
                if (!isCustom) gpsAddressName = addressName;
                applyLocationAndSearch(lon, lat, addressName, doSearch, isCustom);
            });
        } else {
            applyLocationAndSearch(lon, lat, "지도 API 오류", doSearch, isCustom);
        }
    }

    function updateLocationUI() {
        const gpsNameElem = document.getElementById('gpsLocationName');
        const customTextSpan = document.getElementById('customLocationText');
        const customNameSpan = document.getElementById('customLocationName');

        if (gpsNameElem) gpsNameElem.textContent = gpsAddressName;

        if (isCustomLocation && activeAddressName && activeAddressName !== gpsAddressName) {
            if(customTextSpan) customTextSpan.style.display = 'inline';
            if(customNameSpan) customNameSpan.textContent = activeAddressName;
        } else {
            if(customTextSpan) customTextSpan.style.display = 'none';
        }
    }

    function saveLocationToSession() {
        const locData = {
            gpsWgsX, gpsWgsY, gpsAddressName,
            activeTmX, activeTmY, activeAddressName,
            isCustomLocation
        };
        sessionStorage.setItem('petcity_loc_data', JSON.stringify(locData));
    }

    function applyLocationAndSearch(lon, lat, addressName, doSearch = false, isCustom = false) {
        activeAddressName = addressName;
        isCustomLocation = isCustom;
        if (!isCustom) gpsAddressName = addressName;

        updateLocationUI();

        if (geocoder) {
            geocoder.transCoord(lon, lat, function(result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    activeTmX = result[0].x;
                    activeTmY = result[0].y;

                    saveLocationToSession();

                    if (doSearch || (sortInput && sortInput.value === 'distance')) {
                        loadHospitalList();
                    }
                }
            }, {
                input_coord: kakao.maps.services.Coords.WGS84,
                output_coord: kakao.maps.services.Coords.TM
            });
        }
    }

    function makeParams() {
        const params = new URLSearchParams();

        if (activeTmX && activeTmY) {
            params.append("userLat", activeTmX);
            params.append("userLng", activeTmY);
        }

        if (pageInput && pageInput.value) params.append("page", pageInput.value);
        if (sortInput && sortInput.value) params.append("sort", sortInput.value);
        if (openStatusInput && openStatusInput.value) params.append("openStatus", openStatusInput.value);

        const checkedAnimal = form.querySelector("input[name='animalId']:checked");
        if (checkedAnimal && checkedAnimal.value !== "") params.append("animalId", checkedAnimal.value);

        const checkedSubAnimal = form.querySelector("input[name='subAnimalId']:checked");
        if (checkedSubAnimal && checkedSubAnimal.value !== "") params.append("subAnimalId", checkedSubAnimal.value);

        if (serviceAll && !serviceAll.checked) {
            form.querySelectorAll("input[name='serviceIds']:checked").forEach(svc => {
                if (svc.value !== "") params.append("serviceIds", svc.value);
            });
        }

        if (seoulAll && !seoulAll.checked) {
            form.querySelectorAll("input[name='districts']:checked").forEach(district => {
                if (district.value !== "") params.append("districts", district.value);
            });
        }

        if (keywordInput && keywordInput.value.trim() !== "") {
            params.append("keyword", keywordInput.value.trim());
        }

        return params;
    }

    function loadHospitalList() {
        const params = makeParams();
        const queryString = params.toString();

        const requestUrl = queryString ? ajaxUrl + "?" + queryString : ajaxUrl;
        const browserUrl = queryString ? pageUrl + "?" + queryString : pageUrl;
        const resultArea = document.getElementById("hospitalResultArea");

        if (resultArea) resultArea.classList.add("is-loading");

        if (abortController) abortController.abort();
        abortController = new AbortController();

        fetch(requestUrl, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" },
            signal: abortController.signal
        })
            .then(response => {
                if (!response.ok) throw new Error("네트워크 에러");
                return response.text();
            })
            .then(html => {
                const oldResultArea = document.getElementById("hospitalResultArea");
                if (oldResultArea) oldResultArea.outerHTML = html;
                window.history.pushState(null, "", browserUrl);

                rebindToolbarEvents();
                rebindMapModalTrigger();
                rebindDetailLinks();
                updateLocationUI();
            })
            .catch(error => {
                if (error.name !== "AbortError") console.error(error);
            });
    }

    function rebindDetailLinks() {
        document.querySelectorAll('.go-detail-link, .detail-button').forEach(elem => {
            elem.addEventListener('click', function(e) {
                e.preventDefault();
                let href = this.tagName === 'A' ? this.href : this.dataset.url;
                let url = new URL(href, window.location.origin);
                if (activeTmX && activeTmY) {
                    url.searchParams.set('userLat', activeTmX);
                    url.searchParams.set('userLng', activeTmY);
                }
                window.location.href = url.toString();
            });
        });

        // 🌟 찜하기(하트) 토글 로직
        document.querySelectorAll('.btn-zzim-toggle').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                const hospitalId = this.dataset.id;

                fetch('/hospital/api/zzim', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ hospitalId: hospitalId })
                })
                    .then(res => res.json())
                    .then(data => {
                        if(data.isSuccess) {
                            this.classList.toggle('active', data.isZzim);
                            const countSpan = this.querySelector('.count');
                            if(countSpan) countSpan.textContent = data.zzimCount;
                        } else {
                            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                            location.href = '/member/login';
                        }
                    })
                    .catch(err => console.error("찜하기 통신 에러:", err));
            });
        });

        // 🌟 추천하기(별) 토글 로직
        document.querySelectorAll('.btn-like-toggle').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                const hospitalId = this.dataset.id;

                fetch('/hospital/api/like', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ hospitalId: hospitalId })
                })
                    .then(res => res.json())
                    .then(data => {
                        if(data.isSuccess) {
                            this.classList.toggle('active', data.isLike);
                            this.querySelector('.count').textContent = data.likeCount;
                        } else {
                            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                            location.href = '/member/login';
                        }
                    })
                    .catch(err => console.error("추천하기 통신 에러:", err));
            });
        });
    }

    function rebindToolbarEvents() {
        const sortSelect = document.getElementById("sortSelect");
        if (sortSelect) {
            sortSelect.addEventListener("change", function() {
                if (this.value === 'distance' && (!activeTmX || !activeTmY)) {
                    alert("가까운순 정렬을 이용하시려면 기준 위치를 먼저 설정해주세요.");
                    this.value = sortInput.value;
                    return;
                }
                if (sortInput) sortInput.value = this.value;
                if (pageInput) pageInput.value = 1;
                loadHospitalList();
            });
        }

        const statusBtns = document.querySelectorAll(".status-btn");
        statusBtns.forEach(btn => {
            btn.addEventListener("click", function() {
                statusBtns.forEach(b => b.classList.remove("active"));
                this.classList.add("active");
                if (openStatusInput) openStatusInput.value = this.dataset.status;
                if (pageInput) pageInput.value = 1;
                loadHospitalList();
            });
        });

        document.querySelectorAll(".page-link").forEach(link => {
            link.addEventListener("click", function(event) {
                event.preventDefault();
                const parentLi = this.parentElement;
                if (!parentLi.classList.contains("disabled") && !parentLi.classList.contains("active")) {
                    if (pageInput) {
                        pageInput.value = this.dataset.page;
                        loadHospitalList();
                    }
                }
            });
        });
    }

    const locationModal = document.getElementById('locationModal');
    const btnCloseModal = document.getElementById('btnCloseModal');
    const btnConfirmLocation = document.getElementById('btnConfirmLocation');
    const btnGoMyLocation = document.getElementById('btnGoMyLocation');
    const selectMapContainer = document.getElementById('selectMap');

    let selectMap = null;
    let tempWgsX = null;
    let tempWgsY = null;
    let tempAddressName = null;

    if(btnCloseModal) {
        btnCloseModal.addEventListener('click', () => { locationModal.style.display = 'none'; });
    }

    if(btnConfirmLocation) {
        btnConfirmLocation.addEventListener('click', () => {
            if(tempWgsX && tempWgsY && tempAddressName) {
                applyLocationAndSearch(tempWgsX, tempWgsY, tempAddressName, true, true);
            }
            locationModal.style.display = 'none';
        });
    }

    if(btnGoMyLocation) {
        btnGoMyLocation.addEventListener('click', function() {
            if (gpsWgsX && gpsWgsY) {
                locationModal.style.display = 'none';
                resolveAddressAndApply(gpsWgsX, gpsWgsY, true, false);
            } else {
                alert("현재 내 위치(GPS) 정보를 확인할 수 없습니다.");
            }
        });
    }

    function rebindMapModalTrigger() {
        const btnLocationSelect = document.getElementById('btnLocationSelect');
        if(btnLocationSelect) {
            btnLocationSelect.addEventListener('click', function() {
                locationModal.style.display = 'flex';

                setTimeout(() => {
                    let mapLat = gpsWgsY || 37.566826;
                    let mapLng = gpsWgsX || 126.9786567;

                    if (isCustomLocation && activeTmX && activeTmY && geocoder) {
                        geocoder.transCoord(activeTmX, activeTmY, function(result, status) {
                            if (status === kakao.maps.services.Status.OK) {
                                openMap(result[0].y, result[0].x);
                            } else {
                                openMap(mapLat, mapLng);
                            }
                        }, { input_coord: kakao.maps.services.Coords.TM, output_coord: kakao.maps.services.Coords.WGS84 });
                    } else {
                        openMap(mapLat, mapLng);
                    }
                }, 150);
            });
        }
    }

    function openMap(lat, lng) {
        if(!selectMap) {
            const mapOption = { center: new kakao.maps.LatLng(lat, lng), level: 4 };
            selectMap = new kakao.maps.Map(selectMapContainer, mapOption);

            kakao.maps.event.addListener(selectMap, 'idle', function() {
                const center = selectMap.getCenter();
                tempWgsX = center.getLng();
                tempWgsY = center.getLat();

                if(geocoder) {
                    geocoder.coord2RegionCode(tempWgsX, tempWgsY, function(result, status) {
                        if (status === kakao.maps.services.Status.OK) {
                            for(let i=0; i<result.length; i++) {
                                if(result[i].region_type === 'H') {
                                    tempAddressName = result[i].address_name;
                                    document.getElementById('selectedAddressText').textContent = tempAddressName;
                                    break;
                                }
                            }
                        }
                    });
                }
            });
        } else {
            selectMap.relayout();
            selectMap.setCenter(new kakao.maps.LatLng(lat, lng));
        }
    }

    function renderRecentHospitals() {
        const recentBox = document.getElementById('quickRecentList');
        if (!recentBox) return;

        const recents = JSON.parse(localStorage.getItem('petcity_recent') || '[]');
        if (recents.length === 0) {
            recentBox.innerHTML = '<li style="font-size:11px; color:#94a3b8; padding:10px 0;">최근 본 병원이<br>없습니다.</li>';
            return;
        }

        let html = '';
        recents.forEach(h => {
            let imgHtml = h.img && h.img !== 'null' ? `<img src="${h.img}" alt="병원">` : `<div style="width:100%; height:60px; background:#e0f2fe; border-radius:8px; display:flex; align-items:center; justify-content:center; font-size:10px; color:#0284c7; font-weight:bold;">이미지 없음</div>`;
            html += `<li>
                <a href="/hospital/view?hospitalId=${h.id}" class="quick-recent-item">
                    ${imgHtml}
                    <span>${h.name}</span>
                </a>
            </li>`;
        });
        recentBox.innerHTML = html;
    }

    rebindToolbarEvents();
    rebindMapModalTrigger();
    rebindDetailLinks();
    renderRecentHospitals();

    function updateSubAnimalUI() {
        const checkedAnimal = form.querySelector("input[name='animalId']:checked");
        const subAnimalBox = document.getElementById("subAnimalBox");
        const subAnimalItems = document.querySelectorAll(".sub-animal-item");

        let hasChildren = false;

        if (checkedAnimal && checkedAnimal.value !== "") {
            const parentId = String(checkedAnimal.value);

            subAnimalItems.forEach(item => {
                if (String(item.dataset.parent) === parentId) {
                    item.style.display = "inline-flex";
                    hasChildren = true;
                } else {
                    item.style.display = "none";
                    const input = item.querySelector("input");
                    if (input && input.checked) {
                        input.checked = false;
                        input.dataset.wasChecked = "false";
                    }
                }
            });
        } else {
            subAnimalItems.forEach(item => {
                item.style.display = "none";
                const input = item.querySelector("input");
                if (input && input.checked) {
                    input.checked = false;
                    input.dataset.wasChecked = "false";
                }
            });
        }

        if (subAnimalBox) {
            subAnimalBox.style.display = hasChildren ? "block" : "none";
        }
    }

    [...animalRadios, ...subAnimalRadios].forEach(radio => {
        if (radio.checked) radio.dataset.wasChecked = "true";
        radio.addEventListener("click", function () {
            if (this.dataset.wasChecked === "true") {
                this.checked = false;
                this.dataset.wasChecked = "false";
            } else {
                const group = form.querySelectorAll(`input[name='${this.name}']`);
                group.forEach(r => r.dataset.wasChecked = "false");
                this.dataset.wasChecked = "true";
            }
            if(this.name === 'animalId') {
                updateSubAnimalUI();
            }
        });
    });

    updateSubAnimalUI();

    if (seoulAll) {
        seoulAll.addEventListener("change", function () {
            districtChecks.forEach(c => c.checked = this.checked);
            if (pageInput) pageInput.value = 1;
            loadHospitalList();
        });
    }

    districtChecks.forEach(function (check) {
        check.addEventListener("change", function () {
            if (seoulAll) {
                const total = districtChecks.length;
                const checkedCount = form.querySelectorAll("input[name='districts']:checked").length;
                seoulAll.checked = (total === checkedCount);
            }
            if (pageInput) pageInput.value = 1;
            loadHospitalList();
        });
    });

    if (serviceAll) {
        serviceAll.addEventListener("change", function () {
            serviceChecks.forEach(c => c.checked = this.checked);
            if (pageInput) pageInput.value = 1;
            loadHospitalList();
        });
    }

    serviceChecks.forEach(function (check) {
        check.addEventListener("change", function () {
            if (serviceAll) {
                const total = serviceChecks.length;
                const checkedCount = form.querySelectorAll("input[name='serviceIds']:checked").length;
                serviceAll.checked = (total === checkedCount);
            }
            if (pageInput) pageInput.value = 1;
            loadHospitalList();
        });
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        if (pageInput) pageInput.value = 1;
        loadHospitalList();
    });

    if (resetButton) {
        resetButton.addEventListener("click", function (event) {
            event.preventDefault();

            form.querySelectorAll("input[type='radio'], input[type='checkbox']").forEach(c => {
                c.checked = false;
                if (c.type === 'radio') c.dataset.wasChecked = "false";
            });

            if (keywordInput) keywordInput.value = "";
            if (pageInput) pageInput.value = 1;
            if (sortInput) sortInput.value = "recommend";
            if (openStatusInput) openStatusInput.value = "ALL";

            updateSubAnimalUI();

            sessionStorage.removeItem('petcity_loc_data');
            if (gpsWgsX && gpsWgsY) {
                resolveAddressAndApply(gpsWgsX, gpsWgsY, true, false);
            } else {
                loadHospitalList();
            }
        });
    }

    const toggleButtons = document.querySelectorAll(".btn-toggle-filter");
    toggleButtons.forEach(button => {
        button.addEventListener("click", function() {
            const contentWrap = this.parentElement.previousElementSibling;

            if(this.dataset.state === "open") {
                contentWrap.classList.add("is-minimized");
                this.dataset.state = "closed";
                this.textContent = "+ 펼치기";
            } else {
                contentWrap.classList.remove("is-minimized");
                this.dataset.state = "open";
                this.textContent = "- 접기";
            }
        });
    });

    window.addEventListener("popstate", function () {
        window.location.reload();
    });
});