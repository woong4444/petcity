document.addEventListener(
    "DOMContentLoaded",
    function () {

        initBusinessNumber();
        initHospitalPhone();
        initBreakTime();
        initClosedDays();
        initAnimalSelection();
        initServiceSelection();
        initMedicalSubjectSelection();
        initAddressSearch();
        initHospitalImagePreview();
        initDocumentFileName();
        initCharacterCounters();
        /*
            ADMIN, OWNER 신청 버튼 클릭 차단
            필수 입력값 검사보다 먼저 실행
        */
        initOwnerRoleBlock();
        initOwnerApplyForm();
    }
);


/* ========================================
   사업자등록번호 자동 하이픈
======================================== */

function initBusinessNumber() {

    const businessNumberInput =
        document.getElementById(
            "businessNumber"
        );

    if (!businessNumberInput) {
        return;
    }

    businessNumberInput.addEventListener(
        "input",
        function () {

            const numberOnly =
                this.value
                    .replace(/[^0-9]/g, "")
                    .slice(0, 10);

            let formattedNumber =
                numberOnly;

            if (numberOnly.length > 5) {

                formattedNumber =
                    numberOnly.slice(0, 3)
                    + "-"
                    + numberOnly.slice(3, 5)
                    + "-"
                    + numberOnly.slice(5);

            } else if (numberOnly.length > 3) {

                formattedNumber =
                    numberOnly.slice(0, 3)
                    + "-"
                    + numberOnly.slice(3);
            }

            this.value =
                formattedNumber;
        }
    );
}



/* ========================================
   병원 전화번호 분리 입력
======================================== */

function initHospitalPhone() {

    const hiddenInput =
        document.getElementById(
            "hospitalPhone"
        );

    const prefixSelect =
        document.getElementById(
            "hospitalPhonePrefix"
        );

    const middleInput =
        document.getElementById(
            "hospitalPhoneMiddle"
        );

    const lastInput =
        document.getElementById(
            "hospitalPhoneLast"
        );

    if (!hiddenInput
        || !prefixSelect
        || !middleInput
        || !lastInput) {

        return;
    }


    /*
        신청 실패 후 다시 돌아왔을 때
        기존 전화번호를 세 칸으로 복원한다.

        예:
        031-1234-5678
    */
    const savedPhone =
        hiddenInput.value.trim();

    if (savedPhone) {

        const phoneParts =
            savedPhone.split("-");

        if (phoneParts.length === 3) {

            prefixSelect.value =
                phoneParts[0];

            middleInput.value =
                phoneParts[1]
                    .replace(/[^0-9]/g, "")
                    .slice(0, 4);

            lastInput.value =
                phoneParts[2]
                    .replace(/[^0-9]/g, "")
                    .slice(0, 4);
        }
    }


    /*
        가운데와 마지막 칸에는
        숫자만 입력할 수 있게 한다.
    */
    middleInput.addEventListener(
        "input",
        function () {

            this.value =
                this.value
                    .replace(/[^0-9]/g, "")
                    .slice(0, 4);

            updateHospitalPhoneValue();
        }
    );

    lastInput.addEventListener(
        "input",
        function () {

            this.value =
                this.value
                    .replace(/[^0-9]/g, "")
                    .slice(0, 4);

            updateHospitalPhoneValue();
        }
    );

    prefixSelect.addEventListener(
        "change",
        updateHospitalPhoneValue
    );


    /*
        select + 가운데 + 마지막 값을 합쳐서
        DTO의 hospitalPhone으로 전송한다.
    */
    function updateHospitalPhoneValue() {

        const prefix =
            prefixSelect.value;

        const middle =
            middleInput.value;

        const last =
            lastInput.value;

        if (!prefix
            || !middle
            || !last) {

            hiddenInput.value = "";
            return;
        }

        hiddenInput.value =
            prefix
            + "-"
            + middle
            + "-"
            + last;
    }


    updateHospitalPhoneValue();
}


/* ========================================
   휴게시간 시작·종료 입력
======================================== */

