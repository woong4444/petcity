/*
 * 회원탈퇴 공통 확인 동작
 * 일반회원·SNS 회원 탈퇴 폼에서 동일하게 사용합니다.
 */
document.addEventListener('DOMContentLoaded', function () {
    document
        .querySelectorAll('.withdraw-confirm-form')
        .forEach(function (form) {
            form.addEventListener('submit', function (event) {
                const message =
                    form.dataset.confirmMessage
                    || '회원탈퇴를 요청하시겠습니까?';

                if (!window.confirm(message)) {
                    event.preventDefault();
                }
            });
        });
});
