document.addEventListener("DOMContentLoaded", function () {

    /* =====================================================
       HTML 요소
    ===================================================== */

    const chatbot = document.getElementById("petcityChatbot");

    const toggleButton = document.getElementById("chatbotToggleButton");

    const closeButton = document.getElementById("chatbotCloseButton");

    const homeButton = document.getElementById("chatbotHomeButton");

    const panel = document.getElementById("chatbotPanel");

    const messageList = document.getElementById("chatbotMessageList");

    const chatForm = document.getElementById("chatbotChatForm");

    const chatInput = document.getElementById("chatbotChatInput");

    const chatSendButton = document.getElementById("chatbotChatSendButton");

    const chatLength = document.getElementById("chatbotChatLength");

    const chatNotice = document.getElementById("chatbotChatNotice");

    const customerNotificationButton = document.getElementById("customerChatNotificationButton");


    if (!chatbot || !toggleButton || !closeButton || !homeButton || !panel || !messageList || !chatForm || !chatInput || !chatSendButton || !chatLength || !chatNotice || !customerNotificationButton) {
        return;
    }


    /* =====================================================
       알림 문구

       알림 멘트를 변경하려면 이 객체만 수정
    ===================================================== */

    const NOTIFICATION_TEXT = {

        requestButton: "알림받기",

        enableButton: "알림·소리 켜기",

        disableButton: "알림·소리 끄기",

        deniedButton: "알림 차단됨",

        title: "PetCity 관리자 답변",

        defaultAdminName: "PetCity 관리자",

        defaultMessage: "관리자의 새로운 답변이 도착했습니다."
    };


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


    const renderedMessageIds = new Set();


    /* =====================================================
       사용자 알림 상태
    ===================================================== */

    const customerNotificationIconUrl = "/images/chatbot/admin-letter-dog.png";

    const customerAlertSound = new Audio("/audio/admin-chat-alert.mp3");


    customerAlertSound.preload = "auto";

    customerAlertSound.volume = 0.7;


    let customerNotificationEnabled = false;

    let customerNotificationSoundEnabled = false;

    let customerNotificationSoundTimer = null;


    const notifiedAdminMessageIds = new Set();


    /* =====================================================
       초기 실행
    ===================================================== */

    initializeCustomerNotificationButton();


    /* =====================================================
       챗봇 열기 및 닫기 이벤트
    ===================================================== */

    toggleButton.addEventListener("click", function () {

        openChatbot();

        if (!chatbotStarted) {

            chatbotStarted = true;

            showChatbotHome();
        }
    });


    closeButton.addEventListener("click", closeChatbot);


    homeButton.addEventListener("click", showChatbotHome);


    document.addEventListener("keydown", function (event) {

        if (event.key === "Escape") {

            closeChatbot();
        }
    });


    document.addEventListener("visibilitychange", function () {

        if (isLiveChatVisible()) {

            markLiveChatRead();
        }
    });


    window.addEventListener("beforeunload", function () {

        if (stompClient && stompClient.active) {

            stompClient.deactivate();
        }
    });


    /* =====================================================
       사용자 알림 버튼
    ===================================================== */

    function initializeCustomerNotificationButton() {

        if (!("Notification" in window)) {

            customerNotificationButton.hidden = true;

            return;
        }


        updateCustomerNotificationButton();


        customerNotificationButton.addEventListener("click", async function () {

            /*
                알림이 켜진 상태에서 다시 누르면
                알림과 소리를 끈다.
            */
            if (customerNotificationEnabled) {

                disableCustomerNotification();

                updateCustomerNotificationButton();

                return;
            }


            /*
                브라우저 알림 권한을 아직
                선택하지 않은 상태
            */
            if (Notification.permission === "default") {

                await Notification
                    .requestPermission();
            }


            /*
                브라우저에서 알림을 허용한 경우
                PetCity 알림과 소리를 활성화한다.
            */
            if (Notification.permission === "granted") {

                await enableCustomerNotification();
            }


            updateCustomerNotificationButton();
        });
    }


    function updateCustomerNotificationButton() {

        const buttonText = customerNotificationButton
            .querySelector("span");


        /*
            브라우저에서 알림을 차단한 상태
        */
        if (Notification.permission === "denied") {

            if (buttonText) {

                buttonText.textContent = NOTIFICATION_TEXT.deniedButton;
            }


            customerNotificationButton.disabled = true;


            customerNotificationButton.setAttribute("aria-pressed", "false");


            customerNotificationButton.setAttribute("aria-label", "채팅 알림 차단됨");


            customerNotificationButton
                .classList
                .remove("is-active");

            return;
        }


        /*
            PetCity 알림과 소리가 켜진 상태
        */
        if (customerNotificationEnabled) {

            if (buttonText) {

                buttonText.textContent = NOTIFICATION_TEXT.disableButton;
            }


            customerNotificationButton.disabled = false;


            customerNotificationButton.setAttribute("aria-pressed", "true");


            customerNotificationButton.setAttribute("aria-label", "채팅 알림과 소리 끄기");


            customerNotificationButton
                .classList
                .add("is-active");

            return;
        }


        /*
            알림이 꺼진 상태
        */
        if (buttonText) {

            buttonText.textContent = Notification.permission === "granted" ? NOTIFICATION_TEXT.enableButton : NOTIFICATION_TEXT.requestButton;
        }


        customerNotificationButton.disabled = false;


        customerNotificationButton.setAttribute("aria-pressed", "false");


        customerNotificationButton.setAttribute("aria-label", "채팅 알림과 소리 켜기");


        customerNotificationButton
            .classList
            .remove("is-active");
    }


    async function enableCustomerNotification() {

        customerNotificationEnabled = await unlockCustomerNotificationSound();
    }


    function disableCustomerNotification() {

        customerNotificationEnabled = false;

        customerNotificationSoundEnabled = false;


        customerAlertSound.pause();

        customerAlertSound.currentTime = 0;


        if (customerNotificationSoundTimer !== null) {

            clearTimeout(customerNotificationSoundTimer);

            customerNotificationSoundTimer = null;
        }
    }


    async function unlockCustomerNotificationSound() {

        try {

            /*
                사용자 클릭 시점에 음원을 조용히 한 번 재생해
                이후 자동 알림음 재생을 허용한다.
            */
            customerAlertSound.muted = true;

            customerAlertSound.currentTime = 0;


            await customerAlertSound.play();


            customerAlertSound.pause();

            customerAlertSound.currentTime = 0;

            customerAlertSound.muted = false;


            customerNotificationSoundEnabled = true;


            return true;

        } catch (error) {

            console.warn("사용자 알림음 활성화 실패:", error);


            customerAlertSound.muted = false;

            customerNotificationSoundEnabled = false;


            return false;
        }
    }


    /* =====================================================
       챗봇 열기 및 닫기
    ===================================================== */

    function isLiveChatVisible() {

        return liveChatMode && currentChatRoom !== null && chatbot.classList.contains("open") && document.visibilityState === "visible";
    }


    function openChatbot() {

        chatbot.classList.add("open");


        toggleButton.setAttribute("aria-expanded", "true");


        panel.setAttribute("aria-hidden", "false");


        /*
            관리자의 답변이 온 뒤
            사용자가 챗봇을 다시 열면 읽음 처리
        */
        if (liveChatMode && currentChatRoom) {

            markLiveChatRead();
        }
    }


    function closeChatbot() {

        chatbot.classList.remove("open");


        toggleButton.setAttribute("aria-expanded", "false");


        panel.setAttribute("aria-hidden", "true");
    }


    /* =====================================================
       처음 화면
    ===================================================== */

    async function showChatbotHome() {

        /*
            화면만 FAQ 모드로 변경한다.

            기존 상담방 구독은 해제하지 않으므로
            FAQ 화면에서도 관리자 답변 알림을 받을 수 있다.
        */
        liveChatMode = false;


        chatForm.hidden = true;


        chatInput.value = "";

        chatInput.style.height = "44px";


        chatLength.textContent = "0";


        chatNotice.textContent = "관리자 답변 전 최대 3개까지 전송할 수 있습니다.";


        currentCategoryId = null;

        currentCategoryName = "";

        currentFaqList = [];


        clearMessages();


        addBotTextMessage("안녕하세요.<br>" + "PetCity 고객센터입니다.<br>" + "궁금한 분야를 선택해 주세요.");


        const loadingMessage = addLoadingMessage();


        try {

            const response = await fetch("/api/chatbot/categories");


            if (!response.ok) {

                throw new Error("카테고리 조회에 실패했습니다.");
            }


            const categoryList = await response.json();


            loadingMessage.remove();


            if (categoryList.length === 0) {

                addBotTextMessage("현재 등록된 챗봇 질문이 없습니다.");


                renderLiveChatOnlyButton();

                return;
            }


            renderCategoryButtons(categoryList);

        } catch (error) {

            console.error(error);


            loadingMessage.remove();


            addBotTextMessage("질문 목록을 불러오지 못했습니다.<br>" + "잠시 후 다시 시도해 주세요.");


            renderRetryButton(showChatbotHome);


            renderLiveChatOnlyButton();
        }
    }


    /* =====================================================
       카테고리 버튼
    ===================================================== */

    function renderCategoryButtons(categoryList) {

        const optionGroup = createOptionGroup();


        categoryList.forEach(function (category) {

            const button = createOptionButton(category.categoryName);


            button.addEventListener("click", function () {

                disableOptionGroup(optionGroup);


                selectCategory(category);
            });


            optionGroup.append(button);
        });


        const counselButton = createOptionButton("1:1 상담 연결", true);


        counselButton.addEventListener("click", function () {

            startLiveChat(optionGroup);
        });


        optionGroup.append(counselButton);


        messageList.append(optionGroup);


        scrollToBottom();
    }


    function renderLiveChatOnlyButton() {

        const optionGroup = createOptionGroup();


        const counselButton = createOptionButton("1:1 상담 연결", true);


        counselButton.addEventListener("click", function () {

            startLiveChat(optionGroup);
        });


        optionGroup.append(counselButton);


        messageList.append(optionGroup);


        scrollToBottom();
    }


    /* =====================================================
       카테고리 선택
    ===================================================== */

    async function selectCategory(category) {

        currentCategoryId = category.categoryId;

        currentCategoryName = category.categoryName;


        addUserMessage(category.categoryName);


        addBotTextMessage(category.categoryName + "과 관련된 질문을 선택해 주세요.");


        const loadingMessage = addLoadingMessage();


        try {

            const response = await fetch("/api/chatbot/categories/" + category.categoryId + "/faqs");


            if (!response.ok) {

                throw new Error("FAQ 조회에 실패했습니다.");
            }


            currentFaqList = await response.json();


            loadingMessage.remove();


            if (currentFaqList.length === 0) {

                addBotTextMessage("해당 카테고리에 등록된 질문이 없습니다.");


                renderHomeOptionButton();

                return;
            }


            renderFaqButtons(currentFaqList);

        } catch (error) {

            console.error(error);


            loadingMessage.remove();


            addBotTextMessage("질문을 불러오지 못했습니다.<br>" + "잠시 후 다시 시도해 주세요.");


            renderRetryButton(function () {

                selectCategory(category);
            });
        }
    }


    /* =====================================================
       FAQ 버튼
    ===================================================== */

    function renderFaqButtons(faqList) {

        const optionGroup = createOptionGroup();


        faqList.forEach(function (faq) {

            const button = createOptionButton(faq.title);


            button.addEventListener("click", function () {

                disableOptionGroup(optionGroup);


                selectFaq(faq);
            });


            optionGroup.append(button);
        });


        const homeOptionButton = createOptionButton("다른 카테고리 보기", true);


        homeOptionButton.addEventListener("click", function () {

            disableOptionGroup(optionGroup);


            showChatbotHome();
        });


        optionGroup.append(homeOptionButton);


        messageList.append(optionGroup);


        scrollToBottom();
    }


    /* =====================================================
       FAQ 선택
    ===================================================== */

    function selectFaq(faq) {

        addUserMessage(faq.title);


        addBotHtmlMessage(sanitizeFaqHtml(faq.content));


        addBotTextMessage("답변이 도움이 되었나요?");


        renderAnswerButtons();
    }


    /* =====================================================
       답변 이후 버튼
    ===================================================== */

    function renderAnswerButtons() {

        const optionGroup = createOptionGroup();


        const solvedButton = createOptionButton("네, 해결됐어요");


        const otherQuestionButton = createOptionButton("같은 분야의 다른 질문 보기");


        const counselButton = createOptionButton("1:1 상담 연결", true);


        solvedButton.addEventListener("click", function () {

            disableOptionGroup(optionGroup);


            addUserMessage("네, 해결됐어요");


            addBotTextMessage("도움이 되었다니 다행입니다.<br>" + "다른 궁금한 내용도 언제든 확인해 주세요.");


            renderFinishButtons();
        });


        otherQuestionButton.addEventListener("click", function () {

            disableOptionGroup(optionGroup);


            addUserMessage("다른 질문 보기");


            addBotTextMessage(currentCategoryName + "의 다른 질문을 선택해 주세요.");


            renderFaqButtons(currentFaqList);
        });


        counselButton.addEventListener("click", function () {

            startLiveChat(optionGroup);
        });


        optionGroup.append(solvedButton, otherQuestionButton, counselButton);


        messageList.append(optionGroup);


        scrollToBottom();
    }


    /* =====================================================
       완료 이후 버튼
    ===================================================== */

    function renderFinishButtons() {

        const optionGroup = createOptionGroup();


        const homeOptionButton = createOptionButton("다른 카테고리 질문하기");


        const closeOptionButton = createOptionButton("상담창 닫기", true);


        homeOptionButton.addEventListener("click", function () {

            disableOptionGroup(optionGroup);


            showChatbotHome();
        });


        closeOptionButton.addEventListener("click", function () {

            disableOptionGroup(optionGroup);


            closeChatbot();
        });


        optionGroup.append(homeOptionButton, closeOptionButton);


        messageList.append(optionGroup);


        scrollToBottom();
    }


    /* =====================================================
       처음으로 버튼
    ===================================================== */

    function renderHomeOptionButton() {

        const optionGroup = createOptionGroup();


        const button = createOptionButton("카테고리 목록으로 돌아가기", true);


        button.addEventListener("click", function () {

            disableOptionGroup(optionGroup);


            showChatbotHome();
        });


        optionGroup.append(button);


        messageList.append(optionGroup);


        scrollToBottom();
    }


    /* =====================================================
       재시도 버튼
    ===================================================== */

    function renderRetryButton(retryFunction) {

        const optionGroup = createOptionGroup();


        const retryButton = createOptionButton("다시 시도하기", true);


        retryButton.addEventListener("click", function () {

            disableOptionGroup(optionGroup);


            retryFunction();
        });


        optionGroup.append(retryButton);


        messageList.append(optionGroup);


        scrollToBottom();
    }


    /* =====================================================
       1:1 상담 시작
    ===================================================== */

    async function startLiveChat(optionGroup, showRequestMessage = true) {

        if (optionGroup) {

            disableOptionGroup(optionGroup);
        }


        if (showRequestMessage) {

            addUserMessage("1:1 상담 연결");
        }


        const loadingMessage = addLoadingMessage();


        try {

            const response = await fetch("/api/chat/rooms/open", {
                method: "POST",

                credentials: "same-origin",

                headers: createCsrfHeaders()
            });


            if (!response.ok) {

                throw await createApiError(response);
            }


            currentChatRoom = await response.json();


            liveChatMode = true;


            renderedMessageIds.clear();


            loadingMessage.remove();


            clearMessages();


            chatForm.hidden = false;


            addBotTextMessage("1:1 상담이 연결되었습니다.<br>" + "문의 내용을 입력해 주세요.<br>" + "관리자가 확인한 뒤 답변드립니다.");


            await connectLiveChatSocket();

            await loadLiveChatMessages();


            updateLiveChatState();


            chatInput.focus();

        } catch (error) {

            console.error(error);


            loadingMessage.remove();


            liveChatMode = false;

            chatForm.hidden = true;


            addBotPlainMessage(error.message || "상담 연결에 실패했습니다.");


            renderRetryButton(function () {

                startLiveChat(null);
            });
        }
    }


    /* =====================================================
       WebSocket 연결
    ===================================================== */

    function connectLiveChatSocket() {

        if (stompClient && stompClient.connected) {

            subscribeLiveChat();

            return Promise.resolve();
        }


        if (stompConnectPromise) {

            return stompConnectPromise;
        }


        if (!window.StompJs) {

            return Promise.reject(new Error("WebSocket 라이브러리를 불러오지 못했습니다."));
        }


        const webSocketProtocol = location.protocol === "https:" ? "wss" : "ws";


        stompClient = new StompJs.Client({
            brokerURL: webSocketProtocol + "://" + location.host + "/ws-stomp",

            reconnectDelay: 5000,

            heartbeatIncoming: 10000,

            heartbeatOutgoing: 10000,

            debug: function () {
            }
        });


        stompConnectPromise = new Promise(function (resolve, reject) {

            stompClient.onConnect = function () {

                stompConnectPromise = null;


                if (liveChatMode && currentChatRoom) {

                    subscribeLiveChat();

                    updateLiveChatState();
                }


                resolve();
            };


            stompClient.onStompError = function (frame) {

                console.error("STOMP 오류:", frame);


                stompConnectPromise = null;


                reject(new Error("실시간 상담 서버 연결에 실패했습니다."));
            };


            stompClient.onWebSocketError = function (error) {

                console.error("WebSocket 오류:", error);
            };


            stompClient.onWebSocketClose = function () {

                updateLiveChatState();
            };


            stompClient.activate();
        });


        return stompConnectPromise;
    }


    /* =====================================================
       WebSocket 구독
    ===================================================== */

    function subscribeLiveChat() {

        if (!stompClient || !stompClient.connected || !currentChatRoom) {
            return;
        }


        unsubscribeLiveChatSubscriptions();


        roomSubscription = stompClient.subscribe("/sub/chat/room/" + currentChatRoom.roomUuid,

            function (frame) {

                const event = JSON.parse(frame.body);


                handleLiveChatEvent(event);
            });


        errorSubscription = stompClient.subscribe("/user/queue/chat-errors",

            function (frame) {

                const errorResponse = JSON.parse(frame.body);


                chatNotice.textContent = errorResponse.message || "메시지를 처리하지 못했습니다.";
            });
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

        const response = await fetch("/api/chat/rooms/" + currentChatRoom.roomUuid + "/messages?size=100",

            {
                credentials: "same-origin"
            });


        if (!response.ok) {

            throw await createApiError(response);
        }


        const responseMessages = await response.json();


        responseMessages.forEach(function (message) {

            renderLiveChatMessage(message);
        });


        await markLiveChatRead();
    }


    /* =====================================================
       관리자 답변 알림
    ===================================================== */

    function showAdminReplyNotification(message, roomUuid) {

        if (!customerNotificationEnabled || !message) {
            return;
        }


        const messageId = message.messageId ? String(message.messageId) : null;


        /*
            같은 메시지 알림이
            두 번 표시되는 것을 방지한다.
        */
        if (messageId && notifiedAdminMessageIds.has(messageId)) {
            return;
        }


        if (messageId) {

            notifiedAdminMessageIds.add(messageId);
        }


        playCustomerNotificationSound();


        if (!("Notification" in window) || Notification.permission !== "granted") {
            return;
        }


        const adminName = message.senderNameSnapshot || NOTIFICATION_TEXT.defaultAdminName;


        const content = truncateNotificationText(message.content || NOTIFICATION_TEXT.defaultMessage, 100);


        const notification = new Notification(NOTIFICATION_TEXT.title, {
            body: adminName + "\n" + content,

            icon: customerNotificationIconUrl,

            badge: customerNotificationIconUrl,

            image: customerNotificationIconUrl,

            tag: "petcity-customer-chat-" + roomUuid + "-" + (messageId || Date.now()),

            renotify: true,

            /*
                브라우저 기본음 대신
                프로젝트 음원을 사용한다.
            */
            silent: true
        });


        notification.onclick = async function () {

            window.focus();


            openChatbot();


            /*
                사용자가 FAQ 화면에 있다면
                기존 상담방 화면을 다시 연다.
            */
            if (!liveChatMode) {

                await startLiveChat(null, false);

            } else {

                await markLiveChatRead();
            }


            notification.close();
        };
    }


    function playCustomerNotificationSound() {

        if (!customerNotificationSoundEnabled) {
            return;
        }


        if (customerNotificationSoundTimer !== null) {

            clearTimeout(customerNotificationSoundTimer);
        }


        customerAlertSound.pause();

        customerAlertSound.currentTime = 0;


        customerAlertSound.play()
            .then(function () {

                customerNotificationSoundTimer = window.setTimeout(function () {

                    customerAlertSound.pause();

                    customerAlertSound.currentTime = 0;

                    customerNotificationSoundTimer = null;
                }, 3000);
            })
            .catch(function (error) {

                console.warn("사용자 알림음 재생 실패:", error);
            });
    }


    function truncateNotificationText(value, maxLength) {

        const text = String(value || "");


        if (text.length <= maxLength) {
            return text;
        }


        return text.substring(0, maxLength) + "...";
    }


    /* =====================================================
       WebSocket 이벤트 처리
    ===================================================== */

    function handleLiveChatEvent(event) {

        if (!currentChatRoom || event.roomUuid !== currentChatRoom.roomUuid) {
            return;
        }


        if (event.roomStatus) {

            currentChatRoom.status = event.roomStatus;
        }


        if (event.customerUnansweredCount !== null && event.customerUnansweredCount !== undefined) {

            currentChatRoom.customerUnansweredCount = event.customerUnansweredCount;
        }


        if (event.customerUnreadCount !== null && event.customerUnreadCount !== undefined) {

            currentChatRoom.customerUnreadCount = event.customerUnreadCount;
        }


        if (event.guestDailyRemaining !== null && event.guestDailyRemaining !== undefined) {

            currentChatRoom.guestDailyRemaining = event.guestDailyRemaining;
        }


        if (event.eventType === "MESSAGE" && event.message) {

            /*
                1:1 상담 화면을 보고 있을 때만
                메시지 영역에 바로 출력한다.

                FAQ 화면에서는 데스크톱 알림만 표시하고,
                상담 화면으로 돌아올 때 메시지를 재조회한다.
            */
            if (liveChatMode) {

                renderLiveChatMessage(event.message);
            }


            if (event.message.senderType === "ADMIN") {

                if (isLiveChatVisible()) {

                    markLiveChatRead();

                } else {

                    showAdminReplyNotification(event.message, event.roomUuid);
                }
            }
        }


        if (event.eventType === "ROOM_CLOSED") {

            currentChatRoom.status = "CLOSED";


            if (liveChatMode) {

                addBotTextMessage("상담이 종료되었습니다.");
            }
        }


        updateLiveChatState();
    }


    /* =====================================================
       실시간 메시지 출력
    ===================================================== */

    function renderLiveChatMessage(message) {

        if (message.messageId && renderedMessageIds.has(String(message.messageId))) {
            return;
        }


        if (message.messageId) {

            renderedMessageIds.add(String(message.messageId));
        }


        const isAdmin = message.senderType === "ADMIN" || message.senderType === "SYSTEM";


        const messageRow = createMessageRow(isAdmin ? "bot" : "user");


        if (isAdmin) {

            messageRow.classList.add("live-admin");
        }


        const messageBubble = createMessageBubble();


        messageBubble.textContent = message.content || "";


        messageRow.append(messageBubble);


        messageList.append(messageRow);


        scrollToBottom();
    }


    /* =====================================================
       메시지 입력
    ===================================================== */

    chatInput.addEventListener("input", function () {

        chatLength.textContent = String(chatInput.value.length);


        chatInput.style.height = "auto";


        chatInput.style.height = Math.min(chatInput.scrollHeight, 100) + "px";
    });


    chatInput.addEventListener("keydown", function (event) {

        if (event.key === "Enter" && !event.shiftKey) {

            event.preventDefault();


            chatForm.requestSubmit();
        }
    });


    /* =====================================================
       고객 메시지 전송
    ===================================================== */

    chatForm.addEventListener("submit", function (event) {

        event.preventDefault();


        if (!liveChatMode || !currentChatRoom) {
            return;
        }


        const content = chatInput.value.trim();


        if (!content) {

            chatNotice.textContent = "메시지를 입력해 주세요.";

            return;
        }


        if (content.length > 500) {

            chatNotice.textContent = "메시지는 최대 500자까지 입력할 수 있습니다.";

            return;
        }


        if (!stompClient || !stompClient.connected) {

            chatNotice.textContent = "실시간 상담 서버에 연결 중입니다.";

            return;
        }


        stompClient.publish({
            destination: "/pub/chat/customer/message",

            body: JSON.stringify({
                roomUuid: currentChatRoom.roomUuid,

                clientMessageUuid: crypto.randomUUID(),

                content: content
            })
        });


        chatInput.value = "";


        chatInput.style.height = "44px";


        chatLength.textContent = "0";
    });


    /* =====================================================
       전송 가능 상태
    ===================================================== */

    function updateLiveChatState() {

        if (!currentChatRoom || !liveChatMode) {
            return;
        }


        const unansweredCount = Number(currentChatRoom.customerUnansweredCount || 0);


        const guestDailyRemaining = currentChatRoom.guestDailyRemaining;


        const roomClosed = currentChatRoom.status === "CLOSED";


        const waitingAdmin = unansweredCount >= 3;


        const guestLimitReached = guestDailyRemaining === 0;


        const socketDisconnected = !stompClient || !stompClient.connected;


        const disabled = roomClosed || waitingAdmin || guestLimitReached || socketDisconnected;


        chatInput.disabled = disabled;


        chatSendButton.disabled = disabled;


        if (roomClosed) {

            chatNotice.textContent = "종료된 상담입니다.";

            return;
        }


        if (guestLimitReached) {

            chatNotice.textContent = "비회원 하루 메시지 10개를 모두 사용했습니다.";

            return;
        }


        if (waitingAdmin) {

            chatNotice.textContent = "관리자의 답변을 기다려 주세요. " + "답변 전에는 최대 3개까지 보낼 수 있습니다.";

            return;
        }


        if (socketDisconnected) {

            chatNotice.textContent = "실시간 상담 서버에 연결 중입니다.";

            return;
        }


        const remainingBeforeReply = 3 - unansweredCount;


        if (guestDailyRemaining !== null && guestDailyRemaining !== undefined) {

            chatNotice.textContent = "관리자 답변 전 " + remainingBeforeReply + "개 더 전송 가능 · 오늘 " + guestDailyRemaining + "개 남음";

            return;
        }


        chatNotice.textContent = "관리자 답변 전 " + remainingBeforeReply + "개 더 전송할 수 있습니다.";
    }


    /* =====================================================
       읽음 처리
    ===================================================== */

    async function markLiveChatRead() {

        if (!currentChatRoom) {
            return;
        }


        try {

            const response = await fetch("/api/chat/rooms/" + currentChatRoom.roomUuid + "/read",

                {
                    method: "POST",

                    credentials: "same-origin",

                    headers: createCsrfHeaders()
                });


            if (!response.ok) {

                throw await createApiError(response);
            }

        } catch (error) {

            console.error("채팅 읽음 처리 실패:", error);
        }
    }


    /* =====================================================
       메시지 생성 함수
    ===================================================== */

    function clearMessages() {

        messageList.replaceChildren();
    }


    function addUserMessage(text) {

        const messageRow = createMessageRow("user");


        const messageBubble = createMessageBubble();


        messageBubble.textContent = text;


        messageRow.append(messageBubble);


        messageList.append(messageRow);


        scrollToBottom();
    }


    function addBotTextMessage(htmlText) {

        const messageRow = createMessageRow("bot");


        const messageBubble = createMessageBubble();


        messageBubble.innerHTML = htmlText;


        messageRow.append(messageBubble);


        messageList.append(messageRow);


        scrollToBottom();
    }


    function addBotPlainMessage(text) {

        const messageRow = createMessageRow("bot");


        const messageBubble = createMessageBubble();


        messageBubble.textContent = text;


        messageRow.append(messageBubble);


        messageList.append(messageRow);


        scrollToBottom();
    }


    function addBotHtmlMessage(htmlContent) {

        const messageRow = createMessageRow("bot");


        const messageBubble = createMessageBubble();


        messageBubble.innerHTML = htmlContent;


        messageRow.append(messageBubble);


        messageList.append(messageRow);


        scrollToBottom();
    }


    function createMessageRow(type) {

        const messageRow = document.createElement("div");


        messageRow.className = "chatbot-message-row " + type;


        return messageRow;
    }


    function createMessageBubble() {

        const messageBubble = document.createElement("div");


        messageBubble.className = "chatbot-message-bubble";


        return messageBubble;
    }


    /* =====================================================
       선택지 버튼 생성
    ===================================================== */

    function createOptionGroup() {

        const optionGroup = document.createElement("div");


        optionGroup.className = "chatbot-option-group";


        return optionGroup;
    }


    function createOptionButton(text, secondary) {

        const button = document.createElement("button");


        button.type = "button";


        button.className = "chatbot-option-button";


        if (secondary === true) {

            button.classList.add("secondary");
        }


        const textSpan = document.createElement("span");


        textSpan.textContent = text;


        button.append(textSpan);


        return button;
    }


    function disableOptionGroup(optionGroup) {

        if (!optionGroup) {
            return;
        }


        optionGroup
            .querySelectorAll("button")
            .forEach(function (button) {

                button.disabled = true;
            });
    }


    /* =====================================================
       로딩 메시지
    ===================================================== */

    function addLoadingMessage() {

        const messageRow = createMessageRow("bot");


        const messageBubble = createMessageBubble();


        const loading = document.createElement("div");


        loading.className = "chatbot-loading";


        for (let index = 0; index < 3; index++) {

            loading.append(document.createElement("span"));
        }


        messageBubble.append(loading);


        messageRow.append(messageBubble);


        messageList.append(messageRow);


        scrollToBottom();


        return messageRow;
    }


    /* =====================================================
       공통 보조 함수
    ===================================================== */

    function scrollToBottom() {

        window.requestAnimationFrame(function () {

            messageList.scrollTop = messageList.scrollHeight;
        });
    }


    async function createApiError(response) {

        let message = "요청을 처리하지 못했습니다.";


        try {

            const errorResponse = await response.json();


            if (errorResponse.message) {

                message = errorResponse.message;
            }

        } catch (error) {

            console.warn("오류 응답 JSON 변환 실패:", error);
        }


        return new Error(message);
    }


    function createCsrfHeaders() {

        const headers = {};


        const csrfToken = document.querySelector('meta[name="_csrf"]');


        const csrfHeader = document.querySelector('meta[name="_csrf_header"]');


        if (csrfToken && csrfHeader) {

            headers[csrfHeader.content] = csrfToken.content;
        }


        return headers;
    }


    /* =====================================================
       FAQ HTML 정리
    ===================================================== */

    function sanitizeFaqHtml(html) {

        const template = document.createElement("template");


        template.innerHTML = html || "";


        template.content
            .querySelectorAll("script, style, iframe, object, embed, form")
            .forEach(function (element) {

                element.remove();
            });


        template.content
            .querySelectorAll("*")
            .forEach(function (element) {

                Array.from(element.attributes)
                    .forEach(function (attribute) {

                        const attributeName = attribute.name
                            .toLowerCase();


                        const attributeValue = attribute.value
                            .trim()
                            .toLowerCase();


                        if (attributeName
                            .startsWith("on")) {

                            element.removeAttribute(attribute.name);
                        }


                        if ((attributeName === "href" || attributeName === "src") && attributeValue
                            .startsWith("javascript:")) {

                            element.removeAttribute(attribute.name);
                        }
                    });
            });


        return template.innerHTML;
    }
});