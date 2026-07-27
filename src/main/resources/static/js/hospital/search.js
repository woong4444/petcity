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

    const form = document.getElementById('customSearchForm');
    if (form) {
        form.addEventListener('change', function (e) {
            fetchDynamicResults();
        });

        form.addEventListener('click', function (e) {
            const target = e.target.closest('button, a, input, label');
            if (target) {
                setTimeout(fetchDynamicResults, 50);
            }
        });
    }

    // 🌟 변경점: 반려동물 선택 시 파란색 하이라이팅 버그 완벽 수정!
    const petSelectBtns = document.querySelectorAll('.btn-select-pet');
    petSelectBtns.forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();

            // 데이터 뽑기
            const animalId = this.getAttribute('data-animal-id');
            const subId = this.getAttribute('data-sub-animal-id') || '';

            // 검색창 폼에 값 숨겨서 넣기
            const animalInput = document.getElementById('searchAnimalId');
            const subAnimalInput = document.getElementById('searchSubAnimalId');
            if (animalInput) animalInput.value = animalId;
            if (subAnimalInput) subAnimalInput.value = subId;

            // 기존에 선택됐던 파란색 테두리 다 초기화
            document.querySelectorAll('.pet-card-wrap').forEach(el => {
                el.classList.remove('border-sky-500', 'bg-sky-50');
                el.classList.add('border-slate-200', 'bg-white');
            });

            // 지금 클릭한 애한테만 파란색 하이라이트 추가!
            const currentCard = this.closest('.pet-card-wrap');
            if (currentCard) {
                currentCard.classList.remove('border-slate-200', 'bg-white');
                currentCard.classList.add('border-sky-500', 'bg-sky-50');
            }

            // 다음 탭으로 스무스하게 넘어가기
            if (typeof toggleStep === 'function') toggleStep('step2');

            // 결과 바로 보여주기
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