document.addEventListener("DOMContentLoaded", function () {
    // 병원장 신청 알림
    initOwnerRequestNotification();
});

/* ========================================
   병원장 신청 승인·반려 알림
======================================== */
function initOwnerRequestNotification() {
    const notificationBadge = document.getElementById("ownerStatusUnreadBadge");

    if (!notificationBadge) {
        return;
    }

    fetch("/owner/unread-count", {
        method: "GET",
        credentials: "same-origin",
        headers: {"Accept": "application/json"}
    })
        .then(function (response) {
            if (!response.ok) throw new Error("병원장 신청 알림 조회에 실패했습니다.");
            return response.json();
        })
        .then(function (data) {
            const unreadCount = Number(data.count || 0);
            if (unreadCount <= 0) {
                notificationBadge.textContent = "0";
                notificationBadge.hidden = true;
                return;
            }
            notificationBadge.textContent = unreadCount > 99 ? "99+" : String(unreadCount);
            notificationBadge.hidden = false;
        })
        .catch(function (error) {
            notificationBadge.hidden = true;
            console.error(error);
        });
}