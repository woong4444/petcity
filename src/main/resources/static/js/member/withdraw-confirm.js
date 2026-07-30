/*
 * 07-30 상각: 일반회원·SNS 회원 공통 탈퇴 사유 검증 및 요청 확인
 */
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".withdraw-confirm-form").forEach(function (form) {
        const reasonInput = form.querySelector('textarea[name="deleteReason"]');
        const counter = form.querySelector(".withdraw-reason-count");

        if (!reasonInput) {
            return;
        }

        // 한글과 영문을 동일하게 사용자에게 보이는 문자 단위로 계산합니다.
        function countCharacters(value) {
            return Array.from(value.trim()).length;
        }

        function updateCounter() {
            const length = countCharacters(reasonInput.value);

            if (counter) {
                counter.textContent = `${length} / 500자`;
            }

            reasonInput.setCustomValidity("");
        }

        reasonInput.addEventListener("input", updateCounter);
        updateCounter();

        form.addEventListener("submit", function (event) {
            const length = countCharacters(reasonInput.value);

            if (length < 5 || length > 500) {
                event.preventDefault();
                reasonInput.setCustomValidity(
                    "탈퇴 사유는 한글·영문 관계없이 공백 제외 5자 이상 500자 이하로 입력해 주세요."
                );
                reasonInput.reportValidity();
                reasonInput.focus();
                return;
            }

            reasonInput.setCustomValidity("");

            const confirmMessage =
                form.dataset.confirmMessage || "회원탈퇴를 요청하시겠습니까?";

            if (!window.confirm(confirmMessage)) {
                event.preventDefault();
            }
        });
    });
});
