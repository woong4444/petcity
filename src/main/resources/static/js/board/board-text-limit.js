document.addEventListener("DOMContentLoaded", function () {

    const TITLE_MAX_LENGTH = 100;
    const CONTENT_MAX_LENGTH = 3000;

    /*
        게시글 제목 제한
    */
    const titleInput =
        document.querySelector('input[name="title"]');

    if (titleInput) {

        titleInput.maxLength =
            TITLE_MAX_LENGTH;

        createTitleCount(
            titleInput,
            TITLE_MAX_LENGTH
        );
    }


    /*
        Summernote가 생성될 때까지 확인
    */
    const contentTextarea =
        document.querySelector(
            "textarea.summernote-editor"
        );

    if (!contentTextarea) {
        return;
    }

    let checkCount = 0;

    const editorCheckTimer =
        window.setInterval(function () {

            const editableArea =
                document.querySelector(
                    ".note-editable"
                );

            checkCount++;

            if (editableArea) {

                window.clearInterval(
                    editorCheckTimer
                );

                applyEditorLimit(
                    contentTextarea,
                    editableArea,
                    CONTENT_MAX_LENGTH
                );

                return;
            }

            /*
                약 5초 동안 Summernote가 생성되지 않으면 종료
            */
            if (checkCount >= 50) {

                window.clearInterval(
                    editorCheckTimer
                );
            }

        }, 100);
});


/*
    제목 글자 수 표시
*/
/*
    제목 입력칸과 글자 수를 하나의 박스로 묶기
*/
function createTitleCount(
    titleInput,
    maxLength
) {

    /*
        제목 input을 감쌀 박스 생성
    */
    let titleInputBox =
        titleInput.closest(".title-input-box");

    if (!titleInputBox) {

        titleInputBox =
            document.createElement("div");

        titleInputBox.className =
            "title-input-box";

        /*
            기존 input 자리에 박스를 넣은 뒤
            input을 박스 안으로 이동
        */
        titleInput.parentNode.insertBefore(
            titleInputBox,
            titleInput
        );

        titleInputBox.appendChild(
            titleInput
        );
    }


    /*
        글자 수 표시 생성
    */
    let countElement =
        titleInputBox.querySelector(
            ".title-character-count"
        );

    if (!countElement) {

        countElement =
            document.createElement("p");

        countElement.className =
            "title-character-count";

        titleInputBox.appendChild(
            countElement
        );
    }


    function updateTitleCount() {

        const characters =
            Array.from(titleInput.value);

        /*
            100자를 넘으면 초과 부분 삭제
        */
        if (characters.length > maxLength) {

            titleInput.value =
                characters
                    .slice(0, maxLength)
                    .join("");
        }

        const currentLength =
            Array.from(titleInput.value).length;

        countElement.textContent =
            currentLength
            + " / "
            + maxLength
            + "자";

        countElement.classList.toggle(
            "limit",
            currentLength >= maxLength
        );
    }


    titleInput.addEventListener(
        "input",
        updateTitleCount
    );

    titleInput.addEventListener(
        "paste",
        function () {

            window.setTimeout(
                updateTitleCount,
                0
            );
        }
    );

    updateTitleCount();
}

/*
    Summernote 본문 제한
*/
function applyEditorLimit(
    originalTextarea,
    editableArea,
    maxLength
) {

    let lastValidHtml =
        editableArea.innerHTML;

    const countElement =
        document.createElement("p");

    countElement.className =
        "board-content-count";

    const noteEditor =
        editableArea.closest(".note-editor");

    if (noteEditor) {

        noteEditor.insertAdjacentElement(
            "afterend",
            countElement
        );
    }


    /*
        HTML 태그를 제외한 실제 글자 수
    */
    function getContentLength() {

        const text =
            editableArea.innerText
                .replace(/\u200B/g, "")
                .replace(/\n$/, "");

        return Array.from(text).length;
    }


    function updateContentCount() {

        const currentLength =
            getContentLength();

        countElement.textContent =
            currentLength
            + " / "
            + maxLength
            + "자";

        countElement.classList.toggle(
            "limit",
            currentLength >= maxLength
        );
    }


    editableArea.addEventListener(
        "input",
        function () {

            const currentLength =
                getContentLength();

            if (currentLength <= maxLength) {

                lastValidHtml =
                    editableArea.innerHTML;

                originalTextarea.value =
                    editableArea.innerHTML;

                updateContentCount();

                return;
            }


            /*
                제한을 넘기면 마지막 정상 내용으로 복구
            */
            editableArea.innerHTML =
                lastValidHtml;

            originalTextarea.value =
                lastValidHtml;

            moveCaretToEnd(
                editableArea
            );

            updateContentCount();
        }
    );


    /*
        붙여넣기로 한꺼번에 넘기는 것도 방지
    */
    editableArea.addEventListener(
        "paste",
        function () {

            window.setTimeout(
                function () {

                    const currentLength =
                        getContentLength();

                    if (currentLength > maxLength) {

                        editableArea.innerHTML =
                            lastValidHtml;

                        originalTextarea.value =
                            lastValidHtml;

                        moveCaretToEnd(
                            editableArea
                        );

                        updateContentCount();
                    }

                },
                0
            );
        }
    );


    /*
        등록·수정 직전 최종 검사
    */
    const form =
        originalTextarea.closest("form");

    if (form) {

        form.addEventListener(
            "submit",
            function (event) {

                const currentLength =
                    getContentLength();

                if (currentLength > maxLength) {

                    event.preventDefault();

                    alert(
                        "본문은 "
                        + maxLength
                        + "자 이하로 작성해 주세요."
                    );

                    editableArea.focus();

                    return;
                }

                originalTextarea.value =
                    editableArea.innerHTML;
            }
        );
    }

    updateContentCount();
}


/*
    커서를 본문 마지막으로 이동
*/
function moveCaretToEnd(element) {

    element.focus();

    const range =
        document.createRange();

    const selection =
        window.getSelection();

    range.selectNodeContents(element);
    range.collapse(false);

    selection.removeAllRanges();
    selection.addRange(range);
}