function initBreakTime() {

    const hiddenInput =
        document.getElementById(
            "hospitalBreakTime"
        );

    const startInput =
        document.getElementById(
            "hospitalBreakStart"
        );

    const endInput =
        document.getElementById(
            "hospitalBreakEnd"
        );

    if (!hiddenInput
        || !startInput
        || !endInput) {

        return;
    }

    /*
        신청 실패 후 돌아왔을 때
        기존 12:30~13:30 값을 다시 나눈다.
    */
    const savedValue =
        hiddenInput.value.trim();

    if (savedValue.includes("~")) {

        const [
            savedStart,
            savedEnd
        ] = savedValue.split("~");

        startInput.value =
            savedStart || "";

        endInput.value =
            savedEnd || "";
    }

    function updateBreakTime() {

        const start =
            startInput.value;

        const end =
            endInput.value;

        /*
            두 값이 모두 없으면 휴게시간 없음
        */
        if (!start && !end) {

            hiddenInput.value = "";
            startInput.setCustomValidity("");
            endInput.setCustomValidity("");
            return;
        }

        /*
            한쪽만 입력한 경우
        */
        if (!start || !end) {

            hiddenInput.value = "";

            const message =
                "휴게시간 시작과 종료 시간을 모두 선택해 주세요.";

            startInput.setCustomValidity(
                message
            );

            endInput.setCustomValidity(
                message
            );

            return;
        }

        /*
            종료 시간이 시작 시간보다 빠르거나 같으면 차단
        */
        if (start >= end) {

            hiddenInput.value = "";

            endInput.setCustomValidity(
                "휴게시간 종료 시간은 시작 시간보다 늦어야 합니다."
            );

            startInput.setCustomValidity("");
            return;
        }

        startInput.setCustomValidity("");
        endInput.setCustomValidity("");

        hiddenInput.value =
            start
            + "~"
            + end;
    }

    startInput.addEventListener(
        "change",
        updateBreakTime
    );

    endInput.addEventListener(
        "change",
        updateBreakTime
    );

    updateBreakTime();
}


/* ========================================
   정기 휴무일 다중 선택
======================================== */

function initClosedDays() {

    const hiddenInput =
        document.getElementById(
            "hospitalClosedDays"
        );

    const checkboxes =
        document.querySelectorAll(
            ".closed-day-checkbox"
        );

    if (!hiddenInput
        || checkboxes.length === 0) {

        return;
    }

    /*
        신청 실패 후 돌아왔을 때
        기존 선택 요일 복원
    */
    const savedDays =
        hiddenInput.value
            .split(",")
            .map(
                day => day.trim()
            )
            .filter(
                day => day
            );

    checkboxes.forEach(
        function (checkbox) {

            checkbox.checked =
                savedDays.includes(
                    checkbox.value
                );

            checkbox.addEventListener(
                "change",
                updateClosedDays
            );
        }
    );

    function updateClosedDays() {

        const selectedDays =
            Array.from(
                checkboxes
            )
                .filter(
                    checkbox =>
                        checkbox.checked
                )
                .map(
                    checkbox =>
                        checkbox.value
                );

        hiddenInput.value =
            selectedDays.join(
                ", "
            );
    }

    updateClosedDays();
}


/* ========================================
   진료 가능 동물 선택
======================================== */

