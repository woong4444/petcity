document.addEventListener("DOMContentLoaded", function () {
    initRequestPanels();
    initAnimalSelection();
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