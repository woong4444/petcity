// 07-16 상각: SNS 가입 전 이메일 수집 동의 서버 저장
const agreementEmail = document.querySelector("#agreementEmail");

const googleLoginBtn = document.querySelector("#googleLoginBtn");
const kakaoLoginBtn = document.querySelector("#kakaoLoginBtn");
const naverLoginBtn = document.querySelector("#naverLoginBtn");

async function checkAgreement(event) {

    event.preventDefault();

    if (!agreementEmail || !agreementEmail.checked) {
        alert("이메일 수집 및 개인정보 이용에 동의해야 SNS 회원가입이 가능합니다.");
        return;
    }

    const loginButton = event.currentTarget;

    try {
        const response = await fetch("/member/oauth-email-agreement", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                provider: loginButton.dataset.provider
            })
        });

        if (!response.ok || await response.text() !== "true") {
            throw new Error("agreement save failed");
        }

        window.location.href = loginButton.href;

    } catch (error) {
        alert("동의 정보를 저장하지 못했습니다. 잠시 후 다시 시도해주세요.");
    }
}

googleLoginBtn?.addEventListener("click", checkAgreement);
kakaoLoginBtn?.addEventListener("click", checkAgreement);
naverLoginBtn?.addEventListener("click", checkAgreement);