function initAnimalSelection() {

    const categoryElements =
        document.querySelectorAll(
            ".animal-category"
        );

    categoryElements.forEach(
        function (categoryElement) {

            const parentToggle =
                /** @type {HTMLButtonElement | null} */ (
                categoryElement.querySelector(
                    ".animal-parent-toggle"
                )
            );

            const childArea =
                /** @type {HTMLElement | null} */ (
                categoryElement.querySelector(
                    ".animal-child-area"
                )
            );

            const allCheckbox =
                /** @type {HTMLInputElement | null} */ (
                categoryElement.querySelector(
                    ".animal-all-checkbox"
                )
            );

            const childCheckboxes =
                /** @type {NodeListOf<HTMLInputElement>} */ (
                categoryElement.querySelectorAll(
                    ".animal-child-checkbox"
                )
            );

            if (!parentToggle
                || !childArea
                || !allCheckbox) {

                return;
            }

            /*
                신청 실패 후 돌아왔을 때
                선택값이 있는 동물 그룹은 자동으로 펼친다.
            */
            const hasSelectedValue =
                allCheckbox.checked
                || Array.from(
                    childCheckboxes
                ).some(
                    checkbox =>
                        checkbox.checked
                );

            if (hasSelectedValue) {

                openAnimalCategory(
                    categoryElement,
                    childArea,
                    parentToggle
                );
            }

            /*
                대분류 버튼은 목록을 열고 닫기만 한다.
                닫더라도 기존 선택값은 절대 지우지 않는다.
            */
            parentToggle.addEventListener(
                "click",
                function () {

                    const isOpen =
                        childArea.classList.contains(
                            "open"
                        );

                    if (isOpen) {

                        closeAnimalCategory(
                            categoryElement,
                            childArea,
                            parentToggle
                        );

                    } else {

                        openAnimalCategory(
                            categoryElement,
                            childArea,
                            parentToggle
                        );
                    }
                }
            );

            /*
                전체 진료 가능을 선택하면
                같은 그룹의 하위 품종 선택만 해제하고 비활성화한다.
            */
            if (allCheckbox.checked) {

                childCheckboxes.forEach(
                    function (childCheckbox) {

                        childCheckbox.checked =
                            false;

                        childCheckbox.disabled =
                            true;
                    }
                );
            }

            allCheckbox.addEventListener(
                "change",
                function () {

                    if (this.checked) {

                        childCheckboxes.forEach(
                            function (childCheckbox) {

                                childCheckbox.checked =
                                    false;

                                childCheckbox.disabled =
                                    true;
                            }
                        );

                    } else {

                        childCheckboxes.forEach(
                            function (childCheckbox) {

                                childCheckbox.disabled =
                                    false;
                            }
                        );
                    }
                }
            );

            /*
                하위 품종을 선택하면
                전체 진료 가능 선택은 자동 해제한다.
            */
            childCheckboxes.forEach(
                function (childCheckbox) {

                    childCheckbox.addEventListener(
                        "change",
                        function () {

                            if (this.checked) {

                                allCheckbox.checked =
                                    false;

                                childCheckboxes.forEach(
                                    function (checkbox) {

                                        checkbox.disabled =
                                            false;
                                    }
                                );
                            }
                        }
                    );
                }
            );
        }
    );
}


function openAnimalCategory(
    categoryElement,
    childArea,
    parentToggle
) {

    categoryElement.classList.add(
        "active"
    );

    childArea.classList.add(
        "open"
    );

    parentToggle.setAttribute(
        "aria-expanded",
        "true"
    );
}


function closeAnimalCategory(
    categoryElement,
    childArea,
    parentToggle
) {

    categoryElement.classList.remove(
        "active"
    );

    childArea.classList.remove(
        "open"
    );

    parentToggle.setAttribute(
        "aria-expanded",
        "false"
    );
}


/* ========================================
   진료 서비스 전체 선택
======================================== */

function initServiceSelection() {

    const selectAllCheckbox =
        document.getElementById(
            "serviceSelectAll"
        );

    const serviceCheckboxes =
        document.querySelectorAll(
            ".service-checkbox"
        );

    if (!selectAllCheckbox
        || serviceCheckboxes.length === 0) {

        return;
    }
    /*
        전체 선택 체크박스
    */
    selectAllCheckbox.addEventListener(
        "change",
        function () {

            serviceCheckboxes.forEach(
                function (serviceCheckbox) {

                    serviceCheckbox.checked =
                        selectAllCheckbox.checked;
                }
            );
        }
    );


    /*
        개별 서비스 선택 상태에 따라
        전체 선택 상태 갱신
    */
    serviceCheckboxes.forEach(
        function (serviceCheckbox) {

            serviceCheckbox.addEventListener(
                "change",
                updateServiceSelectAll
            );
        }
    );

    updateServiceSelectAll();


    function updateServiceSelectAll() {

        const checkedCount =
            Array.from(
                serviceCheckboxes
            ).filter(
                checkbox =>
                    checkbox.checked
            ).length;

        selectAllCheckbox.checked =
            checkedCount
            === serviceCheckboxes.length;

        selectAllCheckbox.indeterminate =
            checkedCount > 0
            && checkedCount
            < serviceCheckboxes.length;
    }
}


/* ========================================
   진료과목 전체 선택
======================================== */

