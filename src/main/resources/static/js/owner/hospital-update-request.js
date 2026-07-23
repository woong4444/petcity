document.addEventListener("DOMContentLoaded", function () {
    initBusinessNumber();
    initDocumentFileName();
    initHospitalImagePreview();
    initAddressSearch();
    showExistingHospitalMap();
    initSubmitValidation();
});


function initBusinessNumber() {
    const businessNumberInput =
        document.getElementById("businessNumber");

    if (!businessNumberInput) {
        return;
    }

    businessNumberInput.addEventListener("input", function () {
        const numberOnly = this.value
            .replace(/[^0-9]/g, "")
            .slice(0, 10);

        let formattedNumber = numberOnly;

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

        this.value = formattedNumber;
    });
}


function initDocumentFileName() {
    const documentFile =
        document.getElementById("documentFile");

    const fileNameElement =
        document.getElementById("documentFileName");

    if (!documentFile || !fileNameElement) {
        return;
    }

    documentFile.addEventListener("change", function () {
        const file = this.files[0];

        fileNameElement.textContent = file
            ? "선택한 파일: " + file.name
            : "사업자등록증 또는 동물병원 개설신고증을 첨부해 주세요.";
    });
}


function initHospitalImagePreview() {
    const imageInput =
        document.getElementById("hospitalImage");

    const previewBox =
        document.getElementById("hospitalImagePreview");

    if (!imageInput || !previewBox) {
        return;
    }

    let previewUrl = null;

    imageInput.addEventListener("change", function () {
        previewBox.innerHTML = "";
        previewBox.classList.remove("show");

        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
            previewUrl = null;
        }

        const file = this.files[0];

        if (!file) {
            return;
        }

        if (!file.type.startsWith("image/")) {
            alert("이미지 파일을 선택해 주세요.");
            this.value = "";
            return;
        }

        previewUrl = URL.createObjectURL(file);

        const image = document.createElement("img");
        image.src = previewUrl;
        image.alt = "새 병원 대표이미지 미리보기";

        previewBox.appendChild(image);
        previewBox.classList.add("show");
    });
}


function initAddressSearch() {
    const searchButton =
        document.getElementById("addressSearchButton");

    const addressInput =
        document.getElementById("hospitalAddress");

    const detailAddressInput =
        document.getElementById("hospitalDetailAddress");

    const districtInput =
        document.getElementById("hospitalDistrict");

    const latitudeInput =
        document.getElementById("hospitalLatitude");

    const longitudeInput =
        document.getElementById("hospitalLongitude");

    if (!searchButton
        || !addressInput
        || !districtInput
        || !latitudeInput
        || !longitudeInput) {
        return;
    }

    searchButton.addEventListener("click", function () {
        if (!window.kakao || !window.kakao.Postcode) {
            alert("주소 검색 서비스를 불러오지 못했습니다.");
            return;
        }

        new kakao.Postcode({
            oncomplete: function (data) {
                const selectedAddress =
                    data.roadAddress
                    || data.jibunAddress
                    || data.address;

                addressInput.value = selectedAddress;

                districtInput.value =
                    data.sigungu
                    || extractDistrict(selectedAddress);

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
    });
}


function extractDistrict(address) {
    if (!address) {
        return "";
    }

    const parts = address.split(/\s+/);

    const districtParts = parts.filter(function (part) {
        return part.endsWith("구")
            || part.endsWith("군")
            || part.endsWith("시");
    });

    if (districtParts.length >= 2) {
        return districtParts[districtParts.length - 2]
            + " "
            + districtParts[districtParts.length - 1];
    }

    return districtParts.length === 1
        ? districtParts[0]
        : "";
}


function searchAddressCoordinates(
    address,
    latitudeInput,
    longitudeInput
) {
    if (!window.kakao
        || !window.kakao.maps
        || !window.kakao.maps.services) {
        alert("지도 좌표 서비스를 불러오지 못했습니다.");
        return;
    }

    const geocoder =
        new kakao.maps.services.Geocoder();

    geocoder.addressSearch(address, function (result, status) {
        if (status !== kakao.maps.services.Status.OK
            || !result
            || result.length === 0) {

            alert("주소의 지도 좌표를 찾지 못했습니다.");
            return;
        }

        latitudeInput.value = result[0].y;
        longitudeInput.value = result[0].x;

        showHospitalMap(
            result[0].y,
            result[0].x
        );
    });
}


let hospitalMap = null;
let hospitalMarker = null;

function showHospitalMap(latitude, longitude) {
    const mapContainer =
        document.getElementById("hospitalMap");

    if (!mapContainer
        || !window.kakao
        || !window.kakao.maps) {
        return;
    }

    mapContainer.hidden = false;

    const position = new kakao.maps.LatLng(
        latitude,
        longitude
    );

    if (!hospitalMap) {
        hospitalMap = new kakao.maps.Map(
            mapContainer,
            {
                center: position,
                level: 3
            }
        );

        hospitalMarker = new kakao.maps.Marker({
            map: hospitalMap,
            position: position
        });

    } else {
        hospitalMap.setCenter(position);
        hospitalMarker.setPosition(position);
        hospitalMap.relayout();
    }
}


function showExistingHospitalMap() {
    const latitude =
        document.getElementById("hospitalLatitude")?.value;

    const longitude =
        document.getElementById("hospitalLongitude")?.value;

    if (!latitude || !longitude) {
        return;
    }

    showHospitalMap(latitude, longitude);
}


function initSubmitValidation() {
    const form =
        document.getElementById("hospitalUpdateRequestForm");

    if (!form) {
        return;
    }

    form.addEventListener("submit", function (event) {
        if (!form.checkValidity()) {
            event.preventDefault();
            form.reportValidity();
            return;
        }

        const latitude =
            document.getElementById("hospitalLatitude");

        const longitude =
            document.getElementById("hospitalLongitude");

        if (!latitude.value || !longitude.value) {
            event.preventDefault();

            alert("주소 검색을 통해 병원 위치를 설정해 주세요.");

            document.getElementById("hospitalAddress")
                ?.scrollIntoView({
                    behavior: "smooth",
                    block: "center"
                });

            return;
        }

        const submitButton =
            form.querySelector(".submit-button");

        if (submitButton) {
            submitButton.disabled = true;
            submitButton.textContent = "수정 요청 제출 중...";
        }
    });
}