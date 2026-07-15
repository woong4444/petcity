document.addEventListener("DOMContentLoaded", function () {
    const bannerList = document.querySelector("#bannerList");
    const previewContent = document.querySelector("#bannerPreviewContent");
    const toggleBannerPreview = document.querySelector("#toggleBannerPreview");
    const bannerTotalCount = document.querySelector("#bannerTotalCount");
    const bannerDeleteModal = document.querySelector("#bannerDeleteModal");
    const deleteBannerTitle = document.querySelector("#deleteBannerTitle");
    const confirmDeleteBanner = document.querySelector("#confirmDeleteBanner");
    const closeDeleteModalButtons = Array.from(document.querySelectorAll("[data-close-delete-modal]"));
    let deleteTargetBanner = null;

    const previewSwiper =new Swiper(".main-banner-preview-swiper",{
        loop : false, rewind:true, speed: 550,
        autoplay:{
            delay: 4500,
            disableOnInteraction: false,
        },
        pagination: {
            el: ".main-banner-preview-swiper .swiper-pagination",
            clickable: true,
        },
        navigation: {
            nextEl: ".main-banner-preview-swiper .swiper-button-next",
            prevEl: ".main-banner-preview-swiper .swiper-button-prev",
        },
    },
    );

    if (toggleBannerPreview !== null) {
        toggleBannerPreview.addEventListener("click", function () {
            const isHidden = previewContent.hidden;
            previewContent.hidden = !isHidden;
            toggleBannerPreview.setAttribute("aria-expanded", String(isHidden),);
        
            if (isHidden){
                toggleBannerPreview.innerHTML = '미리보기 접기 <span class="preview-toggle-arrow">▲</span>';
                window.setTimeout(function () {
                    previewSwiper.update();
                }, 0,);
            }else{
                toggleBannerPreview.innerHTML = '미리보기 펼치기 <span class="preview-toggle-arrow">▼</span>';
            }
        },);
    }

    if (bannerList !== null) {
        bannerList.addEventListener("click", function (event) {
            const moveUpButton = event.target.closest(".move-up-button");
            const moveDownButton = event.target.closest(".move-down-button");
            const deleteButton = event.target.closest(".banner-delete-button");

            if (moveUpButton !== null) {
                const bannerItem = moveUpButton.closest(".banner-item");
                moveBannerUp(bannerItem);
                return;
            }

            if (moveDownButton !== null) {
                const bannerItem = moveDownButton.closest(".banner-item");
                moveBannerDown(bannerItem);
                return;
            }
            if (deleteButton !== null) {
                const bannerItem = deleteButton.closest(".banner-item");
                openDeleteModal(bannerItem);
            }
        },);
    }


    closeDeleteModalButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            closeDeleteModal();
        },);
    },);

    if (confirmDeleteBanner !== null) {
        confirmDeleteBanner.addEventListener("click", function () {
            if (deleteTargetBanner === null) {
                return;
            }
            const bannerId = deleteTargetBanner.dataset.bannerId;

            const previewSlide = document.querySelector('.swiper-slide[data-banner-id="'+bannerId+'"]',);
            deleteTargetBanner.remove();
            if (previewSlide!==null){
                previewSlide.remove();
            }
            deleteTargetBanner=null;

            updateBannerOrders();
            updateBannerCount();
            previewSwiper.update();
            previewSwiper.slideTo(0);
            closeDeleteModal();
        },);
    }

    document.addEventListener("keydown", function (event) {
        if (event.key !== "Escape") {
            return;
        }
        if (bannerDeleteModal !== null && !bannerDeleteModal.hidden) {
            closeDeleteModal();
        }
    },);

    function moveBannerUp(bannerItem) {
        if (bannerItem === null || bannerItem.previousElementSibling === null) {
            return;
        }
        const previousBanner = bannerItem.previousElementSibling;
        bannerList.insertBefore(bannerItem, previousBanner);
        movePreviewSlideUp(bannerItem.dataset.bannerId,);
        updateBannerOrders();
        previewSwiper.update();
    }

    function moveBannerDown(bannerItem){
        if (bannerItem === null || bannerItem.nextElementSibling === null) {
            return;
        }
        const nextBanner = bannerItem.nextElementSibling;
        bannerList.insertBefore(nextBanner, bannerItem,);
        movePreviewSlideDown(bannerItem.dataset.bannerId,);

        updateBannerOrders();
        previewSwiper.update();
    }

    function movePreviewSlideUp(bannerId) {
        const previewSlide = document.querySelector('.swiper-slide[data-banner-id="'+bannerId+'"]',);

        if (previewSlide === null || previewSlide.previousElementSibling === null) {
            return;
        }
        previewSlide.parentElement.insertBefore(previewSlide, previewSlide.previousElementSibling);
    }


    function movePreviewSlideDown(bannerId) {
        const previewSlide = document.querySelector('.swiper-slide[data-banner-id="'+bannerId+'"]',);
        if (previewSlide === null || previewSlide.nextElementSibling === null) {
            return;
        }
        previewSlide.parentElement.insertBefore(previewSlide.nextElementSibling, previewSlide);
    }


    function updateBannerOrders() {
        const bannerItems = Array.from(
            document.querySelectorAll(".banner-item"),
        );


        bannerItems.forEach(
            function (bannerItem, index) {
                const orderNumber =
                    bannerItem.querySelector(
                        ".banner-order-number",
                    );

                const moveUpButton =
                    bannerItem.querySelector(
                        ".move-up-button",
                    );

                const moveDownButton =
                    bannerItem.querySelector(
                        ".move-down-button",
                    );


                if (orderNumber !== null) {
                    orderNumber.textContent =
                        index + 1;
                }


                if (moveUpButton !== null) {
                    moveUpButton.disabled =
                        index === 0;
                }


                if (moveDownButton !== null) {
                    moveDownButton.disabled =
                        index === bannerItems.length - 1;
                }
            },
        );
    }


    function updateBannerCount() {
        if (bannerTotalCount === null) {
            return;
        }


        const count =
            document.querySelectorAll(
                ".banner-item",
            ).length;


        bannerTotalCount.textContent =
            count;
    }


    function openDeleteModal(bannerItem) {
        if (
            bannerDeleteModal === null
            || bannerItem === null
        ) {

            return;
        }


        deleteTargetBanner =
            bannerItem;


        if (deleteBannerTitle !== null) {
            deleteBannerTitle.textContent =
                bannerItem.dataset.bannerTitle;
        }


        bannerDeleteModal.hidden =
            false;


        bannerDeleteModal.setAttribute(
            "aria-hidden",
            "false",
        );


        document.body.classList.add(
            "modal-open",
        );
    }


    function closeDeleteModal() {
        if (bannerDeleteModal === null) {
            return;
        }


        bannerDeleteModal.hidden =
            true;


        bannerDeleteModal.setAttribute(
            "aria-hidden",
            "true",
        );


        document.body.classList.remove(
            "modal-open",
        );


        deleteTargetBanner =
            null;


        if (deleteBannerTitle !== null) {
            deleteBannerTitle.textContent =
                "-";
        }
    }


    updateBannerOrders();
    updateBannerCount();
});