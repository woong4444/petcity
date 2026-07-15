/* =====================================================
   병원장 신청 - 진료 가능 동물
===================================================== */

CREATE TABLE OWNER_REQUEST_ANIMAL (

                                      REQUEST_ID NUMBER NOT NULL,
                                      ANIMAL_ID NUMBER NOT NULL,

                                      CONSTRAINT PK_OWNER_REQUEST_ANIMAL
                                          PRIMARY KEY (
                                                       REQUEST_ID,
                                                       ANIMAL_ID
                                              ),

                                      CONSTRAINT FK_OWNER_REQ_ANIMAL_REQUEST
                                          FOREIGN KEY (REQUEST_ID)
                                              REFERENCES HOSPITAL_OWNER_REQUEST(REQUEST_ID)
                                              ON DELETE CASCADE,

                                      CONSTRAINT FK_OWNER_REQ_ANIMAL_TYPE
                                          FOREIGN KEY (ANIMAL_ID)
                                              REFERENCES ANIMAL_TYPE(ANIMAL_ID)
);