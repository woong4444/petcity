document.addEventListener("DOMContentLoaded", function () {

    const adminLayout = document.querySelector(".admin-layout");
    const sidebarToggle = document.querySelector(".sidebar-toggle");

    if (adminLayout === null || sidebarToggle === null) {
        return;
    }
    const savedSidebarState = localStorage.getItem("petcityAdminSidebar");
    if (savedSidebarState === "collapsed") {
        adminLayout.classList.add("is-sidebar-collapsed");
        updateButtonState(true);
    }

    sidebarToggle.addEventListener("click", function () {
        adminLayout.classList.toggle("is-sidebar-collapsed");

        const isCollapsed = adminLayout.classList.contains("is-sidebar-collapsed");

        if (isCollapsed) {
            localStorage.setItem("petcityAdminSidebar", "collapsed");
        } else {
            localStorage.setItem("petcityAdminSidebar", "expanded");
        }
        updateButtonState(isCollapsed);
    });

    function updateButtonState(isCollapsed) {
        sidebarToggle.setAttribute("aria-expanded", String(!isCollapsed));

        sidebarToggle.setAttribute("aria-label", isCollapsed ? "사이드바 펼치기" : "사이드바 접기");

    }


});