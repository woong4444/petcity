document.addEventListener("DOMContentLoaded", function () {
  const STORAGE_KEY = "selectedMemberIds";

  const selectAllMembers = document.querySelector("#selectAllMembers");
  const memberCheckboxes = Array.from(
    document.querySelectorAll(".member-checkbox"),
  );

  const selectedMemberCount = document.querySelector("#selectedMemberCount");
  const clearSelectedMembers = document.querySelector("#clearSelectedMembers");
  const allSelectionNotice = document.querySelector("#allSelectionNotice");
  const selectEveryMember = document.querySelector("#selectEveryMember");

  const selectedMemberIds = loadSelectedMemberIds();

  restoreCheckboxState();
  updateSelectAllState();
  updateSelectedCount();
  updateAllSelectionNotice();

  if (selectAllMembers !== null) {
    selectAllMembers.addEventListener("change", function () {
      memberCheckboxes.forEach(function (checkbox) {
        checkbox.checked = selectAllMembers.checked;
        if (checkbox.checked) {
          selectedMemberIds.add(checkbox.value);
        } else {
          selectedMemberIds.delete(checkbox.value);
        }
      });

      saveSelectedMemberIds();
      updateSelectAllState();
      updateSelectedCount();
      updateAllSelectionNotice();
    });
  }

  memberCheckboxes.forEach(function (checkbox) {
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
    } catch (error) {
      sessionStorage.removeItem(STORAGE_KEY);
      return new Set();
    }
  }

  function saveSelectedMemberIds() {
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

    const isCurrentPageAllSelected =
      memberCheckboxes.length > 0 && checkedCount === memberCheckboxes.length;

    selectAllMembers.checked = isCurrentPageAllSelected;
    selectAllMembers.indeterminate = false;
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

    const isCurrentPageAllSelected =
      memberCheckboxes.length > 0 && checkedCount === memberCheckboxes.length;
    allSelectionNotice.hidden = !isCurrentPageAllSelected;
  }

  if (selectEveryMember !== null) {
    selectEveryMember.addEventListener("click", async function () {
      selectEveryMember.disabled = true;

      try {
        const queryParams = new URLSearchParams();
        queryParams.set("keyword", selectEveryMember.dataset.keyword || "");
        queryParams.set("role", selectEveryMember.dataset.role || "");
        queryParams.set("status", selectEveryMember.dataset.status || "");
        queryParams.set("memberStatus", selectEveryMember.dataset.memberStatus  || "");


        const response = await fetch("/admin/members/all-ids" + queryParams.toString(), {
          method: "GET",
          headers: { Accept: "application/json" },
        });

        if (!response.ok) {
          throw new Error("전체 회원 번호 조회 실패");
        }
        const memberIds = await response.json();
        selectedMemberIds.clear();
        memberIds.forEach(function (memberId) {
          selectedMemberIds.add(String(memberId));
        });
        saveSelectedMemberIds();

        restoreCheckboxState();
        updateSelectAllState();
        updateSelectedCount();
        updateAllSelectionNotice();
      } catch (error) {
        console.error(error);
        alert("전체 회원을 선택하는 중 오류가 발생했습니다.");
      } finally {
        selectEveryMember.disabled = false;
      }
    });
  }
});
