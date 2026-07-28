/**
 * search.js
 * 맞춤 탐색(병원 검색), 위치 기반 조회, 지도 연동, 찜 기능 등을 처리합니다.
 */

// --- 전역 상태 변수 ---
let activeLat = null;
let activeLng = null;
let geocoder = null;
let selectMap = null;
let tempWgsX = null;
let tempWgsY = null;
let tempAddressName = null;
let pendingDetailUrl = "";

// --- 초기화 및 정적 이벤트 바인딩 ---
document.addEventListener("DOMContentLoaded", () => {
    initStaticEvents();
    initDelegatedEvents();

    // 카카오 맵 API 로드 시 위치 초기화
    if (typeof kakao !== 'undefined') {
        kakao.maps.load(() => {
            initSearchLocation();
        });
    }
});

/**
 * 페이지 렌더링 시점에 세션 스토리지에서 위치 불러오기
 */
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
    // 초기 렌더링 시 거리 계산 텍스트 업데이트
    updateAjaxLocationUI();
}

/**
 * CSRF 토큰을 가져오는 헬퍼 함수
 */
function getCsrfHeaders() {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };

    if (csrfMeta && csrfHeaderMeta) {
        headers[csrfHeaderMeta.content] = csrfMeta.content;
    }
    return headers;
}

/**
 * 정적인 요소(항상 존재하는 요소)들에 대한 이벤트 리스너 세팅
 */
function initStaticEvents() {
    // 1. 다른 지역 전체 선택 체크박스
    const otherAllSearch = document.getElementById('otherAllSearch');
    if (otherAllSearch) {
        otherAllSearch.addEventListener('change', function () {
            document.querySelectorAll('.hidden-other-district-search').forEach(chk => {
                chk.checked = this.checked;
            });
            fetchDynamicResults();
        });
    }

    // 2. 검색 폼 변경 및 클릭 이벤트
    const form = document.getElementById('customSearchForm');
    if (form) {
        form.addEventListener('change', fetchDynamicResults);
        form.addEventListener('click', (e) => {
            const target = e.target.closest('button, a, input, label');
            // 반려동물 수정, 위치변경 모달 여는 버튼은 AJAX 조회를 막음
            if (target && !target.classList.contains('btn-edit-pet') && target.id !== 'btnLocationSelect') {
                setTimeout(fetchDynamicResults, 50);
            }
        });
    }

    // 3. 반려동물 선택 버튼 이벤트
    document.querySelectorAll('.btn-select-pet').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const animalId = this.dataset.animalId;
            const subId = this.dataset.subAnimalId || '';

            const animalInput = document.getElementById('searchAnimalId');
            const subAnimalInput = document.getElementById('searchSubAnimalId');
            if (animalInput) animalInput.value = animalId;
            if (subAnimalInput) subAnimalInput.value = subId;

            // 스타일 토글
            document.querySelectorAll('#myPetListArea > div').forEach(el =>
                el.classList.remove('border-sky-500', 'bg-sky-50')
            );
            this.closest('div.bg-white').classList.add('border-sky-500', 'bg-sky-50');

            if (typeof toggleStep === 'function') toggleStep('step2');
            fetchDynamicResults();
        });
    });

    // 4. 지도 모달 제어 이벤트
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

    // 5. 휴업 확인 모달 이벤트
    document.getElementById('btnSuspendYes')?.addEventListener('click', () => {
        if (pendingDetailUrl) window.location.href = pendingDetailUrl;
    });

    document.getElementById('btnSuspendNo')?.addEventListener('click', () => {
        const suspendModal = document.getElementById('suspendModal');
        if (suspendModal) suspendModal.style.display = 'none';
        pendingDetailUrl = "";
    });
}

/**
 * 동적 요소(AJAX로 그려진 결과 등)에 대한 이벤트 위임(Event Delegation) 설정
 * cloneNode 같은 꼼수 없이 상위 부모에게 한 번만 이벤트를 걸어 성능과 안정성 확보
 */
