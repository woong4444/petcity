
/* =====================================================
   11. 병원장 신청 테이블
===================================================== */

CREATE TABLE HOSPITAL_OWNER_REQUEST (
                                        REQUEST_ID NUMBER PRIMARY KEY,

                                        MEMBER_ID NUMBER NOT NULL,
                                        HOSPITAL_ID NUMBER NOT NULL,

                                        BUSINESS_NUMBER VARCHAR2(50 CHAR),
                                        DOCUMENT_URL VARCHAR2(1000 CHAR),

                                        STATUS VARCHAR2(20 CHAR) DEFAULT 'PENDING' NOT NULL
        CHECK (STATUS IN ('PENDING', 'APPROVED', 'REJECTED')),

                                        REJECT_REASON VARCHAR2(500 CHAR),

                                        CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                                        PROCESSED_AT TIMESTAMP,

                                        CONSTRAINT FK_OWNER_REQ_MEMBER
                                            FOREIGN KEY (MEMBER_ID)
                                                REFERENCES APP_MEMBER(MEMBER_ID)
                                                ON DELETE CASCADE,

                                        CONSTRAINT FK_OWNER_REQ_HOSPITAL
                                            FOREIGN KEY (HOSPITAL_ID)
                                                REFERENCES HOSPITAL(HOSPITAL_ID)
                                                ON DELETE CASCADE
);

CREATE SEQUENCE SEQ_HOSPITAL_OWNER_REQUEST
    START WITH 1
    INCREMENT BY 1
    NOCACHE;