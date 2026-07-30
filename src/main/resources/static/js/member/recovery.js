/*
 * 탈퇴 계정 복구 화면 동작
 * 일반회원과 SNS 회원에 맞는 안내와 입력 항목을 표시합니다.
 */
document.addEventListener('DOMContentLoaded', function () {
    const recoveryState = window.petcityRecovery || {};
    const initialSns = Boolean(recoveryState.initialSns);
    const verifiedEmailRecovery = Boolean(
        recoveryState.verifiedEmailRecovery
    );

    const idInput = document.getElementById('loginId');
    const snsNote = document.getElementById('snsNote');
    const localNote = document.getElementById('localNote');
    const snsHelp = document.getElementById('snsHelp');
    const label = document.getElementById('loginIdLabel');
    const accountType = document.getElementById('accountType');
    const recoveryEmailLink =
        document.getElementById('recoveryEmailLink');

    // 서버가 전달한 회원 유형에 맞춰 안내와 복구 경로를 동기화합니다.
    function syncAccountType() {
        if (!idInput || !snsHelp || !label) {
            return;
        }

        if (verifiedEmailRecovery) {
            label.hidden = true;
            idInput.hidden = true;
            snsHelp.hidden = true;
            return;
        }

        if (accountType) {
            accountType.value = initialSns ? 'sns' : 'local';
        }

        if (recoveryEmailLink) {
            recoveryEmailLink.href = initialSns
                ? '/member/recovery-code-email?type=sns'
                : '/member/recovery-code-email?type=local';
        }

        if (snsNote) {
            snsNote.hidden = !initialSns;
        }

        if (localNote) {
            localNote.hidden = initialSns;
        }

        snsHelp.hidden = !initialSns;
        label.textContent = initialSns
            ? 'SNS 등록 이메일'
            : '등록 이메일';
        idInput.placeholder = initialSns
            ? 'SNS 가입에 사용한 이메일 주소'
            : '가입할 때 등록한 이메일 주소';
    }

    if (idInput) {
        idInput.addEventListener('input', syncAccountType);
        idInput.addEventListener('change', syncAccountType);
    }

    syncAccountType();
});
