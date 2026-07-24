document.addEventListener("DOMContentLoaded", function () {

    const sortButton = Array.from(document.querySelectorAll(".hospital-sort-button",),);

    const queryParams = new URLSearchParams(window.location.search,);
    const currentSortBy = queryParams.get("sortBy") || "hospitalId";

    const currentDirection = queryParams.get("direction") || "asc";

    sortButton.forEach(function (sortButton){
        sortButton.addEventListener("click",function() {
            const clickedSortBy = sortButton.dataset.sortBy;
            if (clickedSortBy === undefined || clickedSortBy === "") {
                return;
            }
            const nextDirection = getNextDirection(clickedSortBy, currentSortBy, currentDirection);
            queryParams.set("sortBy", clickedSortBy,);
            queryParams.set("direction", nextDirection,);

            window.location.href = "/admin/hospitals?" + queryParams.toString();
        },);
    },);


    function getNextDirection(clickedSortBy, currentSortBy, currentDirection) {
        if (clickedSortBy !== currentSortBy) {
            return "asc";
        }

        if (currentDirection === "asc") {
            return "desc";
        }
        return "asc";
    }

});