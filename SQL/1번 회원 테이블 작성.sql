
/* =====================================================
   1. 회원 테이블
===================================================== */

CREATE TABLE APP_MEMBER (
                            MEMBER_ID NUMBER PRIMARY KEY,

                            NICKNAME VARCHAR2(50 CHAR) NOT NULL,
                            EMAIL VARCHAR2(100 CHAR),
                            PHONE VARCHAR2(30 CHAR),

                            ROLE VARCHAR2(20 CHAR) DEFAULT 'USER' NOT NULL
        CHECK (ROLE IN ('USER', 'OWNER', 'ADMIN')),

                            CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                            UPDATED_AT TIMESTAMP,

                            PROFILE_IMAGE VARCHAR2(1000 CHAR),

                            LOGIN_ID VARCHAR2(150 CHAR) UNIQUE NOT NULL,
                            PASSWORD VARCHAR2(200 CHAR) NOT NULL,

                            EMAIL_VERIFIED CHAR(1) DEFAULT 'N' NOT NULL
                                CHECK (EMAIL_VERIFIED IN ('Y', 'N')),

                            STATUS VARCHAR2(20 CHAR) DEFAULT 'ACTIVE' NOT NULL
        CHECK (STATUS IN ('ACTIVE', 'BLOCKED', 'DELETED')),

                            LAST_LOGIN_AT TIMESTAMP,

                            MEMBER_STATUS VARCHAR2(30 CHAR) DEFAULT 'ACTIVE' NOT NULL
        CHECK (MEMBER_STATUS IN ('ACTIVE', 'DELETE_PENDING', 'DELETED')),

                            DELETE_REQUESTED_AT TIMESTAMP,
                            HARD_DELETE_AT TIMESTAMP,
                            DELETED_AT TIMESTAMP
);

CREATE SEQUENCE SEQ_APP_MEMBER
    START WITH 1
    INCREMENT BY 1
    NOCACHE;



-------------추가 사항
ALTER TABLE APP_MEMBER
    ADD (
    BLOCK_REASON VARCHAR2(500),
    BLOCKED_AT TIMESTAMP
);

ALTER TABLE APP_MEMBER
    ADD (
    BLOCKED_BY NUMBER
);
/* =====================================================
   추가사항 7-15 회원 타입 관련 컬럼 추가
ALTER TABLE APP_MEMBER
    ADD LOGIN_TYPE VARCHAR2(20);

ALTER TABLE APP_MEMBER
    ADD SOCIAL_ID VARCHAR2(100);

ALTER TABLE APP_MEMBER
    ADD AGREEMENT_EMAIL CHAR(1) DEFAULT 'N';
