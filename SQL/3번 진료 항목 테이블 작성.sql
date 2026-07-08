/* =====================================================
   3. 진료 항목 테이블
===================================================== */

CREATE TABLE MEDICAL_SERVICE (
                                 SERVICE_ID NUMBER PRIMARY KEY,
                                 SERVICE_NAME VARCHAR2(50 CHAR) NOT NULL
);

CREATE SEQUENCE SEQ_MEDICAL_SERVICE
    START WITH 1
    INCREMENT BY 1
    NOCACHE;