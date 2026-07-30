
let activeLat = null;
let activeLng = null;
let geocoder = null;
let selectMap = null;
let tempWgsX = null;
let tempWgsY = null;
let tempAddressName = null;
let pendingDetailUrl = "";

document.addEventListener("DOMContentLoaded", () => {
    initStaticEvents();

    if (typeof kakao !== 'undefined') {
        kakao.maps.load(() => {
            initSearchLocation();
        });
    } else {
        updateAjaxLocationUI();
    }
});

function initSearchLocation() {
    geocoder = new kakao.maps.services.Geocoder();
    const savedLocStr = sessionStorage.getItem('petcity_loc_data');
    if (savedLocStr) {
        try {
            const savedLoc = JSON.parse(savedLocStr);
            activeLat = savedLoc.activeLat;
            activeLng = savedLoc.activeLng;
        } catch (e) {
            console.error("위치 정보 파싱 오류", e);
        }
    }
    updateAjaxLocationUI();
}

function getCsrfHeaders() {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };

    if (csrfMeta && csrfHeaderMeta) {
        headers[csrfHeaderMeta.content] = csrfMeta.content;
    }
    return headers;
}

function bindCheckAll(allId, itemClass) {
    const allChk = document.getElementById(allId);
    const items = document.querySelectorAll(itemClass);
    if (!allChk || items.length === 0) return;

    allChk.addEventListener('change', function () {
        items.forEach(item => item.checked = this.checked);
    });

    items.forEach(item => {
        item.addEventListener('change', function () {
            const checkedCount = document.querySelectorAll(itemClass + ':checked').length;
            allChk.checked = (checkedCount === items.length);
        });
    });
}

function initStaticEvents() {
    const form = document.getElementById('customSearchForm');

    if (form) {
        form.addEventListener('change', (e) => {
            if (e.target.id === 'sortSelect') return;
            clearTimeout(window.searchTimeout);
            window.searchTimeout = setTimeout(fetchDynamicResults, 100);
        });

        form.addEventListener('click', (e) => {
            if (e.target.closest('#ajaxDynamicResult')) return;

            const target = e.target.closest('button, a, input, label');
            if (target && !target.classList.contains('btn-edit-pet')) {
                clearTimeout(window.searchTimeout);
                window.searchTimeout = setTimeout(fetchDynamicResults, 100);
            }
        });
    }

    bindCheckAll('subjectAllSearch', '.step2-subject-chk');
    bindCheckAll('serviceAllSearch', '.step2-service-chk');
    bindCheckAll('seoulAllSearch', '.step3-district-chk');
    bindCheckAll('otherAllSearch', '.hidden-other-district-search');

    document.querySelectorAll('.btn-select-pet').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const animalId = this.dataset.animalId;
            const subId = this.dataset.subAnimalId || '';

            const animalInput = document.getElementById('searchAnimalId');
            const subAnimalInput = document.getElementById('searchSubAnimalId');
            if (animalInput) animalInput.value = animalId;
            if (subAnimalInput) subAnimalInput.value = subId;

            document.querySelectorAll('#myPetListArea > div').forEach(el =>
                el.classList.remove('border-sky-500', 'bg-sky-50')
            );
            this.closest('div.bg-white').classList.add('border-sky-500', 'bg-sky-50');

            if (typeof toggleStep === 'function') toggleStep('step2');
            fetchDynamicResults();
        });
    });

    document.getElementById('btnCloseModal')?.addEventListener('click', () => {
        document.getElementById('locationModal').style.display = 'none';
    });

    document.getElementById('btnConfirmLocation')?.addEventListener('click', () => {
        if (tempWgsX && tempWgsY && tempAddressName) {
            activeLng = tempWgsX;
            activeLat = tempWgsY;

            const locData = {
                gpsWgsX: tempWgsX, gpsWgsY: tempWgsY, gpsAddressName: tempAddressName,
                activeLat: tempWgsY, activeLng: tempWgsX, activeAddressName: tempAddressName,
                isCustomLocation: true
            };
            sessionStorage.setItem('petcity_loc_data', JSON.stringify(locData));
            fetchDynamicResults();
        }
        document.getElementById('locationModal').style.display = 'none';
    });

    document.getElementById('btnGoMyLocation')?.addEventListener('click', () => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(position => {
                const lon = position.coords.longitude;
                const lat = position.coords.latitude;
                if (geocoder) {
                    geocoder.coord2RegionCode(lon, lat, (result, status) => {
                        if (status === kakao.maps.services.Status.OK) {
                            const region = result.find(r => r.region_type === 'H');
                            if (region) {
                                tempAddressName = region.address_name;
                                document.getElementById('selectedAddressText').textContent = tempAddressName;
                            }
                        }
                        openMap(lat, lon, document.getElementById('selectMap'));
                    });
                }
            }, () => {
                alert('GPS 위치 권한을 허용해주세요.');
            });
        }
    });

    document.getElementById('btnSuspendYes')?.addEventListener('click', () => {
        if (pendingDetailUrl) window.location.href = pendingDetailUrl;
    });

    document.getElementById('btnSuspendNo')?.addEventListener('click', () => {
        const suspendModal = document.getElementById('suspendModal');
        if (suspendModal) suspendModal.style.display = 'none';
        pendingDetailUrl = "";
    });
}

