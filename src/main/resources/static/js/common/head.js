document.addEventListener("DOMContentLoaded", function () {
    // 병원장 신청 알림
    initOwnerRequestNotification();

    // 🌟 모든 페이지에서 '최근 본 병원' 불러오기
    renderGlobalRecentHospitals();
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

/* ========================================
   전역 퀵메뉴 '최근 본 병원' 렌더링 기능
======================================== */
function renderGlobalRecentHospitals() {
    const recentBox = document.getElementById('quickRecentList');
    if (!recentBox) return;

    // 로컬스토리지(브라우저 저장소)에서 데이터 꺼내기
    const recents = JSON.parse(localStorage.getItem('petcity_recent') || '[]');

    if (recents.length === 0) {
        recentBox.innerHTML = '<li class="quick-recent-empty">최근 본 병원이<br>없습니다.</li>';
        return;
    }

    let html = '';
    // 🌟 배열이 혹시 꼬이더라도 무조건 '최대 3개'까지만 반복해서 그리도록 안전장치(slice) 추가!
    recents.slice(0, 3).forEach(h => {
        let imgHtml = (h.img && h.img !== 'null')
            ? `<img src="${h.img}" alt="병원">`
            : `<div class="quick-recent-no-img">이미지 없음</div>`;

        html += `<li>
            <a href="/hospital/view?hospitalId=${h.id}" class="quick-recent-item">
                ${imgHtml}
                <span>${h.name}</span>
            </a>
        </li>`;
    });
    recentBox.innerHTML = html;
}