function initMedicalSubjectSelection() {

    const selectAllCheckbox =
        document.getElementById(
            "subjectSelectAll"
        );

    const subjectCheckboxes =
        document.querySelectorAll(
            ".subject-checkbox"
        );

    if (!selectAllCheckbox
        || subjectCheckboxes.length === 0) {

        return;
    }


    /*
        전체 선택 또는 전체 해제
    */
    selectAllCheckbox.addEventListener(
        "change",
        function () {

            subjectCheckboxes.forEach(
                function (subjectCheckbox) {

                    subjectCheckbox.checked =
                        selectAllCheckbox.checked;
                }
            );

            /*
                사용자가 전체 선택을 직접 누른 경우
                중간 상태 표시를 해제한다.
            */
            selectAllCheckbox.indeterminate =
                false;
        }
    );


    /*
        개별 선택 상태에 따라
        전체 선택 체크박스 상태 변경
    */
    subjectCheckboxes.forEach(
        function (subjectCheckbox) {

            subjectCheckbox.addEventListener(
                "change",
                updateSubjectSelectAll
            );
        }
    );

    updateSubjectSelectAll();


    function updateSubjectSelectAll() {

        const checkedCount =
            Array.from(
                subjectCheckboxes
            ).filter(
                checkbox =>
                    checkbox.checked
            ).length;

        selectAllCheckbox.checked =
            checkedCount
            === subjectCheckboxes.length;

        selectAllCheckbox.indeterminate =
            checkedCount > 0
            && checkedCount
            < subjectCheckboxes.length;
    }
}


/* ========================================
   주소 검색 및 좌표 자동 입력
======================================== */

function initAddressSearch() {

    const addressSearchButton =
        document.getElementById(
            "addressSearchButton"
        );

    const hospitalAddressInput =
        document.getElementById(
            "hospitalAddress"
        );

    const detailAddressInput =
        document.getElementById(
            "hospitalDetailAddress"
        );

    const districtInput =
        document.getElementById(
            "hospitalDistrict"
        );

    const latitudeInput =
        document.getElementById(
            "hospitalLatitude"
        );

    const longitudeInput =
        document.getElementById(
            "hospitalLongitude"
        );

    if (!addressSearchButton
        || !hospitalAddressInput
        || !districtInput
        || !latitudeInput
        || !longitudeInput) {

        return;
    }


    addressSearchButton.addEventListener(
        "click",
        function () {

            /*
                카카오 우편번호 스크립트 확인
            */
            if (!window.kakao
                || !window.kakao.Postcode) {

                alert(
                    "주소 검색 서비스를 불러오지 못했습니다."
                );

                return;
            }


            new kakao.Postcode({

                oncomplete: function (data) {

                    /*
                        도로명주소를 우선 사용하고
                        없으면 지번주소 사용
                    */
                    const selectedAddress =
                        data.roadAddress
                        || data.jibunAddress
                        || data.address;

                    hospitalAddressInput.value =
                        selectedAddress;


                    /*
                        서초구, 강남구,
                        고양시 일산동구 등
                    */
                    const district =
                        data.sigungu
                        || extractDistrict(
                            selectedAddress
                        );

                    districtInput.value =
                        district;


                    /*
                        기존 좌표 초기화 후
                        새 주소의 좌표 조회
                    */
                    latitudeInput.value = "";
                    longitudeInput.value = "";

                    searchAddressCoordinates(
                        selectedAddress,
                        latitudeInput,
                        longitudeInput
                    );


                    if (detailAddressInput) {

                        detailAddressInput.focus();
                    }
                }

            }).open();
        }
    );
}


/*
    주소 문자열에서 구 단위 추출

    우편번호 API의 sigungu가 없는 경우에만 사용
*/
function extractDistrict(
    address
) {

    if (!address) {
        return "";
    }

    const parts =
        address.split(/\s+/);

    const districtParts =
        parts.filter(
            function (part) {

                return part.endsWith("구")
                    || part.endsWith("군")
                    || part.endsWith("시");
            }
        );

    /*
        고양시 일산동구처럼
        시와 구가 함께 있는 경우
    */
    if (districtParts.length >= 2) {

        return districtParts[
            districtParts.length - 2
                ]
            + " "
            + districtParts[
            districtParts.length - 1
                ];
    }

    return districtParts.length === 1
        ? districtParts[0]
        : "";
}


