-- 1. 병원 정보 수정 요청 테이블 새로 생성
CREATE TABLE HOSPITAL_UPDATE_REQUEST (
                                         UPDATE_ID NUMBER PRIMARY KEY,
                                         HOSPITAL_ID NUMBER NOT NULL,
                                         MEMBER_ID NUMBER NOT NULL,
                                         HOSPITAL_NAME VARCHAR2(100),
                                         HOSPITAL_PHONE VARCHAR2(50),
                                         HOSPITAL_ADDRESS VARCHAR2(300),
                                         HOSPITAL_DETAIL_ADDRESS VARCHAR2(200),
                                         OPEN_TIME VARCHAR2(50),
                                         CLOSE_TIME VARCHAR2(50),
                                         LUNCH_TIME VARCHAR2(100),
                                         HOLIDAY VARCHAR2(100),
                                         MEDICAL_SUBJECTS VARCHAR2(1000),
                                         STATUS VARCHAR2(20) DEFAULT 'PENDING',
                                         CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- 2. 해당 테이블에 사용할 시퀀스 생성 (이미 있다고 에러나면 무시해도 됨)
CREATE SEQUENCE SEQ_HOSPITAL_UPDATE_REQUEST START WITH 1 INCREMENT BY 1;