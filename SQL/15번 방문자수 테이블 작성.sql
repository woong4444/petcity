CREATE TABLE SITE_VISIT_LOG (
                                VISIT_ID NUMBER PRIMARY KEY,
                                SESSION_ID VARCHAR2(200 CHAR),
                                IP_ADDRESS VARCHAR2(100 CHAR),
                                USER_AGENT VARCHAR2(1000 CHAR),
                                VISITED_URL VARCHAR2(1000 CHAR),
                                CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE SEQUENCE SEQ_SITE_VISIT_LOG
    START WITH 1
    INCREMENT BY 1
    NOCACHE;