function fetchDynamicResults() {
    const form = document.getElementById('customSearchForm');
    if (!form) return;

    const formData = new FormData(form);
    const searchParams = new URLSearchParams(formData);

    const otherAllSearch = document.getElementById('otherAllSearch');
    if (otherAllSearch && otherAllSearch.checked && !searchParams.has('districts')) {
        searchParams.append('districts', 'UNKNOWN_REGION_DUMMY');
    }

    if (activeLat && activeLng) {
        searchParams.append("userLat", activeLat);
        searchParams.append("userLng", activeLng);
    }

    const resultArea = document.getElementById('ajaxDynamicResult');
    if (!resultArea) return;

    resultArea.classList.remove('hidden');
    resultArea.innerHTML = '<div class="text-center py-10 text-slate-500 font-bold">맞춤 병원을 불러오는 중입니다...</div>';

    fetch('/hospital/list/ajax?' + searchParams.toString(), {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(res => res.text())
        .then(html => {
            resultArea.innerHTML = html;

            let searchOpenStatus = form.querySelector('input[name="openStatus"]');
            const currentStatus = searchOpenStatus ? searchOpenStatus.value : 'ALL';

            resultArea.querySelectorAll('.status-btn').forEach(btn => {
                if (btn.dataset.status === currentStatus) {
                    btn.classList.add('active', 'text-sky-500', 'font-bold');
                    btn.classList.remove('text-slate-500');
                } else {
                    btn.classList.remove('active', 'text-sky-500', 'font-bold');
                    btn.classList.add('text-slate-500');
                }
            });

            updateAjaxLocationUI();
            rebindAjaxResults();
        })
        .catch(err => {
            console.error(err);
            resultArea.innerHTML = '<div class="text-center py-10 text-red-500 font-bold">오류가 발생했습니다. 다시 시도해주세요.</div>';
        });
}

function rebindAjaxResults() {
    const resultArea = document.getElementById('ajaxDynamicResult');
    if (!resultArea) return;

    resultArea.querySelectorAll('.go-detail-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const status = this.getAttribute('data-status');
            const notice = this.getAttribute('data-notice') || '등록된 휴업 사유(공지)가 없습니다.';

            let href = this.tagName === 'A' ? this.href : this.dataset.url;
            let url = new URL(href, window.location.origin);

            if (activeLat && activeLng) {
                url.searchParams.set('userLat', activeLat);
                url.searchParams.set('userLng', activeLng);
            }

            if (status === '휴업' || status === 'SUSPENDED') {
                const noticeElem = document.getElementById('suspendNoticeText');
                if (noticeElem) noticeElem.textContent = notice;

                const suspendModal = document.getElementById('suspendModal');
                if (suspendModal) suspendModal.style.display = 'flex';

                pendingDetailUrl = url.toString();
                return;
            }
            window.location.href = url.toString();
        });
    });

    const btnLocationSelect = resultArea.querySelector('.btn-location-select, #btnLocationSelect');
    if (btnLocationSelect) {
        btnLocationSelect.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const locationModal = document.getElementById('locationModal');
            const selectMapContainer = document.getElementById('selectMap');
            if (locationModal) {
                locationModal.style.display = 'flex';
                setTimeout(() => {
                    let mapLat = activeLat || 37.566826;
                    let mapLng = activeLng || 126.9786567;
                    openMap(mapLat, mapLng, selectMapContainer);
                }, 150);
            }
        });
    }

    resultArea.querySelectorAll('.status-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const form = document.getElementById('customSearchForm');
            if (!form) return;

            let searchOpenStatus = form.querySelector('input[name="openStatus"]');
            if (!searchOpenStatus) {
                searchOpenStatus = document.createElement('input');
                searchOpenStatus.type = 'hidden';
                searchOpenStatus.name = 'openStatus';
                form.appendChild(searchOpenStatus);
            }
            searchOpenStatus.value = this.dataset.status;

            let searchPage = form.querySelector('input[name="page"]');
            if (!searchPage) {
                searchPage = document.createElement('input');
                searchPage.type = 'hidden';
                searchPage.name = 'page';
                form.appendChild(searchPage);
            }
            searchPage.value = 1;

            fetchDynamicResults();
        });
    });

    resultArea.querySelectorAll('.page-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            const parent = this.parentElement;
            if (parent && !parent.classList.contains('disabled') && !parent.classList.contains('active')) {
                const form = document.getElementById('customSearchForm');

                let searchPage = form.querySelector('input[name="page"]');
                if (!searchPage && form) {
                    searchPage = document.createElement('input');
                    searchPage.type = 'hidden';
                    searchPage.name = 'page';
                    form.appendChild(searchPage);
                }
                searchPage.value = this.dataset.page;
                fetchDynamicResults();
            }
        });
    });

    const sortSelect = resultArea.querySelector('#sortSelect');
    if (sortSelect) {
        sortSelect.addEventListener('change', function(e) {
            e.stopPropagation();

            if (this.value === 'distance' && (!activeLat || !activeLng)) {
                alert("가까운순 정렬을 이용하시려면 기준 위치를 먼저 설정해주세요.");
                const form = document.getElementById('customSearchForm');
                const existingSort = form.querySelector('input[name="sort"]');
                this.value = existingSort ? existingSort.value : 'recommend';
                return;
            }

            const form = document.getElementById('customSearchForm');

            let searchSort = form.querySelector('input[name="sort"]');
            if (!searchSort && form) {
                searchSort = document.createElement('input');
                searchSort.type = 'hidden';
                searchSort.name = 'sort';
                form.appendChild(searchSort);
            }
            if (searchSort) searchSort.value = this.value;

            let searchPage = form.querySelector('input[name="page"]');
            if (!searchPage && form) {
                searchPage = document.createElement('input');
                searchPage.type = 'hidden';
                searchPage.name = 'page';
                form.appendChild(searchPage);
            }
            if (searchPage) searchPage.value = 1;

            fetchDynamicResults();
        });
    }

    resultArea.querySelectorAll('.btn-zzim-toggle').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            toggleZzim(this);
        });
    });
}

