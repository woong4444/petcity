document.addEventListener("DOMContentLoaded", function () {
    initRequestPanels();
    initAnimalSelection();
    initOperatingInfoForm();
});


function initRequestPanels() {
    const panels = document.querySelectorAll(".request-panel");

    document.querySelectorAll("[data-panel-target]").forEach(
        function (button) {
            button.addEventListener("click", function () {
                const targetPanel = document.getElementById(
                    button.dataset.panelTarget
                );

                panels.forEach(function (panel) {
                    panel.classList.remove("active");
                });

                targetPanel.classList.add("active");

                targetPanel.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });
            });
        }
    );

    document.querySelectorAll("[data-panel-close]").forEach(
        function (button) {
            button.addEventListener("click", function () {
                button.closest(".request-panel")
                    .classList.remove("active");
            });
        }
    );
}


function initAnimalSelection() {
    const categoryElements =
        document.querySelectorAll(".animal-category");

    categoryElements.forEach(function (categoryElement) {
        const parentToggle =
            categoryElement.querySelector(".animal-parent-toggle");

        const childArea =
            categoryElement.querySelector(".animal-child-area");

        const allCheckbox =
            categoryElement.querySelector(".animal-all-checkbox");

        const childCheckboxes =
            categoryElement.querySelectorAll(".animal-child-checkbox");

        if (!parentToggle || !childArea || !allCheckbox) {
            return;
        }

        const hasSelectedValue =
            allCheckbox.checked
            || Array.from(childCheckboxes).some(function (checkbox) {
                return checkbox.checked;
            });

        if (hasSelectedValue) {
            openAnimalCategory(
                categoryElement,
                childArea,
                parentToggle
            );
        }

        parentToggle.addEventListener("click", function () {
            if (childArea.classList.contains("open")) {
                closeAnimalCategory(
                    categoryElement,
                    childArea,
                    parentToggle
                );
            } else {
                openAnimalCategory(
                    categoryElement,
                    childArea,
                    parentToggle
                );
            }
        });

        if (allCheckbox.checked) {
            childCheckboxes.forEach(function (childCheckbox) {
                childCheckbox.checked = false;
                childCheckbox.disabled = true;
            });
        }

        allCheckbox.addEventListener("change", function () {
            if (this.checked) {
                childCheckboxes.forEach(function (childCheckbox) {
                    childCheckbox.checked = false;
                    childCheckbox.disabled = true;
                });
            } else {
                childCheckboxes.forEach(function (childCheckbox) {
                    childCheckbox.disabled = false;
                });
            }
        });

        childCheckboxes.forEach(function (childCheckbox) {
            childCheckbox.addEventListener("change", function () {
                if (this.checked) {
                    allCheckbox.checked = false;

                    childCheckboxes.forEach(function (checkbox) {
                        checkbox.disabled = false;
                    });
                }
            });
        });
    });
}


function openAnimalCategory(
    categoryElement,
    childArea,
    parentToggle
) {
    categoryElement.classList.add("active");
    childArea.classList.add("open");
    parentToggle.setAttribute("aria-expanded", "true");
}


function closeAnimalCategory(
    categoryElement,
    childArea,
    parentToggle
) {
    categoryElement.classList.remove("active");
    childArea.classList.remove("open");
    parentToggle.setAttribute("aria-expanded", "false");
}

function initOperatingInfoForm() {
    const directForm = document.querySelector("form[action*='direct-update']");

    if (!directForm) {
        return;
    }

    const hospitalPhone = directForm.querySelector("#hospitalPhone");
    const phonePrefix = directForm.querySelector("#phonePrefix");
    const phoneMiddle = directForm.querySelector("#phoneMiddle");
    const phoneLast = directForm.querySelector("#phoneLast");

    const breakTime = directForm.querySelector("#breakTime");
    const breakStart = directForm.querySelector("#breakStart");
    const breakEnd = directForm.querySelector("#breakEnd");

    const closedDays = directForm.querySelector("#closedDays");
    const holidayButtons = directForm.querySelectorAll(".holiday-day-button");

    // 기존 전화번호를 앞자리/가운데/뒷자리 입력칸에 표시
    const phoneParts = (hospitalPhone.value || "").split("-");

    if (phoneParts.length === 3) {
        phonePrefix.value = phoneParts[0];
        phoneMiddle.value = phoneParts[1];
        phoneLast.value = phoneParts[2];
    }

    // 기존 휴게시간(예: 13:00~14:00)을 시작/종료 칸에 표시
    const breakParts = (breakTime.value || "").split("~");

    if (breakParts.length === 2) {
        breakStart.value = breakParts[0].trim();
        breakEnd.value = breakParts[1].trim();
    }

    // 기존 정기 휴무일 버튼 선택 상태 표시
    const selectedDays = new Set(
        (closedDays.value || "")
            .split(",")
            .map(day => day.trim())
            .filter(day => day !== "")
    );

    holidayButtons.forEach(button => {
        if (selectedDays.has(button.dataset.day)) {
            button.classList.add("selected");
        }

        button.addEventListener("click", function () {
            this.classList.toggle("selected");
            updateClosedDays();
        });
    });

    // 숫자만 입력
    [phoneMiddle, phoneLast].forEach(input => {
        input.addEventListener("input", function () {
            this.value = this.value.replace(/\D/g, "");
            updateHospitalPhone();
        });
    });

    phonePrefix.addEventListener("change", updateHospitalPhone);

    function updateHospitalPhone() {
        const middle = phoneMiddle.value.trim();
        const last = phoneLast.value.trim();

        hospitalPhone.value =
            middle && last
                ? `${phonePrefix.value}-${middle}-${last}`
                : "";
    }

    function updateClosedDays() {
        const days = Array.from(
            directForm.querySelectorAll(".holiday-day-button.selected")
        ).map(button => button.dataset.day);

        closedDays.value = days.join(", ");
    }

    // 저장 직전에 휴게시간 값을 하나로 합친다.
    directForm.addEventListener("submit", function (event) {
        updateHospitalPhone();
        updateClosedDays();

        const start = breakStart.value;
        const end = breakEnd.value;

        if ((start && !end) || (!start && end)) {
            event.preventDefault();
            alert("휴게시간은 시작 시간과 종료 시간을 모두 입력해 주세요.");
            return;
        }

        const openTime = directForm.querySelector("#openTime").value;
        const closeTime = directForm.querySelector("#closeTime").value;

        if(openTime >= closeTime) {
            event.preventDefault();
            alert("진료 종료 시간은 진료 시작 시간보다 늦어야 합니다.");
            return;
        }

        if(start&&end) {
            if(start >= end) {
                event.preventDefault();
                alert("휴게 종료 시간은 휴게 시작 시간보다 늦어야 합니다.");
                return;
            }

            if(start < openTime || end > closeTime) {
                event.preventDefault();
                alert("휴게시간은 진료 시작 시간과 종료 시간 사이로 설정해 주세요.");
                return;
            }
        }

        breakTime.value = start && end ? `${start}~${end}` : "";
    });
}