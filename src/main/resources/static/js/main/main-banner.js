document.addEventListener("DOMContentLoaded", function () {
    const bannerElement =
        document.querySelector(".main-banner-swiper");

    if (bannerElement === null) {
        return;
    }

    if (typeof Swiper === "undefined") {
        return;
    }

    const slideCount =
        bannerElement.querySelectorAll(
            ".swiper-slide",
        ).length;

    if (slideCount <= 1) {
        bannerElement.classList.add(
            "single-slide",
        );
    }

    new Swiper(bannerElement, {
        loop: slideCount > 1,
        speed: 700,

        autoplay: slideCount > 1
            ? {
                delay: 5000,
                disableOnInteraction: false,
                pauseOnMouseEnter: true,
            }
            : false,

        pagination: {
            el: bannerElement.querySelector(
                ".swiper-pagination",
            ),
            clickable: true,
        },

        navigation: {
            nextEl: bannerElement.querySelector(
                ".swiper-button-next",
            ),

            prevEl: bannerElement.querySelector(
                ".swiper-button-prev",
            ),
        },

        keyboard: {
            enabled: true,
        },

        a11y: {
            enabled: true,
            prevSlideMessage: "이전 배너",
            nextSlideMessage: "다음 배너",
            firstSlideMessage: "첫 번째 배너입니다.",
            lastSlideMessage: "마지막 배너입니다.",
        },
    });

    const disabledLinks =
        bannerElement.querySelectorAll(
            ".main-banner-link-disabled",
        );

    disabledLinks.forEach(function (link) {
        link.addEventListener("click", function (event) {
            event.preventDefault();
        });
    });
});