function initDelegatedEvents() {
    const resultArea = document.getElementById('ajaxDynamicResult');
    if (!resultArea) return;

    // AJAX 결과 내 클릭 이벤트 위임
    resultArea.addEventListener('click', (e) => {
        // [A] 상세 페이지 이동 링크 클릭 시
        const detailLink = e.target.closest('.go-detail-link');
        if (detailLink) {
            e.preventDefault();
            const status = detailLink.dataset.status;
            const notice = detailLink.dataset.notice || '등록된 휴업 사유(공지)가 없습니다.';
            let url = detailLink.tagName === 'A' ? detailLink.href : detailLink.dataset.url;

            // 좌표 담아서 보내기
            if (activeLat && activeLng && url) {
                const tempUrl = new URL(url, window.location.origin);
                tempUrl.searchParams.set('userLat', activeLat);
                tempUrl.searchParams.set('userLng', activeLng);
                url = tempUrl.toString();
            }

            if (status === '휴업' || status === 'SUSPENDED') {
                document.getElementById('suspendNoticeText').textContent = notice;
                document.getElementById('suspendModal').style.display = 'flex';
                pendingDetailUrl = url;
                return;
            }
            if (url) window.location.href = url;
        }

        // [B] 진료상태 탭 클릭 시 (전체/진료중/휴무일)
        const statusBtn = e.target.closest('.status-btn');
        if (statusBtn) {
            document.getElementById('searchOpenStatus').value = statusBtn.dataset.status;
            document.getElementById('searchPage').value = 1;
            fetchDynamicResults();
        }

        // [C] 페이징 버튼 클릭 시
        const pageLink = e.target.closest('.page-link');
        if (pageLink) {
            e.preventDefault();
            const parent = pageLink.parentElement;
            if (!parent.classList.contains('disabled') && !parent.classList.contains('active')) {
                document.getElementById('searchPage').value = pageLink.dataset.page;
                fetchDynamicResults();
            }
        }

        // [D] 찜(좋아요) 토글 버튼 클릭 시
        const zzimBtn = e.target.closest('.btn-zzim-toggle');
        if (zzimBtn) {
            e.preventDefault();
            e.stopPropagation();
            toggleZzim(zzimBtn);
        }
    });

    // AJAX 결과 내 체인지 이벤트 위임 (정렬 셀렉트 박스 등)
    resultArea.addEventListener('change', (e) => {
        if (e.target.id === 'sortSelect') {
            document.getElementById('searchSort').value = e.target.value;
            document.getElementById('searchPage').value = 1;
            fetchDynamicResults();
        }
    });

    // 지도 모달 여는 버튼 이벤트 위임 (전체 document에 걸어 어디서든 작동하게 함)
    document.addEventListener('click', (e) => {
        const btnLocationSelect = e.target.closest('#btnLocationSelect');
        if (btnLocationSelect) {
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
        }
    });
}

// --- AJAX 통신 및 UI 업데이트 ---

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
            resultArea.classList.add('bg-white', 'p-6', 'rounded-xl', 'border', 'border-slate-200', 'shadow-sm');

            // 결과가 화면에 뿌려진 후 거리계산 UI만 업데이트 (이벤트 위임덕에 리바인딩 함수 불필요)
            updateAjaxLocationUI();
        })
        .catch(err => {
            console.error(err);
            resultArea.innerHTML = '<div class="text-center py-10 text-red-500 font-bold">오류가 발생했습니다. 다시 시도해주세요.</div>';
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
        if (gpsNameElem) gpsNameElem.textContent = "위치 미설정";
    }

    // 비동기로 계산해야할 거리('.async-distance') UI 업데이트 로직
    const distanceElements = document.querySelectorAll('.async-distance');
    if (distanceElements.length > 0 && activeLat && activeLng && geocoder) {
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
                }
            });
        });
    }
}

function getDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // 지구의 반지름 (km)
    const dLat = (lat2 - lat1) * (Math.PI / 180);
    const dLon = (lon2 - lon1) * (Math.PI / 180);
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

// --- 지도 및 모달 관련 UI 조작 ---

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

