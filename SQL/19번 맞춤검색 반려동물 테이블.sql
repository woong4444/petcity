-- 반려동물 테이블 생성
CREATE TABLE APP_PET (
                         PET_ID NUMBER PRIMARY KEY,
                         MEMBER_ID NUMBER NOT NULL,
                         PET_NAME VARCHAR2(100) NOT NULL,
                         ANIMAL_ID NUMBER NOT NULL,
                         SUB_ANIMAL_ID NUMBER NOT NULL,
                         GENDER VARCHAR2(10) NOT NULL,
                         BIRTH_DATE VARCHAR2(20) NOT NULL,
                         WEIGHT NUMBER(5,2) NOT NULL,
                         IMAGE_URL VARCHAR2(500),
                         REG_NUMBER VARCHAR2(100),
                         CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- 시퀀스 생성
CREATE SEQUENCE SEQ_APP_PET;
COMMIT;