/*
 * ?쇰컲?뚯썝 媛?????꾩슜 JavaScript
 * ?꾩씠?붋룸땳?ㅼ엫 以묐났 ?뺤씤, 鍮꾨?踰덊샇 寃利? ?대찓???몄쬆 諛????쒖텧???대떦?⑸땲??
 */

const signupForm =
        document.querySelector("#signupForm");

    const loginId =
        document.querySelector("#loginId");

    const checkLoginIdBtn =
        document.querySelector("#checkLoginIdBtn");

    const loginIdMessage =
        document.querySelector("#loginIdMessage");

    const password =
        document.querySelector("#password");

    const passwordCheck =
        document.querySelector("#passwordCheck");

    const passwordStrengthMessage =
        document.querySelector("#passwordStrengthMessage");

    const passwordMatchMessage =
        document.querySelector("#passwordMatchMessage");

    const togglePasswordBtn =
        document.querySelector("#togglePasswordBtn");

    const togglePasswordCheckBtn =
        document.querySelector("#togglePasswordCheckBtn");

    const nickname =
        document.querySelector("#nickname");

    const checkNicknameBtn =
        document.querySelector("#checkNicknameBtn");

    const nicknameMessage =
        document.querySelector("#nicknameMessage");

    const email =
        document.querySelector("#email");

    const checkEmailBtn =
        document.querySelector("#checkEmailBtn");

    const emailActivationMessage =
        document.querySelector("#emailActivationMessage");

    const emailDuplicateMessage =
        document.querySelector("#emailDuplicateMessage");

    const sendEmailCodeBtn =
        document.querySelector("#sendEmailCodeBtn");

    const resendEmailCodeBtn =
        document.querySelector("#resendEmailCodeBtn");

    const emailCode =
        document.querySelector("#emailCode");

    const emailVerificationMessage =
        document.querySelector("#emailVerificationMessage");

    const phone =
        document.querySelector("#phone");

    let loginIdChecked = false;
    let nicknameChecked = false;

    let emailChecked =
        serverEmailVerified;

    let emailCodeSent = false;

    let emailVerified =
        serverEmailVerified;

    function showMessage(
        element,
        message,
        success
    ) {

        element.textContent =
            message;

        element.classList.remove(
            "text-success",
            "text-error"
        );

        element.classList.add(
            success
                ? "text-success"
                : "text-error"
        );
    }

    function clearMessage(element) {

        element.textContent = "";

        element.classList.remove(
            "text-success",
            "text-error"
        );
    }

    function isValidLoginId(value) {

        return /^[a-z][a-z0-9]{4,19}$/
            .test(value);
    }

    function isWeakLoginId(value) {

        const weakIds = [
            "administrator",
            "root",
            "manager",
            "test",
            "test01",
            "guest",
            "user",
            "member",
            "petcity",
            "qwerty",
            "asdf",
            "abcde"
        ];

        return weakIds.includes(value)
            || /^(.)\1{4,}$/.test(value)
            || value.includes("12345")
            || value.includes("01234")
            || value.includes("abcde");
    }

    checkLoginIdBtn.addEventListener(
        "click",
        async () => {

            const value =
                loginId.value
                    .trim()
                    .toLowerCase();

            loginId.value =
                value;

            if (!isValidLoginId(value)) {

                loginIdChecked = false;

                showMessage(
                    loginIdMessage,
                    "아이디는 영문 소문자로 시작하는 " +
                    "5~20자의 영문 소문자와 숫자만 사용할 수 있습니다.",
                    false
                );

                loginId.focus();
                updateEmailControls();
                return;
            }

            if (isWeakLoginId(value)) {

                loginIdChecked = false;

                showMessage(
                    loginIdMessage,
                    "보안에 취약하거나 쉽게 추측되는 아이디는 사용할 수 없습니다.",
                    false
                );

                loginId.focus();
                updateEmailControls();
                return;
            }

            try {

                const response =
                    await fetch(
                        "/member/check-loginId" +
                        "?loginId=" +
                        encodeURIComponent(value)
                    );

                if (!response.ok) {
                    throw new Error();
                }

                const exists =
                    await response.json();

                loginIdChecked =
                    !exists;

                showMessage(
                    loginIdMessage,
                    exists
                        ? "이미 사용 중인 아이디입니다."
                        : "사용 가능한 아이디입니다.",
                    !exists
                );

            } catch (error) {

                loginIdChecked = false;

                showMessage(
                    loginIdMessage,
                    "아이디 중복확인 중 오류가 발생했습니다.",
                    false
                );
            }

            updateEmailControls();
        }
    );

    loginId.addEventListener(
        "input",
        () => {

            loginIdChecked = false;

            showMessage(
                loginIdMessage,
                "아이디 중복확인이 필요합니다.",
                false
            );

            updateEmailControls();
        }
    );

    function getPasswordError(value) {

        if (value.length === 0) {
            return "";
        }

        if (value.length < 8
            || value.length > 64) {

            return "비밀번호는 8~64자로 입력해주세요.";
        }

        if (/\s/.test(value)) {

            return "비밀번호에는 공백을 사용할 수 없습니다.";
        }

        if (!/[A-Za-z]/.test(value)) {

            return "비밀번호에 영문을 최소 1개 포함해주세요.";
        }

        if (!/[0-9]/.test(value)) {

            return "비밀번호에 숫자를 최소 1개 포함해주세요.";
        }

        if (!/[^A-Za-z0-9\s]/.test(value)) {

            return "비밀번호에 특수문자를 최소 1개 포함해주세요.";
        }

        return "";
    }

    function isStrongPassword(value) {

        return value.length > 0
            && getPasswordError(value) === "";
    }

    function updatePasswordMessages() {

        const passwordError =
            getPasswordError(
                password.value
            );

        if (password.value.length === 0) {

            clearMessage(
                passwordStrengthMessage
            );

        } else if (passwordError) {

            showMessage(
                passwordStrengthMessage,
                passwordError,
                false
            );

        } else {

            showMessage(
                passwordStrengthMessage,
                "사용 가능한 비밀번호입니다.",
                true
            );
        }

        if (passwordCheck.value.length === 0) {

            clearMessage(
                passwordMatchMessage
            );

        } else if (
            password.value
            === passwordCheck.value
        ) {

            showMessage(
                passwordMatchMessage,
                "비밀번호가 일치합니다.",
                true
            );

        } else {

            showMessage(
                passwordMatchMessage,
                "비밀번호가 일치하지 않습니다.",
                false
            );
        }
    }

    password.addEventListener(
        "input",
        () => {

            updatePasswordMessages();
            updateEmailControls();
        }
    );

    passwordCheck.addEventListener(
        "input",
        () => {

            updatePasswordMessages();
            updateEmailControls();
        }
    );

    function togglePasswordVisibility(
        input,
        button
    ) {

        const hidden =
            input.type === "password";

        input.type =
            hidden
                ? "text"
                : "password";

        button.textContent =
            hidden
                ? "숨기기"
                : "보기";
    }

    togglePasswordBtn.addEventListener(
        "click",
        () => {

            togglePasswordVisibility(
                password,
                togglePasswordBtn
            );
        }
    );

    togglePasswordCheckBtn.addEventListener(
        "click",
        () => {

            togglePasswordVisibility(
                passwordCheck,
                togglePasswordCheckBtn
            );
        }
    );

    checkNicknameBtn.addEventListener(
        "click",
        async () => {

            const value =
                nickname.value.trim();

            nickname.value =
                value;

            if (!nickname.reportValidity()) {

                nicknameChecked = false;
                updateEmailControls();
                return;
            }

            try {

                const response =
                    await fetch(
                        "/member/check-nickname" +
                        "?nickname=" +
                        encodeURIComponent(value)
                    );

                if (!response.ok) {
                    throw new Error();
                }

                const exists =
                    await response.json();

                nicknameChecked =
                    !exists;

                showMessage(
                    nicknameMessage,
                    exists
                        ? "이미 사용 중인 닉네임입니다."
                        : "사용 가능한 닉네임입니다.",
                    !exists
                );

            } catch (error) {

                nicknameChecked = false;

                showMessage(
                    nicknameMessage,
                    "닉네임 중복확인 중 오류가 발생했습니다.",
                    false
                );
            }

            updateEmailControls();
        }
    );

    nickname.addEventListener(
        "input",
        () => {

            nicknameChecked = false;

            showMessage(
                nicknameMessage,
                "닉네임 중복확인이 필요합니다.",
                false
            );

            updateEmailControls();
        }
    );

    function isPreviousStepComplete() {

        return loginIdChecked
            && nicknameChecked
            && isStrongPassword(
                password.value
            )
            && password.value
            === passwordCheck.value;
    }

    function updateEmailControls() {

        if (emailVerified) {

            email.disabled = false;
            email.readOnly = true;

            checkEmailBtn.disabled = true;
            sendEmailCodeBtn.disabled = true;
            emailCode.disabled = true;

            resendEmailCodeBtn.style.display =
                "none";

            sendEmailCodeBtn.textContent =
                "인증 완료";

            sendEmailCodeBtn.classList.remove(
                "is-verifying"
            );

            sendEmailCodeBtn.classList.add(
                "is-complete"
            );

            showMessage(
                emailActivationMessage,
                "이메일 인증이 완료되었습니다.",
                true
            );

            showMessage(
                emailVerificationMessage,
                "이미 인증이 완료된 이메일입니다.",
                true
            );

            return;
        }

        const canUseEmail =
            isPreviousStepComplete();

        email.disabled =
            !canUseEmail;

        email.readOnly =
            emailCodeSent;

        checkEmailBtn.disabled =
            !canUseEmail
            || emailCodeSent;

        sendEmailCodeBtn.disabled =
            !canUseEmail
            || !emailChecked;

        emailCode.disabled =
            !emailCodeSent;

        if (emailCodeSent) {

            resendEmailCodeBtn.style.display =
                "inline-block";

        } else {

            resendEmailCodeBtn.style.display =
                "none";
        }

        if (canUseEmail) {

            showMessage(
                emailActivationMessage,
                "이메일 중복확인 후 인증번호를 발송할 수 있습니다.",
                true
            );

        } else {

            showMessage(
                emailActivationMessage,
                "아이디 중복확인, 비밀번호 조건, 닉네임 중복확인을 먼저 완료해주세요.",
                false
            );
        }
    }

    function resetEmailVerificationUi() {

        emailChecked = false;
        emailCodeSent = false;
        emailVerified = false;

        emailCode.value = "";
        emailCode.disabled = true;

        sendEmailCodeBtn.textContent =
            "인증번호 발송";

        sendEmailCodeBtn.classList.remove(
            "is-verifying",
            "is-complete"
        );

        resendEmailCodeBtn.style.display =
            "none";

        clearMessage(
            emailDuplicateMessage
        );

        clearMessage(
            emailVerificationMessage
        );
    }

    email.addEventListener(
        "input",
        () => {

            if (emailVerified) {
                return;
            }

            resetEmailVerificationUi();
            updateEmailControls();
        }
    );

    checkEmailBtn.addEventListener(
        "click",
        async () => {

            if (!email.reportValidity()) {
                return;
            }

            try {

                const normalizedEmail =
                    email.value
                        .trim()
                        .toLowerCase();

                email.value =
                    normalizedEmail;

                const response =
                    await fetch(
                        "/member/check-email" +
                        "?email=" +
                        encodeURIComponent(
                            normalizedEmail
                        )
                    );

                if (!response.ok) {
                    throw new Error();
                }

                const exists =
                    await response.json();

                emailChecked =
                    !exists;

                showMessage(
                    emailDuplicateMessage,
                    exists
                        ? "이미 사용 중인 이메일입니다."
                        : "사용 가능한 이메일입니다.",
                    !exists
                );

            } catch (error) {

                emailChecked = false;

                showMessage(
                    emailDuplicateMessage,
                    "이메일 중복확인 중 오류가 발생했습니다.",
                    false
                );
            }

            updateEmailControls();
        }
    );

    async function requestEmailVerification(
        url,
        parameters
    ) {

        const response =
            await fetch(
                url,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body: new URLSearchParams(
                        parameters
                    )
                }
            );

        const message =
            await response.text();

        showMessage(
            emailVerificationMessage,
            message,
            response.ok
        );

        return {
            success: response.ok,
            message: message
        };
    }

    async function sendVerificationEmail(
        resend
    ) {

        if (!email.reportValidity()) {
            return false;
        }

        const normalizedEmail =
            email.value
                .trim()
                .toLowerCase();

        email.value =
            normalizedEmail;

        const button =
            resend
                ? resendEmailCodeBtn
                : sendEmailCodeBtn;

        const originalText =
            button.textContent;

        button.disabled = true;

        button.textContent =
            "발송 중...";

        try {

            const result =
                await requestEmailVerification(
                    "/member/signup/email/send",
                    {
                        email:
                        normalizedEmail
                    }
                );

            if (!result.success) {
                return false;
            }

            if (
                result.message.includes(
                    "이미 인증 완료"
                )
            ) {

                emailVerified = true;
                updateEmailControls();
                return true;
            }

            emailCodeSent = true;

            email.readOnly = true;
            emailCode.disabled = false;
            emailCode.value = "";
            emailCode.focus();

            sendEmailCodeBtn.textContent =
                "이메일 인증";

            sendEmailCodeBtn.classList.add(
                "is-verifying"
            );

            resendEmailCodeBtn.style.display =
                "inline-block";

            if (resend) {

                showMessage(
                    emailVerificationMessage,
                    "새 인증번호를 발송했습니다.",
                    true
                );
            }

            updateEmailControls();

            return true;

        } finally {

            button.disabled = false;

            if (resend) {

                button.textContent =
                    "인증번호 재발송";

            } else if (!emailCodeSent) {

                button.textContent =
                    originalText;
            }
        }
    }

    sendEmailCodeBtn.addEventListener(
        "click",
        async () => {

            if (!emailCodeSent) {

                await sendVerificationEmail(
                    false
                );

                return;
            }

            const code =
                emailCode.value.trim();

            if (!/^\d{6}$/.test(code)) {

                showMessage(
                    emailVerificationMessage,
                    "인증번호 6자리를 입력해주세요.",
                    false
                );

                emailCode.focus();
                return;
            }

            const normalizedEmail =
                email.value
                    .trim()
                    .toLowerCase();

            const result =
                await requestEmailVerification(
                    "/member/signup/email/verify",
                    {
                        email:
                        normalizedEmail,

                        code:
                        code
                    }
                );

            if (result.success) {

                emailVerified = true;

                resendEmailCodeBtn.style.display =
                    "none";

                /*
                    입력창과 버튼을 인증 완료 상태로 변경
                */
                updateEmailControls();

                /*
                    updateEmailControls()가 출력한
                    '이미 인증이 완료된 이메일입니다.' 문구를
                    최초 인증 성공 문구로 다시 변경
                */
                showMessage(
                    emailVerificationMessage,
                    "이메일 인증이 완료되었습니다.",
                    true
                );

                return;
            }

            if (
                result.message.includes("먼저 발송")
                || result.message.includes("다시 발송")
                || result.message.includes("만료")
            ) {

                resendEmailCodeBtn.style.display =
                    "inline-block";
            }
        }
    );

    resendEmailCodeBtn.addEventListener(
        "click",
        async () => {

            await sendVerificationEmail(
                true
            );
        }
    );

    phone.addEventListener(
        "input",
        () => {

            const numbers =
                phone.value
                    .replace(/[^0-9]/g, "")
                    .slice(0, 11);

            if (numbers.length <= 3) {

                phone.value =
                    numbers;

            } else if (numbers.length <= 7) {

                phone.value =
                    numbers.slice(0, 3)
                    + "-"
                    + numbers.slice(3);

            } else {

                phone.value =
                    numbers.slice(0, 3)
                    + "-"
                    + numbers.slice(
                        3,
                        numbers.length - 4
                    )
                    + "-"
                    + numbers.slice(-4);
            }
        }
    );

    signupForm.addEventListener(
        "submit",
        event => {

            const loginIdValue =
                loginId.value
                    .trim()
                    .toLowerCase();

            const passwordError =
                getPasswordError(
                    password.value
                );

            if (!loginIdChecked) {

                event.preventDefault();
                alert("아이디 중복확인을 완료해주세요.");
                loginId.focus();
                return;
            }

            if (!isValidLoginId(
                loginIdValue
            )) {

                event.preventDefault();
                alert(
                    "아이디는 영문 소문자로 시작하는 " +
                    "5~20자의 영문 소문자와 숫자만 사용할 수 있습니다."
                );
                loginId.focus();
                return;
            }

            if (isWeakLoginId(
                loginIdValue
            )) {

                event.preventDefault();
                alert(
                    "보안에 취약하거나 쉽게 추측되는 아이디는 사용할 수 없습니다."
                );
                loginId.focus();
                return;
            }

            if (passwordError) {

                event.preventDefault();
                alert(passwordError);
                password.focus();
                return;
            }

            if (
                password.value
                !== passwordCheck.value
            ) {

                event.preventDefault();
                alert("비밀번호 확인이 일치하지 않습니다.");
                passwordCheck.focus();
                return;
            }

            if (!nicknameChecked) {

                event.preventDefault();
                alert("닉네임 중복확인을 완료해주세요.");
                nickname.focus();
                return;
            }

            if (!emailVerified) {

                event.preventDefault();
                alert("이메일 인증을 완료해주세요.");
                email.focus();
                return;
            }

            email.disabled = false;
            email.readOnly = true;
        }
    );

    window.addEventListener(
        "DOMContentLoaded",
        () => {

            const savedLoginId =
                loginId.value.trim();

            const savedNickname =
                nickname.value.trim();

            if (savedLoginId !== "") {

                checkLoginIdBtn.click();
            }

            if (savedNickname !== "") {

                checkNicknameBtn.click();
            }

            updatePasswordMessages();
            updateEmailControls();
        }
    );