/* ========================================================
   [search.js] 맞춤 검색 화면의 그 외 지역 로직 + AJAX 검색 기능
======================================================== */
document.addEventListener("DOMContentLoaded", function () {

    // 1. 그 외 지역 버튼 클릭 이벤트
    const otherAllSearch = document.getElementById('otherAllSearch');
    if (otherAllSearch) {
        otherAllSearch.addEventListener('change', function () {
            const hiddenDistricts = document.querySelectorAll('.hidden-other-district-search');
            hiddenDistricts.forEach(chk => {
                chk.checked = this.checked;
            });
            fetchDynamicResults(); // AJAX 결과 갱신
        });
    }

    // 2. 다른 필터 클릭 시 AJAX 호출 설정
    const formElements = document.querySelectorAll('.step2-service-chk, .step3-district-chk, .step2-subject-chk');
    formElements.forEach(el => {
        el.addEventListener('change', fetchDynamicResults);
    });

    // 3. 반려동물 폼 선택 이벤트 (step 2로 이동)
    const petSelectBtns = document.querySelectorAll('.btn-select-pet');
    petSelectBtns.forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const animalId = this.getAttribute('data-animal-id');
            const subId = this.getAttribute('data-sub-animal-id') || '';
            document.getElementById('searchAnimalId').value = animalId;
            document.getElementById('searchSubAnimalId').value = subId;

            document.querySelectorAll('#myPetListArea > div').forEach(el => el.classList.remove('border-sky-500', 'bg-sky-50'));
            this.closest('div.bg-white').classList.add('border-sky-500', 'bg-sky-50');

            if (typeof toggleStep === 'function') toggleStep('step2');
            fetchDynamicResults();
        });
    });
});

// 비동기 통신 (AJAX) 함수
function fetchDynamicResults() {
    const form = document.getElementById('customSearchForm');
    const formData = new FormData(form);
    const searchParams = new URLSearchParams(formData);

    if (!searchParams.get('animalId')) return;

    const resultArea = document.getElementById('ajaxDynamicResult');
    resultArea.classList.remove('hidden');
    resultArea.innerHTML = '<div class="text-center py-10 text-slate-500 font-bold">맞춤 병원을 불러오는 중입니다...</div>';

    fetch('/hospital/list/ajax?' + searchParams.toString(), {
        headers: {'X-Requested-With': 'XMLHttpRequest'}
    })
        .then(res => res.text())
        .then(html => {
            resultArea.innerHTML = html;
            resultArea.classList.add('bg-white', 'p-6', 'rounded-xl', 'border', 'border-slate-200', 'shadow-sm');
        })
        .catch(err => {
            console.error(err);
            resultArea.innerHTML = '<div class="text-center py-10 text-red-500 font-bold">오류가 발생했습니다. 다시 시도해주세요.</div>';
        });
}