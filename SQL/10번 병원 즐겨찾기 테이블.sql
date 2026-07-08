/* =====================================================
   10. 병원 즐겨찾기 테이블
===================================================== */

CREATE TABLE FAVORITE_HOSPITAL (
                                   MEMBER_ID NUMBER NOT NULL,
                                   HOSPITAL_ID NUMBER NOT NULL,
                                   CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

                                   CONSTRAINT PK_FAVORITE_HOSPITAL
                                       PRIMARY KEY (MEMBER_ID, HOSPITAL_ID),

                                   CONSTRAINT FK_FAV_MEMBER
                                       FOREIGN KEY (MEMBER_ID)
                                           REFERENCES APP_MEMBER(MEMBER_ID)
                                           ON DELETE CASCADE,

                                   CONSTRAINT FK_FAV_HOSPITAL
                                       FOREIGN KEY (HOSPITAL_ID)
                                           REFERENCES HOSPITAL(HOSPITAL_ID)
                                           ON DELETE CASCADE
);
