/* =====================================================
   1. 비회원 채팅 사용자
===================================================== */

CREATE TABLE CUSTOMER_CHAT_GUEST (
                                     GUEST_ID NUMBER
                                         CONSTRAINT PK_CHAT_GUEST PRIMARY KEY,

                                     GUEST_TOKEN_HASH VARCHAR2(64 CHAR) NOT NULL,

                                     GUEST_NICKNAME VARCHAR2(50 CHAR) NOT NULL,

                                     BLOCKED_YN CHAR(1)
                                         DEFAULT 'N'
                                         NOT NULL,

                                     CREATED_AT TIMESTAMP
                                         DEFAULT SYSTIMESTAMP
                                         NOT NULL,

                                     LAST_SEEN_AT TIMESTAMP
                                         DEFAULT SYSTIMESTAMP
                                         NOT NULL,

                                     EXPIRES_AT TIMESTAMP,

                                     CONSTRAINT UQ_CHAT_GUEST_TOKEN
                                         UNIQUE (GUEST_TOKEN_HASH),

                                     CONSTRAINT CK_CHAT_GUEST_BLOCKED
                                         CHECK (BLOCKED_YN IN ('Y', 'N'))
);


CREATE SEQUENCE SEQ_CUSTOMER_CHAT_GUEST
    START WITH 1
    INCREMENT BY 1
    NOCACHE;



/* =====================================================
   2. 고객센터 1:1 채팅방

   VISITOR_TYPE
   - MEMBER
   - GUEST

   STATUS
   - WAITING
   - CHATTING
   - CLOSED

   CUSTOMER_UNANSWERED_COUNT
   - 관리자 답장 이후 고객이 연속 전송한 개수
   - 최대 3개
===================================================== */

CREATE TABLE CUSTOMER_CHAT_ROOM (
                                    ROOM_ID NUMBER
                                        CONSTRAINT PK_CHAT_ROOM PRIMARY KEY,

                                    ROOM_UUID VARCHAR2(36 CHAR) NOT NULL,

                                    VISITOR_TYPE VARCHAR2(10 CHAR) NOT NULL,

                                    MEMBER_ID NUMBER,

                                    GUEST_ID NUMBER,

                                    CUSTOMER_NAME_SNAPSHOT VARCHAR2(100 CHAR) NOT NULL,

                                    ASSIGNED_ADMIN_ID NUMBER,

                                    STATUS VARCHAR2(20 CHAR)
        DEFAULT 'WAITING'
        NOT NULL,

                                    CUSTOMER_UNANSWERED_COUNT NUMBER
                                        DEFAULT 0
                                        NOT NULL,

                                    ADMIN_UNREAD_COUNT NUMBER
                                        DEFAULT 0
                                        NOT NULL,

                                    CUSTOMER_UNREAD_COUNT NUMBER
                                        DEFAULT 0
                                        NOT NULL,

                                    LAST_MESSAGE_PREVIEW VARCHAR2(100 CHAR),

                                    LAST_MESSAGE_SENDER_TYPE VARCHAR2(10 CHAR),

                                    CREATED_AT TIMESTAMP
                                        DEFAULT SYSTIMESTAMP
                                        NOT NULL,

                                    ACCEPTED_AT TIMESTAMP,

                                    LAST_MESSAGE_AT TIMESTAMP
                                        DEFAULT SYSTIMESTAMP
                                        NOT NULL,

                                    UPDATED_AT TIMESTAMP
                                        DEFAULT SYSTIMESTAMP
                                        NOT NULL,

                                    CLOSED_AT TIMESTAMP,

                                    CONSTRAINT UQ_CHAT_ROOM_UUID
                                        UNIQUE (ROOM_UUID),

                                    CONSTRAINT FK_CHAT_ROOM_MEMBER
                                        FOREIGN KEY (MEMBER_ID)
                                            REFERENCES APP_MEMBER(MEMBER_ID)
                                            ON DELETE SET NULL,

                                    CONSTRAINT FK_CHAT_ROOM_GUEST
                                        FOREIGN KEY (GUEST_ID)
                                            REFERENCES CUSTOMER_CHAT_GUEST(GUEST_ID),

                                    CONSTRAINT FK_CHAT_ROOM_ADMIN
                                        FOREIGN KEY (ASSIGNED_ADMIN_ID)
                                            REFERENCES APP_MEMBER(MEMBER_ID)
                                            ON DELETE SET NULL,

                                    CONSTRAINT CK_CHAT_ROOM_VISITOR_TYPE
                                        CHECK (
                                            VISITOR_TYPE IN ('MEMBER', 'GUEST')
                                            ),

                                    CONSTRAINT CK_CHAT_ROOM_VISITOR
                                        CHECK (
                                            (
                                                VISITOR_TYPE = 'MEMBER'
                                                    AND GUEST_ID IS NULL
                                                )
                                                OR
                                            (
                                                VISITOR_TYPE = 'GUEST'
                                                    AND MEMBER_ID IS NULL
                                                    AND GUEST_ID IS NOT NULL
                                                )
                                            ),

                                    CONSTRAINT CK_CHAT_ROOM_STATUS
                                        CHECK (
                                            STATUS IN (
                                                       'WAITING',
                                                       'CHATTING',
                                                       'CLOSED'
                                                )
                                            ),

                                    CONSTRAINT CK_CHAT_ROOM_UNANSWERED
                                        CHECK (
                                            CUSTOMER_UNANSWERED_COUNT
                                                BETWEEN 0 AND 3
                                            ),

                                    CONSTRAINT CK_CHAT_ROOM_ADMIN_UNREAD
                                        CHECK (
                                            ADMIN_UNREAD_COUNT >= 0
                                            ),

                                    CONSTRAINT CK_CHAT_ROOM_USER_UNREAD
                                        CHECK (
                                            CUSTOMER_UNREAD_COUNT >= 0
                                            ),

                                    CONSTRAINT CK_CHAT_ROOM_LAST_SENDER
                                        CHECK (
                                            LAST_MESSAGE_SENDER_TYPE IS NULL
                                                OR LAST_MESSAGE_SENDER_TYPE IN (
                                                                                'MEMBER',
                                                                                'GUEST',
                                                                                'ADMIN',
                                                                                'SYSTEM'
                                                )
                                            )
);


