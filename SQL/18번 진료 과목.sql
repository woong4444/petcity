
CREATE TABLE OWNER_REQUEST_MEDICAL_SUBJECT (
                                               REQUEST_ID NUMBER NOT NULL,
                                               SUBJECT_ID NUMBER NOT NULL,

                                               CONSTRAINT PK_OWNER_REQ_MED_SUBJ
                                                   PRIMARY KEY (REQUEST_ID, SUBJECT_ID),

                                               CONSTRAINT FK_OWNER_REQ_MED_REQUEST
                                                   FOREIGN KEY (REQUEST_ID)
                                                       REFERENCES HOSPITAL_OWNER_REQUEST (REQUEST_ID)
                                                       ON DELETE CASCADE,

                                               CONSTRAINT FK_OWNER_REQ_MED_SUBJECT
                                                   FOREIGN KEY (SUBJECT_ID)
                                                       REFERENCES MEDICAL_SUBJECT (SUBJECT_ID)
);



/* =========================================================
   실제 병원과 진료과목 연결 테이블

   예:
   병원 번호 25번이 내과, 외과, 피부과를 진료하는 경우

   HOSPITAL_ID | SUBJECT_ID
   25          | 1
   25          | 2
   25          | 4
   ========================================================= */

CREATE TABLE HOSPITAL_MEDICAL_SUBJECT (
                                          HOSPITAL_ID NUMBER NOT NULL,
                                          SUBJECT_ID  NUMBER NOT NULL,

                                          CONSTRAINT PK_HOSPITAL_MED_SUBJ
                                              PRIMARY KEY (HOSPITAL_ID, SUBJECT_ID),

                                          CONSTRAINT FK_HOSPITAL_MED_HOSPITAL
                                              FOREIGN KEY (HOSPITAL_ID)
                                                  REFERENCES HOSPITAL (HOSPITAL_ID)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT FK_HOSPITAL_MED_SUBJECT
                                              FOREIGN KEY (SUBJECT_ID)
                                                  REFERENCES MEDICAL_SUBJECT (SUBJECT_ID)
);

CREATE INDEX IDX_OWNER_REQ_MED_SUBJECT
    ON OWNER_REQUEST_MEDICAL_SUBJECT (SUBJECT_ID);

CREATE INDEX IDX_HOSPITAL_MED_SUBJECT
    ON HOSPITAL_MEDICAL_SUBJECT (SUBJECT_ID);

SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    R_CONSTRAINT_NAME,
    DELETE_RULE
FROM USER_CONSTRAINTS
WHERE TABLE_NAME IN (
                     'OWNER_REQUEST_MEDICAL_SUBJECT',
                     'HOSPITAL_MEDICAL_SUBJECT'
    )
ORDER BY TABLE_NAME, CONSTRAINT_TYPE;
