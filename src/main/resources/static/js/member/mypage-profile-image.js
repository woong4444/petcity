document.addEventListener("DOMContentLoaded", () => {
    // =====================================================
    // 07-27 상각: 마이페이지 프로필 사진 선택 및 즉시 업로드
    // 회원 기본정보는 수정하지 않고 사진 파일만 전송합니다.
    // =====================================================
    const form = document.querySelector("#profileImageForm");
    const fileInput = document.querySelector("#profileImageFile");
    const selectButton = document.querySelector("#profileImageSelectButton");

    if (!form || !fileInput || !selectButton) return;

    selectButton.addEventListener("click", () => {
        fileInput.click();
    });

    fileInput.addEventListener("change", () => {
        const file = fileInput.files[0];
        if (!file) return;

        const allowedTypes = ["image/jpeg", "image/png", "image/webp"];

        if (!allowedTypes.includes(file.type)) {
            alert("JPG, PNG, WEBP 이미지 파일만 선택해주세요.");
            fileInput.value = "";
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            alert("프로필 사진은 5MB 이하만 업로드할 수 있습니다.");
            fileInput.value = "";
            return;
        }

        selectButton.disabled = true;
        selectButton.textContent = "업로드 중...";
        form.submit();
    });
});