CREATE SEQUENCE SEQ_CUSTOMER_CHAT_ROOM
    START WITH 1
    INCREMENT BY 1
    NOCACHE;



/* =====================================================
   3. 고객센터 1:1 채팅 메시지

   SENDER_TYPE
   - MEMBER
   - GUEST
   - ADMIN
   - SYSTEM

   메시지 최대 길이
   - 500자
===================================================== */

CREATE TABLE CUSTOMER_CHAT_MESSAGE (
                                       MESSAGE_ID NUMBER
                                           CONSTRAINT PK_CHAT_MESSAGE PRIMARY KEY,

                                       ROOM_ID NUMBER NOT NULL,

                                       CLIENT_MESSAGE_UUID VARCHAR2(36 CHAR) NOT NULL,

                                       SENDER_TYPE VARCHAR2(10 CHAR) NOT NULL,

                                       SENDER_MEMBER_ID NUMBER,

                                       SENDER_NAME_SNAPSHOT VARCHAR2(100 CHAR) NOT NULL,

                                       MESSAGE_TYPE VARCHAR2(10 CHAR)
        DEFAULT 'TEXT'
        NOT NULL,

                                       CONTENT VARCHAR2(500 CHAR) NOT NULL,

                                       CREATED_AT TIMESTAMP
                                           DEFAULT SYSTIMESTAMP
                                                      NOT NULL,

                                       READ_AT TIMESTAMP,

                                       CONSTRAINT UQ_CHAT_MESSAGE_UUID
                                           UNIQUE (CLIENT_MESSAGE_UUID),

                                       CONSTRAINT FK_CHAT_MESSAGE_ROOM
                                           FOREIGN KEY (ROOM_ID)
                                               REFERENCES CUSTOMER_CHAT_ROOM(ROOM_ID)
                                               ON DELETE CASCADE,

                                       CONSTRAINT FK_CHAT_MESSAGE_MEMBER
                                           FOREIGN KEY (SENDER_MEMBER_ID)
                                               REFERENCES APP_MEMBER(MEMBER_ID)
                                               ON DELETE SET NULL,

                                       CONSTRAINT CK_CHAT_MESSAGE_SENDER
                                           CHECK (
                                               SENDER_TYPE IN (
                                                               'MEMBER',
                                                               'GUEST',
                                                               'ADMIN',
                                                               'SYSTEM'
                                                   )
                                               ),

                                       CONSTRAINT CK_CHAT_MESSAGE_TYPE
                                           CHECK (
                                               MESSAGE_TYPE IN (
                                                                'TEXT',
                                                                'SYSTEM'
                                                   )
                                               ),

                                       CONSTRAINT CK_CHAT_MESSAGE_CONTENT
                                           CHECK (
                                               TRIM(CONTENT) IS NOT NULL
                                               )
);


