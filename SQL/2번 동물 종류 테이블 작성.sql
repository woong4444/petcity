/* =====================================================
   2. 동물 종류 테이블
===================================================== */

CREATE TABLE ANIMAL_TYPE (
                             ANIMAL_ID NUMBER PRIMARY KEY,
                             ANIMAL_NAME VARCHAR2(50 CHAR) NOT NULL,
                             CATEGORY VARCHAR2(50 CHAR) NOT NULL
);

CREATE SEQUENCE SEQ_ANIMAL_TYPE
    START WITH 1
    INCREMENT BY 1
    NOCACHE;