/*
    카카오 지도 주소-좌표 변환
*/
function searchAddressCoordinates(
    address,
    latitudeInput,
    longitudeInput
) {

    if (!window.kakao
        || !window.kakao.maps
        || !window.kakao.maps.services) {

        alert(
            "지도 좌표 서비스를 불러오지 못했습니다."
        );

        return;
    }

    const geocoder =
        new kakao.maps.services.Geocoder();

    geocoder.addressSearch(
        address,
        function (
            result,
            status
        ) {

            if (
                status
                !== kakao.maps.services.Status.OK
                || !result
                || result.length === 0
            ) {

                alert(
                    "주소의 지도 좌표를 찾지 못했습니다."
                );

                return;
            }

            const latitude =
                result[0].y;

            const longitude =
                result[0].x;

            latitudeInput.value =
                latitude;

            longitudeInput.value =
                longitude;

            showHospitalMap(
                latitude,
                longitude
            );
        }
    );
}


/*
    검색된 병원 위치를 지도에 표시
*/
let hospitalMap = null;
let hospitalMarker = null;

function showHospitalMap(
    latitude,
    longitude
) {

    const mapContainer =
        document.getElementById(
            "hospitalMap"
        );

    if (!mapContainer) {
        return;
    }

    mapContainer.hidden = false;

    const position =
        new kakao.maps.LatLng(
            latitude,
            longitude
        );

    if (!hospitalMap) {

        hospitalMap =
            new kakao.maps.Map(
                mapContainer,
                {
                    center: position,
                    level: 3
                }
            );

        hospitalMarker =
            new kakao.maps.Marker({
                map: hospitalMap,
                position: position
            });

    } else {

        hospitalMap.setCenter(
            position
        );

        hospitalMarker.setPosition(
            position
        );

        hospitalMap.relayout();
    }
}


/* ========================================
   병원 대표 이미지 미리보기
======================================== */

function initHospitalImagePreview() {

    const hospitalImageInput =
        document.getElementById(
            "hospitalImage"
        );

    const previewBox =
        document.getElementById(
            "hospitalImagePreview"
        );

    if (!hospitalImageInput
        || !previewBox) {

        return;
    }

    let previewUrl = null;

    hospitalImageInput.addEventListener(
        "change",
        function () {

            previewBox.innerHTML = "";
            previewBox.classList.remove(
                "show"
            );

            if (previewUrl) {

                URL.revokeObjectURL(
                    previewUrl
                );

                previewUrl = null;
            }

            const file =
                this.files[0];

            if (!file) {
                return;
            }

            if (!file.type.startsWith(
                "image/"
            )) {

                alert(
                    "이미지 파일을 선택해 주세요."
                );

                this.value = "";
                return;
            }

            previewUrl =
                URL.createObjectURL(
                    file
                );

            const image =
                document.createElement(
                    "img"
                );

            image.src =
                previewUrl;

            image.alt =
                "병원 대표 이미지 미리보기";

            previewBox.appendChild(
                image
            );

            previewBox.classList.add(
                "show"
            );
        }
    );
}


/* ========================================
   증빙서류 이름 표시
======================================== */

function initDocumentFileName() {

    const documentInput =
        document.getElementById(
            "documentFile"
        );

    if (!documentInput) {
        return;
    }

    const fileNameElement =
        document.createElement(
            "p"
        );

    fileNameElement.className =
        "selected-file-name";

    documentInput.insertAdjacentElement(
        "afterend",
        fileNameElement
    );

    documentInput.addEventListener(
        "change",
        function () {

            const file =
                this.files[0];

            fileNameElement.textContent =
                file
                    ? "선택한 파일: "
                    + file.name
                    : "";
        }
    );
}
/* ========================================
   글자 수 표시
======================================== */

function initCharacterCounters() {

    const targets =
        document.querySelectorAll(
            "[data-character-count='true'][maxlength]"
        );

    targets.forEach(
        function (target) {

            const maxLength =
                Number(target.maxLength);

            const counter =
                document.createElement(
                    "p"
                );

            counter.className =
                "character-count";

            target.insertAdjacentElement(
                "afterend",
                counter
            );

            function updateCounter() {

                const currentLength =
                    target.value.length;

                counter.textContent =
                    currentLength
                    + " / "
                    + maxLength
                    + "자";

                counter.classList.toggle(
                    "limit-warning",
                    currentLength
                    >= Math.floor(
                        maxLength * 0.9
                    )
                );
            }

            target.addEventListener(
                "input",
                updateCounter
            );

            updateCounter();
        }
    );
}


