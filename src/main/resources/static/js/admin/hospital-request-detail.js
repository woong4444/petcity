document.addEventListener("DOMContentLoaded", function () {

    const approveForm =
        document.querySelector(".approve-form");

    const rejectForm =
        document.querySelector(".reject-form");


    /*
     * 승인 처리
     */
    if (approveForm !== null) {

        approveForm.addEventListener("submit", function (event) {

            if (approveForm.dataset.submitted === "true") {
                event.preventDefault();
                return;
            }


            const requestType =
                approveForm.dataset.requestType;

            const hospitalName =
                approveForm.dataset.hospitalName || "해당 병원";

            const confirmMessage =
                getApproveConfirmMessage(
                    requestType,
                    hospitalName
                );


            const approved =
                window.confirm(confirmMessage);


            if (!approved) {
                event.preventDefault();
                return;
            }


            approveForm.dataset.submitted = "true";

            disableSubmitButton(
                approveForm,
                "처리 중..."
            );

        });

    }


    /*
     * 반려 처리
     */
    if (rejectForm !== null) {

        rejectForm.addEventListener("submit", function (event) {

            if (rejectForm.dataset.submitted === "true") {
                event.preventDefault();
                return;
            }


            const rejectReason =
                rejectForm.querySelector(
                    "#rejectReason"
                );


            if (rejectReason === null) {
                event.preventDefault();

                window.alert(
                    "반려 사유 입력창을 찾을 수 없습니다."
                );

                return;
            }


            const checkedRejectReason =
                rejectReason.value.trim();


            if (checkedRejectReason === "") {
                event.preventDefault();

                window.alert(
                    "반려 사유를 입력해 주세요."
                );

                rejectReason.focus();

                return;
            }


            if (checkedRejectReason.length > 1000) {
                event.preventDefault();

                window.alert(
                    "반려 사유는 1000자 이하로 입력해 주세요."
                );

                rejectReason.focus();

                return;
            }


            /*
             * 앞뒤 공백을 제거한 값을 서버로 전송
             */
            rejectReason.value =
                checkedRejectReason;


            const hospitalName =
                rejectForm.dataset.hospitalName || "해당 병원";


            const rejected =
                window.confirm(
                    hospitalName
                    + "의 요청을 반려하시겠습니까?\n\n"
                    + "반려 후에는 같은 요청을 다시 승인할 수 없습니다."
                );


            if (!rejected) {
                event.preventDefault();
                return;
            }


            rejectForm.dataset.submitted = "true";

            disableSubmitButton(
                rejectForm,
                "처리 중..."
            );

        });

    }


    /*
     * 요청 종류에 맞는 승인 확인 문구
     */
    function getApproveConfirmMessage(
        requestType,
        hospitalName
    ) {

        if (requestType === "UPDATE") {

            return hospitalName
                + "의 정보 수정 요청을 승인하시겠습니까?\n\n"
                + "승인하면 요청 정보가 병원 정보에 반영됩니다.";

        }


        if (requestType === "TEMP_CLOSE") {

            return hospitalName
                + "의 예약 휴업 요청을 승인하시겠습니까?\n\n"
                + "승인하면 설정된 시작·종료 시간에 맞춰 "
                + "병원 상태가 자동 변경됩니다.";

        }


        if (requestType === "CLOSE") {

            return hospitalName
                + "의 폐업 요청을 승인하시겠습니까?\n\n"
                + "승인하면 일반 병원 목록에서 제외되고 "
                + "폐업 관리 대상으로 이동합니다.";

        }


        return hospitalName
            + "의 요청을 승인하시겠습니까?";

    }


    /*
     * 중복 클릭 방지
     */
    function disableSubmitButton(
        form,
        processingText
    ) {

        const submitButton =
            form.querySelector(
                "button[type='submit']"
            );


        if (submitButton === null) {
            return;
        }


        submitButton.disabled = true;
        submitButton.textContent = processingText;

    }

});