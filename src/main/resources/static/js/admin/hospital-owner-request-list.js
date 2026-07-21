document.addEventListener("DOMContentLoaded", function () {

    /*
        HTML의 실제 form ID와 정확하게 맞춰야 한다.

        HTML:
        id="processedRequestDeleteForm"
    */
    const deleteForm =
        document.querySelector(
            "#processedRequestDeleteForm"
        );

    const selectAllCheckbox =
        document.querySelector(
            "#selectAllProcessedRequests"
        );

    const deleteButton =
        document.querySelector(
            "#processedDeleteButton"
        );

    const clearSelectionButton =
        document.querySelector(
            "#clearSelectedRequests"
        );

    const selectedRequestCount =
        document.querySelector(
            "#selectedRequestCount"
        );

    /*
        PENDING은 disabled 상태이므로 제외한다.

        APPROVED와 REJECTED만 선택 대상이다.
    */
    const requestCheckboxes =
        document.querySelectorAll(
            ".processed-request-checkbox:not(:disabled)"
        );


    /*
        필요한 HTML 요소가 없으면 실행 종료
    */
    if (
        deleteForm === null ||
        selectAllCheckbox === null ||
        deleteButton === null ||
        clearSelectionButton === null ||
        selectedRequestCount === null
    ) {
        return;
    }


    /*
        삭제 가능한 신청이 하나도 없는 경우
    */
    if (requestCheckboxes.length === 0) {

        selectAllCheckbox.disabled = true;
        deleteButton.disabled = true;
        clearSelectionButton.disabled = true;

        selectedRequestCount.textContent = "0";

        return;
    }


    /* ========================================
       현재 페이지 전체 선택
    ======================================== */

    selectAllCheckbox.addEventListener(
        "change",
        function () {

            requestCheckboxes.forEach(
                function (checkbox) {

                    checkbox.checked =
                        selectAllCheckbox.checked;
                }
            );

            selectAllCheckbox.indeterminate = false;

            updateSelectionState();
        }
    );


    /* ========================================
       개별 체크박스 선택
    ======================================== */

    requestCheckboxes.forEach(
        function (checkbox) {

            checkbox.addEventListener(
                "change",
                function () {

                    updateSelectionState();
                }
            );
        }
    );


    /* ========================================
       선택 초기화
    ======================================== */

    clearSelectionButton.addEventListener(
        "click",
        function () {

            requestCheckboxes.forEach(
                function (checkbox) {

                    checkbox.checked = false;
                }
            );

            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;

            updateSelectionState();
        }
    );


    /* ========================================
       삭제 폼 제출
    ======================================== */

    deleteForm.addEventListener(
        "submit",
        function (event) {

            const checkedCheckboxes =
                getCheckedCheckboxes();


            /*
                선택된 신청이 없으면 폼 제출 중단
            */
            if (checkedCheckboxes.length === 0) {

                event.preventDefault();

                return;
            }


            const requestIds =
                Array.from(checkedCheckboxes)
                    .map(function (checkbox) {

                        return checkbox.value;
                    })
                    .join(", ");


            const confirmed =
                window.confirm(
                    checkedCheckboxes.length +
                    "개의 처리 완료 신청을 삭제하시겠습니까?\n\n" +

                    "선택 신청 번호: " +
                    requestIds +
                    "\n\n" +

                    "신청서와 신청 단계의 동물, 서비스, " +
                    "진료과목 연결 데이터가 삭제됩니다.\n" +

                    "승인으로 생성된 실제 병원 데이터는 유지됩니다.\n\n" +

                    "삭제한 신청 기록은 복구할 수 없습니다."
                );


            /*
                취소를 누르면 제출 중단
            */
            if (!confirmed) {

                event.preventDefault();

                return;
            }


            /*
                중복 클릭 방지
            */
            deleteButton.disabled = true;
            clearSelectionButton.disabled = true;

            deleteButton.textContent = "삭제 중...";
        }
    );


    /* ========================================
       선택 상태 전체 갱신
    ======================================== */

    function updateSelectionState() {

        const checkedCount =
            getCheckedCheckboxes().length;


        /*
            선택 개수 표시
        */
        selectedRequestCount.textContent =
            String(checkedCount);


        /*
            모두 선택한 경우
        */
        selectAllCheckbox.checked =
            checkedCount === requestCheckboxes.length;


        /*
            일부만 선택한 경우
            전체 선택 체크박스에 - 표시
        */
        selectAllCheckbox.indeterminate =
            checkedCount > 0 &&
            checkedCount < requestCheckboxes.length;


        /*
            선택 항목이 없으면 버튼 비활성화
        */
        deleteButton.disabled =
            checkedCount === 0;

        clearSelectionButton.disabled =
            checkedCount === 0;


        /*
            삭제 버튼 문구
        */
        if (checkedCount === 0) {

            deleteButton.textContent =
                "선택 신청 삭제";

            return;
        }

        deleteButton.textContent =
            "선택 신청 삭제 (" +
            checkedCount +
            ")";
    }


    /* ========================================
       현재 선택된 체크박스 조회
    ======================================== */

    function getCheckedCheckboxes() {

        return document.querySelectorAll(
            ".processed-request-checkbox:not(:disabled):checked"
        );
    }


    /*
        페이지 최초 실행 상태
    */
    updateSelectionState();

});