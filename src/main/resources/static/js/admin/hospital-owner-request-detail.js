document.addEventListener("DOMContentLoaded", function () {

    const approveModal =
        document.querySelector("#approveModal");

    const rejectModal =
        document.querySelector("#rejectModal");

    const openApproveModalButton =
        document.querySelector("#openApproveModal");

    const openRejectModalButton =
        document.querySelector("#openRejectModal");

    const confirmApproveButton =
        document.querySelector("#confirmApproveButton");

    const confirmRejectButton =
        document.querySelector("#confirmRejectButton");

    const rejectReason =
        document.querySelector("#rejectReason");

    const rejectReasonLength =
        document.querySelector("#rejectReasonLength");

    const rejectErrorMessage =
        document.querySelector("#rejectErrorMessage");

    const detailToast =
        document.querySelector("#detailToast");

    const modalCloseButtons =
        document.querySelectorAll("[data-modal-close]");

    let activeModal = null;
    let toastTimer = null;

    initializeHospitalMap();
    initializeHospitalImage();


    /* =========================
       승인 모달 열기
       ========================= */

    if (openApproveModalButton !== null) {

        openApproveModalButton.addEventListener(
            "click",
            function () {

                openModal(approveModal);
            }
        );
    }


    /* =========================
       반려 모달 열기
       ========================= */

    if (openRejectModalButton !== null) {

        openRejectModalButton.addEventListener(
            "click",
            function () {

                resetRejectModal();
                openModal(rejectModal);
            }
        );
    }


    /* =========================
       모달 닫기 버튼
       ========================= */

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


    /* =========================
       모달 바깥 클릭
       ========================= */

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


    /* =========================
       ESC 키로 모달 닫기
       ========================= */

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


    /* =========================
       반려 사유 입력
       ========================= */

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


    /* =========================
       승인 확인
       ========================= */

    if (confirmApproveButton !== null) {

        confirmApproveButton.addEventListener(
            "click",
            function () {

                closeModal(approveModal);

                showToast(
                    "승인 화면 동작을 확인했습니다. " +
                    "백엔드 연결 후 실제 승인 처리가 실행됩니다."
                );
            }
        );
    }


    /* =========================
       반려 확인
       ========================= */

    if (confirmRejectButton !== null) {

        confirmRejectButton.addEventListener(
            "click",
            function () {

                if (rejectReason === null) {
                    return;
                }

                const reason =
                    rejectReason.value.trim();

                if (reason === "") {

                    rejectReason.classList.add(
                        "is-invalid"
                    );

                    if (rejectErrorMessage !== null) {
                        rejectErrorMessage.hidden = false;
                    }

                    rejectReason.focus();

                    return;
                }

                closeModal(rejectModal);

                showToast(
                    "반려 화면 동작을 확인했습니다. " +
                    "백엔드 연결 후 반려 사유가 저장됩니다."
                );
            }
        );
    }


    /* =========================
       모달 열기
       ========================= */

    function openModal(modal) {

        if (modal === null) {
            return;
        }

        activeModal = modal;
        modal.hidden = false;

        document.body.classList.add(
            "modal-open"
        );
    }


    /* =========================
       모달 닫기
       ========================= */

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


    /* =========================
       반려 모달 초기화
       ========================= */

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


    /* =========================
       안내 메시지
       ========================= */

    function showToast(message) {

        if (detailToast === null) {
            return;
        }

        if (toastTimer !== null) {
            clearTimeout(toastTimer);
        }

        detailToast.textContent = message;
        detailToast.hidden = false;

        toastTimer = setTimeout(
            function () {

                detailToast.hidden = true;
                toastTimer = null;

            },
            3000
        );
    }

});


/* =========================
   카카오 지도 초기화
   ========================= */

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

        setTimeout(function () {

            map.relayout();
            map.setCenter(hospitalPosition);

        }, 100);
    });


    function showMapError() {

        mapContainer.hidden = true;

        if (mapErrorMessage !== null) {
            mapErrorMessage.hidden = false;
        }
    }
}


/* =========================
   이미지 로딩 실패 처리
   ========================= */

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


/* =========================
   지도 말풍선 문자열 보호
   ========================= */

function escapeHtml(value) {

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}