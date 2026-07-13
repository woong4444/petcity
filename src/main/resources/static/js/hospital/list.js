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

    // 🌟 핵심 상태 변수
    let gpsWgsX = null;
    let gpsWgsY = null;
    let gpsAddressName = "위치 확인 중...";

    let activeTmX = null;
    let activeTmY = null;
    let activeAddressName = null;
    let isCustomLocation = false; // 현재 사용자가 임의의 위치를 선택했는지 여부

    let geocoder = null;
    let isLocationInited = false;

    // 1. 카카오맵 서비스 준비
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

    // 2. 초기 GPS 위치 가져오기 (무한 로딩 방지 타임아웃 포함)
    function initLocation() {
        if (isLocationInited) return;

        const timeoutId = setTimeout(() => {
            if (!isLocationInited) {
                console.log("GPS 응답 지연: 기본 위치(서울시청)로 강제 설정합니다.");
                setDefaultLocation();
            }
        }, 3000);

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                function(position) {
                    clearTimeout(timeoutId);
                    if (isLocationInited) return;
                    isLocationInited = true;

                    gpsWgsX = position.coords.longitude;
                    gpsWgsY = position.coords.latitude;
                    convertAndApply(gpsWgsX, gpsWgsY, false, false);
                },
                function(error) {
                    clearTimeout(timeoutId);
                    if (!isLocationInited) setDefaultLocation();
                },
                { timeout: 2500 }
            );
        } else {
            clearTimeout(timeoutId);
            setDefaultLocation();
        }
    }

    function setDefaultLocation() {
        isLocationInited = true;
        gpsWgsX = 126.9786567;
        gpsWgsY = 37.566826;
        gpsAddressName = "기본 위치 (서울시청)";
        applyLocationAndSearch(gpsWgsX, gpsWgsY, gpsAddressName, false, false);
    }

    // 위경도 -> 행정동 이름 변환
    function convertAndApply(lon, lat, doSearch, isCustom) {
        if (geocoder) {
            geocoder.coord2RegionCode(lon, lat, function(result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    for(let i=0; i<result.length; i++) {
                        if(result[i].region_type === 'H') {
                            const addressName = result[i].address_name;
                            if (!isCustom) {
                                gpsAddressName = addressName;
                            }
                            applyLocationAndSearch(lon, lat, addressName, doSearch, isCustom);
                            return;
                        }
                    }
                }
                if (!isCustom) gpsAddressName = "위치 변환 실패";
                applyLocationAndSearch(lon, lat, "위치 변환 실패", doSearch, isCustom);
            });
        } else {
            applyLocationAndSearch(lon, lat, "지도 API 오류", doSearch, isCustom);
        }
    }

    // 🌟 3. 위치 UI 텍스트 렌더링 (Ajax로 화면이 바뀌어도 텍스트를 유지하는 핵심 함수)
    function updateLocationUI() {
        const gpsNameElem = document.getElementById('gpsLocationName');
        const customTextSpan = document.getElementById('customLocationText');
        const customNameSpan = document.getElementById('customLocationName');

        // GPS 진짜 위치는 항상 고정
        if (gpsNameElem) {
            gpsNameElem.textContent = gpsAddressName;
        }

        // 사용자가 지도로 설정한 위치가 있고, 그게 내 진짜 위치랑 다를 때만 '설정한 위치' 박스 노출!
        if (isCustomLocation && activeAddressName && activeAddressName !== gpsAddressName) {
            if(customTextSpan) customTextSpan.style.display = 'inline';
            if(customNameSpan) customNameSpan.textContent = activeAddressName;
        } else {
            if(customTextSpan) customTextSpan.style.display = 'none';
        }
    }

    // 서버로 좌표 넘길 준비 및 검색 실행
    function applyLocationAndSearch(lon, lat, addressName, doSearch = false, isCustom = false) {
        activeAddressName = addressName;
        isCustomLocation = isCustom;

        if (!isCustom) {
            gpsAddressName = addressName;
        }

        // 화면 텍스트 갱신
        updateLocationUI();

        if (geocoder) {
            geocoder.transCoord(lon, lat, function(result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    activeTmX = result[0].x;
                    activeTmY = result[0].y;

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

                // 🌟 Ajax로 화면이 새로 그려졌으므로 이벤트와 UI 상태를 다시 씌워줌
                rebindToolbarEvents();
                rebindMapModalTrigger();
                rebindDetailLinks();
                updateLocationUI(); // <-- 무한 로딩 버그 해결의 핵심!!!
            })
            .catch(error => {
                if (error.name !== "AbortError") console.error(error);
            });
    }

    function rebindDetailLinks() {
        document.querySelectorAll('.detail-button').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                let url = new URL(this.href, window.location.origin);
                if (activeTmX && activeTmY) {
                    url.searchParams.set('userLat', activeTmX);
                    url.searchParams.set('userLng', activeTmY);
                }
                window.location.href = url.toString();
            });
        });

        document.querySelectorAll('.btn-list-zzim.is-anonymous').forEach(btn => {
            btn.addEventListener('click', function() {
                alert('회원가입 및 로그인 기능은 현재 통합 준비 중입니다.');
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

    /* =======================================
       🌟 지도 모달(위치 팝업창) 전역 제어
    ======================================= */
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

    // 모달창에서 '이 위치로 설정' 버튼 클릭 시
    if(btnConfirmLocation) {
        btnConfirmLocation.addEventListener('click', () => {
            if(tempWgsX && tempWgsY && tempAddressName) {
                // 사용자가 핀을 옮겨서 확인했으므로 isCustom = true
                applyLocationAndSearch(tempWgsX, tempWgsY, tempAddressName, true, true);
            }
            locationModal.style.display = 'none';
        });
    }

    // 🌟 '내 위치로 이동' 버튼 클릭 시 (팝업 없이 즉시 원위치 복귀!)
    if(btnGoMyLocation) {
        btnGoMyLocation.addEventListener('click', function() {
            if (gpsWgsX && gpsWgsY) {
                // 팝업창 즉시 닫기
                locationModal.style.display = 'none';
                // 내 원래 GPS 위치로 즉시 복구하고 검색 시작 (isCustom = false)
                applyLocationAndSearch(gpsWgsX, gpsWgsY, gpsAddressName, true, false);
            } else {
                alert("내 위치(GPS) 정보를 찾을 수 없습니다.");
            }
        });
    }

    function rebindMapModalTrigger() {
        const btnLocationSelect = document.getElementById('btnLocationSelect');
        if(btnLocationSelect) {
            btnLocationSelect.addEventListener('click', function() {
                locationModal.style.display = 'flex';

                // 모달을 열었을 때 지도의 중심점 (커스텀 위치가 있으면 그곳, 아니면 내 위치)
                let mapLat = (isCustomLocation && activeTmY) ? activeTmY : gpsWgsY;
                let mapLng = (isCustomLocation && activeTmX) ? activeTmX : gpsWgsX;

                // 좌표계가 TM이면 WGS84로 변환해서 지도를 열어줌
                if (isCustomLocation && activeTmX && activeTmY && geocoder) {
                    geocoder.transCoord(activeTmX, activeTmY, function(result, status) {
                        if (status === kakao.maps.services.Status.OK) {
                            openMap(result[0].y, result[0].x);
                        }
                    }, { input_coord: kakao.maps.services.Coords.TM, output_coord: kakao.maps.services.Coords.WGS84 });
                } else {
                    openMap(mapLat, mapLng);
                }
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
            selectMap.setCenter(new kakao.maps.LatLng(lat, lng));
            selectMap.relayout();
        }
    }

    rebindToolbarEvents();
    rebindMapModalTrigger();
    rebindDetailLinks();

    /* =======================================
       🌟 이하 각종 필터 및 초기화 버튼 로직
    ======================================= */
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

    // 초기화 버튼 클릭 로직
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

            // 모든 걸 비우고 내 원위치(GPS)로 복구하여 검색
            if (gpsWgsX && gpsWgsY) {
                applyLocationAndSearch(gpsWgsX, gpsWgsY, gpsAddressName, true, false);
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