CREATE SEQUENCE SEQ_CUSTOMER_CHAT_MESSAGE
    START WITH 1
    INCREMENT BY 1
    NOCACHE;



/* =====================================================
   4. 비회원 일일 메시지 사용량

   비회원은 하루 최대 10개
===================================================== */

CREATE TABLE CHAT_GUEST_DAILY_USAGE (
                                        GUEST_ID NUMBER NOT NULL,

                                        USAGE_DATE DATE NOT NULL,

                                        MESSAGE_COUNT NUMBER
                                            DEFAULT 0
                                                        NOT NULL,

                                        UPDATED_AT TIMESTAMP
                                            DEFAULT SYSTIMESTAMP
                                                        NOT NULL,

                                        CONSTRAINT PK_CHAT_GUEST_DAILY
                                            PRIMARY KEY (
                                                         GUEST_ID,
                                                         USAGE_DATE
                                                ),

                                        CONSTRAINT FK_CHAT_DAILY_GUEST
                                            FOREIGN KEY (GUEST_ID)
                                                REFERENCES CUSTOMER_CHAT_GUEST(GUEST_ID)
                                                ON DELETE CASCADE,

                                        CONSTRAINT CK_CHAT_DAILY_DATE
                                            CHECK (
                                                USAGE_DATE = TRUNC(USAGE_DATE)
                                                ),

                                        CONSTRAINT CK_CHAT_DAILY_COUNT
                                            CHECK (
                                                MESSAGE_COUNT
                                                    BETWEEN 0 AND 10
                                                )
);



/* =====================================================
   5. 일반 조회용 인덱스
===================================================== */

/* 채팅방의 메시지를 시간순으로 조회 */
CREATE INDEX IDX_CHAT_MESSAGE_ROOM_DATE
    ON CUSTOMER_CHAT_MESSAGE (
                              ROOM_ID,
                              CREATED_AT
        );


/* 읽지 않은 메시지 조회 */
CREATE INDEX IDX_CHAT_MESSAGE_UNREAD
    ON CUSTOMER_CHAT_MESSAGE (
                              ROOM_ID,
                              SENDER_TYPE,
                              READ_AT
        );


/* 전체 관리자 채팅방 목록 */
CREATE INDEX IDX_CHAT_ROOM_STATUS_DATE
    ON CUSTOMER_CHAT_ROOM (
                           STATUS,
                           LAST_MESSAGE_AT
        );


/* 담당 관리자별 채팅방 목록 */
CREATE INDEX IDX_CHAT_ROOM_ADMIN
    ON CUSTOMER_CHAT_ROOM (
                           ASSIGNED_ADMIN_ID,
                           STATUS,
                           LAST_MESSAGE_AT
        );



/* =====================================================
   6. 활성 채팅방 중복 방지 인덱스

   Oracle에서 일반 CREATE INDEX로 실행하면
   ORA-02158이 발생하여 동적 SQL로 생성한다.

   회원 1명당 활성 채팅방 1개
===================================================== */

BEGIN

