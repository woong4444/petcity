document.addEventListener("DOMContentLoaded", function () {

    const faqButtons =
        document.querySelectorAll(".faq-question");

    faqButtons.forEach(function (button) {

        button.addEventListener("click", function () {

            const clickedItem =
                button.closest(".faq-item");

            if (!clickedItem) {
                return;
            }

            const alreadyOpen =
                clickedItem.classList.contains("open");


            /*
                현재 열려 있는 다른 FAQ 모두 닫기
            */
            document
                .querySelectorAll(".faq-item.open")
                .forEach(function (openItem) {

                    openItem.classList.remove("open");

                    const openButton =
                        openItem.querySelector(".faq-question");

                    if (openButton) {
                        openButton.setAttribute(
                            "aria-expanded",
                            "false"
                        );
                    }
                });


            /*
                방금 클릭한 FAQ가 닫혀 있던 상태라면 열기
            */
            if (!alreadyOpen) {

                clickedItem.classList.add("open");

                button.setAttribute(
                    "aria-expanded",
                    "true"
                );

            } else {

                button.setAttribute(
                    "aria-expanded",
                    "false"
                );
            }
        });
    });
});