/* =====================================================
   9. 병원 좋아요 테이블
===================================================== */

CREATE TABLE HOSPITAL_LIKE (
                               MEMBER_ID NUMBER NOT NULL,
                               HOSPITAL_ID NUMBER NOT NULL,
                               CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

                               CONSTRAINT PK_HOSPITAL_LIKE
                                   PRIMARY KEY (MEMBER_ID, HOSPITAL_ID),

                               CONSTRAINT FK_HLIKE_MEMBER
                                   FOREIGN KEY (MEMBER_ID)
                                       REFERENCES APP_MEMBER(MEMBER_ID)
                                       ON DELETE CASCADE,

                               CONSTRAINT FK_HLIKE_HOSPITAL
                                   FOREIGN KEY (HOSPITAL_ID)
                                       REFERENCES HOSPITAL(HOSPITAL_ID)
                                       ON DELETE CASCADE
);