/* =====================================================
   6. 병원별 진료 가능 동물 연결 테이블
===================================================== */

CREATE TABLE HOSPITAL_ANIMAL (
                                 HOSPITAL_ID NUMBER NOT NULL,
                                 ANIMAL_ID NUMBER NOT NULL,

                                 CONSTRAINT PK_HOSPITAL_ANIMAL
                                     PRIMARY KEY (HOSPITAL_ID, ANIMAL_ID),

                                 CONSTRAINT FK_HA_HOSPITAL
                                     FOREIGN KEY (HOSPITAL_ID)
                                         REFERENCES HOSPITAL(HOSPITAL_ID)
                                         ON DELETE CASCADE,

                                 CONSTRAINT FK_HA_ANIMAL
                                     FOREIGN KEY (ANIMAL_ID)
                                         REFERENCES ANIMAL_TYPE(ANIMAL_ID)
                                         ON DELETE CASCADE
);
