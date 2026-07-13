CREATE TABLE MAIN_BANNER (
                             BANNER_ID NUMBER PRIMARY KEY,

                             TITLE VARCHAR2(200 CHAR),
                             SUB_TITLE VARCHAR2(500 CHAR),

                             IMAGE_URL VARCHAR2(1000 CHAR) NOT NULL,
                             LINK_URL VARCHAR2(1000 CHAR),

                             DISPLAY_ORDER NUMBER DEFAULT 0 NOT NULL,

                             ACTIVE_YN CHAR(1) DEFAULT 'Y' NOT NULL
                                 CHECK (ACTIVE_YN IN ('Y', 'N')),

                             START_AT TIMESTAMP,
                             END_AT TIMESTAMP,

                             CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                             UPDATED_AT TIMESTAMP
);

CREATE SEQUENCE SEQ_MAIN_BANNER
    START WITH 1
    INCREMENT BY 1
    NOCACHE;