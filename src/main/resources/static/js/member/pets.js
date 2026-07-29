/*
 * ?뚯썝 諛섎젮?숇Ъ 愿由??붾㈃ ?꾩슜 JavaScript
 * ?깅줉쨌?섏젙쨌??젣, ?덉쥌 ?좏깮, ?낅젰媛?諛??대?吏 寃利앹쓣 ?대떦?⑸땲??
 */

const MIN_PET_WEIGHT = 0.1;
    const MAX_PET_WEIGHT = 100;

    const MAX_PET_IMAGE_SIZE =
        5 * 1024 * 1024;

    const ALLOWED_PET_IMAGE_TYPES = [
        'image/jpeg',
        'image/png',
        'image/webp'
    ];

    const petForm =
        document.getElementById(
            'petForm'
        );

    const weightInput =
        document.getElementById(
            'weight'
        );

    const birthDateInput =
        document.getElementById(
            'birthDate'
        );

    const petPhotoInput =
        document.getElementById(
            'petPhoto'
        );

    const saveMessage =
        document.getElementById(
            'saveMessage'
        );

    function showError(message) {
        saveMessage.classList.remove(
            'success'
        );

        saveMessage.textContent =
            message;
    }

    function showSuccess(message) {
        saveMessage.classList.add(
            'success'
        );

        saveMessage.textContent =
            message;
    }

    function clearMessage() {
        saveMessage.classList.remove(
            'success'
        );

        saveMessage.textContent = '';
    }

    function fillBreeds(
        selectedBreed
    ) {

        const animalId =
            document.getElementById(
                'animalId'
            ).value;

        const breedSelect =
            document.getElementById(
                'breedName'
            );

        breedSelect.innerHTML =
            '<option value="">품종을 선택하세요</option>';

        subAnimals
            .filter(function (item) {
                return String(
                    item.parentId
                ) === String(
                    animalId
                );
            })
            .forEach(function (item) {
                const option =
                    document.createElement(
                        'option'
                    );

                option.value =
                    item.animalName;

                option.textContent =
                    item.animalName;

                if (item.animalName
                    === selectedBreed) {

                    option.selected =
                        true;
                }

                breedSelect.appendChild(
                    option
                );
            });
    }

    function loadPet(button) {
        document.getElementById(
            'petId'
        ).value = button.dataset.id;

        document.getElementById(
            'petName'
        ).value = button.dataset.name;

        document.getElementById(
            'animalId'
        ).value = button.dataset.animal;

        fillBreeds(
            button.dataset.breed
        );

        document.getElementById(
            'gender'
        ).value = button.dataset.gender;

        document.getElementById(
            'birthDate'
        ).value = button.dataset.birth;

        weightInput.value =
            button.dataset.weight;

        document.getElementById(
            'registrationNo'
        ).value =
            button.dataset.reg || '';

        petPhotoInput.value = '';

        document.getElementById(
            'formTitle'
        ).textContent =
            '반려동물 수정';

        document.getElementById(
            'saveBtn'
        ).disabled = false;

        clearMessage();

        document.getElementById(
            'petFormCard'
        ).scrollIntoView({
            behavior: 'smooth'
        });
    }

    function resetForm() {
        if (petCount >= maxPetCount) {
            showError(
                '반려동물은 최대 4마리까지 등록할 수 있습니다.'
            );

            return;
        }

        petForm.reset();

        document.getElementById(
            'petId'
        ).value = 0;

        document.getElementById(
            'breedName'
        ).innerHTML =
            '<option value="">종류를 먼저 선택하세요</option>';

        document.getElementById(
            'formTitle'
        ).textContent =
            '반려동물 신규 등록';

        document.getElementById(
            'saveBtn'
        ).disabled = false;

        clearMessage();
    }

    function validatePetWeight() {
        const weight =
            Number(
                weightInput.value
            );

        if (!Number.isFinite(weight)
            || weight < MIN_PET_WEIGHT
            || weight > MAX_PET_WEIGHT) {

            showError(
                '몸무게는 0.1kg 이상 100kg 이하로 입력해 주세요.'
            );

            weightInput.focus();

            return false;
        }

        return true;
    }

    /* 현재 사용자의 로컬 날짜를 YYYY-MM-DD 형식으로 반환 */
    function getTodayDateString() {
        const today =
            new Date();

        const timezoneOffset =
            today.getTimezoneOffset()
            * 60
            * 1000;

        return new Date(
            today.getTime()
            - timezoneOffset
        )
            .toISOString()
            .slice(
                0,
                10
            );
    }

    /* 반려동물 생년월일 필수 입력 및 미래 날짜 검증 */
    function validatePetBirthDate() {
        const birthDate =
            birthDateInput.value;

        if (!birthDate) {
            showError(
                '반려동물의 생년월일을 입력해 주세요.'
            );

            birthDateInput.focus();

            return false;
        }

        if (birthDate
            > getTodayDateString()) {

            showError(
                '반려동물의 생년월일은 오늘 이후로 입력할 수 없습니다.'
            );

            birthDateInput.focus();

            return false;
        }

        return true;
    }

    function validatePetImage() {
        const file =
            petPhotoInput.files[0];

        if (!file) {
            return true;
        }

        if (!ALLOWED_PET_IMAGE_TYPES.includes(
            file.type
        )) {

            showError(
                '사진은 JPG, JPEG, PNG, WEBP 형식만 등록할 수 있습니다.'
            );

            petPhotoInput.value = '';

            return false;
        }

        if (file.size
            > MAX_PET_IMAGE_SIZE) {

            showError(
                '사진은 최대 5MB까지만 등록할 수 있습니다.'
            );

            petPhotoInput.value = '';

            return false;
        }

        return true;
    }

    /* 달력에서 오늘 이후 날짜를 선택하지 못하도록 최대 날짜 지정 */
    birthDateInput.max =
        getTodayDateString();

    /* 날짜를 직접 변경했을 때 즉시 오류 안내 */
    birthDateInput.addEventListener(
        'change',
        function () {
            clearMessage();

            if (this.value
                && !validatePetBirthDate()) {

                return;
            }
        }
    );

    weightInput.addEventListener(
        'input',
        function () {

            const weight =
                Number(
                    this.value
                );

            if (this.value === '') {
                clearMessage();
                return;
            }

            if (!Number.isFinite(weight)
                || weight < MIN_PET_WEIGHT
                || weight > MAX_PET_WEIGHT) {

                showError(
                    '몸무게는 0.1kg 이상 100kg 이하로 입력해 주세요.'
                );

                return;
            }

            clearMessage();
        }
    );

    petPhotoInput.addEventListener(
        'change',
        function () {

            clearMessage();

            if (!this.files[0]) {
                return;
            }

            if (validatePetImage()) {
                showSuccess(
                    '등록 가능한 사진입니다.'
                );
            }
        }
    );

    petForm.addEventListener(
        'submit',
        async function (event) {

            event.preventDefault();

            clearMessage();

            const petId =
                document.getElementById(
                    'petId'
                ).value;

            if ((petId === ''
                    || petId === '0')
                && petCount >= maxPetCount) {

                showError(
                    '반려동물은 최대 4마리까지 등록할 수 있습니다.'
                );

                return;
            }

            if (!validatePetWeight()) {
                return;
            }

            // 등록·수정 제출 직전에 미래 생년월일을 다시 확인
            if (!validatePetBirthDate()) {
                return;
            }

            if (!validatePetImage()) {
                return;
            }

            const saveButton =
                document.getElementById(
                    'saveBtn'
                );

            saveButton.disabled = true;

            try {
                const response =
                    await fetch(
                        '/pet/api/save',
                        {
                            method: 'POST',
                            body: new FormData(
                                event.target
                            )
                        }
                    );

                const result =
                    await response.json();

                if (!response.ok
                    || !result.isSuccess) {

                    showError(
                        result.message
                        || '저장하지 못했습니다. 입력 내용을 확인해 주세요.'
                    );

                    saveButton.disabled =
                        false;

                    return;
                }

                if (returnTo
                    === 'hospitalSearch') {

                    location.href =
                        '/hospital/search';

                    return;
                }

                location.reload();
            } catch (error) {
                console.error(
                    error
                );

                showError(
                    '저장 중 오류가 발생했습니다. 다시 시도해 주세요.'
                );

                saveButton.disabled =
                    false;
            }
        }
    );

    document
        .querySelectorAll(
            '.pet-delete-form'
        )
        .forEach(function (form) {

            form.addEventListener(
                'submit',
                async function (event) {

                    event.preventDefault();

                    if (!confirm(
                        '정말 이 반려동물을 삭제하시겠습니까?'
                    )) {
                        return;
                    }

                    try {
                        const response =
                            await fetch(
                                '/pet/api/delete',
                                {
                                    method: 'POST',
                                    body: new FormData(
                                        form
                                    )
                                }
                            );

                        const result =
                            await response.json();

                        if (result.isSuccess) {
                            location.reload();
                            return;
                        }

                        alert(
                            result.message
                            || '반려동물을 삭제하지 못했습니다.'
                        );
                    } catch (error) {
                        console.error(
                            error
                        );

                        alert(
                            '삭제 중 오류가 발생했습니다. 다시 시도해 주세요.'
                        );
                    }
                }
            );
        });

    if (petCount >= maxPetCount) {
        document.getElementById(
            'saveBtn'
        ).disabled = true;

        showError(
            '현재 4마리가 등록되어 있습니다. '
            + '새로 등록하려면 기존 반려동물을 삭제해 주세요.'
        );
    }

    if (editPetId) {
        const editButton =
            Array.from(
                document.querySelectorAll(
                    '.edit-btn'
                )
            ).find(function (button) {
                return button.dataset.id
                    === String(
                        editPetId
                    );
            });

        if (editButton) {
            loadPet(
                editButton
            );
        }
    }