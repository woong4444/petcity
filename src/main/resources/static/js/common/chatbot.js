document.addEventListener("DOMContentLoaded", function () {

    /* =====================================================
       HTML 요소 가져오기
    ===================================================== */

    const chatbot = document.getElementById("petcityChatbot");
    const toggleButton = document.getElementById("chatbotToggleButton");
    const closeButton = document.getElementById("chatbotCloseButton");
    const homeButton = document.getElementById("chatbotHomeButton");
    const panel = document.getElementById("chatbotPanel");
    const messageList = document.getElementById("chatbotMessageList");


    /*
        공통 헤더가 없는 페이지에서는
        챗봇 HTML도 존재하지 않는다.

        이 경우 아래 코드를 실행하지 않는다.
    */
    if (
        !chatbot
        || !toggleButton
        || !closeButton
        || !homeButton
        || !panel
        || !messageList
    ) {
        return;
    }


    /* =====================================================
       현재 선택 상태
    ===================================================== */

    let currentCategoryId = null;
    let currentCategoryName = "";
    let currentFaqList = [];

    let chatbotStarted = false;


    /* =====================================================
       챗봇 열기 및 닫기
    ===================================================== */

    toggleButton.addEventListener("click", function () {

        openChatbot();

        /*
            처음 열었을 때만
            카테고리를 조회한다.
        */
        if (!chatbotStarted) {

            chatbotStarted = true;

            showChatbotHome();
        }
    });


    closeButton.addEventListener("click", function () {

        closeChatbot();
    });


    homeButton.addEventListener("click", function () {

        showChatbotHome();
    });


    /*
        ESC 키를 누르면 챗봇 닫기
    */
    document.addEventListener("keydown", function (event) {

        if (event.key === "Escape") {
            closeChatbot();
        }
    });


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

        currentCategoryId = null;
        currentCategoryName = "";
        currentFaqList = [];

        clearMessages();

        addBotTextMessage(
            "안녕하세요.<br>"
            + "PetCity 고객센터입니다.<br>"
            + "궁금한 분야를 선택해 주세요."
        );

        const loadingMessage = addLoadingMessage();

        try {

            const response = await fetch(
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
        }
    }


    /* =====================================================
       카테고리 버튼 출력
    ===================================================== */

    function renderCategoryButtons(categoryList) {

        const optionGroup =
            createOptionGroup();

        categoryList.forEach(function (category) {

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

            optionGroup.append(button);
        });

        messageList.append(optionGroup);

        scrollToBottom();
    }


    /* =====================================================
       카테고리 선택
    ===================================================== */

    async function selectCategory(category) {

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

            const response = await fetch(
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
                    selectCategory(category);
                }
            );
        }
    }


    /* =====================================================
       FAQ 질문 버튼 출력
    ===================================================== */

    function renderFaqButtons(faqList) {

        const optionGroup =
            createOptionGroup();

        faqList.forEach(function (faq) {

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

                    selectFaq(faq);
                }
            );

            optionGroup.append(button);
        });


        /*
            다른 카테고리 보기 버튼
        */
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
       FAQ 질문 선택
    ===================================================== */

    function selectFaq(faq) {

        addUserMessage(
            faq.title
        );

        /*
            BOARD.CONTENT에는
            <p> 등의 HTML이 들어 있다.

            따라서 textContent가 아니라
            HTML 형태로 출력한다.
        */
        addBotHtmlMessage(
            sanitizeFaqHtml(faq.content)
        );

        addBotTextMessage(
            "답변이 도움이 되었나요?"
        );

        renderAnswerButtons();
    }


    /* =====================================================
       답변 이후 선택지
    ===================================================== */

    function renderAnswerButtons() {

        const optionGroup =
            createOptionGroup();


        /* 해결됐어요 */

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


        /* 같은 카테고리 질문 다시 보기 */

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


        /* 1:1 상담 */

        const counselButton =
            createOptionButton(
                "1:1 상담 연결",
                true
            );

        counselButton.addEventListener(
            "click",
            function () {

                disableOptionGroup(
                    optionGroup
                );

                addUserMessage(
                    "1:1 상담 연결"
                );

                addBotTextMessage(
                    "1:1 상담 기능은 현재 준비 중입니다.<br>"
                    + "우선 FAQ 전체 보기에서 추가 내용을 확인해 주세요."
                );

                renderFinishButtons();
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
       답변 완료 이후 버튼
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
       처음으로 돌아가기 버튼
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

        optionGroup.append(button);

        messageList.append(
            optionGroup
        );

        scrollToBottom();
    }


    /* =====================================================
       다시 시도 버튼
    ===================================================== */

    function renderRetryButton(retryFunction) {

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
       메시지 생성 공통 함수
    ===================================================== */

    function clearMessages() {

        messageList.replaceChildren();
    }


    function addUserMessage(text) {

        const messageRow =
            createMessageRow("user");

        const messageBubble =
            createMessageBubble();

        /*
            사용자 선택값은 일반 문자열이므로
            textContent로 넣는다.
        */
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


    function addBotTextMessage(htmlText) {

        const messageRow =
            createMessageRow("bot");

        const messageBubble =
            createMessageBubble();

        /*
            여기로 전달되는 문장은
            우리가 JS에 직접 작성한 고정 문장이다.
        */
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


    function addBotHtmlMessage(htmlContent) {

        const messageRow =
            createMessageRow("bot");

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
            document.createElement("div");

        messageRow.className =
            "chatbot-message-row "
            + type;

        return messageRow;
    }


    function createMessageBubble() {

        const messageBubble =
            document.createElement("div");

        messageBubble.className =
            "chatbot-message-bubble";

        return messageBubble;
    }


    /* =====================================================
       선택지 버튼 생성
    ===================================================== */

    function createOptionGroup() {

        const optionGroup =
            document.createElement("div");

        optionGroup.className =
            "chatbot-option-group";

        return optionGroup;
    }


    function createOptionButton(
        text,
        secondary
    ) {

        const button =
            document.createElement("button");

        button.type = "button";

        button.className =
            "chatbot-option-button";

        if (secondary === true) {

            button.classList.add(
                "secondary"
            );
        }

        const textSpan =
            document.createElement("span");

        textSpan.textContent =
            text;

        button.append(
            textSpan
        );

        return button;
    }


    function disableOptionGroup(optionGroup) {

        if (!optionGroup) {
            return;
        }

        const buttonList =
            optionGroup.querySelectorAll(
                "button"
            );

        buttonList.forEach(
            function (button) {

                button.disabled = true;
            }
        );
    }


    /* =====================================================
       로딩 메시지
    ===================================================== */

    function addLoadingMessage() {

        const messageRow =
            createMessageRow("bot");

        const messageBubble =
            createMessageBubble();

        const loading =
            document.createElement("div");

        loading.className =
            "chatbot-loading";

        for (let i = 0; i < 3; i++) {

            const dot =
                document.createElement("span");

            loading.append(dot);
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
       가장 아래로 스크롤
    ===================================================== */

    function scrollToBottom() {

        window.requestAnimationFrame(
            function () {

                messageList.scrollTop =
                    messageList.scrollHeight;
            }
        );
    }


    /* =====================================================
       FAQ HTML 정리

       BOARD.CONTENT에 들어 있는 HTML에서
       script 같은 위험한 태그를 제거한다.
    ===================================================== */

    function sanitizeFaqHtml(html) {

        const template =
            document.createElement("template");

        template.innerHTML =
            html || "";

        /*
            실행될 가능성이 있는 태그 제거
        */
        const dangerousElements =
            template.content.querySelectorAll(
                "script, style, iframe, object, embed, form"
            );

        dangerousElements.forEach(
            function (element) {

                element.remove();
            }
        );


        /*
            onclick, onerror 같은
            이벤트 속성 제거
        */
        const allElements =
            template.content.querySelectorAll("*");

        allElements.forEach(
            function (element) {

                const attributes =
                    Array.from(
                        element.attributes
                    );

                attributes.forEach(
                    function (attribute) {

                        const attributeName =
                            attribute.name.toLowerCase();

                        const attributeValue =
                            attribute.value
                                .trim()
                                .toLowerCase();

                        if (
                            attributeName.startsWith("on")
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
                            && attributeValue.startsWith(
                                "javascript:"
                            )
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