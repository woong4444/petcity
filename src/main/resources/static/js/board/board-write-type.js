document.addEventListener("DOMContentLoaded", function () {

    const boardTypeSelect =
        document.getElementById("boardTypeSelect");

    const boardWriteGuide =
        document.getElementById("boardWriteGuide");

    const titleLabel =
        document.getElementById("titleLabel");

    const titleInput =
        document.getElementById("title");

    const contentLabel =
        document.getElementById("contentLabel");

    const animalArea =
        document.getElementById("animalArea");

    const parentAnimalSelect =
        document.getElementById("parentAnimalSelect");

    const childAnimalSelect =
        document.getElementById("childAnimalSelect");

    const infoOnlyArea =
        document.getElementById("infoOnlyArea");

    const infoImageInput =
        document.getElementById("infoImage");

    const linkUrlInput =
        document.getElementById("linkUrl");

    const imagePreview =
        document.getElementById("imagePreview");


    if (!boardTypeSelect) {
        return;
    }


    function changeFormByBoardType() {

        const boardType =
            boardTypeSelect.value;

        const isInfo =
            boardType === "INFO";

        const isNotice =
            boardType === "NOTICE";

        const isFaq =
            boardType === "FAQ";

        /*
            공지사항과 FAQ는 동물 종류를 사용하지 않음
        */
        const noAnimalBoard =
            isNotice || isFaq;


        /*
            게시판별 안내 문구
        */
        if (boardWriteGuide) {

            if (boardType === "QNA") {

                boardWriteGuide.textContent =
                    "반려동물의 증상과 궁금한 내용을 작성해 주세요.";

            } else if (boardType === "FREE") {

                boardWriteGuide.textContent =
                    "반려동물과 관련된 자유로운 이야기를 작성해 주세요.";

            } else if (boardType === "INFO") {

                boardWriteGuide.textContent =
                    "반려동물 보호자에게 도움이 되는 정보를 작성해 주세요.";

            } else if (boardType === "NOTICE") {

                boardWriteGuide.textContent =
                    "PetCity 이용자에게 전달할 공지사항을 작성해 주세요.";

            } else if (boardType === "FAQ") {

                boardWriteGuide.textContent =
                    "자주 묻는 질문과 답변을 작성해 주세요.";
            }
        }


        /*
            제목 라벨과 입력 안내
        */
        if (titleLabel) {

            titleLabel.textContent =
                isFaq
                    ? "질문"
                    : "제목";
        }

        if (titleInput) {

            titleInput.placeholder =
                isFaq
                    ? "자주 묻는 질문을 입력하세요."
                    : "제목을 입력하세요.";
        }


        /*
            내용 라벨
        */
        if (contentLabel) {

            contentLabel.textContent =
                isFaq
                    ? "답변"
                    : "내용";
        }


        /*
            동물 선택 영역
        */
        if (animalArea) {

            animalArea.style.display =
                noAnimalBoard
                    ? "none"
                    : "block";
        }


        /*
            부모 동물 선택 필수 여부
        */
        if (parentAnimalSelect) {

            parentAnimalSelect.required =
                !noAnimalBoard;

            if (noAnimalBoard) {
                parentAnimalSelect.value = "";
            }
        }


        /*
            하위 동물 선택 필수 여부
        */
        if (childAnimalSelect) {

            childAnimalSelect.required =
                !noAnimalBoard;

            if (noAnimalBoard) {

                childAnimalSelect.value = "";

                childAnimalSelect.innerHTML =
                    '<option value="">먼저 반려동물을 선택하세요</option>';
            }
        }


        /*
            멍냥백서 전용 영역
        */
        if (infoOnlyArea) {

            infoOnlyArea.style.display =
                isInfo
                    ? "block"
                    : "none";
        }


        /*
            멍냥백서 대표 이미지
        */
        if (infoImageInput) {

            infoImageInput.required =
                isInfo;

            if (!isInfo) {
                infoImageInput.value = "";
            }
        }


        /*
            멍냥백서 링크
        */
        if (linkUrlInput) {

            linkUrlInput.required =
                isInfo;

            if (!isInfo) {
                linkUrlInput.value = "";
            }
        }


        /*
            멍냥백서가 아니면 이미지 미리보기 비우기
        */
        if (imagePreview && !isInfo) {
            imagePreview.innerHTML = "";
        }
    }


    boardTypeSelect.addEventListener(
        "change",
        changeFormByBoardType
    );

    changeFormByBoardType();
});