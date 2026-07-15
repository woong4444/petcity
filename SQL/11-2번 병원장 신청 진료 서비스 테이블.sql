/* =====================================================
   병원장 신청 - 진료 서비스
===================================================== */

CREATE TABLE OWNER_REQUEST_SERVICE (

                                       REQUEST_ID NUMBER NOT NULL,
                                       SERVICE_ID NUMBER NOT NULL,

                                       CONSTRAINT PK_OWNER_REQUEST_SERVICE
                                           PRIMARY KEY (
                                                        REQUEST_ID,
                                                        SERVICE_ID
                                               ),

                                       CONSTRAINT FK_OWNER_REQ_SERVICE_REQUEST
                                           FOREIGN KEY (REQUEST_ID)
                                               REFERENCES HOSPITAL_OWNER_REQUEST(REQUEST_ID)
                                               ON DELETE CASCADE,

                                       CONSTRAINT FK_OWNER_REQ_SERVICE_TYPE
                                           FOREIGN KEY (SERVICE_ID)
                                               REFERENCES MEDICAL_SERVICE(SERVICE_ID)
);


-- 사업자등록번호 중복 방지 인덱스
CREATE INDEX IDX_OWNER_REQUEST_BUSINESS
    ON HOSPITAL_OWNER_REQUEST(BUSINESS_NUMBER);

CREATE INDEX IDX_OWNER_REQUEST_STATUS
    ON HOSPITAL_OWNER_REQUEST(STATUS);

CREATE INDEX IDX_OWNER_REQUEST_MEMBER
    ON HOSPITAL_OWNER_REQUEST(MEMBER_ID);


UPDATE HOSPITAL_OWNER_REQUEST
SET STATUS = 'PENDING'
WHERE STATUS IS NULL;

UPDATE HOSPITAL_OWNER_REQUEST
SET CREATED_AT = SYSDATE
WHERE CREATED_AT IS NULL;



ALTER TABLE HOSPITAL_OWNER_REQUEST MODIFY (
    STATUS DEFAULT 'PENDING' NOT NULL,
    CREATED_AT DEFAULT SYSDATE NOT NULL
    );

COMMIT;