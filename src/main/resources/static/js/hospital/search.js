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

function openPetModalForAdd() {
    document.getElementById('petRegForm').reset();
    document.getElementById('petIdInput').value = '0';
    document.getElementById('modalSubAnimalType').innerHTML = '<option value="">유형을 먼저 선택해주세요</option>';

    const petModal = document.getElementById('petModal');
    petModal.classList.remove('hidden');
    petModal.classList.add('flex');
}

document.addEventListener("DOMContentLoaded", function () {

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

    // 🌟 '수정' 버튼 클릭 시
    document.querySelectorAll('.btn-edit-pet').forEach(btn => {
        btn.addEventListener('click', function() {
            document.getElementById('petRegForm').reset();
            const dataset = this.dataset;

            document.getElementById('petIdInput').value = dataset.id;
            document.querySelector('input[name="petName"]').value = dataset.name;
            document.getElementById('modalAnimalType').value = dataset.animal;

            // 품종 리스트 강제 세팅 (하위 셀렉트박스 생성 유도)
            document.getElementById('modalAnimalType').dispatchEvent(new Event('change'));

            // 리스트가 생성될 시간을 약간 확보한 뒤 품종 값 세팅
            setTimeout(() => {
                document.getElementById('modalSubAnimalType').value = dataset.breed;
            }, 50);

            document.querySelector('select[name="gender"]').value = dataset.gender;
            document.querySelector('input[name="birthDate"]').value = dataset.birth;
            document.querySelector('input[name="weight"]').value = dataset.weight;
            document.querySelector('input[name="regNumber"]').value = dataset.reg;

            petModal.classList.remove('hidden');
            petModal.classList.add('flex');
        });
    });

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
                // 🌟 MEMBER_PET DB 구조에 맞춰품종 '이름(String)'을 value로 세팅합니다.
                opt.value = sub.animalName;
                opt.textContent = sub.animalName;
                modalSubAnimalType.appendChild(opt);
            });
        });
    }

    const btnSubmitPet = document.getElementById('btnSubmitPet');
    if(btnSubmitPet) {
        btnSubmitPet.addEventListener('click', function() {
            const form = document.getElementById('petRegForm');

            if(!form.checkValidity()) {
                form.reportValidity();
                return;
            }

            // 만약에라도 스크립트 우회해서 마이너스 입력이 들어오면 여기서 2차 차단
            const weightInput = document.querySelector('input[name="weight"]').value;
            if(weightInput <= 0 || weightInput > 150) {
                alert('몸무게는 0.1kg ~ 150kg 사이로 올바르게 입력해주세요.');
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
                        window.location.reload();
                    } else {
                        alert('저장에 실패했습니다.');
                    }
                })
                .catch(err => console.error(err));
        });
    }
});