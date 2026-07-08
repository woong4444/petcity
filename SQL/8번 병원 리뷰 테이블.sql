/* =====================================================
   8. 병원 리뷰 테이블
===================================================== */

CREATE TABLE HOSPITAL_REVIEW (
                                 REVIEW_ID NUMBER PRIMARY KEY,

                                 HOSPITAL_ID NUMBER NOT NULL,
                                 MEMBER_ID NUMBER NOT NULL,

                                 RATING NUMBER(2, 1) NOT NULL
        CHECK (RATING BETWEEN 0 AND 5),

                                 CONTENT VARCHAR2(1000 CHAR),
                                 IMAGE_URL VARCHAR2(1000 CHAR),

                                 CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                                 UPDATED_AT TIMESTAMP,

                                 CONSTRAINT FK_REVIEW_HOSPITAL
                                     FOREIGN KEY (HOSPITAL_ID)
                                         REFERENCES HOSPITAL(HOSPITAL_ID)
                                         ON DELETE CASCADE,

                                 CONSTRAINT FK_REVIEW_MEMBER
                                     FOREIGN KEY (MEMBER_ID)
                                         REFERENCES APP_MEMBER(MEMBER_ID)
                                         ON DELETE CASCADE
);

CREATE SEQUENCE SEQ_HOSPITAL_REVIEW
    START WITH 1
    INCREMENT BY 1
    NOCACHE;