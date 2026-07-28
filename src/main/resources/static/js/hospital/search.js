document.addEventListener("DOMContentLoaded", function () {
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

            document.querySelectorAll('#myPetListArea > div').forEach(el =>
                el.classList.remove('border-sky-500', 'bg-sky-50'));
            this.closest('div.bg-white').classList.add('border-sky-500', 'bg-sky-50');

            if (typeof toggleStep === 'function') toggleStep('step2');
            fetchDynamicResults();
        });
    });
});

function fetchDynamicResults() {
    const form = document.getElementById('customSearchForm');
    if (!form) return;

    const formData = new FormData(form);
    const searchParams = new URLSearchParams(formData);

    const otherAllSearch = document.getElementById('otherAllSearch');
    if (otherAllSearch && otherAllSearch.checked && !searchParams.has('districts')) {
        searchParams.append('districts', 'UNKNOWN_REGION_DUMMY');
    }

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

function toggleStep(stepId) {
    const content = document.getElementById('content-' + stepId);
    const icon = document.getElementById('icon-' + stepId);
    if (content.classList.contains('hidden')) {
        content.classList.remove('hidden');
        icon.textContent = '−';
    } else {
        content.classList.add('hidden');
        icon.textContent = '+';
    }
}