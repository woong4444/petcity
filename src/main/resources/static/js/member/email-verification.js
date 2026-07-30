/*
 * 이메일 인증 화면 동작
 * 인증번호 재발송 요청과 결과 안내를 담당합니다.
 */
document.addEventListener('DOMContentLoaded', function () {
    const resendButton = document.querySelector('#resendButton');

    if (!resendButton) {
        return;
    }

    resendButton.addEventListener('click', async function () {
        try {
            resendButton.disabled = true;

            const response = await fetch('/member/email-verification/send', {
                method: 'POST'
            });

            alert(await response.text());
        } catch (error) {
            console.error(error);
            alert('인증번호 재발송 중 오류가 발생했습니다.');
        } finally {
            resendButton.disabled = false;
        }
    });
});
