document.addEventListener(
    "DOMContentLoaded",
    function () {

        /* ========================================
           HTML 요소
        ======================================== */

        const page =
            document.getElementById(
                "adminChatPage"
            );

        if (page === null) {
            return;
        }


        const roomListElement =
            document.getElementById(
                "adminChatRoomList"
            );

        const refreshButton =
            document.getElementById(
                "adminChatRoomRefreshButton"
            );

        const emptyState =
            document.getElementById(
                "adminChatEmptyState"
            );

        const conversation =
            document.getElementById(
                "adminChatConversation"
            );

        const roomStatus =
            document.getElementById(
                "adminChatRoomStatus"
            );

        const roomTitle =
            document.getElementById(
                "adminChatRoomTitle"
            );

        const roomMeta =
            document.getElementById(
                "adminChatRoomMeta"
            );

        const closeRoomButton =
            document.getElementById(
                "adminChatCloseRoomButton"
            );

        const messageList =
            document.getElementById(
                "adminChatMessageList"
            );

        const replyForm =
            document.getElementById(
                "adminChatReplyForm"
            );

        const replyInput =
            document.getElementById(
                "adminChatReplyInput"
            );

        const replyLength =
            document.getElementById(
                "adminChatReplyLength"
            );

        const replySendButton =
            document.getElementById(
                "adminChatReplySendButton"
            );

        const replyNotice =
            document.getElementById(
                "adminChatReplyNotice"
            );

        const notificationButton =
            document.getElementById(
                "adminChatNotificationButton"
            );


        if (
            roomListElement === null
            || refreshButton === null
            || emptyState === null
            || conversation === null
            || roomStatus === null
            || roomTitle === null
            || roomMeta === null
            || closeRoomButton === null
            || messageList === null
            || replyForm === null
            || replyInput === null
            || replyLength === null
            || replySendButton === null
            || replyNotice === null
        ) {
            return;
        }


        /* ========================================
           초기 화면 값
        ======================================== */

        const initialView =
            page.dataset.chatView === "unread"
                ? "unread"
                : "all";

        const initialRoomUuid =
            page.dataset.selectedRoomUuid
            || "";


        /* ========================================
           알림 이미지 및 소리
        ======================================== */

        const notificationIconUrl =
            "/images/admin-letter-dog.png";

        const alertSound =
            new Audio(
                "/audio/admin-chat-alert.mp3"
            );

        alertSound.preload =
            "auto";

        alertSound.volume =
            0.7;


        /* ========================================
           채팅 상태
        ======================================== */

        let roomList = [];

        let selectedRoom = null;

        let stompClient = null;

        let stompConnectPromise = null;

        let roomSubscription = null;

        let adminTopicSubscription = null;

        let errorSubscription = null;

        let notificationSoundEnabled = false;


        const renderedMessageIds =
            new Set();

        const notifiedMessageIds =
            new Set();


        /* ========================================
           초기 실행
        ======================================== */

        initialize();


        async function initialize() {

            initializeNotificationButton();

            refreshButton.addEventListener(
                "click",
                function () {

                    loadRooms(true);
                }
            );

            closeRoomButton.addEventListener(
                "click",
                closeSelectedRoom
            );

            replyInput.addEventListener(
                "input",
                handleReplyInput
            );

            replyInput.addEventListener(
                "keydown",
                function (event) {

                    if (
                        event.key === "Enter"
                        && !event.shiftKey
                    ) {

                        event.preventDefault();

                        replyForm.requestSubmit();
                    }
                }
            );

            replyForm.addEventListener(
                "submit",
                sendAdminReply
            );


            try {

                await connectStomp();

            } catch (error) {

                console.error(
                    "관리자 WebSocket 연결 실패:",
                    error
                );

                replyNotice.textContent =
                    "실시간 채팅 서버 연결에 실패했습니다.";
            }


            await loadRooms(true);


            if (initialRoomUuid) {

                await selectRoom(
                    initialRoomUuid
                );
            }
        }


        window.addEventListener(
            "beforeunload",
            function () {

                if (
                    stompClient
                    && stompClient.active
                ) {

                    stompClient.deactivate();
                }
            }
        );


        /* ========================================
           브라우저 알림 설정
        ======================================== */

        function initializeNotificationButton() {

            if (notificationButton === null) {
                return;
            }


            if (!("Notification" in window)) {

                notificationButton.hidden =
                    true;

                return;
            }


            updateNotificationButton();


            notificationButton.addEventListener(
                "click",
                async function () {

                    if (
                        Notification.permission
                        === "default"
                    ) {

                        await Notification
                            .requestPermission();
                    }


                    if (
                        Notification.permission
                        === "granted"
                    ) {

                        await unlockNotificationSound();
                    }


                    updateNotificationButton();
                }
            );
        }


        function updateNotificationButton() {

            if (
                notificationButton === null
                || !("Notification" in window)
            ) {
                return;
            }


            if (
                Notification.permission
                === "denied"
            ) {

                notificationButton.textContent =
                    "알림 차단됨";

                notificationButton.disabled =
                    true;

                return;
            }


            if (
                Notification.permission
                === "granted"
                && notificationSoundEnabled
            ) {

                notificationButton.textContent =
                    "알림·소리 켜짐";

                notificationButton.disabled =
                    true;

                return;
            }


            notificationButton.textContent =
                "알림·소리 켜기";

            notificationButton.disabled =
                false;
        }


        async function unlockNotificationSound() {

            try {

                alertSound.muted =
                    true;

                alertSound.currentTime =
                    0;

                await alertSound.play();

                alertSound.pause();

                alertSound.currentTime =
                    0;

                alertSound.muted =
                    false;

                notificationSoundEnabled =
                    true;

            } catch (error) {

                console.warn(
                    "알림음 활성화 실패:",
                    error
                );

                alertSound.muted =
                    false;

                notificationSoundEnabled =
                    false;
            }
        }


        function playNotificationSound() {

            if (!notificationSoundEnabled) {
                return;
            }


            alertSound.pause();

            alertSound.currentTime =
                0;


            alertSound.play()
                .catch(
                    function (error) {

                        console.warn(
                            "알림음 재생 실패:",
                            error
                        );
                    }
                );
        }


        function showDesktopNotification(event) {

            const message =
                event.message;


            if (!message) {
                return;
            }


            const messageId =
                message.messageId
                    ? String(
                        message.messageId
                    )
                    : null;


            if (
                messageId
                && notifiedMessageIds.has(
                    messageId
                )
            ) {
                return;
            }


            if (messageId) {

                notifiedMessageIds.add(
                    messageId
                );
            }


            playNotificationSound();


            if (
                !("Notification" in window)
                || Notification.permission
                !== "granted"
            ) {
                return;
            }


            const senderName =
                message.senderNameSnapshot
                || "고객";


            const content =
                message.content
                || "새로운 상담 메시지가 도착했습니다.";


            const notification =
                new Notification(
                    "PetCity 새 상담 문의",
                    {
                        body:
                            senderName
                            + "\n"
                            + content,

                        icon:
                        notificationIconUrl,

                        badge:
                        notificationIconUrl,

                        image:
                        notificationIconUrl,

                        tag:
                            "petcity-chat-"
                            + event.roomUuid
                            + "-"
                            + (
                                messageId
                                || Date.now()
                            ),

                        renotify:
                            true,

                        silent:
                            true
                    }
                );


            notification.onclick =
                function () {

                    window.focus();

                    window.location.href =
                        "/admin/chat?roomUuid="
                        + encodeURIComponent(
                            event.roomUuid
                        );

                    notification.close();
                };
        }


        /* ========================================
           채팅방 목록 조회
        ======================================== */

        async function loadRooms(
            showLoading = true
        ) {

            if (showLoading) {

                renderRoomLoading();
            }


            try {

                const response =
                    await fetch(
                        "/admin/api/chat/rooms",
                        {
                            method: "GET",
                            credentials: "same-origin"
                        }
                    );


                if (!response.ok) {

                    throw await createApiError(
                        response
                    );
                }


                const responseRooms =
                    await response.json();


                roomList =
                    Array.isArray(
                        responseRooms
                    )
                        ? responseRooms
                        : [];


                refreshSelectedRoom();


                renderRooms();


            } catch (error) {

                console.error(
                    "관리자 채팅방 목록 조회 실패:",
                    error
                );


                if (showLoading) {

                    renderRoomError(
                        error.message
                        || "채팅방 목록을 불러오지 못했습니다."
                    );
                }
            }
        }


        function refreshSelectedRoom() {

            if (!selectedRoom) {
                return;
            }


            const refreshedRoom =
                roomList.find(
                    function (room) {

                        return room.roomUuid
                            === selectedRoom.roomUuid;
                    }
                );


            if (refreshedRoom) {

                selectedRoom =
                    refreshedRoom;

                updateConversationHeader();
            }
        }


        function getVisibleRooms() {

            if (initialView !== "unread") {

                return roomList;
            }


            return roomList.filter(
                function (room) {

                    return Number(
                        room.adminUnreadCount
                        || 0
                    ) > 0;
                }
            );
        }


        function renderRoomLoading() {

            roomListElement.replaceChildren();


            roomListElement.append(
                createPlaceholder(
                    "채팅방 목록을 불러오는 중입니다.",
                    "잠시만 기다려 주세요."
                )
            );
        }


        function renderRoomError(message) {

            roomListElement.replaceChildren();


            roomListElement.append(
                createPlaceholder(
                    "채팅방 목록 조회 실패",
                    message
                )
            );
        }


        function renderRooms() {

            roomListElement.replaceChildren();


            const visibleRooms =
                getVisibleRooms();


            if (visibleRooms.length === 0) {

                roomListElement.append(
                    createPlaceholder(
                        "표시할 채팅방이 없습니다.",
                        initialView === "unread"
                            ? "현재 안 읽은 채팅방이 없습니다."
                            : "아직 생성된 채팅방이 없습니다."
                    )
                );

                return;
            }


            visibleRooms.forEach(
                function (room) {

                    roomListElement.append(
                        createRoomItem(
                            room
                        )
                    );
                }
            );
        }


        function createRoomItem(room) {

            const button =
                document.createElement(
                    "button"
                );


            button.type =
                "button";

            button.className =
                "admin-chat-room-item";

            button.dataset.roomUuid =
                room.roomUuid;


            if (
                selectedRoom
                && selectedRoom.roomUuid
                === room.roomUuid
            ) {

                button.classList.add(
                    "active"
                );
            }


            const top =
                document.createElement(
                    "div"
                );

            top.className =
                "admin-chat-room-top";


            const name =
                document.createElement(
                    "strong"
                );

            name.className =
                "admin-chat-room-name";

            name.textContent =
                room.customerName
                || "이름 없는 고객";


            const badges =
                document.createElement(
                    "div"
                );

            badges.className =
                "admin-chat-room-badges";


            const typeBadge =
                document.createElement(
                    "span"
                );

            typeBadge.className =
                "admin-chat-room-type";

            typeBadge.textContent =
                room.visitorType === "GUEST"
                    ? "비회원"
                    : "회원";


            badges.append(
                typeBadge
            );


            const unreadCount =
                Number(
                    room.adminUnreadCount
                    || 0
                );


            if (unreadCount > 0) {

                const unreadBadge =
                    document.createElement(
                        "span"
                    );

                unreadBadge.className =
                    "admin-chat-room-unread";

                unreadBadge.textContent =
                    String(
                        unreadCount
                    );


                badges.append(
                    unreadBadge
                );
            }


            top.append(
                name,
                badges
            );


            const preview =
                document.createElement(
                    "p"
                );

            preview.className =
                "admin-chat-room-preview";

            preview.textContent =
                room.lastMessagePreview
                || "아직 메시지가 없습니다.";


            const bottom =
                document.createElement(
                    "div"
                );

            bottom.className =
                "admin-chat-room-bottom";


            const status =
                document.createElement(
                    "span"
                );

            status.className =
                "admin-chat-room-status-label "
                + getStatusClass(
                    room.status
                );

            status.textContent =
                getStatusText(
                    room.status
                );


            const time =
                document.createElement(
                    "span"
                );

            time.textContent =
                formatDateTime(
                    room.lastMessageAt
                    || room.createdAt
                );


            bottom.append(
                status,
                time
            );


            button.append(
                top,
                preview,
                bottom
            );


            button.addEventListener(
                "click",
                function () {

                    selectRoom(
                        room.roomUuid
                    );
                }
            );


            return button;
        }


        function createPlaceholder(
            title,
            description
        ) {

            const wrapper =
                document.createElement(
                    "div"
                );

            wrapper.className =
                "admin-chat-placeholder";


            const strong =
                document.createElement(
                    "strong"
                );

            strong.textContent =
                title;


            const paragraph =
                document.createElement(
                    "p"
                );

            paragraph.textContent =
                description;


            wrapper.append(
                strong,
                paragraph
            );


            return wrapper;
        }


        /* ========================================
           채팅방 선택
        ======================================== */

        async function selectRoom(roomUuid) {

            const room =
                roomList.find(
                    function (item) {

                        return item.roomUuid
                            === roomUuid;
                    }
                );


            if (!room) {
                return;
            }


            selectedRoom =
                room;


            renderedMessageIds.clear();


            showConversation();

            updateConversationHeader();

            updateRoomActiveState();

            updateAddressBar();

            subscribeRoom();


            await loadMessages();

            await markSelectedRoomRead();

            await loadRooms(false);
        }


        function showConversation() {

            emptyState.hidden =
                true;

            conversation.hidden =
                false;
        }


        function updateRoomActiveState() {

            const roomItems =
                roomListElement.querySelectorAll(
                    ".admin-chat-room-item"
                );


            roomItems.forEach(
                function (item) {

                    item.classList.toggle(
                        "active",
                        selectedRoom !== null
                        && item.dataset.roomUuid
                        === selectedRoom.roomUuid
                    );
                }
            );
        }


        function updateAddressBar() {

            if (!selectedRoom) {
                return;
            }


            const url =
                new URL(
                    window.location.href
                );


            url.searchParams.set(
                "roomUuid",
                selectedRoom.roomUuid
            );


            if (initialView === "unread") {

                url.searchParams.set(
                    "view",
                    "unread"
                );

            } else {

                url.searchParams.delete(
                    "view"
                );
            }


            history.replaceState(
                null,
                "",
                url
            );
        }


        function updateConversationHeader() {

            if (!selectedRoom) {
                return;
            }


            roomStatus.textContent =
                getStatusText(
                    selectedRoom.status
                );


            roomStatus.className =
                "admin-chat-room-status "
                + getStatusClass(
                    selectedRoom.status
                );


            roomTitle.textContent =
                selectedRoom.customerName
                || "이름 없는 고객";


            const visitorText =
                selectedRoom.visitorType === "GUEST"
                    ? "비회원"
                    : "회원";


            roomMeta.textContent =
                visitorText
                + " · 마지막 메시지 "
                + formatDateTime(
                    selectedRoom.lastMessageAt
                    || selectedRoom.createdAt
                );


            updateReplyAvailability();
        }


        function updateReplyAvailability() {

            if (!selectedRoom) {
                return;
            }


            const roomClosed =
                selectedRoom.status === "CLOSED";


            const socketDisconnected =
                !stompClient
                || !stompClient.connected;


            closeRoomButton.disabled =
                roomClosed;


            replyInput.disabled =
                roomClosed
                || socketDisconnected;


            replySendButton.disabled =
                roomClosed
                || socketDisconnected;


            if (roomClosed) {

                replyNotice.textContent =
                    "종료된 상담방입니다.";

                return;
            }


            if (socketDisconnected) {

                replyNotice.textContent =
                    "실시간 채팅 서버에 연결 중입니다.";

                return;
            }


            replyNotice.textContent =
                "고객에게 실시간으로 답변이 전송됩니다.";
        }


        /* ========================================
           채팅 메시지 조회
        ======================================== */

        async function loadMessages() {

            if (!selectedRoom) {
                return;
            }


            messageList.replaceChildren();


            try {

                const response =
                    await fetch(
                        "/admin/api/chat/rooms/"
                        + encodeURIComponent(
                            selectedRoom.roomUuid
                        )
                        + "/messages?size=100",
                        {
                            method: "GET",
                            credentials: "same-origin"
                        }
                    );


                if (!response.ok) {

                    throw await createApiError(
                        response
                    );
                }


                const messages =
                    await response.json();


                if (
                    Array.isArray(messages)
                ) {

                    messages.forEach(
                        function (message) {

                            renderMessage(
                                message
                            );
                        }
                    );
                }


                scrollMessagesToBottom();


            } catch (error) {

                console.error(
                    "관리자 채팅 메시지 조회 실패:",
                    error
                );


                replyNotice.textContent =
                    error.message
                    || "메시지를 불러오지 못했습니다.";
            }
        }


        function renderMessage(message) {

            const messageId =
                message.messageId
                    ? String(
                        message.messageId
                    )
                    : null;


            if (
                messageId
                && renderedMessageIds.has(
                    messageId
                )
            ) {
                return;
            }


            if (messageId) {

                renderedMessageIds.add(
                    messageId
                );
            }


            const isAdmin =
                message.senderType === "ADMIN";


            const row =
                document.createElement(
                    "div"
                );

            row.className =
                "admin-chat-message-row "
                + (
                    isAdmin
                        ? "admin"
                        : "customer"
                );


            const bubbleArea =
                document.createElement(
                    "div"
                );

            bubbleArea.className =
                "admin-chat-message-bubble-area";


            const sender =
                document.createElement(
                    "div"
                );

            sender.className =
                "admin-chat-message-sender";

            sender.textContent =
                isAdmin
                    ? "관리자"
                    : (
                        message.senderNameSnapshot
                        || "고객"
                    );


            const bubble =
                document.createElement(
                    "div"
                );

            bubble.className =
                "admin-chat-message-bubble";

            bubble.textContent =
                message.content
                || "";


            const time =
                document.createElement(
                    "div"
                );

            time.className =
                "admin-chat-message-time";

            time.textContent =
                formatDateTime(
                    message.createdAt
                );


            bubbleArea.append(
                sender,
                bubble,
                time
            );


            row.append(
                bubbleArea
            );


            messageList.append(
                row
            );


            scrollMessagesToBottom();
        }


        /* ========================================
           답변 입력
        ======================================== */

        function handleReplyInput() {

            replyLength.textContent =
                String(
                    replyInput.value.length
                );


            replyInput.style.height =
                "auto";


            replyInput.style.height =
                Math.min(
                    replyInput.scrollHeight,
                    100
                )
                + "px";
        }


        /* ========================================
           관리자 답변 전송
        ======================================== */

        function sendAdminReply(event) {

            event.preventDefault();


            if (!selectedRoom) {

                replyNotice.textContent =
                    "채팅방을 먼저 선택해 주세요.";

                return;
            }


            if (
                selectedRoom.status
                === "CLOSED"
            ) {

                replyNotice.textContent =
                    "종료된 상담방에는 답변할 수 없습니다.";

                return;
            }


            const content =
                replyInput.value.trim();


            if (!content) {

                replyNotice.textContent =
                    "답변 내용을 입력해 주세요.";

                return;
            }


            if (content.length > 500) {

                replyNotice.textContent =
                    "답변은 최대 500자까지 입력할 수 있습니다.";

                return;
            }


            if (
                !stompClient
                || !stompClient.connected
            ) {

                replyNotice.textContent =
                    "실시간 채팅 서버에 연결 중입니다.";

                return;
            }


            stompClient.publish(
                {
                    destination:
                        "/pub/chat/admin/message",

                    body:
                        JSON.stringify(
                            {
                                roomUuid:
                                selectedRoom.roomUuid,

                                clientMessageUuid:
                                    crypto.randomUUID(),

                                content:
                                content
                            }
                        )
                }
            );


            replyInput.value =
                "";

            replyInput.style.height =
                "46px";

            replyLength.textContent =
                "0";

            replyNotice.textContent =
                "답변을 전송했습니다.";
        }


        /* ========================================
           읽음 처리
        ======================================== */

        async function markSelectedRoomRead() {

            if (!selectedRoom) {
                return;
            }


            try {

                const response =
                    await fetch(
                        "/admin/api/chat/rooms/"
                        + encodeURIComponent(
                            selectedRoom.roomUuid
                        )
                        + "/read",
                        {
                            method: "POST",
                            credentials: "same-origin",
                            headers: createCsrfHeaders()
                        }
                    );


                if (!response.ok) {

                    throw await createApiError(
                        response
                    );
                }


                selectedRoom.adminUnreadCount =
                    0;


            } catch (error) {

                console.error(
                    "관리자 읽음 처리 실패:",
                    error
                );
            }
        }


        /* ========================================
           상담 종료
        ======================================== */

        async function closeSelectedRoom() {

            if (!selectedRoom) {
                return;
            }


            const confirmed =
                window.confirm(
                    "이 상담을 종료하시겠습니까?"
                );


            if (!confirmed) {
                return;
            }


            try {

                const response =
                    await fetch(
                        "/admin/api/chat/rooms/"
                        + encodeURIComponent(
                            selectedRoom.roomUuid
                        )
                        + "/close",
                        {
                            method: "POST",
                            credentials: "same-origin",
                            headers: createCsrfHeaders()
                        }
                    );


                if (!response.ok) {

                    throw await createApiError(
                        response
                    );
                }


                selectedRoom.status =
                    "CLOSED";


                updateConversationHeader();

                await loadRooms(false);


            } catch (error) {

                console.error(
                    "상담 종료 실패:",
                    error
                );


                replyNotice.textContent =
                    error.message
                    || "상담 종료에 실패했습니다.";
            }
        }


        /* ========================================
           WebSocket 연결
        ======================================== */

        function connectStomp() {

            if (
                stompClient
                && stompClient.connected
            ) {

                subscribeAdminTopic();

                subscribeErrorQueue();

                return Promise.resolve();
            }


            if (stompConnectPromise) {

                return stompConnectPromise;
            }


            if (!window.StompJs) {

                return Promise.reject(
                    new Error(
                        "STOMP 라이브러리를 불러오지 못했습니다."
                    )
                );
            }


            const webSocketProtocol =
                location.protocol === "https:"
                    ? "wss"
                    : "ws";


            stompClient =
                new StompJs.Client(
                    {
                        brokerURL:
                            webSocketProtocol
                            + "://"
                            + location.host
                            + "/ws-stomp",

                        reconnectDelay:
                            5000,

                        heartbeatIncoming:
                            10000,

                        heartbeatOutgoing:
                            10000,

                        debug:
                            function () {
                            }
                    }
                );


            stompConnectPromise =
                new Promise(
                    function (
                        resolve,
                        reject
                    ) {

                        stompClient.onConnect =
                            function () {

                                stompConnectPromise =
                                    null;


                                subscribeAdminTopic();

                                subscribeErrorQueue();


                                if (selectedRoom) {

                                    subscribeRoom();
                                }


                                updateReplyAvailability();


                                resolve();
                            };


                        stompClient.onStompError =
                            function (frame) {

                                console.error(
                                    "STOMP 오류:",
                                    frame
                                );


                                stompConnectPromise =
                                    null;


                                replyNotice.textContent =
                                    "실시간 채팅 서버에서 오류가 발생했습니다.";


                                reject(
                                    new Error(
                                        "실시간 채팅 서버 연결에 실패했습니다."
                                    )
                                );
                            };


                        stompClient.onWebSocketError =
                            function (error) {

                                console.error(
                                    "WebSocket 오류:",
                                    error
                                );
                            };


                        stompClient.onWebSocketClose =
                            function () {

                                updateReplyAvailability();
                            };


                        stompClient.activate();
                    }
                );


            return stompConnectPromise;
        }


        /* ========================================
           관리자 전체 알림 구독
        ======================================== */

        function subscribeAdminTopic() {

            if (
                !stompClient
                || !stompClient.connected
                || adminTopicSubscription
            ) {
                return;
            }


            adminTopicSubscription =
                stompClient.subscribe(
                    "/topic/admin/chat",
                    function (frame) {

                        const event =
                            JSON.parse(
                                frame.body
                            );


                        handleAdminTopicEvent(
                            event
                        );
                    }
                );
        }


        /* ========================================
           선택 채팅방 구독
        ======================================== */

        function subscribeRoom() {

            if (
                !selectedRoom
                || !stompClient
                || !stompClient.connected
            ) {
                return;
            }


            if (roomSubscription) {

                try {

                    roomSubscription.unsubscribe();

                } catch (error) {

                    console.warn(
                        "기존 채팅방 구독 해제 실패:",
                        error
                    );
                }


                roomSubscription =
                    null;
            }


            roomSubscription =
                stompClient.subscribe(
                    "/sub/chat/room/"
                    + selectedRoom.roomUuid,
                    function (frame) {

                        const event =
                            JSON.parse(
                                frame.body
                            );


                        handleRoomEvent(
                            event
                        );
                    }
                );
        }


        /* ========================================
           개인 오류 메시지 구독
        ======================================== */

        function subscribeErrorQueue() {

            if (
                !stompClient
                || !stompClient.connected
                || errorSubscription
            ) {
                return;
            }


            errorSubscription =
                stompClient.subscribe(
                    "/user/queue/chat-errors",
                    function (frame) {

                        const errorResponse =
                            JSON.parse(
                                frame.body
                            );


                        replyNotice.textContent =
                            errorResponse.message
                            || "채팅 처리 중 오류가 발생했습니다.";
                    }
                );
        }


        /* ========================================
           관리자 전체 이벤트 처리
        ======================================== */

        function handleAdminTopicEvent(event) {

            const isCustomerMessage =
                event.eventType === "MESSAGE"
                && event.message
                && event.message.senderType
                !== "ADMIN";


            if (isCustomerMessage) {

                showDesktopNotification(
                    event
                );
            }


            if (
                selectedRoom
                && event.roomUuid
                === selectedRoom.roomUuid
            ) {

                handleRoomEvent(
                    event
                );


                markSelectedRoomRead();
            }


            loadRooms(false);
        }


        /* ========================================
           선택한 채팅방 이벤트 처리
        ======================================== */

        function handleRoomEvent(event) {

            if (
                !selectedRoom
                || event.roomUuid
                !== selectedRoom.roomUuid
            ) {
                return;
            }


            if (event.roomStatus) {

                selectedRoom.status =
                    event.roomStatus;
            }


            if (
                event.eventType === "MESSAGE"
                && event.message
            ) {

                renderMessage(
                    event.message
                );
            }


            if (
                event.eventType
                === "ROOM_CLOSED"
            ) {

                selectedRoom.status =
                    "CLOSED";
            }


            updateConversationHeader();

            loadRooms(false);
        }


        /* ========================================
           REST 오류 응답 변환
        ======================================== */

        async function createApiError(response) {

            let message =
                "요청을 처리하지 못했습니다.";


            try {

                const errorResponse =
                    await response.json();


                if (errorResponse.message) {

                    message =
                        errorResponse.message;
                }


            } catch (error) {

                console.warn(
                    "오류 응답 JSON 변환 실패:",
                    error
                );
            }


            return new Error(
                message
            );
        }


        /* ========================================
           CSRF 헤더
        ======================================== */

        function createCsrfHeaders() {

            const headers = {};


            const csrfToken =
                document.querySelector(
                    'meta[name="_csrf"]'
                );


            const csrfHeader =
                document.querySelector(
                    'meta[name="_csrf_header"]'
                );


            if (
                csrfToken
                && csrfHeader
            ) {

                headers[csrfHeader.content] =
                    csrfToken.content;
            }


            return headers;
        }


        /* ========================================
           상태 표시
        ======================================== */

        function getStatusText(status) {

            if (status === "WAITING") {
                return "답변 대기";
            }


            if (status === "CHATTING") {
                return "상담 중";
            }


            if (status === "CLOSED") {
                return "종료";
            }


            return status || "-";
        }


        function getStatusClass(status) {

            if (status === "WAITING") {
                return "waiting";
            }


            if (status === "CHATTING") {
                return "chatting";
            }


            if (status === "CLOSED") {
                return "closed";
            }


            return "";
        }


        /* ========================================
           날짜 출력
        ======================================== */

        function formatDateTime(value) {

            if (!value) {
                return "-";
            }


            const date =
                new Date(value);


            if (
                Number.isNaN(
                    date.getTime()
                )
            ) {

                return String(value);
            }


            const month =
                String(
                    date.getMonth() + 1
                )
                    .padStart(
                        2,
                        "0"
                    );


            const day =
                String(
                    date.getDate()
                )
                    .padStart(
                        2,
                        "0"
                    );


            const hour =
                String(
                    date.getHours()
                )
                    .padStart(
                        2,
                        "0"
                    );


            const minute =
                String(
                    date.getMinutes()
                )
                    .padStart(
                        2,
                        "0"
                    );


            return month
                + "."
                + day
                + " "
                + hour
                + ":"
                + minute;
        }


        /* ========================================
           메시지 하단 이동
        ======================================== */

        function scrollMessagesToBottom() {

            window.requestAnimationFrame(
                function () {

                    messageList.scrollTop =
                        messageList.scrollHeight;
                }
            );
        }
    }
);