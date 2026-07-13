document.addEventListener("DOMContentLoaded", function () {
    const STORAGE_KEY="selectedMemberIds"

    const selectAllMembers = document.querySelector("#selectAllMembers");
    const memberCheckboxes = Array.from(document.querySelectorAll(".member-checkbox"));

    const selectedMemberCount = document.querySelector("#selectedMemberCount");
    const clearSelectedMembers = document.querySelector("#clearSelectedMembers");
    const allSelectionNotice = document.querySelector("#allSelectionNotice");

    const selectedMemberIds = loadSelectedMemberIds();

    restoreCheckboxState();
    updateSelectAllState();
    updateSelectedCount();
    updateAllSelectionNotice();

    if (selectAllMembers !== null) {
        selectAllMembers.addEventListener("change", function () {
            memberCheckboxes.forEach(function (checkbox) {
                checkbox.checked=selectAllMembers.checked;
                if (checkbox.checked){
                    selectedMemberIds.add(checkbox.value);
                }else{
                    selectedMemberIds.delete(checkbox.value);
                }
            });

            saveSelectedMemberIds();
            updateSelectAllState();
            updateSelectedCount();
            updateAllSelectionNotice();

            }
        );
    }

    memberCheckboxes.forEach(function(checkbox) {

        checkbox.addEventListener("change", function () {
            if (checkbox.checked) {
                selectedMemberIds.add(checkbox.value);
            } else {
                selectedMemberIds.delete(checkbox.value);
            }


        saveSelectedMemberIds();
        updateSelectAllState();
        updateSelectedCount();
            updateAllSelectionNotice();

        });
    });

    if (clearSelectedMembers !== null) {
        clearSelectedMembers.addEventListener("click", function () {
            selectedMemberIds.clear();
            sessionStorage.removeItem(STORAGE_KEY);

            memberCheckboxes.forEach(function (checkbox) {
                checkbox.checked = false;
            });
            updateSelectAllState();
            updateSelectedCount();
            updateAllSelectionNotice();

        });
    }

    function loadSelectedMemberIds() {
        const savedValue = sessionStorage.getItem(STORAGE_KEY);
        if (savedValue === null) {
            return new Set();
        }

        try {
            const memberIds = JSON.parse(savedValue);
            return new Set(memberIds.map(String));
        } catch (error){
            sessionStorage.removeItem(STORAGE_KEY);
            return new Set();
        }
    }

    function saveSelectedMemberIds(){
        const memberIds = Array.from(selectedMemberIds);
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(memberIds));
    }

    function restoreCheckboxState() {
        memberCheckboxes.forEach(function (checkbox) {
            checkbox.checked = selectedMemberIds.has(checkbox.value);
        });
    }

    function updateSelectAllState() {
        if (selectAllMembers === null) {
            return;
        }
        const checkedCount = memberCheckboxes.filter(function (checkbox) {
            return checkbox.checked;
        }).length;

        selectAllMembers.checked = memberCheckboxes.length > 0 && checkedCount === memberCheckboxes.length;

        selectAllMembers.indeterminate = checkedCount > 0 && checkedCount < memberCheckboxes.length;
    }

    function updateSelectedCount() {
        if (selectedMemberCount === null) {
            return;
        }
        selectedMemberCount.textContent = selectedMemberIds.size;
    }

    function updateAllSelectionNotice() {
        if (allSelectionNotice === null) {
            return;
        }
        const checkedCount = memberCheckboxes.filter(function (checkbox) {
            return checkbox.checked;
        }).length;

        const isCurrentPageAllSelected = memberCheckboxes.length > 0 && checkedCount === memberCheckboxes.length;
        allSelectionNotice.hidden = !isCurrentPageAllSelected;
    }
});