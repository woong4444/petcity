document.addEventListener(
    "DOMContentLoaded",
    function () {
        initQuickMenuPosition();
        renderGlobalRecentHospitals();
    }
);


function initQuickMenuPosition() {
    const quickMenu = document.querySelector(
        ".quick-menu"
    );

    const siteHeader = document.querySelector(
        ".site-header"
    );

    if (!quickMenu || !siteHeader) {
        return;
    }

    let ticking = false;

    function updateQuickMenuPosition() {
        const headerBottom =
            siteHeader.getBoundingClientRect().bottom;

        const minimumTop = 16;
        const headerGap = 16;

        const quickMenuTop = Math.max(
            minimumTop,
            headerBottom + headerGap
        );

        quickMenu.style.top =
            quickMenuTop + "px";

        ticking = false;
    }

    function requestQuickMenuPositionUpdate() {
        if (ticking) {
            return;
        }

        ticking = true;

        window.requestAnimationFrame(
            updateQuickMenuPosition
        );
    }

    updateQuickMenuPosition();

    window.addEventListener(
        "scroll",
        requestQuickMenuPositionUpdate,
        {
            passive: true
        }
    );

    window.addEventListener(
        "resize",
        requestQuickMenuPositionUpdate
    );
}


function renderGlobalRecentHospitals() {
    const recentBox = document.getElementById(
        "quickRecentList"
    );

    if (!recentBox) {
        return;
    }

    const memberIdElem = document.getElementById('globalLoginMemberId');
    const memberId = memberIdElem ? memberIdElem.value : 'guest';
    const storageKey = 'petcity_recent_' + memberId;

    let recents = [];

    try {
        recents = JSON.parse(
            localStorage.getItem(storageKey) || "[]"
        );
    } catch (error) {
        console.error(
            "최근 본 병원 정보를 불러오지 못했습니다.",
            error
        );

        localStorage.removeItem(storageKey);
    }

    if (!Array.isArray(recents)
        || recents.length === 0) {
        recentBox.innerHTML = `
            <li class="quick-recent-empty">
                최근 본 병원이<br>
                없습니다.
            </li>
        `;

        return;
    }

    let html = "";

    recents.slice(0, 3).forEach(
        function (hospital) {
            let imageHtml;

            if (hospital.img
                && hospital.img !== "null") {
                imageHtml = `
                        <img src="${hospital.img}"
                             alt="병원">
                    `;
            } else {
                imageHtml = `
                        <div class="quick-recent-no-img">
                            이미지 없음
                        </div>
                    `;
            }

            html += `
                    <li>
                        <a href="/hospital/view?hospitalId=${hospital.id}"
                           class="quick-recent-item">
                            ${imageHtml}

                            <span>
                                ${hospital.name}
                            </span>
                        </a>
                    </li>
                `;
        }
    );

    recentBox.innerHTML = html;
}