/* ========================================
   관리자 및 병원장 신청 차단
======================================== */

function initOwnerRoleBlock() {

    const form =
        document.getElementById(
            "ownerApplyForm"
        );

    const roleBlockMessage =
        document.getElementById(
            "roleBlockMessage"
        );

    if (!form
        || !roleBlockMessage) {

        return;
    }

    /*
        apply.html의
        th:data-member-role 값 읽기
    */
    const memberRole =
        form.dataset.memberRole;

    /*
        기존 신청 버튼
    */
    const submitButton =
        form.querySelector(
            ".submit-button"
        );

    if (!submitButton) {
        return;
    }

    /*
        USER는 정상 제출하므로
        클릭 차단 이벤트를 등록하지 않는다.
    */
    if (memberRole !== "ADMIN"
        && memberRole !== "OWNER") {

        return;
    }

    /*
        버튼 클릭 단계에서 먼저 막는다.

        form submit 이벤트보다 먼저 실행되므로
        required 입력 검사가 나오기 전에
        권한 안내 문구가 표시된다.
    */
    submitButton.addEventListener(
        "click",
        function (event) {

            event.preventDefault();

            let message =
                "병원장 권한을 신청할 수 없습니다.";

            if (memberRole === "ADMIN") {

                message =
                    "관리자 계정은 병원장 권한을 신청할 수 없습니다.";

            } else if (
                memberRole === "OWNER"
            ) {

                message =
                    "이미 병원장 권한을 보유한 회원입니다.";
            }

            roleBlockMessage.textContent =
                message;

            roleBlockMessage.style.display =
                "block";

            scrollToElement(
                roleBlockMessage
            );
        }
    );
}

/* ========================================
   최종 제출 검사
======================================== */

function initOwnerApplyForm() {

    const form =
        document.getElementById(
            "ownerApplyForm"
        );

    if (!form) {
        return;
    }

    form.addEventListener(
        "submit",
        function (event) {

            /*
                브라우저 기본 필수값 검사
            */
            if (!form.checkValidity()) {

                event.preventDefault();
                form.reportValidity();
                return;
            }


            /*
                진료 가능 동물 선택 검사
            */
            const selectedAnimals =
                document.querySelectorAll(
                    "input[name='animalIds']:checked"
                );

            if (selectedAnimals.length === 0) {

                event.preventDefault();

                alert(
                    "진료 가능한 동물을 하나 이상 선택해 주세요."
                );

                scrollToElement(
                    document.querySelector(
                        ".animal-category-list"
                    )
                );

                return;
            }


            /*
                진료 서비스 선택 검사
            */
            const selectedServices =
                document.querySelectorAll(
                    "input[name='serviceIds']:checked"
                );

            if (selectedServices.length === 0) {

                event.preventDefault();

                alert(
                    "제공 진료 서비스를 하나 이상 선택해 주세요."
                );

                scrollToElement(
                    document.querySelector(
                        ".service-grid"
                    )
                );

                return;
            }

            /*
    진료과목 선택 검사
*/
            const selectedSubjects =
                document.querySelectorAll(
                    "input[name='subjectIds']:checked"
                );

            if (selectedSubjects.length === 0) {

                event.preventDefault();

                alert(
                    "진료과목을 하나 이상 선택해 주세요."
                );

                scrollToElement(
                    document.querySelector(
                        ".medical-subject-grid"
                    )
                );

                return;
            }


            /*
                주소 좌표 검사
            */
            const latitudeInput =
                document.getElementById(
                    "hospitalLatitude"
                );

            const longitudeInput =
                document.getElementById(
                    "hospitalLongitude"
                );

            if (!latitudeInput.value
                || !longitudeInput.value) {

                event.preventDefault();

                alert(
                    "주소 검색을 통해 병원 위치를 설정해 주세요."
                );

                scrollToElement(
                    document.getElementById(
                        "hospitalAddress"
                    )
                );

                return;
            }


            /*
                중복 제출 방지
            */
            const submitButton =
                form.querySelector(
                    ".submit-button"
                );

            if (submitButton) {

                submitButton.disabled =
                    true;

                submitButton.textContent =
                    "신청서 제출 중...";
            }
        }
    );
}


function scrollToElement(
    element
) {

    if (!element) {
        return;
    }

    element.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}