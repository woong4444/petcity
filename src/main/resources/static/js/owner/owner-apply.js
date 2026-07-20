document.addEventListener(
    "DOMContentLoaded",
    function () {

        initBusinessNumber();
        initAnimalSelection();
        initServiceSelection();
        initAddressSearch();
        initHospitalImagePreview();
        initDocumentFileName();
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
   진료 가능 동물 선택
======================================== */

function initAnimalSelection() {

    const categoryElements =
        document.querySelectorAll(
            ".animal-category"
        );

    categoryElements.forEach(
        function (categoryElement) {

            const categoryId =
                categoryElement.dataset.categoryId;

            /*
     체크박스 요소라는 것을 IntelliJ에 알려줌
 */
            const parentToggle =
                /** @type {HTMLInputElement | null} */ (
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
                화면을 다시 열었을 때
                기존 선택값이 있으면 자동으로 펼치기
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

                parentToggle.checked = true;

                openAnimalCategory(
                    categoryElement,
                    childArea
                );
            }


            /*
                전체 가능이 선택된 상태라면
                하위 품종 비활성화
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


            /*
                대분류 선택 또는 해제
            */
            parentToggle.addEventListener(
                "change",
                function () {

                    if (this.checked) {

                        openAnimalCategory(
                            categoryElement,
                            childArea
                        );

                        return;
                    }

                    /*
                        대분류 해제 시
                        해당 그룹 선택 전부 초기화
                    */
                    closeAnimalCategory(
                        categoryElement,
                        childArea
                    );

                    allCheckbox.checked =
                        false;

                    childCheckboxes.forEach(
                        function (childCheckbox) {

                            childCheckbox.checked =
                                false;

                            childCheckbox.disabled =
                                false;
                        }
                    );
                }
            );


            /*
                해당 동물 전부 가능
            */
            allCheckbox.addEventListener(
                "change",
                function () {

                    if (this.checked) {

                        /*
                            기존 하위 품종 선택을
                            전부 해제하고 비활성화
                        */
                        childCheckboxes.forEach(
                            function (childCheckbox) {

                                childCheckbox.checked =
                                    false;

                                childCheckbox.disabled =
                                    true;
                            }
                        );

                    } else {

                        /*
                            전체 가능 해제 시
                            하위 품종 다시 활성화
                        */
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
                하위 품종 개별 선택
            */
            childCheckboxes.forEach(
                function (childCheckbox) {

                    childCheckbox.addEventListener(
                        "change",
                        function () {

                            /*
                                안전을 위해 하위 품종 선택 시
                                전체 가능은 자동 해제
                            */
                            if (this.checked) {

                                allCheckbox.checked =
                                    false;
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
    childArea
) {

    categoryElement.classList.add(
        "active"
    );

    childArea.classList.add(
        "open"
    );
}


function closeAnimalCategory(
    categoryElement,
    childArea
) {

    categoryElement.classList.remove(
        "active"
    );

    childArea.classList.remove(
        "open"
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