function toggleZzim(btnElement) {
    const hospitalId = btnElement.dataset.id;

    fetch('/hospital/api/zzim', {
        method: 'POST',
        headers: getCsrfHeaders(),
        body: new URLSearchParams({ hospitalId: hospitalId })
    })
        .then(res => res.json())
        .then(data => {
            if (data.isSuccess) {
                const emptyHeart = btnElement.querySelector('.icon-heart-empty');
                const filledHeart = btnElement.querySelector('.icon-heart-filled');

                if (data.isZzim) {
                    btnElement.classList.add('text-rose-500', 'active');
                    btnElement.classList.remove('text-slate-300', 'hover:text-rose-400');
                    if (emptyHeart) { emptyHeart.classList.replace('block', 'hidden'); }
                    if (filledHeart) { filledHeart.classList.replace('hidden', 'block'); }
                } else {
                    btnElement.classList.remove('text-rose-500', 'active');
                    btnElement.classList.add('text-slate-300', 'hover:text-rose-400');
                    if (emptyHeart) { emptyHeart.classList.replace('hidden', 'block'); }
                    if (filledHeart) { filledHeart.classList.replace('block', 'hidden'); }
                }

                const countSpan = btnElement.querySelector('.count');
                if (countSpan) countSpan.textContent = data.zzimCount;
            } else {
                alert("로그인이 필요합니다.");
                window.location.href = '/member/login';
            }
        })
        .catch(err => console.error("찜 에러:", err));
}

