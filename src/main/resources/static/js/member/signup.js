// =====================================================
// SNS 이메일 수집 동의 검사
// =====================================================

const agreementEmail = document.querySelector("#agreementEmail");

const googleLoginBtn = document.querySelector("#googleLoginBtn");
const kakaoLoginBtn = document.querySelector("#kakaoLoginBtn");
const naverLoginBtn = document.querySelector("#naverLoginBtn");

function checkAgreement(event) {

    // 체크박스가 없으면 종료
    if (!agreementEmail) {
        return;
    }

    // 동의 안 했으면 이동 막기
    if (!agreementEmail.checked) {

        event.preventDefault();
        event.stopPropagation();

        alert("이메일 수집 및 개인정보 이용에 동의해야 SNS 회원가입이 가능합니다.");

        return false;
    }

    return true;
}

// Google
googleLoginBtn?.addEventListener("click", checkAgreement);

// Kakao
kakaoLoginBtn?.addEventListener("click", checkAgreement);

// Naver
naverLoginBtn?.addEventListener("click", checkAgreement);