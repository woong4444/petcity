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
    const subjectAll = document.getElementById("subjectAll");

    const subjectChecks = form.querySelectorAll("input[name='subjects']");
    const districtChecks = form.querySelectorAll("input[name='districts']");
    const serviceChecks = form.querySelectorAll("input[name='serviceIds']");
    const animalRadios = form.querySelectorAll("input[name='animalId']");
    const subAnimalRadios = form.querySelectorAll("input[name='subAnimalId']");

    const resetButton = document.getElementById("resetButton");
    let abortController = null;

    let gpsWgsX = null;
    let gpsWgsY = null;
    let gpsAddressName = "위치 확인 중...";

    // 🌟 변경점: TM좌표를 완전히 제거하고 위도/경도(WGS84)를 직접 저장
    let activeLat = null;
    let activeLng = null;
    let activeAddressName = null;
    let isCustomLocation = false;

    let geocoder = null;

    if (typeof kakao !== 'undefined') {
        kakao.maps.load(function () {
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

            // 과거 세션 스토리지에 남아있는 비정상 TM 데이터 대응
            activeLat = savedLoc.activeLat !== undefined ? savedLoc.activeLat : savedLoc.activeTmY;
            activeLng = savedLoc.activeLng !== undefined ? savedLoc.activeLng : savedLoc.activeTmX;

            // TM 좌표 캐시가 남아있다면 과감히 비우고 다시 찾음
            if (activeLat > 1000 || activeLng > 1000) {
                activeLat = null;
                activeLng = null;
            }

            activeAddressName = savedLoc.activeAddressName;
            isCustomLocation = savedLoc.isCustomLocation;

            if (activeLat && activeLng) {
                updateLocationUI();
                loadHospitalList();
                return;
            }
        }

        let isLocationInited = false;
        const timeoutId = setTimeout(() => {
            if (!isLocationInited) {
                setDefaultLocation();
            }
        }, 3000);

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                function (position) {
                    if (isLocationInited) return;
                    clearTimeout(timeoutId);
                    isLocationInited = true;
                    gpsWgsX = position.coords.longitude;
                    gpsWgsY = position.coords.latitude;
                    resolveAddressAndApply(gpsWgsX, gpsWgsY, true, false);
                },
                function (error) {
                    if (isLocationInited) return;
                    clearTimeout(timeoutId);
                    setDefaultLocation();
                },
                {timeout: 2500}
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
            geocoder.coord2RegionCode(lon, lat, function (result, status) {
                let addressName = "주소 알 수 없음";
                if (status === kakao.maps.services.Status.OK) {
                    for (let i = 0; i < result.length; i++) {
                        if (result[i].region_type === 'H') {
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
            if (customTextSpan) customTextSpan.style.display = 'inline';
            if (customNameSpan) customNameSpan.textContent = activeAddressName;
        } else {
            if (customTextSpan) customTextSpan.style.display = 'none';
        }
    }

    function saveLocationToSession() {
        const locData = {
            gpsWgsX, gpsWgsY, gpsAddressName,
            activeLat, activeLng, activeAddressName, // 🌟 저장 객체명 변경
            isCustomLocation
        };
        sessionStorage.setItem('petcity_loc_data', JSON.stringify(locData));
    }

    function applyLocationAndSearch(lon, lat, addressName, doSearch = false, isCustom = false) {
        activeAddressName = addressName;
        isCustomLocation = isCustom;
        if (!isCustom) gpsAddressName = addressName;

        updateLocationUI();

        // 🌟 변경점: 골치 아픈 카카오 맵 TM 변환을 없애고 WGS84 좌표를 그대로 저장
        activeLng = lon;
        activeLat = lat;

        saveLocationToSession();

        if (doSearch || (sortInput && sortInput.value === 'distance')) {
            loadHospitalList();
        }
    }

    function makeParams() {
        const params = new URLSearchParams();

        // 🌟 WGS84 좌표 전송
        if (activeLat && activeLng) {
            params.append("userLat", activeLat);
            params.append("userLng", activeLng);
        }

        if (pageInput && pageInput.value) params.append("page", pageInput.value);
        if (sortInput && sortInput.value) params.append("sort", sortInput.value);
        if (openStatusInput && openStatusInput.value) params.append("openStatus", openStatusInput.value);

        const checkedAnimal = form.querySelector("input[name='animalId']:checked");
        if (checkedAnimal && checkedAnimal.value !== "") params.append("animalId", checkedAnimal.value);

        const checkedSubAnimal = form.querySelector("input[name='subAnimalId']:checked");
        if (checkedSubAnimal && checkedSubAnimal.value !== "") params.append("subAnimalId", checkedSubAnimal.value);

        if (subjectAll && !subjectAll.checked) {
            form.querySelectorAll("input[name='subjects']:checked").forEach(subj => {
                if (subj.value !== "") params.append("subjects", subj.value);
            });
        }

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
            headers: {"X-Requested-With": "XMLHttpRequest"},
            signal: abortController.signal
        })
            .then(response => {
                if (!response.ok) throw new Error("네트워크 에러");
                return response.text();
            })
            .then(html => {
                const oldResultArea = document.getElementById("hospitalResultArea");
                if (oldResultArea) oldResultArea.outerHTML = html;
                window.history.replaceState(null, "", browserUrl);

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

            elem.onclick = null; // 혹시 모를 중복 방지

            elem.addEventListener('click', function (e) {
                e.preventDefault();

                const status = this.getAttribute('data-status');
                const notice = this.getAttribute('data-notice') || '등록된 사유가 없습니다.';

                if (status === '휴업') {
                    const msg = "🏥 해당 병원은 현재 [휴업] 중입니다.\n\n[휴업 사유 / 공지사항]\n" + notice + "\n\n그래도 상세 페이지로 이동하시겠습니까?";
                    if (!confirm(msg)) {
                        return;
                    }
                }

                let href = this.tagName === 'A' ? this.href : this.dataset.url;
                let url = new URL(href, window.location.origin);

                // 🌟 정상적인 WGS84 좌표 전달
                if (activeLat && activeLng) {
                    url.searchParams.set('userLat', activeLat);
                    url.searchParams.set('userLng', activeLng);
                }
                window.location.href = url.toString();
            });
        });

        document.querySelectorAll('.btn-zzim-toggle').forEach(btn => {
            btn.addEventListener('click', function (e) {
                e.stopPropagation();
                const hospitalId = this.dataset.id;

                fetch('/hospital/api/zzim', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: new URLSearchParams({hospitalId: hospitalId})
                })
                    .then(res => res.json())
                    .then(data => {
                        if (data.isSuccess) {
                            this.classList.toggle('active', data.isZzim);
                            const countSpan = this.querySelector('.count');
                            if (countSpan) countSpan.textContent = data.zzimCount;
                        } else {
                            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                            location.href = '/member/login';
                        }
                    })
                    .catch(err => console.error("찜하기 통신 에러:", err));
            });
        });
    }

    function rebindToolbarEvents() {
        const sortSelect = document.getElementById("sortSelect");
        if (sortSelect) {
            sortSelect.addEventListener("change", function () {
                if (this.value === 'distance' && (!activeLat || !activeLng)) {
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
            btn.addEventListener("click", function () {
                statusBtns.forEach(b => b.classList.remove("active"));
                this.classList.add("active");
                if (openStatusInput) openStatusInput.value = this.dataset.status;
                if (pageInput) pageInput.value = 1;
                loadHospitalList();
            });
        });

        document.querySelectorAll(".page-link").forEach(link => {
            link.addEventListener("click", function (event) {
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

    if (btnCloseModal) {
        btnCloseModal.addEventListener('click', () => {
            locationModal.style.display = 'none';
        });
    }

    if (btnConfirmLocation) {
        btnConfirmLocation.addEventListener('click', () => {
            if (tempWgsX && tempWgsY && tempAddressName) {
                applyLocationAndSearch(tempWgsX, tempWgsY, tempAddressName, true, true);
            }
            locationModal.style.display = 'none';
        });
    }

    if (btnGoMyLocation) {
        btnGoMyLocation.addEventListener('click', function () {
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
        if (btnLocationSelect) {
            btnLocationSelect.addEventListener('click', function () {
                locationModal.style.display = 'flex';

                setTimeout(() => {
                    let mapLat = gpsWgsY || 37.566826;
                    let mapLng = gpsWgsX || 126.9786567;

                    // 🌟 TM변환 과정 필요 없이 바로 지도 열기
                    if (isCustomLocation && activeLat && activeLng) {
                        openMap(activeLat, activeLng);
                    } else {
                        openMap(mapLat, mapLng);
                    }
                }, 150);
            });
        }
    }

    function openMap(lat, lng) {
        if (!selectMap) {
            const mapOption = {center: new kakao.maps.LatLng(lat, lng), level: 4};
            selectMap = new kakao.maps.Map(selectMapContainer, mapOption);

            kakao.maps.event.addListener(selectMap, 'idle', function () {
                const center = selectMap.getCenter();
                tempWgsX = center.getLng();
                tempWgsY = center.getLat();

                if (geocoder) {
                    geocoder.coord2RegionCode(tempWgsX, tempWgsY, function (result, status) {
                        if (status === kakao.maps.services.Status.OK) {
                            for (let i = 0; i < result.length; i++) {
                                if (result[i].region_type === 'H') {
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

    rebindToolbarEvents();
    rebindMapModalTrigger();
    rebindDetailLinks();

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
            if (this.name === 'animalId') {
                updateSubAnimalUI();
            }
            if (pageInput) pageInput.value = 1;
            loadHospitalList();
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

    if (subjectAll) {
        subjectAll.addEventListener("change", function () {
            subjectChecks.forEach(c => c.checked = this.checked);
            if (pageInput) pageInput.value = 1;
            loadHospitalList();
        });
    }

    subjectChecks.forEach(function (check) {
        check.addEventListener("change", function () {
            if (subjectAll) {
                const total = subjectChecks.length;
                const checkedCount = form.querySelectorAll("input[name='subjects']:checked").length;
                subjectAll.checked = (total === checkedCount);
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
        button.addEventListener("click", function () {
            const contentWrap = this.parentElement.previousElementSibling;

            if (this.dataset.state === "open") {
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

    const otherAllBtn = document.getElementById('otherAll');
    const hiddenOtherDistricts = document.querySelectorAll('.hidden-other-district');

    if (otherAllBtn) {
        const checkInitialState = () => {
            const isAllChecked = Array.from(hiddenOtherDistricts).length > 0 && Array.from(hiddenOtherDistricts).every(chk => chk.checked);
            otherAllBtn.checked = isAllChecked;
        };
        checkInitialState();

        otherAllBtn.addEventListener('change', function () {
            const isChecked = this.checked;
            hiddenOtherDistricts.forEach(chk => {
                chk.checked = isChecked;
            });

            if (pageInput) pageInput.value = 1;
            loadHospitalList();
        });
    }

    window.addEventListener("popstate", function () {
        window.location.reload();
    });
});