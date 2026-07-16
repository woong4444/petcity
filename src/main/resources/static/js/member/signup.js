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

/* =====================================================
   일반 회원가입 아이디 중복확인
===================================================== */

const signupLoginIdInput =
    document.querySelector("#loginId")
    || document.querySelector(
        'input[name="loginId"]'
    );

const checkLoginIdBtn =
    document.querySelector("#checkLoginIdBtn");

let loginIdCheckCompleted = false;


/*
    메시지 영역을 가져오거나 자동 생성
*/
function getLoginIdMessageElement() {

    let messageElement =
        document.querySelector(
            "#loginIdMessage"
        );

    if (!messageElement
        && signupLoginIdInput) {

        messageElement =
            document.createElement("p");

        messageElement.id =
            "loginIdMessage";

        messageElement.className =
            "check-message";

        signupLoginIdInput
            .closest(".form-control")
            ?.appendChild(messageElement);
    }

    return messageElement;
}


/*
    아이디 메시지 출력
*/
function showLoginIdMessage(
    message,
    success
) {

    const messageElement =
        getLoginIdMessageElement();

    if (!messageElement) {
        return;
    }

    messageElement.textContent =
        message;

    messageElement.classList.remove(
        "text-success",
        "text-error"
    );

    messageElement.classList.add(
        success
            ? "text-success"
            : "text-error"
    );
}


/*
    아이디를 수정하면
    기존 중복확인 결과 무효화
*/
signupLoginIdInput?.addEventListener(
    "input",
    () => {

        loginIdCheckCompleted = false;

        showLoginIdMessage(
            "아이디 중복확인이 필요합니다.",
            false
        );
    }
);


/*
    아이디 중복확인
*/
checkLoginIdBtn?.addEventListener(
    "click",
    async () => {

        const loginId =
            signupLoginIdInput
                ?.value
                .trim()
                .toLowerCase()
            || "";

        const loginIdPattern =
            /^[a-z][a-z0-9_]{4,19}$/;

        /*
            DB 중복확인 전에
            아이디 형식을 먼저 검사
        */
        if (!loginIdPattern.test(loginId)) {

            loginIdCheckCompleted = false;

            showLoginIdMessage(
                "아이디는 영문 소문자로 시작하는 " +
                "5~20자의 영문 소문자, 숫자, " +
                "밑줄만 사용할 수 있습니다.",
                false
            );

            signupLoginIdInput?.focus();
            return;
        }

        try {

            const response =
                await fetch(
                    "/member/check-loginId" +
                    "?loginId=" +
                    encodeURIComponent(loginId)
                );

            if (!response.ok) {
                throw new Error(
                    "아이디 확인 실패"
                );
            }

            /*
                true  = 이미 존재함
                false = 사용 가능함
            */
            const exists =
                await response.json();

            if (exists) {

                loginIdCheckCompleted = false;

                showLoginIdMessage(
                    "이미 사용 중인 아이디입니다.",
                    false
                );

                return;
            }

            loginIdCheckCompleted = true;

            showLoginIdMessage(
                "사용 가능한 아이디입니다.",
                true
            );

        } catch (error) {

            loginIdCheckCompleted = false;

            showLoginIdMessage(
                "중복확인 중 오류가 발생했습니다.",
                false
            );
        }
    }
);

/* =====================================================
   비밀번호 실시간 검증
===================================================== */

const signupPasswordInput =
    document.querySelector("#password")
    || document.querySelector(
        'input[name="password"]'
    );


function getPasswordMessageElement() {

    let messageElement =
        document.querySelector(
            "#passwordMessage"
        );

    if (!messageElement
        && signupPasswordInput) {

        messageElement =
            document.createElement("p");

        messageElement.id =
            "passwordMessage";

        messageElement.className =
            "check-message";

        signupPasswordInput
            .closest(".form-control")
            ?.appendChild(messageElement);
    }

    return messageElement;
}


function showPasswordMessage(
    message,
    success
) {

    const messageElement =
        getPasswordMessageElement();

    if (!messageElement) {
        return;
    }

    messageElement.textContent =
        message;

    messageElement.classList.remove(
        "text-success",
        "text-error"
    );

    messageElement.classList.add(
        success
            ? "text-success"
            : "text-error"
    );
}


signupPasswordInput?.addEventListener(
    "input",
    () => {

        const password =
            signupPasswordInput.value;

        if (password.length < 9) {

            showPasswordMessage(
                "비밀번호는 최소 9자 이상이어야 합니다.",
                false
            );

            return;
        }

        let characterTypeCount = 0;

        if (/[a-z]/.test(password)) {
            characterTypeCount++;
        }

        if (/[A-Z]/.test(password)) {
            characterTypeCount++;
        }

        if (/[0-9]/.test(password)) {
            characterTypeCount++;
        }

        if (/[^a-zA-Z0-9]/.test(password)) {
            characterTypeCount++;
        }

        if (characterTypeCount < 3) {

            showPasswordMessage(
                "영문 대문자, 영문 소문자, 숫자, " +
                "특수문자 중 3종 이상을 사용해주세요.",
                false
            );

            return;
        }

        showPasswordMessage(
            "사용 가능한 비밀번호입니다.",
            true
        );
    }
);