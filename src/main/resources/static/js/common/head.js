document.addEventListener(
    "DOMContentLoaded",
    function () {

        initOwnerRequestNotification();
    }
);


/* ========================================
   병원장 신청 승인·반려 알림
======================================== */

function initOwnerRequestNotification() {

    /*
        head.html에 있는 빨간 알림 숫자 요소
    */
    const notificationBadge =
        document.getElementById(
            "ownerStatusUnreadBadge"
        );


    /*
        비로그인 또는 관리자 화면에서는
        알림 요소가 없으므로 종료
    */
    if (!notificationBadge) {

        return;
    }


    /*
        확인하지 않은 승인·반려 결과 개수 조회
    */
    fetch(
        "/owner/unread-count",
        {
            method: "GET",

            credentials: "same-origin",

            headers: {
                "Accept": "application/json"
            }
        }
    )
        .then(
            function (response) {

                if (!response.ok) {

                    throw new Error(
                        "병원장 신청 알림 조회에 실패했습니다."
                    );
                }

                return response.json();
            }
        )
        .then(
            function (data) {

                const unreadCount =
                    Number(
                        data.count || 0
                    );


                /*
                    읽지 않은 결과가 없으면
                    알림 숫자를 숨긴다.
                */
                if (unreadCount <= 0) {

                    notificationBadge.textContent =
                        "0";

                    notificationBadge.hidden =
                        true;

                    return;
                }


                /*
                    100개 이상이면 99+ 표시
                */
                notificationBadge.textContent =
                    unreadCount > 99
                        ? "99+"
                        : String(
                            unreadCount
                        );


                notificationBadge.hidden =
                    false;
            }
        )
        .catch(
            function (error) {

                /*
                    알림 조회 실패 시
                    알림 숫자만 숨긴다.
                */
                notificationBadge.hidden =
                    true;

                console.error(
                    error
                );
            }
        );
}