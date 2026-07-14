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

function selectPet(animalId, subAnimalId) {
    document.getElementById('searchAnimalId').value = animalId;
    document.getElementById('searchSubAnimalId').value = subAnimalId;

    alert('반려동물이 선택되었습니다. 다음으로 진료과목을 선택해주세요.');

    toggleStep('step1');
    document.getElementById('content-step2').classList.remove('hidden');
    document.getElementById('icon-step2').textContent = '−';
}

// 🌟 '다른 아이 추가등록' 또는 최초 '등록하기' 버튼 클릭 시 폼 초기화 로직
function openPetModalForAdd() {
    document.getElementById('petRegForm').reset();
    document.getElementById('petIdInput').value = '0'; // 0이면 신규등록
    document.getElementById('modalSubAnimalType').innerHTML = '<option value="">유형을 먼저 선택해주세요</option>';

    const petModal = document.getElementById('petModal');
    petModal.classList.remove('hidden');
    petModal.classList.add('flex');
}

document.addEventListener("DOMContentLoaded", function () {

    // --- 지역 전체 선택 로직 ---
    const seoulAllSearch = document.getElementById('seoulAllSearch');
    const districtChecks = document.querySelectorAll("input[name='districts']");

    if(seoulAllSearch) {
        seoulAllSearch.addEventListener('change', function() {
            districtChecks.forEach(chk => chk.checked = this.checked);
        });
    }

    districtChecks.forEach(chk => {
        chk.addEventListener('change', function() {
            const total = districtChecks.length;
            const checkedCount = document.querySelectorAll("input[name='districts']:checked").length;
            if(seoulAllSearch) seoulAllSearch.checked = (total === checkedCount);
        });
    });

    // --- 모달 닫기 로직 ---
    const petModal = document.getElementById('petModal');
    const btnClosePetModal = document.getElementById('btnClosePetModal');
    const btnCancelPetModal = document.getElementById('btnCancelPetModal');

    function closePetModal() {
        petModal.classList.add('hidden');
        petModal.classList.remove('flex');
    }
    if(btnClosePetModal) btnClosePetModal.addEventListener('click', closePetModal);
    if(btnCancelPetModal) btnCancelPetModal.addEventListener('click', closePetModal);

    petModal.addEventListener('click', function(e) {
        if(e.target === petModal) closePetModal();
    });

    // --- 🌟 '수정' 버튼 클릭 시 기존 정보 불러오기 로직 ---
    document.querySelectorAll('.btn-edit-pet').forEach(btn => {
        btn.addEventListener('click', function() {
            document.getElementById('petRegForm').reset();
            const dataset = this.dataset;

            document.getElementById('petIdInput').value = dataset.id;
            document.querySelector('input[name="petName"]').value = dataset.name;
            document.getElementById('modalAnimalType').value = dataset.animal;

            // 품종 리스트 동적 트리거 발생
            document.getElementById('modalAnimalType').dispatchEvent(new Event('change'));

            // 약간의 딜레이 후 품종 선택 (리스트 생성 시간 확보)
            setTimeout(() => {
                document.getElementById('modalSubAnimalType').value = dataset.sub;
            }, 50);

            document.querySelector('select[name="gender"]').value = dataset.gender;
            document.querySelector('input[name="birthDate"]').value = dataset.birth;
            document.querySelector('input[name="weight"]').value = dataset.weight;
            document.querySelector('input[name="regNumber"]').value = dataset.reg;

            petModal.classList.remove('hidden');
            petModal.classList.add('flex');
        });
    });

    // --- 모달 내 반려동물 품종 동적 렌더링 ---
    const modalAnimalType = document.getElementById('modalAnimalType');
    const modalSubAnimalType = document.getElementById('modalSubAnimalType');

    if(modalAnimalType && modalSubAnimalType && typeof subAnimalList !== 'undefined') {
        modalAnimalType.addEventListener('change', function() {
            const parentId = this.value;
            modalSubAnimalType.innerHTML = '<option value="">품종을 선택해주세요</option>';

            if(!parentId) return;

            const filtered = subAnimalList.filter(sub => String(sub.parentId) === String(parentId));
            filtered.forEach(sub => {
                const opt = document.createElement('option');
                opt.value = sub.animalId;
                opt.textContent = sub.animalName;
                modalSubAnimalType.appendChild(opt);
            });
        });
    }

    // --- 🌟 폼 전송 및 예외처리 (몸무게 방어) ---
    const btnSubmitPet = document.getElementById('btnSubmitPet');
    if(btnSubmitPet) {
        btnSubmitPet.addEventListener('click', function() {
            const form = document.getElementById('petRegForm');

            if(!form.checkValidity()) {
                form.reportValidity();
                return;
            }

            // 🌟 2222kg 등 말도 안 되는 몸무게 입력 방어
            const weightInput = document.querySelector('input[name="weight"]').value;
            if(weightInput <= 0 || weightInput > 150) {
                alert('몸무게는 0.1kg ~ 150kg 사이로 정확하게 입력해주세요.');
                return;
            }

            const formData = new FormData(form);

            fetch('/pet/api/save', {
                method: 'POST',
                body: formData
            })
                .then(res => res.json())
                .then(data => {
                    if(data.isSuccess) {
                        alert('반려동물 정보가 성공적으로 저장되었습니다!');
                        window.location.reload(); // 새로고침하여 DB 정보를 화면에 다시 뿌림
                    } else {
                        alert('저장에 실패했습니다.');
                    }
                })
                .catch(err => console.error(err));
        });
    }
});