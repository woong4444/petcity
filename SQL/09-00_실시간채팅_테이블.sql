/* ============================================================================
   PetCity 09 - 실시간 1:1 채팅

   선행 파일: 01_member.sql
   포함 내용: 테이블, PK/UNIQUE/CHECK, 외래키, 관련 인덱스, 관련 시퀀스
   제외 내용: 테스트·샘플 회원/병원/게시글/리뷰/신청 더미 데이터
============================================================================ */


/* 1. 시퀀스 */

CREATE SEQUENCE SEQ_CUSTOMER_CHAT_GUEST START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE SEQUENCE SEQ_CUSTOMER_CHAT_ROOM START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE SEQUENCE SEQ_CUSTOMER_CHAT_MESSAGE START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


/* 2. 테이블 */

CREATE TABLE CUSTOMER_CHAT_GUEST (
    GUEST_ID NUMBER,
    GUEST_TOKEN_HASH VARCHAR2(64 CHAR) NOT NULL,
    GUEST_NICKNAME VARCHAR2(50 CHAR) NOT NULL,
    BLOCKED_YN CHAR(1) DEFAULT 'N' NOT NULL,
    CREATED_AT TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL,
    LAST_SEEN_AT TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL,
    EXPIRES_AT TIMESTAMP (6),
    CONSTRAINT CK_CHAT_GUEST_BLOCKED CHECK (BLOCKED_YN IN ('Y', 'N')),
    CONSTRAINT PK_CHAT_GUEST PRIMARY KEY (GUEST_ID),
    CONSTRAINT UQ_CHAT_GUEST_TOKEN UNIQUE (GUEST_TOKEN_HASH)
);

CREATE TABLE CHAT_GUEST_DAILY_USAGE (
    GUEST_ID NUMBER NOT NULL,
    USAGE_DATE DATE NOT NULL,
    MESSAGE_COUNT NUMBER DEFAULT 0 NOT NULL,
    UPDATED_AT TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT CK_CHAT_DAILY_DATE CHECK (
                                                    USAGE_DATE = TRUNC(USAGE_DATE)
                                                    ),
    CONSTRAINT CK_CHAT_DAILY_COUNT CHECK (
                                                    MESSAGE_COUNT
                                                        BETWEEN 0 AND 10
                                                    ),
    CONSTRAINT PK_CHAT_GUEST_DAILY PRIMARY KEY (GUEST_ID, USAGE_DATE)
);

