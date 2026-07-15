// =====================================================
// 07-15 오후 추가_상각
// SNS 이메일 수집 동의 검사
// =====================================================

const agreementEmail = document.querySelector("#agreementEmail");

const googleLoginBtn = document.querySelector("#googleLoginBtn");
const kakaoLoginBtn = document.querySelector("#kakaoLoginBtn");
const naverLoginBtn = document.querySelector("#naverLoginBtn");

function checkAgreement(e) {

    if (!agreementEmail.checked) {

        alert("이메일 수집 및 개인정보 이용에 동의해야 SNS 회원가입이 가능합니다.");

        e.preventDefault();

    }

}

// Google
googleLoginBtn.addEventListener("click", checkAgreement);

// Kakao
kakaoLoginBtn.addEventListener("click", checkAgreement);

// Naver
naverLoginBtn.addEventListener("click", checkAgreement);