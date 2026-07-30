/*
 * 로그인 화면 동작
 * 비밀번호 표시 전환과 중복 제출 방지를 담당합니다.
 */
document.addEventListener('DOMContentLoaded', function () {
    const loginForm = document.querySelector('#loginForm');
    const loginBtn = document.querySelector('#loginBtn');
    const passwordInput = document.querySelector('#password');
    const togglePasswordBtn = document.querySelector('#togglePasswordBtn');

    // 비밀번호 보기·숨기기
    if (passwordInput && togglePasswordBtn) {
        togglePasswordBtn.addEventListener('click', function () {
            const hidden = passwordInput.type === 'password';
            passwordInput.type = hidden ? 'text' : 'password';
            togglePasswordBtn.textContent = hidden ? '숨기기' : '보기';
        });
    }

    // 로그인 요청이 처리되는 동안 중복 제출 방지
    if (loginForm && loginBtn) {
        loginForm.addEventListener('submit', function () {
            loginBtn.disabled = true;
            loginBtn.textContent = '로그인 중...';
        });
    }
});
