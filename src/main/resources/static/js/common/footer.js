document.addEventListener("DOMContentLoaded", function () {
    const topButton = document.querySelector("[data-footer-top]");

    if (!topButton) {
        return;
    }

    topButton.addEventListener("click", function () {
        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    });
});