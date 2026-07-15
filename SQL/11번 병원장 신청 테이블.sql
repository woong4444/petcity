
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
    NOCACHE

    --  추가사항 --

ALTER TABLE HOSPITAL_OWNER_REQUEST
    MODIFY (
    HOSPITAL_ID NULL
    );

ALTER TABLE HOSPITAL_OWNER_REQUEST
    ADD CONSTRAINT CK_OWNER_REQUEST_HOSPITAL
        CHECK (
            STATUS <> 'APPROVED'
                OR HOSPITAL_ID IS NOT NULL
            );

ALTER TABLE HOSPITAL_OWNER_REQUEST ADD (

    /* 신청자 정보 */
    APPLICANT_NAME VARCHAR2(100 CHAR),

    /* 병원 기본 정보 */
    HOSPITAL_NAME VARCHAR2(200 CHAR),
    HOSPITAL_PHONE VARCHAR2(50 CHAR),
    HOSPITAL_ADDRESS VARCHAR2(500 CHAR),
    HOSPITAL_DETAIL_ADDRESS VARCHAR2(500 CHAR),
    HOSPITAL_DISTRICT VARCHAR2(50 CHAR),
    HOSPITAL_WEBSITE_URL VARCHAR2(1000 CHAR),

    /* 지도 정보 */
    HOSPITAL_LATITUDE NUMBER(18, 9),
    HOSPITAL_LONGITUDE NUMBER(18, 9),

    /* 운영 정보 */
    HOSPITAL_OPEN_TIME VARCHAR2(1000 CHAR),
    HOSPITAL_CLOSE_TIME VARCHAR2(1000 CHAR),
    HOSPITAL_BREAK_TIME VARCHAR2(1000 CHAR),
    HOSPITAL_CLOSED_DAYS VARCHAR2(500 CHAR),

    /* 진료 정보 */
    HOSPITAL_MEDICAL_SUBJECTS VARCHAR2(2000 CHAR),
    HOSPITAL_DOCTOR_INFO VARCHAR2(1000 CHAR),

    /* 공개 정보 */
    HOSPITAL_DESCRIPTION VARCHAR2(2000 CHAR),
    HOSPITAL_IMAGE_URL VARCHAR2(1000 CHAR),
    HOSPITAL_NOTE VARCHAR2(2000 CHAR),

    /* 처리 관리자 */
    PROCESSED_BY NUMBER
);

/* 관리자 외래키*/
ALTER TABLE HOSPITAL_OWNER_REQUEST
    ADD CONSTRAINT FK_OWNER_REQUEST_ADMIN
        FOREIGN KEY (PROCESSED_BY)
            REFERENCES APP_MEMBER(MEMBER_ID);


DELETE FROM HOSPITAL_OWNER_REQUEST;

COMMIT;

ALTER TABLE HOSPITAL_OWNER_REQUEST MODIFY (

    /* 신청자 및 증빙 */
    APPLICANT_NAME NOT NULL,
    BUSINESS_NUMBER NOT NULL,
    DOCUMENT_URL NOT NULL,

    /* 병원 기본 정보 */
    HOSPITAL_NAME NOT NULL,
    HOSPITAL_PHONE NOT NULL,
    HOSPITAL_ADDRESS NOT NULL,

    /* 주소 검색으로 자동 입력 */
    HOSPITAL_DISTRICT NOT NULL,
    HOSPITAL_LATITUDE NOT NULL,
    HOSPITAL_LONGITUDE NOT NULL,

    /* 운영 정보 */
    HOSPITAL_OPEN_TIME NOT NULL,
    HOSPITAL_CLOSE_TIME NOT NULL,

    /* 병원 공개 정보 */
    HOSPITAL_DESCRIPTION NOT NULL,
    HOSPITAL_IMAGE_URL NOT NULL
    );



