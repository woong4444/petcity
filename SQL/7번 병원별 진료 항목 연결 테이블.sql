/* =====================================================
   7. 병원별 진료 항목 연결 테이블
===================================================== */

CREATE TABLE HOSPITAL_SERVICE (
                                  HOSPITAL_ID NUMBER NOT NULL,
                                  SERVICE_ID NUMBER NOT NULL,

                                  CONSTRAINT PK_HOSPITAL_SERVICE
                                      PRIMARY KEY (HOSPITAL_ID, SERVICE_ID),

                                  CONSTRAINT FK_HS_HOSPITAL
                                      FOREIGN KEY (HOSPITAL_ID)
                                          REFERENCES HOSPITAL(HOSPITAL_ID)
                                          ON DELETE CASCADE,

                                  CONSTRAINT FK_HS_SERVICE
                                      FOREIGN KEY (SERVICE_ID)
                                          REFERENCES MEDICAL_SERVICE(SERVICE_ID)
                                          ON DELETE CASCADE
);
