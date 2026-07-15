window.addEventListener("pageshow", function (event) {

    /*
        게시글 상세에서 브라우저 뒤로가기로
        목록 페이지에 돌아왔는지 확인
    */
    const navigationEntries =
        performance.getEntriesByType("navigation");

    const isBackForward =
        navigationEntries.length > 0
        && navigationEntries[0].type === "back_forward";

    /*
        브라우저가 저장해 둔 예전 목록 화면을 보여준 경우
        서버에서 목록을 다시 조회해서 조회수를 갱신
    */
    if (event.persisted || isBackForward) {
        window.location.reload();
    }
});