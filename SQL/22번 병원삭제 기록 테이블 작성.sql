CREATE TABLE HOSPITAL_DELETE_HISTORY (
                                         DELETE_HISTORY_ID  NUMBER
                                             CONSTRAINT HOSPITAL_DELETE_HISTORY_PK
                                                 PRIMARY KEY,

                                         HOSPITAL_ID        NUMBER NOT NULL,

                                         HOSPITAL_NAME      VARCHAR2(200) NOT NULL,

                                         OWNER_ID           NUMBER,

                                         OWNER_NAME         VARCHAR2(100),

                                         CLOSE_REQUEST_ID   NUMBER,

                                         DELETE_REASON      VARCHAR2(1000),

                                         CLOSED_AT          TIMESTAMP,

                                         DELETED_AT         TIMESTAMP
                                             DEFAULT SYSTIMESTAMP
                                                                   NOT NULL,

                                         DELETED_BY         NUMBER NOT NULL
);

CREATE SEQUENCE SEQ_HOSPITAL_DELETE_HISTORY
    START WITH 1
    INCREMENT BY 1
    NOCACHE
NOCYCLE;