EXECUTE IMMEDIATE q'~

        CREATE UNIQUE INDEX UQ_CHAT_ACTIVE_MEMBER
        ON CUSTOMER_CHAT_ROOM (
            CASE
                WHEN VISITOR_TYPE = 'MEMBER'
                 AND STATUS IN ('WAITING', 'CHATTING')
                THEN MEMBER_ID
            END
        )

    ~';

END;



/* =====================================================
   비회원 1명당 활성 채팅방 1개
===================================================== */

BEGIN

EXECUTE IMMEDIATE q'~

        CREATE UNIQUE INDEX UQ_CHAT_ACTIVE_GUEST
        ON CUSTOMER_CHAT_ROOM (
            CASE
                WHEN VISITOR_TYPE = 'GUEST'
                 AND STATUS IN ('WAITING', 'CHATTING')
                THEN GUEST_ID
            END
        )

    ~';

END;


-- 여긴 확인부분
SELECT
    TABLE_NAME
FROM USER_TABLES
WHERE TABLE_NAME IN (
                     'CUSTOMER_CHAT_GUEST',
                     'CUSTOMER_CHAT_ROOM',
                     'CUSTOMER_CHAT_MESSAGE',
                     'CHAT_GUEST_DAILY_USAGE'
    )
ORDER BY TABLE_NAME;


SELECT
    TABLE_NAME,
    COLUMN_ID,
    COLUMN_NAME,
    DATA_TYPE,
    DATA_LENGTH,
    CHAR_LENGTH,
    NULLABLE,
    DATA_DEFAULT
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME IN (
                     'CUSTOMER_CHAT_GUEST',
                     'CUSTOMER_CHAT_ROOM',
                     'CUSTOMER_CHAT_MESSAGE',
                     'CHAT_GUEST_DAILY_USAGE'
    )
ORDER BY
    TABLE_NAME,
    COLUMN_ID;


SELECT
    SEQUENCE_NAME,
    MIN_VALUE,
    INCREMENT_BY,
    CACHE_SIZE,
    LAST_NUMBER
FROM USER_SEQUENCES
WHERE SEQUENCE_NAME IN (
                        'SEQ_CUSTOMER_CHAT_GUEST',
                        'SEQ_CUSTOMER_CHAT_ROOM',
                        'SEQ_CUSTOMER_CHAT_MESSAGE'
    )
ORDER BY SEQUENCE_NAME;


SELECT
    TABLE_NAME,
    INDEX_NAME,
    INDEX_TYPE,
    UNIQUENESS,
    STATUS
FROM USER_INDEXES
WHERE TABLE_NAME IN (
                     'CUSTOMER_CHAT_GUEST',
                     'CUSTOMER_CHAT_ROOM',
                     'CUSTOMER_CHAT_MESSAGE',
                     'CHAT_GUEST_DAILY_USAGE'
    )
ORDER BY
    TABLE_NAME,
    INDEX_NAME;

SELECT
    UI.TABLE_NAME,
    UI.INDEX_NAME,
    UI.UNIQUENESS,
    UIC.COLUMN_POSITION,
    UIC.COLUMN_NAME
FROM USER_INDEXES UI

         JOIN USER_IND_COLUMNS UIC
              ON UI.INDEX_NAME = UIC.INDEX_NAME
                  AND UI.TABLE_NAME = UIC.TABLE_NAME

WHERE UI.TABLE_NAME IN (
                        'CUSTOMER_CHAT_GUEST',
                        'CUSTOMER_CHAT_ROOM',
                        'CUSTOMER_CHAT_MESSAGE',
                        'CHAT_GUEST_DAILY_USAGE'
    )

ORDER BY
    UI.TABLE_NAME,
    UI.INDEX_NAME,
    UIC.COLUMN_POSITION;

SELECT
    INDEX_NAME,
    TABLE_NAME,
    COLUMN_POSITION,
    COLUMN_EXPRESSION
FROM USER_IND_EXPRESSIONS
WHERE INDEX_NAME IN (
                     'UQ_CHAT_ACTIVE_MEMBER',
                     'UQ_CHAT_ACTIVE_GUEST'
    )
ORDER BY
    INDEX_NAME,
    COLUMN_POSITION;