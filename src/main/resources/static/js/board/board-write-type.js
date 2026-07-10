document.addEventListener("DOMContentLoaded", function () {
    const boardTypeSelect = document.getElementById("boardTypeSelect");
    const boardWriteGuide = document.getElementById("boardWriteGuide");

    const animalArea = document.getElementById("animalArea");
    const parentAnimalSelect = document.getElementById("parentAnimalSelect");
    const childAnimalSelect = document.getElementById("childAnimalSelect");

    const infoOnlyArea = document.getElementById("infoOnlyArea");
    const infoImageInput = document.getElementById("infoImage");
    const linkUrlInput = document.getElementById("linkUrl");
    const imagePreview = document.getElementById("imagePreview");

    if (!boardTypeSelect) {
        return;
    }

    function changeWriteFormByBoardType() {
        const boardType = boardTypeSelect.value;

        const isInfo = boardType === "INFO";
        const isNotice = boardType === "NOTICE";

        if (boardWriteGuide) {
            if (boardType === "QNA") {
                boardWriteGuide.textContent = "수의사상담에 게시글을 작성해 주세요.";
            } else if (boardType === "FREE") {
                boardWriteGuide.textContent = "자유토크에 게시글을 작성해 주세요.";
            } else if (boardType === "INFO") {
                boardWriteGuide.textContent = "멍냥백서에 게시글을 작성해 주세요.";
            } else if (boardType === "NOTICE") {
                boardWriteGuide.textContent = "공지사항에 게시글을 작성해 주세요.";
            }
        }

        if (animalArea) {
            animalArea.style.display = isNotice ? "none" : "block";
        }

        if (parentAnimalSelect) {
            parentAnimalSelect.required = !isNotice;

            if (isNotice) {
                parentAnimalSelect.value = "";
            }
        }

        if (childAnimalSelect) {
            childAnimalSelect.required = !isNotice;

            if (isNotice) {
                childAnimalSelect.value = "";
                childAnimalSelect.innerHTML = '<option value="">먼저 반려동물을 선택하세요</option>';
            }
        }

        if (infoOnlyArea) {
            infoOnlyArea.style.display = isInfo ? "block" : "none";
        }

        if (infoImageInput) {
            infoImageInput.required = isInfo;

            if (!isInfo) {
                infoImageInput.value = "";
            }
        }

        if (linkUrlInput) {
            linkUrlInput.required = isInfo;

            if (!isInfo) {
                linkUrlInput.value = "";
            }
        }

        if (imagePreview && !isInfo) {
            imagePreview.innerHTML = "";
        }
    }

    boardTypeSelect.addEventListener("change", changeWriteFormByBoardType);

    changeWriteFormByBoardType();
});