CREATE TABLE CUSTOMER_CHAT_ROOM (
    ROOM_ID NUMBER,
    ROOM_UUID VARCHAR2(36 CHAR) NOT NULL,
    VISITOR_TYPE VARCHAR2(10 CHAR) NOT NULL,
    MEMBER_ID NUMBER,
    GUEST_ID NUMBER,
    CUSTOMER_NAME_SNAPSHOT VARCHAR2(100 CHAR) NOT NULL,
    ASSIGNED_ADMIN_ID NUMBER,
    STATUS VARCHAR2(20 CHAR) DEFAULT 'WAITING' NOT NULL,
    CUSTOMER_UNANSWERED_COUNT NUMBER DEFAULT 0 NOT NULL,
    ADMIN_UNREAD_COUNT NUMBER DEFAULT 0 NOT NULL,
    CUSTOMER_UNREAD_COUNT NUMBER DEFAULT 0 NOT NULL,
    LAST_MESSAGE_PREVIEW VARCHAR2(100 CHAR),
    LAST_MESSAGE_SENDER_TYPE VARCHAR2(10 CHAR),
    CREATED_AT TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL,
    ACCEPTED_AT TIMESTAMP (6),
    LAST_MESSAGE_AT TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL,
    CLOSED_AT TIMESTAMP (6),
    CONSTRAINT CK_CHAT_ROOM_VISITOR_TYPE CHECK (
                                                VISITOR_TYPE IN ('MEMBER', 'GUEST')
                                                ),
    CONSTRAINT CK_CHAT_ROOM_VISITOR CHECK (
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
    CONSTRAINT CK_CHAT_ROOM_STATUS CHECK (
                                                STATUS IN (
                                                           'WAITING',
                                                           'CHATTING',
                                                           'CLOSED'
                                                    )
                                                ),
    CONSTRAINT CK_CHAT_ROOM_UNANSWERED CHECK (
                                                CUSTOMER_UNANSWERED_COUNT
                                                    BETWEEN 0 AND 3
                                                ),
    CONSTRAINT CK_CHAT_ROOM_ADMIN_UNREAD CHECK (
                                                ADMIN_UNREAD_COUNT >= 0
                                                ),
    CONSTRAINT CK_CHAT_ROOM_USER_UNREAD CHECK (
                                                CUSTOMER_UNREAD_COUNT >= 0
                                                ),
    CONSTRAINT CK_CHAT_ROOM_LAST_SENDER CHECK (
                                                LAST_MESSAGE_SENDER_TYPE IS NULL
                                                    OR LAST_MESSAGE_SENDER_TYPE IN (
                                                                                    'MEMBER',
                                                                                    'GUEST',
                                                                                    'ADMIN',
                                                                                    'SYSTEM'
                                                    )
                                                ),
    CONSTRAINT PK_CHAT_ROOM PRIMARY KEY (ROOM_ID),
    CONSTRAINT UQ_CHAT_ROOM_UUID UNIQUE (ROOM_UUID)
);

CREATE TABLE CUSTOMER_CHAT_MESSAGE (
    MESSAGE_ID NUMBER,
    ROOM_ID NUMBER NOT NULL,
    CLIENT_MESSAGE_UUID VARCHAR2(36 CHAR) NOT NULL,
    SENDER_TYPE VARCHAR2(10 CHAR) NOT NULL,
    SENDER_MEMBER_ID NUMBER,
    SENDER_NAME_SNAPSHOT VARCHAR2(100 CHAR) NOT NULL,
    MESSAGE_TYPE VARCHAR2(10 CHAR) DEFAULT 'TEXT' NOT NULL,
    CONTENT VARCHAR2(500 CHAR) NOT NULL,
    CREATED_AT TIMESTAMP (6) DEFAULT SYSTIMESTAMP NOT NULL,
    READ_AT TIMESTAMP (6),
    CONSTRAINT CK_CHAT_MESSAGE_SENDER CHECK (
                                                   SENDER_TYPE IN (
                                                                   'MEMBER',
                                                                   'GUEST',
                                                                   'ADMIN',
                                                                   'SYSTEM'
                                                       )
                                                   ),
    CONSTRAINT CK_CHAT_MESSAGE_TYPE CHECK (
                                                   MESSAGE_TYPE IN (
                                                                    'TEXT',
                                                                    'SYSTEM'
                                                       )
                                                   ),
    CONSTRAINT CK_CHAT_MESSAGE_CONTENT CHECK (
                                                   TRIM(CONTENT) IS NOT NULL
                                                   ),
    CONSTRAINT PK_CHAT_MESSAGE PRIMARY KEY (MESSAGE_ID),
    CONSTRAINT UQ_CHAT_MESSAGE_UUID UNIQUE (CLIENT_MESSAGE_UUID)
);


/* 3. 외래키 */

ALTER TABLE CHAT_GUEST_DAILY_USAGE
    ADD CONSTRAINT FK_CHAT_DAILY_GUEST
    FOREIGN KEY (GUEST_ID)
    REFERENCES CUSTOMER_CHAT_GUEST (GUEST_ID)
    ON DELETE CASCADE;

ALTER TABLE CUSTOMER_CHAT_ROOM
    ADD CONSTRAINT FK_CHAT_ROOM_MEMBER
    FOREIGN KEY (MEMBER_ID)
    REFERENCES APP_MEMBER (MEMBER_ID)
    ON DELETE SET NULL;

ALTER TABLE CUSTOMER_CHAT_ROOM
    ADD CONSTRAINT FK_CHAT_ROOM_GUEST
    FOREIGN KEY (GUEST_ID)
    REFERENCES CUSTOMER_CHAT_GUEST (GUEST_ID);

ALTER TABLE CUSTOMER_CHAT_ROOM
    ADD CONSTRAINT FK_CHAT_ROOM_ADMIN
    FOREIGN KEY (ASSIGNED_ADMIN_ID)
    REFERENCES APP_MEMBER (MEMBER_ID)
    ON DELETE SET NULL;

ALTER TABLE CUSTOMER_CHAT_MESSAGE
    ADD CONSTRAINT FK_CHAT_MESSAGE_ROOM
    FOREIGN KEY (ROOM_ID)
    REFERENCES CUSTOMER_CHAT_ROOM (ROOM_ID)
    ON DELETE CASCADE;

ALTER TABLE CUSTOMER_CHAT_MESSAGE
    ADD CONSTRAINT FK_CHAT_MESSAGE_MEMBER
    FOREIGN KEY (SENDER_MEMBER_ID)
    REFERENCES APP_MEMBER (MEMBER_ID)
    ON DELETE SET NULL;


/* 4. 조회·업무 인덱스 */

CREATE UNIQUE INDEX UQ_CHAT_ACTIVE_GUEST
    ON CUSTOMER_CHAT_ROOM (CASE WHEN (VISITOR_TYPE='GUEST' AND (STATUS='WAITING' OR STATUS='CHATTING')) THEN GUEST_ID END);

CREATE INDEX IDX_CHAT_ROOM_STATUS_DATE
    ON CUSTOMER_CHAT_ROOM (STATUS, LAST_MESSAGE_AT);

CREATE INDEX IDX_CHAT_ROOM_ADMIN
    ON CUSTOMER_CHAT_ROOM (ASSIGNED_ADMIN_ID, STATUS, LAST_MESSAGE_AT);

CREATE UNIQUE INDEX UQ_CHAT_ACTIVE_MEMBER
    ON CUSTOMER_CHAT_ROOM (CASE WHEN (VISITOR_TYPE='MEMBER' AND (STATUS='WAITING' OR STATUS='CHATTING')) THEN MEMBER_ID END);

CREATE INDEX IDX_CHAT_MESSAGE_ROOM_DATE
    ON CUSTOMER_CHAT_MESSAGE (ROOM_ID, CREATED_AT);

CREATE INDEX IDX_CHAT_MESSAGE_UNREAD
    ON CUSTOMER_CHAT_MESSAGE (ROOM_ID, SENDER_TYPE, READ_AT);


COMMIT;