function loadPetModal(btn) {
    const { id: petId, name: petName, gender, birth, weight } = btn.dataset;
    const modal = document.getElementById('petModal');

    modal.innerHTML = `
    <div class="bg-white rounded-[2rem] shadow-2xl w-full max-w-2xl overflow-hidden relative animate-[popUp_0.3s_ease]">
        <div class="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50/80">
            <h3 class="text-2xl font-extrabold text-slate-800 tracking-tight">반려동물 수정</h3>
            <button type="button" onclick="document.getElementById('petModal').classList.replace('flex', 'hidden')" class="text-slate-400 hover:text-rose-500 transition text-3xl font-bold leading-none">&times;</button>
        </div>
        <form action="/pet/update" method="post" enctype="multipart/form-data" class="p-8 space-y-6">
            <input type="hidden" name="petId" value="${petId}">
            
            <div>
                <label class="block text-sm font-bold text-slate-700 mb-2">이름</label>
                <input type="text" name="petName" value="${petName}" class="w-full border border-slate-200 bg-slate-50/50 rounded-xl p-3.5 outline-none focus:border-sky-400 focus:bg-white transition shadow-sm" required>
            </div>
            <div class="grid grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-bold text-slate-700 mb-2">생년월일</label>
                    <input type="date" name="birthDate" value="${birth}" class="w-full border border-slate-200 bg-slate-50/50 rounded-xl p-3.5 outline-none focus:border-sky-400 focus:bg-white transition shadow-sm" required>
                </div>
                <div>
                    <label class="block text-sm font-bold text-slate-700 mb-2">몸무게 (kg)</label>
                    <input type="number" step="0.1" name="weight" value="${weight}" class="w-full border border-slate-200 bg-slate-50/50 rounded-xl p-3.5 outline-none focus:border-sky-400 focus:bg-white transition shadow-sm" required>
                </div>
            </div>
            <div>
                <label class="block text-sm font-bold text-slate-700 mb-2">성별 및 중성화</label>
                <select name="gender" class="w-full border border-slate-200 bg-slate-50/50 rounded-xl p-3.5 outline-none focus:border-sky-400 focus:bg-white transition shadow-sm font-bold text-slate-700 cursor-pointer">
                    <option value="M" ${gender === 'M' ? 'selected' : ''}>수컷</option>
                    <option value="F" ${gender === 'F' ? 'selected' : ''}>암컷</option>
                    <option value="NM" ${gender === 'NM' ? 'selected' : ''}>수컷 (중성화 완료)</option>
                    <option value="NF" ${gender === 'NF' ? 'selected' : ''}>암컷 (중성화 완료)</option>
                </select>
            </div>
            <div>
                <label class="block text-sm font-bold text-slate-700 mb-2">사진 변경 (선택)</label>
                <input type="file" name="photoFile" class="w-full border border-slate-200 bg-slate-50/50 rounded-xl p-3 text-sm cursor-pointer shadow-sm file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-bold file:bg-sky-50 file:text-sky-600 hover:file:bg-sky-100">
            </div>
            
            <div class="pt-6 flex gap-4 border-t border-slate-100">
                <button type="button" onclick="document.getElementById('petModal').classList.replace('flex', 'hidden')" class="flex-1 bg-slate-100 border border-slate-200 text-slate-600 font-extrabold py-4 rounded-xl hover:bg-slate-200 transition shadow-sm">취소</button>
                <button type="submit" class="flex-1 bg-sky-500 text-white font-extrabold py-4 rounded-xl hover:bg-sky-600 transition shadow-md">수정 완료 저장</button>
            </div>
        </form>
    </div>
    `;

    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    if (csrfMeta && csrfHeaderMeta) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = csrfHeaderMeta.content.includes('X-CSRF') ? '_csrf' : (csrfMeta.name || '_csrf');
        csrfInput.value = csrfMeta.content;
        modal.querySelector('form').appendChild(csrfInput);
    }

    modal.classList.replace('hidden', 'flex');
}

function toggleStep(stepId) {
    const content = document.getElementById('content-' + stepId);
    const icon = document.getElementById('icon-' + stepId);
    if (!content || !icon) return;

    if (content.classList.contains('hidden')) {
        content.classList.remove('hidden');
        icon.textContent = '−';
    } else {
        content.classList.add('hidden');
        icon.textContent = '+';
    }
}