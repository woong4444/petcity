document.addEventListener("DOMContentLoaded", function () {

    const editButtons =
        document.querySelectorAll(".comment-edit-button");

    const cancelButtons =
        document.querySelectorAll(".comment-edit-cancel");

    const editForms =
        document.querySelectorAll(".comment-edit-form");

    /*
        수정 버튼 클릭
    */
    editButtons.forEach(function (button) {

        button.addEventListener("click", function () {

            const commentId =
                this.dataset.commentId;

            const content =
                document.getElementById(
                    "commentContent-" + commentId
                );

            const actionButtons =
                document.getElementById(
                    "commentActions-" + commentId
                );

            const editForm =
                document.getElementById(
                    "commentEditForm-" + commentId
                );

            if (content) {
                content.style.display = "none";
            }

            if (actionButtons) {
                actionButtons.style.display = "none";
            }

            if (editForm) {
                editForm.style.display = "block";

                const textarea =
                    editForm.querySelector("textarea");

                if (textarea) {
                    textarea.focus();

                    // 커서를 글 마지막으로 이동
                    textarea.setSelectionRange(
                        textarea.value.length,
                        textarea.value.length
                    );
                }
            }
        });
    });

    /*
        취소 버튼 클릭
    */
    cancelButtons.forEach(function (button) {

        button.addEventListener("click", function () {

            const commentId =
                this.dataset.commentId;

            closeCommentEdit(commentId);
        });
    });

    /*
        댓글 수정 폼 검사
    */
    editForms.forEach(function (form) {

        form.addEventListener("submit", function (event) {

            const textarea =
                this.querySelector("textarea");

            if (!textarea) {
                return;
            }

            const content =
                textarea.value.trim();

            if (!content) {
                event.preventDefault();
                alert("댓글 내용을 입력해 주세요.");
                textarea.focus();
                return;
            }

            if (content.length > 1000) {
                event.preventDefault();
                alert("댓글은 1000자 이하로 작성해 주세요.");
                textarea.focus();
            }
        });
    });
});


function closeCommentEdit(commentId) {

    const content =
        document.getElementById(
            "commentContent-" + commentId
        );

    const actionButtons =
        document.getElementById(
            "commentActions-" + commentId
        );

    const editForm =
        document.getElementById(
            "commentEditForm-" + commentId
        );

    if (content) {
        content.style.display = "block";
    }

    if (actionButtons) {
        actionButtons.style.display = "flex";
    }

    if (editForm) {
        editForm.style.display = "none";
    }
}