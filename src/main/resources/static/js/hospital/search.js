/* ========================================================
   [search.js] 맞춤 검색 화면 - 싹 다 즉시 반응형 AJAX 적용
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
            fetchDynamicResults();
        });
    }

    // 2. 폼 내부의 모든 입력 요소(input, select, checkbox 등) 변경 시 즉시 반응
    const form = document.getElementById('customSearchForm');
    if (form) {
        form.addEventListener('change', function (e) {
            fetchDynamicResults();
        });

        // 3. 버튼이나 필터 항목을 클릭했을 때 (동물 분류, 진료 과목 버튼 등 타겟 포함)
        form.addEventListener('click', function (e) {
            // 버튼이나 칩 형태의 필터 요소를 클릭한 경우 딜레이를 두고 즉시 반영
            const target = e.target.closest('button, a, input, label');
            if (target) {
                setTimeout(fetchDynamicResults, 50);
            }
        });
    }

    // 4. 반려동물 폼 선택 이벤트 (step 2로 이동)
    const petSelectBtns = document.querySelectorAll('.btn-select-pet');
    petSelectBtns.forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const animalId = this.getAttribute('data-animal-id');
            const subId = this.getAttribute('data-sub-animal-id') || '';

            const animalInput = document.getElementById('searchAnimalId');
            const subAnimalInput = document.getElementById('searchSubAnimalId');
            if (animalInput) animalInput.value = animalId;
            if (subAnimalInput) subAnimalInput.value = subId;

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
    if (!form) return;

    const formData = new FormData(form);
    const searchParams = new URLSearchParams(formData);

    const resultArea = document.getElementById('ajaxDynamicResult');
    if (!resultArea) return;

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