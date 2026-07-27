/*
    품종·종류 필터 선택 시 페이지를 새로고침하지 않고
    게시글 목록, 페이지 번호, 선택 상태만 갱신합니다.
*/

document.addEventListener("click", async function (event) {

    const animalChip = event.target.closest(
        ".animal-filter-area .animal-chip"
    );

    /*
        품종·종류 버튼이 아닌 경우 처리하지 않습니다.
    */
    if (animalChip === null) {
        return;
    }

    /*
        Ctrl / Shift / 마우스 휠 클릭처럼
        새 탭을 열려는 기본 브라우저 동작은 유지합니다.
    */
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

    const requestUrl = animalChip.href;

    /*
        연속 클릭 방지 및 로딩 표현
    */
    if (document.body.classList.contains("board-filter-loading")) {
        return;
    }

    document.body.classList.add("board-filter-loading");

    try {
        const response = await fetch(requestUrl, {
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        });

        if (!response.ok) {
            throw new Error("게시글 목록을 불러오지 못했습니다.");
        }

        const responseHtml = await response.text();

        /*
            서버가 반환한 전체 목록 HTML을 임시 문서로 읽습니다.
        */
        const responseDocument = new DOMParser()
            .parseFromString(responseHtml, "text/html");

        replaceSection(
            ".animal-filter-area",
            responseDocument
        );

        replaceSection(
            ".board-table-area",
            responseDocument
        );

        replaceSection(
            ".board-bottom",
            responseDocument
        );

        replaceSection(
            ".board-pagination",
            responseDocument
        );

        /*
            주소만 현재 선택한 품종 주소로 변경합니다.
            화면은 이동하거나 맨 위로 가지 않습니다.
        */
        window.history.pushState({}, "", requestUrl);

    } catch (error) {
        /*
            AJAX 요청에 문제가 있으면 일반 페이지 이동으로 처리합니다.
        */
        window.location.href = requestUrl;

    } finally {
        document.body.classList.remove("board-filter-loading");
    }

});


/*
    현재 화면의 특정 영역을
    서버에서 새로 받아온 같은 영역으로 교체합니다.
*/
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