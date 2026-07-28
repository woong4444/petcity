document.addEventListener("DOMContentLoaded", function () {
    renderGlobalRecentHospitals();
});


function renderGlobalRecentHospitals() {
    const recentBox = document.getElementById('quickRecentList');
    if (!recentBox) return;

    const recents = JSON.parse(localStorage.getItem('petcity_recent') || '[]');

    if (recents.length === 0) {
        recentBox.innerHTML = '<li class="quick-recent-empty">최근 본 병원이<br>없습니다.</li>';
        return;
    }

    let html = '';
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