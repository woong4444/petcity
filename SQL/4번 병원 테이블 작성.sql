/* =====================================================
   4. 병원 테이블
===================================================== */

CREATE TABLE HOSPITAL (
                          HOSPITAL_ID NUMBER PRIMARY KEY,

                          OWNER_ID NUMBER,

                          NAME VARCHAR2(200 CHAR) NOT NULL,
                          ADDRESS VARCHAR2(500 CHAR) NOT NULL,
                          DETAIL_ADDRESS VARCHAR2(500 CHAR),
                          PHONE VARCHAR2(50 CHAR),

                          LATITUDE NUMBER(18, 9),
                          LONGITUDE NUMBER(18, 9),

                          OPEN_TIME VARCHAR2(1000 CHAR),
                          CLOSE_TIME VARCHAR2(1000 CHAR),

                          STATUS VARCHAR2(30 CHAR) DEFAULT 'OPEN' NOT NULL
        CHECK (STATUS IN ('OPEN', 'CLOSED', 'TEMP_CLOSED')),

                          DESCRIPTION VARCHAR2(2000 CHAR),
                          IMAGE_URL VARCHAR2(1000 CHAR),

                          CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                          UPDATED_AT TIMESTAMP,

                          WEBSITE_URL VARCHAR2(1000 CHAR),

                          DISTRICT VARCHAR2(50 CHAR),
                          STATUS_CODE VARCHAR2(20 CHAR),
                          OPERATION_STATUS_TEXT VARCHAR2(100 CHAR),

                          DATA_UPDATED_AT VARCHAR2(30 CHAR),
                          LAST_MODIFIED_AT VARCHAR2(30 CHAR),

                          BREAK_TIME VARCHAR2(1000 CHAR),
                          CLOSED_DAYS VARCHAR2(500 CHAR),
                          MEDICAL_SUBJECTS VARCHAR2(2000 CHAR),
                          DOCTOR_INFO VARCHAR2(1000 CHAR),
                          OWNER_NAME VARCHAR2(100 CHAR),
                          SOURCE_URL VARCHAR2(1000 CHAR),
                          CHECKED_AT VARCHAR2(30 CHAR),
                          NOTE VARCHAR2(2000 CHAR),

                          CONSTRAINT FK_HOSPITAL_OWNER
                              FOREIGN KEY (OWNER_ID)
                                  REFERENCES APP_MEMBER(MEMBER_ID)
                                  ON DELETE SET NULL
);

CREATE SEQUENCE SEQ_HOSPITAL
    START WITH 1
    INCREMENT BY 1
    NOCACHE;