function updateAjaxLocationUI() {
    const savedLocStr = sessionStorage.getItem('petcity_loc_data');
    const gpsNameElem = document.getElementById('gpsLocationName');
    const customTextSpan = document.getElementById('customLocationText');
    const customNameSpan = document.getElementById('customLocationName');

    if (savedLocStr) {
        const savedLoc = JSON.parse(savedLocStr);
        activeLat = savedLoc.activeLat;
        activeLng = savedLoc.activeLng;

        if (gpsNameElem) gpsNameElem.textContent = savedLoc.gpsAddressName || "기본 위치";

        if (savedLoc.isCustomLocation && savedLoc.activeAddressName && savedLoc.activeAddressName !== savedLoc.gpsAddressName) {
            if (customTextSpan) customTextSpan.style.display = 'inline';
            if (customNameSpan) customNameSpan.textContent = savedLoc.activeAddressName;
        } else {
            if (customTextSpan) customTextSpan.style.display = 'none';
        }
    } else {
        if (gpsNameElem) gpsNameElem.textContent = "위치 미설정 (위치 변경을 눌러주세요)";
    }

    const distanceElements = document.querySelectorAll('.async-distance');
    if (distanceElements.length > 0) {
        if (activeLat && activeLng && geocoder) {
            distanceElements.forEach(el => {
                const address = el.dataset.address;
                if (!address) return;

                let cleanAddress = address.replace(/\(.*?\)/g, '').split(',')[0].trim();
                geocoder.addressSearch(cleanAddress, (result, status) => {
                    if (status === kakao.maps.services.Status.OK) {
                        const dist = getDistance(activeLat, activeLng, parseFloat(result[0].y), parseFloat(result[0].x));
                        el.innerHTML = `<strong>${dist.toFixed(1)}</strong>km`;
                        el.classList.remove('async-distance');
                    } else {
                        el.innerHTML = '<span class="text-[12px] font-bold text-slate-500">거리 미제공</span>';
                        el.classList.remove('async-distance');
                    }
                });
            });
        } else {
            distanceElements.forEach(el => {
                el.innerHTML = '<span class="text-[12px] font-bold text-slate-500">- km</span>';
                el.classList.remove('async-distance');
            });
        }
    }
}

function getDistance(lat1, lon1, lat2, lon2) {
    const R = 6371;
    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLon = (lon2 - lon1) * (Math.PI / 180);
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

function openMap(lat, lng, container) {
    if (!selectMap) {
        const mapOption = { center: new kakao.maps.LatLng(lat, lng), level: 4 };
        selectMap = new kakao.maps.Map(container, mapOption);

        kakao.maps.event.addListener(selectMap, 'idle', () => {
            const center = selectMap.getCenter();
            tempWgsX = center.getLng();
            tempWgsY = center.getLat();

            if (geocoder) {
                geocoder.coord2RegionCode(tempWgsX, tempWgsY, (result, status) => {
                    if (status === kakao.maps.services.Status.OK) {
                        const region = result.find(r => r.region_type === 'H');
                        if (region) {
                            tempAddressName = region.address_name;
                            document.getElementById('selectedAddressText').textContent = tempAddressName;
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