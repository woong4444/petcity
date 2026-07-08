/* =====================================================
   5. 회원 반려동물 테이블
===================================================== */

CREATE TABLE MEMBER_PET (
                            PET_ID NUMBER PRIMARY KEY,

                            MEMBER_ID NUMBER NOT NULL,
                            ANIMAL_ID NUMBER NOT NULL,

                            PET_NAME VARCHAR2(50 CHAR) NOT NULL,
                            BREED_NAME VARCHAR2(100 CHAR),

                            GENDER CHAR(1) DEFAULT 'U' NOT NULL
                                CHECK (GENDER IN ('M', 'F', 'U')),

                            BIRTH_DATE DATE,
                            WEIGHT NUMBER(6, 2),

                            PHOTO_URL VARCHAR2(1000 CHAR),
                            REGISTRATION_NO VARCHAR2(100 CHAR),

                            NEUTERED CHAR(1) DEFAULT 'U' NOT NULL
                                CHECK (NEUTERED IN ('Y', 'N', 'U')),

                            ALLERGY_NOTE VARCHAR2(500 CHAR),
                            NOTE VARCHAR2(1000 CHAR),

                            IS_MAIN CHAR(1) DEFAULT 'N' NOT NULL
                                CHECK (IS_MAIN IN ('Y', 'N')),

                            STATUS VARCHAR2(20 CHAR) DEFAULT 'ACTIVE' NOT NULL
        CHECK (STATUS IN ('ACTIVE', 'DELETED')),

                            CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                            UPDATED_AT TIMESTAMP,

                            CONSTRAINT FK_MEMBER_PET_MEMBER
                                FOREIGN KEY (MEMBER_ID)
                                    REFERENCES APP_MEMBER(MEMBER_ID)
                                    ON DELETE CASCADE,

                            CONSTRAINT FK_MEMBER_PET_ANIMAL
                                FOREIGN KEY (ANIMAL_ID)
                                    REFERENCES ANIMAL_TYPE(ANIMAL_ID)
);

CREATE SEQUENCE SEQ_MEMBER_PET
    START WITH 1
    INCREMENT BY 1
    NOCACHE;