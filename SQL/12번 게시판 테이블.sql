/* =====================================================
   12. 게시판 테이블
===================================================== */

CREATE TABLE BOARD (
                       BOARD_ID NUMBER PRIMARY KEY,

                       MEMBER_ID NUMBER NOT NULL,

                       BOARD_TYPE VARCHAR2(30 CHAR) NOT NULL
        CHECK (BOARD_TYPE IN ('QNA', 'FREE', 'INFO', 'MISSING')),

                       TITLE VARCHAR2(200 CHAR) NOT NULL,
                       CONTENT VARCHAR2(4000 CHAR) NOT NULL,

                       HIT NUMBER DEFAULT 0 NOT NULL,

                       CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                       UPDATED_AT TIMESTAMP,

                       CONSTRAINT FK_BOARD_MEMBER
                           FOREIGN KEY (MEMBER_ID)
                               REFERENCES APP_MEMBER(MEMBER_ID)
                               ON DELETE CASCADE
);

CREATE SEQUENCE SEQ_BOARD
    START WITH 1
    INCREMENT BY 1
    NOCACHE;
