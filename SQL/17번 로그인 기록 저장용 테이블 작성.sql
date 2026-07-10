CREATE TABLE SITE_LOGIN_HISTORY (
                                    LOGIN_HISTORY_ID NUMBER PRIMARY KEY,

                                    MEMBER_ID NUMBER,
                                    LOGIN_ID VARCHAR2(100 CHAR),
                                    NICKNAME VARCHAR2(100 CHAR),
                                    ROLE VARCHAR2(30 CHAR),

                                    SESSION_ID VARCHAR2(200 CHAR),
                                    LOGIN_AT TIMESTAMP NOT NULL,

                                    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE SEQUENCE SEQ_SITE_LOGIN_HISTORY
    START WITH 1
    INCREMENT BY 1
    NOCACHE;