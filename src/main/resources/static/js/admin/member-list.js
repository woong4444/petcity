document.addEventListener("DOMContentLoaded", function () {
    const STORAGE_KEY = "selectedMemberIds";

    const selectAllMembers =
        document.querySelector("#selectAllMembers");

    const memberCheckboxes = Array.from(
        document.querySelectorAll(".member-checkbox"),
    );

    const selectedMemberCount =
        document.querySelector("#selectedMemberCount");

    const clearSelectedMembers =
        document.querySelector("#clearSelectedMembers");

    const allSelectionNotice =
        document.querySelector("#allSelectionNotice");

    const selectEveryMember =
        document.querySelector("#selectEveryMember");

    const openDeleteMemberModal =
        document.querySelector("#openDeleteMemberModal");

    const memberDeleteModal =
        document.querySelector("#memberDeleteModal");

    const deleteTargetCount =
        document.querySelector("#deleteTargetCount");

    const deleteReason =
        document.querySelector("#deleteReason");

    const deleteReasonLength =
        document.querySelector("#deleteReasonLength");

    const confirmDeleteMembers =
        document.querySelector("#confirmDeleteMembers");

    const closeDeleteModalButtons = Array.from(
        document.querySelectorAll(
            "[data-close-delete-modal]",
        ),
    );

    const selectedMemberIds =
        loadSelectedMemberIds();


    restoreCheckboxState();
    updateSelectAllState();
    updateSelectedCount();
    updateAllSelectionNotice();
    updateDeleteButtonState();


    if (selectAllMembers !== null) {
        selectAllMembers.addEventListener(
            "change",
            function () {
                memberCheckboxes.forEach(
                    function (checkbox) {
                        checkbox.checked =
                            selectAllMembers.checked;

                        if (checkbox.checked) {
                            selectedMemberIds.add(
                                checkbox.value,
                            );
                        } else {
                            selectedMemberIds.delete(
                                checkbox.value,
                            );
                        }
                    },
                );

                saveSelectedMemberIds();
                updateSelectionScreen();
            },
        );
    }


    memberCheckboxes.forEach(
        function (checkbox) {
            checkbox.addEventListener(
                "change",
                function () {
                    if (checkbox.checked) {
                        selectedMemberIds.add(
                            checkbox.value,
                        );
                    } else {
                        selectedMemberIds.delete(
                            checkbox.value,
                        );
                    }

                    saveSelectedMemberIds();
                    updateSelectionScreen();
                },
            );
        },
    );


    if (clearSelectedMembers !== null) {
        clearSelectedMembers.addEventListener(
            "click",
            function () {
                selectedMemberIds.clear();

                sessionStorage.removeItem(
                    STORAGE_KEY,
                );

                memberCheckboxes.forEach(
                    function (checkbox) {
                        checkbox.checked = false;
                    },
                );

                updateSelectionScreen();
            },
        );
    }


    if (selectEveryMember !== null) {
        selectEveryMember.addEventListener(
            "click",
            async function () {
                selectEveryMember.disabled = true;

                try {
                    const queryParams =
                        new URLSearchParams();

                    queryParams.set(
                        "keyword",
                        selectEveryMember.dataset.keyword
                        || "",
                    );

                    queryParams.set(
                        "role",
                        selectEveryMember.dataset.role
                        || "",
                    );

                    queryParams.set(
                        "status",
                        selectEveryMember.dataset.status
                        || "",
                    );

                    queryParams.set(
                        "memberStatus",
                        selectEveryMember.dataset.memberStatus
                        || "",
                    );


                    const response = await fetch(
                        "/admin/members/all-ids?"
                        + queryParams.toString(),
                        {
                            method: "GET",

                            headers: {
                                Accept: "application/json",
                            },
                        },
                    );


                    if (!response.ok) {
                        throw new Error(
                            "전체 회원 번호 조회에 실패했습니다.",
                        );
                    }


                    const memberIds =
                        await response.json();


                    selectedMemberIds.clear();


                    memberIds.forEach(
                        function (memberId) {
                            selectedMemberIds.add(
                                String(memberId),
                            );
                        },
                    );


                    saveSelectedMemberIds();
                    restoreCheckboxState();
                    updateSelectionScreen();

                } catch (error) {

                    console.error(error);

                    alert(
                        error.message
                        || "전체 회원을 선택하는 중 오류가 발생했습니다.",
                    );

                } finally {

                    selectEveryMember.disabled = false;
                }
            },
        );
    }


    if (openDeleteMemberModal !== null) {
        openDeleteMemberModal.addEventListener(
            "click",
            function () {
                if (selectedMemberIds.size === 0) {
                    return;
                }

                openMemberDeleteModal();
            },
        );
    }


    closeDeleteModalButtons.forEach(
        function (button) {
            button.addEventListener(
                "click",
                function () {
                    closeMemberDeleteModal();
                },
            );
        },
    );


    if (deleteReason !== null) {
        deleteReason.addEventListener(
            "input",
            function () {
                if (deleteReasonLength !== null) {
                    deleteReasonLength.textContent =
                        deleteReason.value.length;
                }

                updateDeleteConfirmButtonState();
            },
        );
    }


    if (confirmDeleteMembers !== null) {
        confirmDeleteMembers.addEventListener(
            "click",
            async function () {
                if (deleteReason === null) {
                    return;
                }


                const reason =
                    deleteReason.value.trim();


                if (reason.length === 0) {
                    deleteReason.focus();

                    return;
                }


                if (selectedMemberIds.size === 0) {
                    alert(
                        "삭제할 회원을 선택해 주세요.",
                    );

                    closeMemberDeleteModal();

                    return;
                }


                const deleteRequestData = {
                    memberIds:
                        Array.from(selectedMemberIds).map(
                            function (memberId) {
                                return Number(memberId);
                            },
                        ),

                    deleteReason: reason,
                };


                confirmDeleteMembers.disabled = true;


                try {
                    const response = await fetch(
                        "/admin/members/delete",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type": "application/json",
                                Accept: "application/json",
                            },

                            body: JSON.stringify(
                                deleteRequestData,
                            ),
                        },
                    );


                    const result =
                        await response.json();


                    if (!response.ok) {
                        throw new Error(
                            result.message
                            || "회원 삭제에 실패했습니다.",
                        );
                    }


                    sessionStorage.removeItem(
                        STORAGE_KEY,
                    );


                    alert(
                        result.message,
                    );


                    window.location.reload();

                } catch (error) {

                    alert(
                        error.message
                        || "회원 삭제 중 오류가 발생했습니다.",
                    );


                    updateDeleteConfirmButtonState();
                }
            },
        );
    }


    document.addEventListener(
        "keydown",
        function (event) {
            if (event.key !== "Escape") {
                return;
            }


            if (
                memberDeleteModal !== null
                && !memberDeleteModal.hidden
            ) {

                closeMemberDeleteModal();
            }
        },
    );


    function loadSelectedMemberIds() {
        const savedValue =
            sessionStorage.getItem(
                STORAGE_KEY,
            );


        if (savedValue === null) {
            return new Set();
        }


        try {
            const memberIds =
                JSON.parse(savedValue);


            if (!Array.isArray(memberIds)) {
                throw new Error(
                    "선택 회원 데이터 형식 오류",
                );
            }


            return new Set(
                memberIds.map(String),
            );

        } catch (error) {

            sessionStorage.removeItem(
                STORAGE_KEY,
            );


            return new Set();
        }
    }


    function saveSelectedMemberIds() {
        const memberIds =
            Array.from(selectedMemberIds);


        sessionStorage.setItem(
            STORAGE_KEY,
            JSON.stringify(memberIds),
        );
    }


    function restoreCheckboxState() {
        memberCheckboxes.forEach(
            function (checkbox) {
                checkbox.checked =
                    selectedMemberIds.has(
                        checkbox.value,
                    );
            },
        );
    }


    function updateSelectionScreen() {
        updateSelectAllState();
        updateSelectedCount();
        updateAllSelectionNotice();
        updateDeleteButtonState();
    }


    function updateSelectAllState() {
        if (selectAllMembers === null) {
            return;
        }


        const checkedCount =
            memberCheckboxes.filter(
                function (checkbox) {
                    return checkbox.checked;
                },
            ).length;


        const allSelected =
            memberCheckboxes.length > 0
            && checkedCount
            === memberCheckboxes.length;


        selectAllMembers.checked =
            allSelected;


        selectAllMembers.indeterminate =
            checkedCount > 0
            && checkedCount
            < memberCheckboxes.length;
    }


    function updateSelectedCount() {
        if (selectedMemberCount === null) {
            return;
        }


        selectedMemberCount.textContent =
            selectedMemberIds.size;
    }


    function updateAllSelectionNotice() {
        if (allSelectionNotice === null) {
            return;
        }


        const checkedCount =
            memberCheckboxes.filter(
                function (checkbox) {
                    return checkbox.checked;
                },
            ).length;


        const allSelected =
            memberCheckboxes.length > 0
            && checkedCount
            === memberCheckboxes.length;


        allSelectionNotice.hidden =
            !allSelected;
    }


    function updateDeleteButtonState() {
        if (openDeleteMemberModal === null) {
            return;
        }


        openDeleteMemberModal.disabled =
            selectedMemberIds.size === 0;
    }


    function openMemberDeleteModal() {
        if (memberDeleteModal === null) {
            return;
        }


        if (deleteTargetCount !== null) {
            deleteTargetCount.textContent =
                selectedMemberIds.size;
        }


        if (deleteReason !== null) {
            deleteReason.value = "";
        }


        if (deleteReasonLength !== null) {
            deleteReasonLength.textContent =
                "0";
        }


        updateDeleteConfirmButtonState();


        memberDeleteModal.hidden = false;

        memberDeleteModal.setAttribute(
            "aria-hidden",
            "false",
        );


        document.body.classList.add(
            "modal-open",
        );


        if (deleteReason !== null) {
            window.setTimeout(
                function () {
                    deleteReason.focus();
                },
                0,
            );
        }
    }


    function closeMemberDeleteModal() {
        if (memberDeleteModal === null) {
            return;
        }


        memberDeleteModal.hidden = true;

        memberDeleteModal.setAttribute(
            "aria-hidden",
            "true",
        );


        document.body.classList.remove(
            "modal-open",
        );


        if (deleteReason !== null) {
            deleteReason.value = "";
        }


        if (deleteReasonLength !== null) {
            deleteReasonLength.textContent =
                "0";
        }


        updateDeleteConfirmButtonState();
    }


    function updateDeleteConfirmButtonState() {
        if (
            confirmDeleteMembers === null
            || deleteReason === null
        ) {

            return;
        }


        confirmDeleteMembers.disabled =
            deleteReason.value
                .trim()
                .length === 0;
    }
});