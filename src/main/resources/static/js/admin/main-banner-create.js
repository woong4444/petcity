document.addEventListener("DOMContentLoaded", function () {
    const MAX_FILE_SIZE = 10 * 1024 * 1024;

    const form = document.querySelector("#mainBannerCreateForm");

    const titleInput = document.querySelector("#title");
    const titleLength = document.querySelector("#titleLength");

    const subTitleInput = document.querySelector("#subTitle");
    const subTitleLength = document.querySelector("#subTitleLength");

    const imageSourceOptions = Array.from(
        document.querySelectorAll(
            'input[name="imageSourceType"]',
        ),
    );

    const imageFileArea =
        document.querySelector("#imageFileArea");

    const imageUrlArea =
        document.querySelector("#imageUrlArea");

    const bannerImageFile =
        document.querySelector("#bannerImageFile");

    const imageUrlInput =
        document.querySelector("#imageUrl");

    const selectedFileInfo =
        document.querySelector("#selectedFileInfo");

    const selectedFileName =
        document.querySelector("#selectedFileName");

    const clearSelectedImage =
        document.querySelector("#clearSelectedImage");

    const bannerPreviewPlaceholder =
        document.querySelector("#bannerPreviewPlaceholder");

    const linkUrlInput =
        document.querySelector("#linkUrl");

    const displayOrderInput =
        document.querySelector("#displayOrder");

    const activeYnSelect =
        document.querySelector("#activeYn");

    const startAtInput =
        document.querySelector("#startAt");

    const endAtInput =
        document.querySelector("#endAt");

    const submitBannerButton =
        document.querySelector("#submitBannerButton");

    const bannerPreviewLink =
        document.querySelector("#bannerPreviewLink");

    const bannerPreviewImage =
        document.querySelector("#bannerPreviewImage");

    const bannerPreviewTitle =
        document.querySelector("#bannerPreviewTitle");

    const bannerPreviewSubTitle =
        document.querySelector("#bannerPreviewSubTitle");

    const previewDisplayOrder =
        document.querySelector("#previewDisplayOrder");

    const previewLinkUrl =
        document.querySelector("#previewLinkUrl");

    const previewImageSourceType =
        document.querySelector("#previewImageSourceType");

    const previewActiveBadge =
        document.querySelector("#previewActiveBadge");

    let previewObjectUrl = null;

    updateTitlePreview();
    updateSubTitlePreview();
    updateTitleLength();
    updateSubTitleLength();
    updateDisplayOrderPreview();
    updateActivePreview();
    updateLinkPreview();
    updateImageSourceScreen();

    if (titleInput !== null) {
        titleInput.addEventListener("input", function () {
            updateTitleLength();
            updateTitlePreview();
            updatePreviewAltText();
        });
    }

    if (subTitleInput !== null) {
        subTitleInput.addEventListener("input", function () {
            updateSubTitleLength();
            updateSubTitlePreview();
        });
    }

    imageSourceOptions.forEach(function (option) {
        option.addEventListener("change", function () {
            updateImageSourceScreen();
        });
    });

    if (bannerImageFile !== null) {
        bannerImageFile.addEventListener("change", function () {
            const file = bannerImageFile.files[0];

            if (file === undefined) {
                clearFilePreview();
                return;
            }

            if (!isAllowedImageFile(file)) {
                alert(
                    "JPG, JPEG, PNG, WEBP, GIF, AVIF 형식의 이미지만 등록할 수 있습니다.",
                );

                clearFileInput();
                clearFilePreview();

                return;
            }

            if (file.size > MAX_FILE_SIZE) {
                alert(
                    "이미지 파일은 10MB 이하로 등록해 주세요.",
                );

                clearFileInput();
                clearFilePreview();

                return;
            }

            showSelectedFile(file);
            showFilePreview(file);
        });
    }

    if (clearSelectedImage !== null) {
        clearSelectedImage.addEventListener("click", function () {
            clearFileInput();
            clearFilePreview();
        });
    }

    if (imageUrlInput !== null) {
        imageUrlInput.addEventListener("input", function () {
            if (getImageSourceType() !== "URL") {
                return;
            }

            const imageUrl =
                imageUrlInput.value.trim();

            showUrlPreview(imageUrl);
        });
    }

    if (linkUrlInput !== null) {
        linkUrlInput.addEventListener("input", function () {
            updateLinkPreview();
        });
    }

    if (displayOrderInput !== null) {
        displayOrderInput.addEventListener("input", function () {
            updateDisplayOrderPreview();
        });

        displayOrderInput.addEventListener("blur", function () {
            const displayOrder =
                Number(displayOrderInput.value);

            if (
                !Number.isInteger(displayOrder)
                || displayOrder < 1
            ) {
                displayOrderInput.value = "1";
                updateDisplayOrderPreview();
            }
        });
    }

    if (activeYnSelect !== null) {
        activeYnSelect.addEventListener("change", function () {
            updateActivePreview();
        });
    }

    if (startAtInput !== null) {
        startAtInput.addEventListener("change", function () {
            validateDisplayPeriod();
        });
    }

    if (endAtInput !== null) {
        endAtInput.addEventListener("change", function () {
            validateDisplayPeriod();
        });
    }

    if (bannerPreviewLink !== null) {
        bannerPreviewLink.addEventListener("click", function (event) {
            const linkUrl =
                linkUrlInput === null
                    ? ""
                    : linkUrlInput.value.trim();

            if (linkUrl.length === 0) {
                event.preventDefault();

                alert(
                    "연결 링크를 입력하면 미리보기에서 확인할 수 있습니다.",
                );

                return;
            }

            if (!isValidLinkUrl(linkUrl)) {
                event.preventDefault();

                alert(
                    "올바른 연결 링크를 입력해 주세요.",
                );
            }
        });
    }

    if (form !== null) {
        form.addEventListener("submit", function (event) {
            const validationMessage =
                validateForm();

            if (validationMessage !== null) {
                event.preventDefault();

                alert(validationMessage);

                return;
            }

            if (submitBannerButton !== null) {
                submitBannerButton.disabled = true;
                submitBannerButton.textContent = "등록 중...";
            }
        });
    }

    function getImageSourceType() {
        const checkedOption =
            imageSourceOptions.find(function (option) {
                return option.checked;
            });

        if (checkedOption === undefined) {
            return "FILE";
        }

        return checkedOption.value;
    }

    function updateImageSourceScreen() {
        const imageSourceType =
            getImageSourceType();

        if (imageSourceType === "FILE") {
            if (imageFileArea !== null) {
                imageFileArea.hidden = false;
            }

            if (imageUrlArea !== null) {
                imageUrlArea.hidden = true;
            }

            if (bannerImageFile !== null) {
                bannerImageFile.disabled = false;
            }

            if (imageUrlInput !== null) {
                imageUrlInput.disabled = true;
            }

            if (previewImageSourceType !== null) {
                previewImageSourceType.textContent =
                    "파일 업로드";
            }

            const file =
                bannerImageFile === null
                    ? undefined
                    : bannerImageFile.files[0];

            if (file === undefined) {
                hidePreviewImage();
            } else {
                showFilePreview(file);
            }

            return;
        }

        if (imageFileArea !== null) {
            imageFileArea.hidden = true;
        }

        if (imageUrlArea !== null) {
            imageUrlArea.hidden = false;
        }

        if (bannerImageFile !== null) {
            bannerImageFile.disabled = true;
        }

        if (imageUrlInput !== null) {
            imageUrlInput.disabled = false;
        }

        if (previewImageSourceType !== null) {
            previewImageSourceType.textContent =
                "이미지 주소";
        }

        const imageUrl =
            imageUrlInput === null
                ? ""
                : imageUrlInput.value.trim();

        showUrlPreview(imageUrl);
    }

    function showSelectedFile(file) {
        if (selectedFileInfo !== null) {
            selectedFileInfo.hidden = false;
        }

        if (selectedFileName !== null) {
            selectedFileName.textContent = file.name;
        }
    }

    function clearFileInput() {
        if (bannerImageFile !== null) {
            bannerImageFile.value = "";
        }

        if (selectedFileInfo !== null) {
            selectedFileInfo.hidden = true;
        }

        if (selectedFileName !== null) {
            selectedFileName.textContent =
                "선택된 파일 없음";
        }
    }

    function clearFilePreview() {
        revokePreviewObjectUrl();
        hidePreviewImage();
    }

    function showFilePreview(file) {
        revokePreviewObjectUrl();

        previewObjectUrl =
            URL.createObjectURL(file);

        showPreviewImage(previewObjectUrl);
    }

    function showUrlPreview(imageUrl) {
        revokePreviewObjectUrl();

        if (imageUrl.length === 0) {
            hidePreviewImage();
            return;
        }

        if (!isAllowedImageUrl(imageUrl)) {
            hidePreviewImage();
            return;
        }

        showPreviewImage(imageUrl);
    }

    function showPreviewImage(imageSource) {
        if (
            bannerPreviewImage === null
            || bannerPreviewPlaceholder === null
        ) {
            return;
        }

        bannerPreviewImage.onload = function () {
            bannerPreviewImage.hidden = false;
            bannerPreviewPlaceholder.hidden = true;
        };

        bannerPreviewImage.onerror = function () {
            hidePreviewImage();
        };

        bannerPreviewImage.src = imageSource;

        updatePreviewAltText();
    }

    function hidePreviewImage() {
        if (bannerPreviewImage !== null) {
            bannerPreviewImage.hidden = true;
            bannerPreviewImage.removeAttribute("src");
        }

        if (bannerPreviewPlaceholder !== null) {
            bannerPreviewPlaceholder.hidden = false;
        }
    }

    function revokePreviewObjectUrl() {
        if (previewObjectUrl === null) {
            return;
        }

        URL.revokeObjectURL(previewObjectUrl);
        previewObjectUrl = null;
    }

    function updateTitleLength() {
        if (
            titleInput === null
            || titleLength === null
        ) {
            return;
        }

        titleLength.textContent =
            titleInput.value.length;
    }

    function updateSubTitleLength() {
        if (
            subTitleInput === null
            || subTitleLength === null
        ) {
            return;
        }

        subTitleLength.textContent =
            subTitleInput.value.length;
    }

    function updateTitlePreview() {
        if (
            titleInput === null
            || bannerPreviewTitle === null
        ) {
            return;
        }

        const title =
            titleInput.value.trim();

        bannerPreviewTitle.textContent =
            title.length > 0
                ? title
                : "배너 제목을 입력해 주세요";
    }

    function updateSubTitlePreview() {
        if (
            subTitleInput === null
            || bannerPreviewSubTitle === null
        ) {
            return;
        }

        const subTitle =
            subTitleInput.value.trim();

        bannerPreviewSubTitle.textContent =
            subTitle.length > 0
                ? subTitle
                : "입력한 부제목이 이곳에 표시됩니다.";
    }

    function updatePreviewAltText() {
        if (bannerPreviewImage === null) {
            return;
        }

        const title =
            titleInput === null
                ? ""
                : titleInput.value.trim();

        bannerPreviewImage.alt =
            title.length > 0
                ? title
                : "메인 배너 이미지";
    }

    function updateDisplayOrderPreview() {
        if (
            displayOrderInput === null
            || previewDisplayOrder === null
        ) {
            return;
        }

        const displayOrder =
            Number(displayOrderInput.value);

        previewDisplayOrder.textContent =
            Number.isInteger(displayOrder)
            && displayOrder >= 1
                ? displayOrder
                : 1;
    }

    function updateActivePreview() {
        if (
            activeYnSelect === null
            || previewActiveBadge === null
        ) {
            return;
        }

        const isActive =
            activeYnSelect.value === "Y";

        previewActiveBadge.textContent =
            isActive ? "노출" : "숨김";

        previewActiveBadge.classList.toggle(
            "active",
            isActive,
        );

        previewActiveBadge.classList.toggle(
            "inactive",
            !isActive,
        );
    }

    function updateLinkPreview() {
        if (
            linkUrlInput === null
            || previewLinkUrl === null
            || bannerPreviewLink === null
        ) {
            return;
        }

        const linkUrl =
            linkUrlInput.value.trim();

        previewLinkUrl.textContent =
            linkUrl.length > 0
                ? linkUrl
                : "링크 없음";

        if (
            linkUrl.length > 0
            && isValidLinkUrl(linkUrl)
        ) {
            bannerPreviewLink.href = linkUrl;
            return;
        }

        bannerPreviewLink.href = "#";
    }

    function validateDisplayPeriod() {
        if (
            startAtInput === null
            || endAtInput === null
        ) {
            return true;
        }

        const startAt =
            startAtInput.value;

        const endAt =
            endAtInput.value;

        endAtInput.setCustomValidity("");

        if (
            startAt.length === 0
            || endAt.length === 0
        ) {
            return true;
        }

        if (
            new Date(startAt)
            > new Date(endAt)
        ) {
            endAtInput.setCustomValidity(
                "노출 종료일은 시작일보다 빠를 수 없습니다.",
            );

            return false;
        }

        return true;
    }

    function validateForm() {
        if (
            titleInput === null
            || titleInput.value.trim().length === 0
        ) {
            titleInput?.focus();

            return "배너 제목을 입력해 주세요.";
        }

        const imageSourceType =
            getImageSourceType();

        if (imageSourceType === "FILE") {
            const file =
                bannerImageFile === null
                    ? undefined
                    : bannerImageFile.files[0];

            if (file === undefined) {
                return "등록할 배너 이미지 파일을 선택해 주세요.";
            }

            if (!isAllowedImageFile(file)) {
                return "등록할 수 없는 이미지 파일 형식입니다.";
            }

            if (file.size > MAX_FILE_SIZE) {
                return "이미지 파일은 10MB 이하로 등록해 주세요.";
            }
        }

        if (imageSourceType === "URL") {
            const imageUrl =
                imageUrlInput === null
                    ? ""
                    : imageUrlInput.value.trim();

            if (imageUrl.length === 0) {
                imageUrlInput?.focus();

                return "배너 이미지 주소를 입력해 주세요.";
            }

            if (!isAllowedImageUrl(imageUrl)) {
                imageUrlInput?.focus();

                return "올바른 이미지 주소를 입력해 주세요.";
            }
        }

        const linkUrl =
            linkUrlInput === null
                ? ""
                : linkUrlInput.value.trim();

        if (
            linkUrl.length > 0
            && !isValidLinkUrl(linkUrl)
        ) {
            linkUrlInput?.focus();

            return "올바른 연결 링크를 입력해 주세요.";
        }

        const displayOrder =
            displayOrderInput === null
                ? 0
                : Number(displayOrderInput.value);

        if (
            !Number.isInteger(displayOrder)
            || displayOrder < 1
        ) {
            displayOrderInput?.focus();

            return "노출 순서는 1 이상의 정수로 입력해 주세요.";
        }

        if (!validateDisplayPeriod()) {
            endAtInput?.focus();

            return "노출 종료일은 시작일보다 빠를 수 없습니다.";
        }

        return null;
    }

    function isAllowedImageFile(file) {
        const allowedMimeTypes = [
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/avif",
        ];

        const allowedExtensions =
            /\.(jpg|jpeg|png|webp|gif|avif)$/i;

        return (
            allowedMimeTypes.includes(file.type)
            && allowedExtensions.test(file.name)
        );
    }

    function isAllowedImageUrl(imageUrl) {
        if (!isValidHttpUrl(imageUrl)) {
            return false;
        }

        return /\.(jpg|jpeg|png|webp|gif|avif)(?:[?#].*)?$/i
            .test(imageUrl);
    }

    function isValidLinkUrl(linkUrl) {
        if (linkUrl.startsWith("/")) {
            return !linkUrl.startsWith("//");
        }

        return isValidHttpUrl(linkUrl);
    }

    function isValidHttpUrl(value) {
        try {
            const url = new URL(value);

            if (
                url.protocol !== "http:"
                && url.protocol !== "https:"
            ) {
                return false;
            }

            const hostname =
                url.hostname.toLowerCase();

            if (hostname === "localhost") {
                return true;
            }

            const allowedDomainSuffixes = [
                ".com",
                ".net",
                ".org",
                ".io",
                ".dev",
                ".app",
                ".ai",
                ".me",
                ".info",
                ".biz",
                ".shop",
                ".store",
                ".site",
                ".online",
                ".xyz",
                ".kr",
                ".co.kr",
                ".or.kr",
                ".go.kr",
                ".ac.kr",
                ".ne.kr",
                ".jp",
                ".co.jp",
            ];

            return allowedDomainSuffixes.some(
                function (suffix) {
                    return (
                        hostname.endsWith(suffix)
                        && hostname.length > suffix.length
                    );
                },
            );

        } catch (error) {
            return false;
        }
    }

    window.addEventListener("beforeunload", function () {
        revokePreviewObjectUrl();
    });
});