document.addEventListener("DOMContentLoaded", function () {

    /* =====================================================
       HTML 요소 가져오기
    ===================================================== */

    const chatbot =
        document.getElementById("petcityChatbot");

    const toggleButton =
        document.getElementById("chatbotToggleButton");

    const closeButton =
        document.getElementById("chatbotCloseButton");

    const homeButton =
        document.getElementById("chatbotHomeButton");

    const panel =
        document.getElementById("chatbotPanel");

    const messageList =
        document.getElementById("chatbotMessageList");

    const chatForm =
        document.getElementById("chatbotChatForm");

    const chatInput =
        document.getElementById("chatbotChatInput");

    const chatSendButton =
        document.getElementById("chatbotChatSendButton");

    const chatLength =
        document.getElementById("chatbotChatLength");

    const chatNotice =
        document.getElementById("chatbotChatNotice");


    if (
        !chatbot
        || !toggleButton
        || !closeButton
        || !homeButton
        || !panel
        || !messageList
        || !chatForm
        || !chatInput
        || !chatSendButton
        || !chatLength
        || !chatNotice
    ) {
        return;
    }


    /* =====================================================
       FAQ 상태
    ===================================================== */

    let currentCategoryId = null;
    let currentCategoryName = "";
    let currentFaqList = [];

    let chatbotStarted = false;


    /* =====================================================
       1:1 상담 상태
    ===================================================== */

    let liveChatMode = false;
    let currentChatRoom = null;

    let stompClient = null;
    let stompConnectPromise = null;

    let roomSubscription = null;
    let errorSubscription = null;

    const renderedMessageIds =
        new Set();


    /* =====================================================
       챗봇 열기 및 닫기
    ===================================================== */

    toggleButton.addEventListener(
        "click",
        function () {

            openChatbot();

            if (!chatbotStarted) {

                chatbotStarted = true;

                showChatbotHome();
            }
        }
    );


    closeButton.addEventListener(
        "click",
        closeChatbot
    );


    homeButton.addEventListener(
        "click",
        showChatbotHome
    );


    document.addEventListener(
        "keydown",
        function (event) {

            if (event.key === "Escape") {
                closeChatbot();
            }
        }
    );


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


    function openChatbot() {

        chatbot.classList.add("open");

        toggleButton.setAttribute(
            "aria-expanded",
            "true"
        );

        panel.setAttribute(
            "aria-hidden",
            "false"
        );
    }


    function closeChatbot() {

        chatbot.classList.remove("open");

        toggleButton.setAttribute(
            "aria-expanded",
            "false"
        );

        panel.setAttribute(
            "aria-hidden",
            "true"
        );
    }


    /* =====================================================
       처음 화면
    ===================================================== */

    async function showChatbotHome() {

        liveChatMode = false;

        chatForm.hidden = true;

        chatInput.value = "";
        chatInput.style.height = "44px";

        chatLength.textContent = "0";

        chatNotice.textContent =
            "관리자 답변 전 최대 3개까지 전송할 수 있습니다.";

        unsubscribeLiveChatSubscriptions();

        currentCategoryId = null;
        currentCategoryName = "";
        currentFaqList = [];

        clearMessages();

        addBotTextMessage(
            "안녕하세요.<br>"
            + "PetCity 고객센터입니다.<br>"
            + "궁금한 분야를 선택해 주세요."
        );

        const loadingMessage =
            addLoadingMessage();

        try {

            const response =
                await fetch(
                    "/api/chatbot/categories"
                );

            if (!response.ok) {

                throw new Error(
                    "카테고리 조회에 실패했습니다."
                );
            }

            const categoryList =
                await response.json();

            loadingMessage.remove();

            if (categoryList.length === 0) {

                addBotTextMessage(
                    "현재 등록된 챗봇 질문이 없습니다."
                );

                renderLiveChatOnlyButton();

                return;
            }

            renderCategoryButtons(
                categoryList
            );

        } catch (error) {

            console.error(error);

            loadingMessage.remove();

            addBotTextMessage(
                "질문 목록을 불러오지 못했습니다.<br>"
                + "잠시 후 다시 시도해 주세요."
            );

            renderRetryButton(
                showChatbotHome
            );

            renderLiveChatOnlyButton();
        }
    }


    /* =====================================================
       카테고리 버튼
    ===================================================== */

    function renderCategoryButtons(
        categoryList
    ) {

        const optionGroup =
            createOptionGroup();

        categoryList.forEach(
            function (category) {

                const button =
                    createOptionButton(
                        category.categoryName
                    );

                button.addEventListener(
                    "click",
                    function () {

                        disableOptionGroup(
                            optionGroup
                        );

                        selectCategory(
                            category
                        );
                    }
                );

                optionGroup.append(
                    button
                );
            }
        );


        const counselButton =
            createOptionButton(
                "1:1 상담 연결",
                true
            );

        counselButton.addEventListener(
            "click",
            function () {

                startLiveChat(
                    optionGroup
                );
            }
        );

        optionGroup.append(
            counselButton
        );

        messageList.append(
            optionGroup
        );

        scrollToBottom();
    }


    function renderLiveChatOnlyButton() {

        const optionGroup =
            createOptionGroup();

        const counselButton =
            createOptionButton(
                "1:1 상담 연결",
                true
            );

        counselButton.addEventListener(
            "click",
            function () {

                startLiveChat(
                    optionGroup
                );
            }
        );

        optionGroup.append(
            counselButton
        );

        messageList.append(
            optionGroup
        );

        scrollToBottom();
    }


    /* =====================================================
       카테고리 선택
    ===================================================== */

    async function selectCategory(
        category
    ) {

        currentCategoryId =
            category.categoryId;

        currentCategoryName =
            category.categoryName;

        addUserMessage(
            category.categoryName
        );

        addBotTextMessage(
            category.categoryName
            + "과 관련된 질문을 선택해 주세요."
        );

        const loadingMessage =
            addLoadingMessage();

        try {

            const response =
                await fetch(
                    "/api/chatbot/categories/"
                    + category.categoryId
                    + "/faqs"
                );

            if (!response.ok) {

                throw new Error(
                    "FAQ 조회에 실패했습니다."
                );
            }

            currentFaqList =
                await response.json();

            loadingMessage.remove();

            if (currentFaqList.length === 0) {

                addBotTextMessage(
                    "해당 카테고리에 등록된 질문이 없습니다."
                );

                renderHomeOptionButton();

                return;
            }

            renderFaqButtons(
                currentFaqList
            );

        } catch (error) {

            console.error(error);

            loadingMessage.remove();

            addBotTextMessage(
                "질문을 불러오지 못했습니다.<br>"
                + "잠시 후 다시 시도해 주세요."
            );

            renderRetryButton(
                function () {

                    selectCategory(
                        category
                    );
                }
            );
        }
    }


    /* =====================================================
       FAQ 버튼
    ===================================================== */

    function renderFaqButtons(
        faqList
    ) {

        const optionGroup =
            createOptionGroup();

        faqList.forEach(
            function (faq) {

                const button =
                    createOptionButton(
                        faq.title
                    );

                button.addEventListener(
                    "click",
                    function () {

                        disableOptionGroup(
                            optionGroup
                        );

                        selectFaq(
                            faq
                        );
                    }
                );

                optionGroup.append(
                    button
                );
            }
        );


        const homeOptionButton =
            createOptionButton(
                "다른 카테고리 보기",
                true
            );

        homeOptionButton.addEventListener(
            "click",
            function () {

                disableOptionGroup(
                    optionGroup
                );

                showChatbotHome();
            }
        );

        optionGroup.append(
            homeOptionButton
        );

        messageList.append(
            optionGroup
        );

        scrollToBottom();
    }


    /* =====================================================
       FAQ 선택
    ===================================================== */

    function selectFaq(faq) {

        addUserMessage(
            faq.title
        );

        addBotHtmlMessage(
            sanitizeFaqHtml(
                faq.content
            )
        );

        addBotTextMessage(
            "답변이 도움이 되었나요?"
        );

        renderAnswerButtons();
    }


    /* =====================================================
       답변 이후 버튼
    ===================================================== */

    function renderAnswerButtons() {

        const optionGroup =
            createOptionGroup();


        const solvedButton =
            createOptionButton(
                "네, 해결됐어요"
            );

        solvedButton.addEventListener(
            "click",
            function () {

                disableOptionGroup(
                    optionGroup
                );

                addUserMessage(
                    "네, 해결됐어요"
                );

                addBotTextMessage(
                    "도움이 되었다니 다행입니다.<br>"
                    + "다른 궁금한 내용도 언제든 확인해 주세요."
                );

                renderFinishButtons();
            }
        );


        const otherQuestionButton =
            createOptionButton(
                "같은 분야의 다른 질문 보기"
            );

        otherQuestionButton.addEventListener(
            "click",
            function () {

                disableOptionGroup(
                    optionGroup
                );

                addUserMessage(
                    "다른 질문 보기"
                );

                addBotTextMessage(
                    currentCategoryName
                    + "의 다른 질문을 선택해 주세요."
                );

                renderFaqButtons(
                    currentFaqList
                );
            }
        );


        const counselButton =
            createOptionButton(
                "1:1 상담 연결",
                true
            );

        counselButton.addEventListener(
            "click",
            function () {

                startLiveChat(
                    optionGroup
                );
            }
        );


        optionGroup.append(
            solvedButton,
            otherQuestionButton,
            counselButton
        );

        messageList.append(
            optionGroup
        );

        scrollToBottom();
    }


    /* =====================================================
       완료 이후 버튼
    ===================================================== */

    function renderFinishButtons() {

        const optionGroup =
            createOptionGroup();


        const homeOptionButton =
            createOptionButton(
                "다른 카테고리 질문하기"
            );

        homeOptionButton.addEventListener(
            "click",
            function () {

                disableOptionGroup(
                    optionGroup
                );

                showChatbotHome();
            }
        );


        const closeOptionButton =
            createOptionButton(
                "상담창 닫기",
                true
            );

        closeOptionButton.addEventListener(
            "click",
            function () {

                disableOptionGroup(
                    optionGroup
                );

                closeChatbot();
            }
        );


        optionGroup.append(
            homeOptionButton,
            closeOptionButton
        );

        messageList.append(
            optionGroup
        );

        scrollToBottom();
    }


    /* =====================================================
       처음으로 버튼
    ===================================================== */

    function renderHomeOptionButton() {

        const optionGroup =
            createOptionGroup();

        const button =
            createOptionButton(
                "카테고리 목록으로 돌아가기",
                true
            );

        button.addEventListener(
            "click",
            function () {

                disableOptionGroup(
                    optionGroup
                );

                showChatbotHome();
            }
        );

        optionGroup.append(
            button
        );

        messageList.append(
            optionGroup
        );

        scrollToBottom();
    }


    /* =====================================================
       재시도 버튼
    ===================================================== */

    function renderRetryButton(
        retryFunction
    ) {

        const optionGroup =
            createOptionGroup();

        const retryButton =
            createOptionButton(
                "다시 시도하기",
                true
            );

        retryButton.addEventListener(
            "click",
            function () {

                disableOptionGroup(
                    optionGroup
                );

                retryFunction();
            }
        );

        optionGroup.append(
            retryButton
        );

        messageList.append(
            optionGroup
        );

        scrollToBottom();
    }


    /* =====================================================
       1:1 상담 시작
    ===================================================== */

    async function startLiveChat(
        optionGroup
    ) {

        if (optionGroup) {

            disableOptionGroup(
                optionGroup
            );
        }

        addUserMessage(
            "1:1 상담 연결"
        );

        const loadingMessage =
            addLoadingMessage();

        try {

            const response =
                await fetch(
                    "/api/chat/rooms/open",
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

            currentChatRoom =
                await response.json();

            liveChatMode = true;

            renderedMessageIds.clear();

            loadingMessage.remove();

            clearMessages();

            chatForm.hidden = false;

            addBotTextMessage(
                "1:1 상담이 연결되었습니다.<br>"
                + "문의 내용을 입력해 주세요.<br>"
                + "관리자가 확인한 뒤 답변드립니다."
            );

            await connectLiveChatSocket();

            await loadLiveChatMessages();

            updateLiveChatState();

            chatInput.focus();

        } catch (error) {

            console.error(error);

            loadingMessage.remove();

            liveChatMode = false;

            chatForm.hidden = true;

            addBotPlainMessage(
                error.message
                || "상담 연결에 실패했습니다."
            );

            renderRetryButton(
                function () {

                    startLiveChat(
                        null
                    );
                }
            );
        }
    }


    /* =====================================================
       WebSocket 연결
    ===================================================== */

    function connectLiveChatSocket() {

        if (
            stompClient
            && stompClient.connected
        ) {

            subscribeLiveChat();

            return Promise.resolve();
        }

        if (stompConnectPromise) {

            return stompConnectPromise;
        }

        if (!window.StompJs) {

            return Promise.reject(
                new Error(
                    "WebSocket 라이브러리를 불러오지 못했습니다."
                )
            );
        }

        const webSocketProtocol =
            location.protocol === "https:"
                ? "wss"
                : "ws";


        stompClient =
            new StompJs.Client({

                brokerURL:
                    webSocketProtocol
                    + "://"
                    + location.host
                    + "/ws-stomp",

                reconnectDelay: 5000,

                heartbeatIncoming: 10000,

                heartbeatOutgoing: 10000,

                debug: function () {

                    /*
                        로그가 필요하면 아래로 변경한다.

                        console.log(arguments);
                    */
                }
            });


        stompConnectPromise =
            new Promise(
                function (
                    resolve,
                    reject
                ) {

                    stompClient.onConnect =
                        function () {

                            stompConnectPromise = null;

                            if (
                                liveChatMode
                                && currentChatRoom
                            ) {

                                subscribeLiveChat();

                                updateLiveChatState();
                            }

                            resolve();
                        };


                    stompClient.onStompError =
                        function (frame) {

                            console.error(
                                "STOMP 오류:",
                                frame
                            );

                            stompConnectPromise = null;

                            reject(
                                new Error(
                                    "실시간 상담 서버 연결에 실패했습니다."
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

                            updateLiveChatState();
                        };


                    stompClient.activate();
                }
            );

        return stompConnectPromise;
    }


    /* =====================================================
       WebSocket 구독
    ===================================================== */

    function subscribeLiveChat() {

        if (
            !stompClient
            || !stompClient.connected
            || !currentChatRoom
        ) {
            return;
        }

        unsubscribeLiveChatSubscriptions();


        roomSubscription =
            stompClient.subscribe(

                "/sub/chat/room/"
                + currentChatRoom.roomUuid,

                function (frame) {

                    const event =
                        JSON.parse(
                            frame.body
                        );

                    handleLiveChatEvent(
                        event
                    );
                }
            );


        errorSubscription =
            stompClient.subscribe(

                "/user/queue/chat-errors",

                function (frame) {

                    const errorResponse =
                        JSON.parse(
                            frame.body
                        );

                    chatNotice.textContent =
                        errorResponse.message
                        || "메시지를 처리하지 못했습니다.";
                }
            );
    }


    function unsubscribeLiveChatSubscriptions() {

        if (roomSubscription) {

            try {

                roomSubscription.unsubscribe();

            } catch (error) {

                console.warn(error);
            }

            roomSubscription = null;
        }


        if (errorSubscription) {

            try {

                errorSubscription.unsubscribe();

            } catch (error) {

                console.warn(error);
            }

            errorSubscription = null;
        }
    }


    /* =====================================================
       기존 메시지 조회
    ===================================================== */

    async function loadLiveChatMessages() {

        const response =
            await fetch(

                "/api/chat/rooms/"
                + currentChatRoom.roomUuid
                + "/messages?size=100",

                {
                    credentials: "same-origin"
                }
            );

        if (!response.ok) {

            throw await createApiError(
                response
            );
        }

        const responseMessages =
            await response.json();

        responseMessages.forEach(
            function (message) {

                renderLiveChatMessage(
                    message
                );
            }
        );

        await markLiveChatRead();
    }


    /* =====================================================
       WebSocket 이벤트 처리
    ===================================================== */

    function handleLiveChatEvent(event) {

        if (
            !currentChatRoom
            || event.roomUuid
            !== currentChatRoom.roomUuid
        ) {
            return;
        }

        currentChatRoom.status =
            event.roomStatus;

        currentChatRoom.customerUnansweredCount =
            event.customerUnansweredCount;

        currentChatRoom.customerUnreadCount =
            event.customerUnreadCount;


        if (
            event.guestDailyRemaining !== null
            && event.guestDailyRemaining !== undefined
        ) {

            currentChatRoom.guestDailyRemaining =
                event.guestDailyRemaining;
        }


        if (
            event.eventType === "MESSAGE"
            && event.message
        ) {

            renderLiveChatMessage(
                event.message
            );

            if (
                event.message.senderType
                === "ADMIN"
            ) {

                markLiveChatRead();
            }
        }


        if (
            event.eventType
            === "ROOM_CLOSED"
        ) {

            addBotTextMessage(
                "상담이 종료되었습니다."
            );
        }

        updateLiveChatState();
    }


    /* =====================================================
       실시간 메시지 출력
    ===================================================== */

    function renderLiveChatMessage(
        message
    ) {

        if (
            message.messageId
            && renderedMessageIds.has(
                String(
                    message.messageId
                )
            )
        ) {
            return;
        }


        if (message.messageId) {

            renderedMessageIds.add(
                String(
                    message.messageId
                )
            );
        }


        const isAdmin =
            message.senderType === "ADMIN"
            || message.senderType === "SYSTEM";


        const messageRow =
            createMessageRow(
                isAdmin
                    ? "bot"
                    : "user"
            );


        if (isAdmin) {

            messageRow.classList.add(
                "live-admin"
            );
        }


        const messageBubble =
            createMessageBubble();

        messageBubble.textContent =
            message.content;

        messageRow.append(
            messageBubble
        );

        messageList.append(
            messageRow
        );

        scrollToBottom();
    }


    /* =====================================================
       메시지 입력
    ===================================================== */

    chatInput.addEventListener(
        "input",
        function () {

            chatLength.textContent =
                String(
                    chatInput.value.length
                );

            chatInput.style.height =
                "auto";

            chatInput.style.height =
                Math.min(
                    chatInput.scrollHeight,
                    100
                )
                + "px";
        }
    );


    chatInput.addEventListener(
        "keydown",
        function (event) {

            if (
                event.key === "Enter"
                && !event.shiftKey
            ) {

                event.preventDefault();

                chatForm.requestSubmit();
            }
        }
    );


    /* =====================================================
       고객 메시지 전송
    ===================================================== */

    chatForm.addEventListener(
        "submit",
        function (event) {

            event.preventDefault();

            if (
                !liveChatMode
                || !currentChatRoom
            ) {
                return;
            }


            const content =
                chatInput.value.trim();


            if (!content) {

                chatNotice.textContent =
                    "메시지를 입력해 주세요.";

                return;
            }


            if (content.length > 500) {

                chatNotice.textContent =
                    "메시지는 최대 500자까지 입력할 수 있습니다.";

                return;
            }


            if (
                !stompClient
                || !stompClient.connected
            ) {

                chatNotice.textContent =
                    "실시간 상담 서버에 연결 중입니다.";

                return;
            }


            stompClient.publish({

                destination:
                    "/pub/chat/customer/message",

                body:
                    JSON.stringify({

                        roomUuid:
                        currentChatRoom.roomUuid,

                        clientMessageUuid:
                            crypto.randomUUID(),

                        content:
                        content
                    })
            });


            chatInput.value = "";

            chatInput.style.height =
                "44px";

            chatLength.textContent =
                "0";
        }
    );


    /* =====================================================
       전송 가능 상태
    ===================================================== */

    function updateLiveChatState() {

        if (
            !currentChatRoom
            || !liveChatMode
        ) {
            return;
        }


        const unansweredCount =
            Number(
                currentChatRoom
                    .customerUnansweredCount
                || 0
            );


        const guestDailyRemaining =
            currentChatRoom
                .guestDailyRemaining;


        const roomClosed =
            currentChatRoom.status
            === "CLOSED";


        const waitingAdmin =
            unansweredCount >= 3;


        const guestLimitReached =
            guestDailyRemaining === 0;


        const socketDisconnected =
            !stompClient
            || !stompClient.connected;


        const disabled =
            roomClosed
            || waitingAdmin
            || guestLimitReached
            || socketDisconnected;


        chatInput.disabled =
            disabled;

        chatSendButton.disabled =
            disabled;


        if (roomClosed) {

            chatNotice.textContent =
                "종료된 상담입니다.";

            return;
        }


        if (guestLimitReached) {

            chatNotice.textContent =
                "비회원 하루 메시지 10개를 모두 사용했습니다.";

            return;
        }


        if (waitingAdmin) {

            chatNotice.textContent =
                "관리자의 답변을 기다려 주세요. "
                + "답변 전에는 최대 3개까지 보낼 수 있습니다.";

            return;
        }


        if (socketDisconnected) {

            chatNotice.textContent =
                "실시간 상담 서버에 연결 중입니다.";

            return;
        }


        const remainingBeforeReply =
            3 - unansweredCount;


        if (
            guestDailyRemaining !== null
            && guestDailyRemaining !== undefined
        ) {

            chatNotice.textContent =
                "관리자 답변 전 "
                + remainingBeforeReply
                + "개 더 전송 가능 · 오늘 "
                + guestDailyRemaining
                + "개 남음";

            return;
        }


        chatNotice.textContent =
            "관리자 답변 전 "
            + remainingBeforeReply
            + "개 더 전송할 수 있습니다.";
    }


    /* =====================================================
       읽음 처리
    ===================================================== */

    async function markLiveChatRead() {

        if (!currentChatRoom) {
            return;
        }

        try {

            const response =
                await fetch(

                    "/api/chat/rooms/"
                    + currentChatRoom.roomUuid
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

        } catch (error) {

            console.error(
                "채팅 읽음 처리 실패:",
                error
            );
        }
    }


    /* =====================================================
       메시지 생성 함수
    ===================================================== */

    function clearMessages() {

        messageList.replaceChildren();
    }


    function addUserMessage(text) {

        const messageRow =
            createMessageRow(
                "user"
            );

        const messageBubble =
            createMessageBubble();

        messageBubble.textContent =
            text;

        messageRow.append(
            messageBubble
        );

        messageList.append(
            messageRow
        );

        scrollToBottom();
    }


    function addBotTextMessage(
        htmlText
    ) {

        const messageRow =
            createMessageRow(
                "bot"
            );

        const messageBubble =
            createMessageBubble();

        messageBubble.innerHTML =
            htmlText;

        messageRow.append(
            messageBubble
        );

        messageList.append(
            messageRow
        );

        scrollToBottom();
    }


    function addBotPlainMessage(text) {

        const messageRow =
            createMessageRow(
                "bot"
            );

        const messageBubble =
            createMessageBubble();

        messageBubble.textContent =
            text;

        messageRow.append(
            messageBubble
        );

        messageList.append(
            messageRow
        );

        scrollToBottom();
    }


    function addBotHtmlMessage(
        htmlContent
    ) {

        const messageRow =
            createMessageRow(
                "bot"
            );

        const messageBubble =
            createMessageBubble();

        messageBubble.innerHTML =
            htmlContent;

        messageRow.append(
            messageBubble
        );

        messageList.append(
            messageRow
        );

        scrollToBottom();
    }


    function createMessageRow(type) {

        const messageRow =
            document.createElement(
                "div"
            );

        messageRow.className =
            "chatbot-message-row "
            + type;

        return messageRow;
    }


    function createMessageBubble() {

        const messageBubble =
            document.createElement(
                "div"
            );

        messageBubble.className =
            "chatbot-message-bubble";

        return messageBubble;
    }


    /* =====================================================
       선택지 버튼 생성
    ===================================================== */

    function createOptionGroup() {

        const optionGroup =
            document.createElement(
                "div"
            );

        optionGroup.className =
            "chatbot-option-group";

        return optionGroup;
    }


    function createOptionButton(
        text,
        secondary
    ) {

        const button =
            document.createElement(
                "button"
            );

        button.type =
            "button";

        button.className =
            "chatbot-option-button";


        if (secondary === true) {

            button.classList.add(
                "secondary"
            );
        }


        const textSpan =
            document.createElement(
                "span"
            );

        textSpan.textContent =
            text;

        button.append(
            textSpan
        );

        return button;
    }


    function disableOptionGroup(
        optionGroup
    ) {

        if (!optionGroup) {
            return;
        }

        optionGroup
            .querySelectorAll("button")
            .forEach(
                function (button) {

                    button.disabled =
                        true;
                }
            );
    }


    /* =====================================================
       로딩 메시지
    ===================================================== */

    function addLoadingMessage() {

        const messageRow =
            createMessageRow(
                "bot"
            );

        const messageBubble =
            createMessageBubble();

        const loading =
            document.createElement(
                "div"
            );

        loading.className =
            "chatbot-loading";


        for (
            let index = 0;
            index < 3;
            index++
        ) {

            loading.append(
                document.createElement(
                    "span"
                )
            );
        }


        messageBubble.append(
            loading
        );

        messageRow.append(
            messageBubble
        );

        messageList.append(
            messageRow
        );

        scrollToBottom();

        return messageRow;
    }


    /* =====================================================
       공통 보조 함수
    ===================================================== */

    function scrollToBottom() {

        window.requestAnimationFrame(
            function () {

                messageList.scrollTop =
                    messageList.scrollHeight;
            }
        );
    }


    async function createApiError(
        response
    ) {

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


    /* =====================================================
       FAQ HTML 정리
    ===================================================== */

    function sanitizeFaqHtml(html) {

        const template =
            document.createElement(
                "template"
            );

        template.innerHTML =
            html || "";


        template.content
            .querySelectorAll(
                "script, style, iframe, object, embed, form"
            )
            .forEach(
                function (element) {

                    element.remove();
                }
            );


        template.content
            .querySelectorAll("*")
            .forEach(
                function (element) {

                    Array.from(
                        element.attributes
                    )
                        .forEach(
                            function (attribute) {

                                const attributeName =
                                    attribute.name
                                        .toLowerCase();

                                const attributeValue =
                                    attribute.value
                                        .trim()
                                        .toLowerCase();


                                if (
                                    attributeName
                                        .startsWith("on")
                                ) {

                                    element.removeAttribute(
                                        attribute.name
                                    );
                                }


                                if (
                                    (
                                        attributeName === "href"
                                        || attributeName === "src"
                                    )
                                    && attributeValue
                                        .startsWith("javascript:")
                                ) {

                                    element.removeAttribute(
                                        attribute.name
                                    );
                                }
                            }
                        );
                }
            );


        return template.innerHTML;
    }
});