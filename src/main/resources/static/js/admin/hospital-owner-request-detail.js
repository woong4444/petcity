document.addEventListener("DOMContentLoaded", function () {

    const approveModal =
        document.querySelector("#approveModal");

    const rejectModal =
        document.querySelector("#rejectModal");

    const openApproveModalButton =
        document.querySelector("#openApproveModal");

    const openRejectModalButton =
        document.querySelector("#openRejectModal");

    const confirmRejectButton =
        document.querySelector("#confirmRejectButton");

    const rejectForm =
        document.querySelector("#rejectForm");

    const rejectReason =
        document.querySelector("#rejectReason");

    const rejectReasonLength =
        document.querySelector("#rejectReasonLength");

    const rejectErrorMessage =
        document.querySelector("#rejectErrorMessage");

    const modalCloseButtons =
        document.querySelectorAll("[data-modal-close]");

    let activeModal = null;

    initializeHospitalMap();
    initializeHospitalImage();



    if (openApproveModalButton !== null) {

        openApproveModalButton.addEventListener(
            "click",
            function () {

                openModal(approveModal);
            }
        );
    }



    if (openRejectModalButton !== null) {

        openRejectModalButton.addEventListener(
            "click",
            function () {

                resetRejectModal();
                openModal(rejectModal);
            }
        );
    }



    modalCloseButtons.forEach(function (button) {

        button.addEventListener(
            "click",
            function () {

                const modal =
                    button.closest(".modal-overlay");

                closeModal(modal);
            }
        );
    });



    if (approveModal !== null) {

        approveModal.addEventListener(
            "click",
            function (event) {

                if (event.target === approveModal) {
                    closeModal(approveModal);
                }
            }
        );
    }



    if (rejectModal !== null) {

        rejectModal.addEventListener(
            "click",
            function (event) {

                if (event.target === rejectModal) {
                    closeModal(rejectModal);
                }
            }
        );
    }



    document.addEventListener(
        "keydown",
        function (event) {

            if (
                event.key === "Escape" &&
                activeModal !== null
            ) {
                closeModal(activeModal);
            }
        }
    );



    if (rejectReason !== null) {

        rejectReason.addEventListener(
            "input",
            function () {

                const reason =
                    rejectReason.value.trim();

                if (rejectReasonLength !== null) {

                    rejectReasonLength.textContent =
                        String(rejectReason.value.length);
                }

                if (confirmRejectButton !== null) {

                    confirmRejectButton.disabled =
                        reason.length === 0;
                }

                if (reason.length > 0) {

                    rejectReason.classList.remove(
                        "is-invalid"
                    );

                    if (rejectErrorMessage !== null) {
                        rejectErrorMessage.hidden = true;
                    }
                }
            }
        );
    }



    if (
        rejectForm !== null &&
        rejectReason !== null
    ) {

        rejectForm.addEventListener(
            "submit",
            function (event) {

                const reason =
                    rejectReason.value.trim();

                if (reason === "") {

                    event.preventDefault();

                    rejectReason.classList.add(
                        "is-invalid"
                    );

                    if (rejectErrorMessage !== null) {
                        rejectErrorMessage.hidden = false;
                    }

                    if (confirmRejectButton !== null) {
                        confirmRejectButton.disabled = true;
                    }

                    rejectReason.focus();
                }
            }
        );
    }



    function openModal(modal) {

        if (modal === null) {
            return;
        }

        activeModal = modal;
        modal.hidden = false;

        document.body.classList.add(
            "modal-open"
        );

        const firstInput =
            modal.querySelector(
                "textarea, button"
            );

        if (firstInput !== null) {
            firstInput.focus();
        }
    }



    function closeModal(modal) {

        if (modal === null) {
            return;
        }

        modal.hidden = true;

        if (activeModal === modal) {
            activeModal = null;
        }

        if (activeModal === null) {

            document.body.classList.remove(
                "modal-open"
            );
        }
    }



    function resetRejectModal() {

        if (rejectReason !== null) {

            rejectReason.value = "";

            rejectReason.classList.remove(
                "is-invalid"
            );
        }

        if (rejectReasonLength !== null) {
            rejectReasonLength.textContent = "0";
        }

        if (rejectErrorMessage !== null) {
            rejectErrorMessage.hidden = true;
        }

        if (confirmRejectButton !== null) {
            confirmRejectButton.disabled = true;
        }
    }

});



function initializeHospitalMap() {

    const mapContainer =
        document.querySelector("#hospitalMap");

    const mapErrorMessage =
        document.querySelector("#mapErrorMessage");

    if (mapContainer === null) {
        return;
    }

    const latitude =
        Number(mapContainer.dataset.latitude);

    const longitude =
        Number(mapContainer.dataset.longitude);

    const hospitalName =
        mapContainer.dataset.hospitalName || "병원";

    const hospitalAddress =
        mapContainer.dataset.hospitalAddress || "";

    if (
        Number.isNaN(latitude) ||
        Number.isNaN(longitude)
    ) {

        showMapError();
        return;
    }

    if (
        typeof kakao === "undefined" ||
        kakao.maps === undefined
    ) {

        showMapError();
        return;
    }

    kakao.maps.load(function () {

        const hospitalPosition =
            new kakao.maps.LatLng(
                latitude,
                longitude
            );

        const map =
            new kakao.maps.Map(
                mapContainer,
                {
                    center: hospitalPosition,
                    level: 3
                }
            );

        const marker =
            new kakao.maps.Marker({
                position: hospitalPosition
            });

        marker.setMap(map);

        const informationContent = `
            <div class="map-information-window">

                <strong>
                    ${escapeHtml(hospitalName)}
                </strong>

                <span>
                    ${escapeHtml(hospitalAddress)}
                </span>

            </div>
        `;

        const informationWindow =
            new kakao.maps.InfoWindow({
                content: informationContent
            });

        informationWindow.open(
            map,
            marker
        );

        kakao.maps.event.addListener(
            marker,
            "click",
            function () {

                informationWindow.open(
                    map,
                    marker
                );
            }
        );

        const zoomControl =
            new kakao.maps.ZoomControl();

        map.addControl(
            zoomControl,
            kakao.maps.ControlPosition.RIGHT
        );

        setTimeout(
            function () {

                map.relayout();
                map.setCenter(hospitalPosition);
            },
            100
        );
    });


    function showMapError() {

        mapContainer.hidden = true;

        if (mapErrorMessage !== null) {
            mapErrorMessage.hidden = false;
        }
    }
}



function initializeHospitalImage() {

    const hospitalPreviewImage =
        document.querySelector(
            "#hospitalPreviewImage"
        );

    const hospitalImageEmpty =
        document.querySelector(
            "#hospitalImageEmpty"
        );

    if (hospitalPreviewImage === null) {
        return;
    }

    hospitalPreviewImage.addEventListener(
        "error",
        function () {

            hospitalPreviewImage.hidden = true;

            if (hospitalImageEmpty !== null) {

                hospitalImageEmpty.classList.remove(
                    "is-hidden"
                );
            }
        }
    );
}



function escapeHtml(value) {

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}