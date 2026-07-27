/*
    펫도감 반려동물·세부 품종 필터를 선택해도
    화면 위치를 유지한 채 카드 목록만 갱신합니다.
*/

document.addEventListener("click", async function (event) {

    const filterLink = event.target.closest(
        ".info-animal-filter .animal-chip, " +
        ".info-animal-filter .animal-reset-link"
    );

    if (filterLink === null) {
        return;
    }

    if (
        event.button !== 0
        || event.ctrlKey
        || event.metaKey
        || event.shiftKey
        || event.altKey
    ) {
        return;
    }

    event.preventDefault();

    if (document.body.classList.contains("info-filter-loading")) {
        return;
    }

    document.body.classList.add("info-filter-loading");

    try {
        const response = await fetch(filterLink.href, {
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        });

        if (!response.ok) {
            throw new Error("펫도감 목록을 불러오지 못했습니다.");
        }

        const responseHtml = await response.text();

        const responseDocument = new DOMParser()
            .parseFromString(responseHtml, "text/html");

        replaceSection(
            ".info-animal-filter",
            responseDocument
        );

        replaceSection(
            ".info-card-list",
            responseDocument
        );

        syncPagination(responseDocument);

        window.history.pushState({}, "", filterLink.href);

    } catch (error) {
        // AJAX 처리 실패 시 기존 방식으로 이동
        window.location.href = filterLink.href;

    } finally {
        document.body.classList.remove("info-filter-loading");
    }

});


function replaceSection(selector, responseDocument) {
    const currentSection = document.querySelector(selector);
    const newSection = responseDocument.querySelector(selector);

    if (currentSection === null || newSection === null) {
        return;
    }

    currentSection.replaceWith(
        document.importNode(newSection, true)
    );
}


function syncPagination(responseDocument) {
    const currentPagination = document.querySelector(".board-pagination");
    const newPagination = responseDocument.querySelector(".board-pagination");

    if (currentPagination !== null && newPagination !== null) {
        currentPagination.replaceWith(
            document.importNode(newPagination, true)
        );
        return;
    }

    if (currentPagination !== null && newPagination === null) {
        currentPagination.remove();
        return;
    }

    if (currentPagination === null && newPagination !== null) {
        const anchor = document.querySelector(".info-bottom")
            || document.querySelector(".info-card-list");

        if (anchor !== null) {
            anchor.insertAdjacentElement(
                "afterend",
                document.importNode(newPagination, true)
            );
        }
    }
}