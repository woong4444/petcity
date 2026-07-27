document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector("#profileForm");
    const nicknameInput = document.querySelector("#profileNickname");
    const checkButton = document.querySelector("#nicknameCheckButton");
    const message = document.querySelector("#nicknameCheckMessage");
    const saveButton = document.querySelector("#profileSaveButton");

    if (form && nicknameInput && checkButton && message && saveButton) {

    const originalNickname = (nicknameInput.dataset.original || "").trim();
    const nicknamePattern = /^[가-힣a-zA-Z0-9_]{2,20}$/;
    let checkedNickname = originalNickname;

    const setMessage = (text, type = "") => {
        message.textContent = text;
        message.className = type;
    };

    nicknameInput.addEventListener("input", () => {
        const nickname = nicknameInput.value.trim();
        checkedNickname = nickname === originalNickname ? originalNickname : "";

        if (!nickname) {
            setMessage("닉네임을 입력해주세요.", "error");
        } else if (!nicknamePattern.test(nickname)) {
            setMessage("2~20자의 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다.", "error");
        } else if (nickname === originalNickname) {
            setMessage("현재 사용 중인 닉네임입니다.", "success");
        } else {
            setMessage("닉네임을 변경하면 중복확인이 필요합니다.");
        }
    });

    checkButton.addEventListener("click", async () => {
        const nickname = nicknameInput.value.trim();

        if (!nicknamePattern.test(nickname)) {
            checkedNickname = "";
            setMessage("2~20자의 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다.", "error");
            nicknameInput.focus();
            return;
        }

        if (nickname === originalNickname) {
            checkedNickname = originalNickname;
            setMessage("현재 사용 중인 닉네임입니다.", "success");
            return;
        }

        checkButton.disabled = true;
        checkButton.textContent = "확인 중";

        try {
            const response = await fetch(`/member/mypage/check-nickname?nickname=${encodeURIComponent(nickname)}`, {
                headers: { "X-Requested-With": "XMLHttpRequest" }
            });
            if (!response.ok) throw new Error("중복확인 요청 실패");

            const duplicated = await response.json();
            if (duplicated) {
                checkedNickname = "";
                setMessage("닉네임이 중복되었습니다.", "error");
            } else {
                checkedNickname = nickname;
                setMessage("사용 가능한 닉네임입니다.", "success");
            }
        } catch (error) {
            checkedNickname = "";
            setMessage("닉네임 중복확인 중 오류가 발생했습니다.", "error");
        } finally {
            checkButton.disabled = false;
            checkButton.textContent = "중복확인";
        }
    });

    form.addEventListener("submit", (event) => {
        const nickname = nicknameInput.value.trim();

        if (nickname === originalNickname) {
            event.preventDefault();
            setMessage("변경된 닉네임이 없습니다.", "error");
            return;
        }

        if (!nicknamePattern.test(nickname)) {
            event.preventDefault();
            setMessage("닉네임 규칙을 확인해주세요.", "error");
            nicknameInput.focus();
            return;
        }

        if (checkedNickname !== nickname) {
            event.preventDefault();
            setMessage("닉네임 중복확인을 먼저 해주세요.", "error");
            return;
        }

        saveButton.disabled = true;
        saveButton.textContent = "저장 중...";
    });
    }

    // =====================================================
    // 07-27 상각: LOCAL 회원 비밀번호 변경 프론트 검증
    // SNS 회원은 폼이 렌더링되지 않으므로 아래 로직을 실행하지 않습니다.
    // 회원가입과 동일한 비밀번호 형식 검사를 적용합니다.
    // =====================================================
    const passwordForm = document.querySelector("#passwordChangeForm");
    if (!passwordForm) return;

    const currentPassword = document.querySelector("#currentPassword");
    const newPassword = document.querySelector("#newPassword");
    const newPasswordConfirm = document.querySelector("#newPasswordConfirm");
    const newPasswordMessage = document.querySelector("#newPasswordMessage");
    const confirmMessage = document.querySelector("#passwordConfirmMessage");
    const passwordButton = document.querySelector("#passwordChangeButton");
    const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9\s])\S{8,64}$/;

    document.querySelectorAll(".password-toggle").forEach((button) => {
        button.addEventListener("click", () => {
            const target = document.getElementById(button.dataset.target);
            if (!target) return;
            const show = target.type === "password";
            target.type = show ? "text" : "password";
            button.textContent = show ? "숨기기" : "보기";
        });
    });

    const validateNewPassword = () => {
        const value = newPassword.value;
        if (!value) {
            newPasswordMessage.textContent = "새 비밀번호를 입력해주세요.";
            newPasswordMessage.className = "error";
            return false;
        }
        if (!passwordPattern.test(value)) {
            newPasswordMessage.textContent = "영문, 숫자, 특수문자를 포함한 8~64자로 입력해주세요.";
            newPasswordMessage.className = "error";
            return false;
        }
        newPasswordMessage.textContent = "사용 가능한 비밀번호 형식입니다.";
        newPasswordMessage.className = "success";
        return true;
    };

    const validatePasswordConfirm = () => {
        if (!newPasswordConfirm.value) {
            confirmMessage.textContent = "새 비밀번호를 다시 입력해주세요.";
            confirmMessage.className = "error";
            return false;
        }
        if (newPassword.value !== newPasswordConfirm.value) {
            confirmMessage.textContent = "새 비밀번호가 일치하지 않습니다.";
            confirmMessage.className = "error";
            return false;
        }
        confirmMessage.textContent = "새 비밀번호가 일치합니다.";
        confirmMessage.className = "success";
        return true;
    };

    newPassword.addEventListener("input", () => {
        validateNewPassword();
        if (newPasswordConfirm.value) validatePasswordConfirm();
    });
    newPasswordConfirm.addEventListener("input", validatePasswordConfirm);

    passwordForm.addEventListener("submit", (event) => {
        let valid = true;
        if (!currentPassword.value) {
            currentPassword.focus();
            valid = false;
        }
        if (!validateNewPassword()) valid = false;
        if (!validatePasswordConfirm()) valid = false;
        if (currentPassword.value && currentPassword.value === newPassword.value) {
            newPasswordMessage.textContent = "현재 비밀번호와 다른 비밀번호를 입력해주세요.";
            newPasswordMessage.className = "error";
            valid = false;
        }
        if (!valid) {
            event.preventDefault();
            return;
        }
        passwordButton.disabled = true;
        passwordButton.textContent = "변경 중...";
    });
});
