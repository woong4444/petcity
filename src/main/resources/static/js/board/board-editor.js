document.addEventListener("DOMContentLoaded", function () {
    const contentTextarea = document.getElementById("content");
    const boardWriteForm = document.getElementById("boardWriteForm");

    if (!contentTextarea || !window.jQuery) {
        return;
    }

    $("#content").summernote({
        height: 360,
        lang: "ko-KR",
        placeholder: "내용을 입력하세요. 이미지 버튼을 누르면 글 중간에 이미지를 넣을 수 있습니다.",
        toolbar: [
            ["style", ["bold", "italic", "underline", "clear"]],
            ["font", ["fontsize", "color"]],
            ["para", ["ul", "ol", "paragraph"]],
            ["insert", ["picture"]],
            ["view", ["fullscreen", "codeview"]]
        ],
        callbacks: {
            onImageUpload: function (files) {
                for (let i = 0; i < files.length; i++) {
                    uploadEditorImage(files[i]);
                }
            }
        }
    });

    if (boardWriteForm) {
        boardWriteForm.addEventListener("submit", function (event) {
            const html = $("#content").summernote("code");
            const text = $("<div>").html(html).text().trim();
            const hasImage = html.includes("<img");

            if (!text && !hasImage) {
                event.preventDefault();
                alert("내용을 입력해 주세요.");
                return false;
            }

            $("#content").val(html);
        });
    }
});

function uploadEditorImage(file) {
    if (!file.type.startsWith("image/")) {
        alert("이미지 파일만 업로드할 수 있습니다.");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    const headers = {};

    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch("/board/editor/image", {
        method: "POST",
        headers: headers,
        body: formData
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error("이미지 업로드 실패");
            }

            return response.json();
        })
        .then(function (data) {
            $("#content").summernote("insertImage", data.imageUrl);
        })
        .catch(function (error) {
            console.error(error);
            alert("이미지 업로드 중 오류가 발생했습니다.");
        });
}