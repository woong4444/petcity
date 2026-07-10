document.addEventListener("DOMContentLoaded", function () {
    const imageInput = document.querySelector("input[name='imageFiles']");
    const previewBox = document.getElementById("imagePreview");

    if (!imageInput || !previewBox) {
        return;
    }

    imageInput.addEventListener("change", function () {
        previewBox.innerHTML = "";

        const files = Array.from(this.files);

        if (files.length === 0) {
            return;
        }

        files.forEach(function (file) {
            if (!file.type.startsWith("image/")) {
                return;
            }

            const reader = new FileReader();

            reader.onload = function (event) {
                const previewItem = document.createElement("div");
                previewItem.className = "image-preview-item";

                const img = document.createElement("img");
                img.src = event.target.result;
                img.alt = "이미지 미리보기";

                previewItem.appendChild(img);
                previewBox.appendChild(previewItem);
            };

            reader.readAsDataURL(file);
        });
    });
});
