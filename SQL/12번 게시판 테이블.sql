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


/* 수정사항 */
ALTER TABLE BOARD
    ADD CONSTRAINT CK_BOARD_TYPE
        CHECK (BOARD_TYPE IN ('NOTICE', 'QNA', 'FREE', 'INFO', 'MISSING'));

ALTER TABLE BOARD
    ADD ANIMAL_ID NUMBER;

ALTER TABLE BOARD
    ADD CONSTRAINT FK_BOARD_ANIMAL
        FOREIGN KEY (ANIMAL_ID)
            REFERENCES ANIMAL_TYPE(ANIMAL_ID);

ALTER TABLE BOARD
    ADD CONTENT_CLOB CLOB;

UPDATE BOARD
SET CONTENT_CLOB = TO_CLOB(CONTENT);

COMMIT;

ALTER TABLE BOARD
DROP COLUMN CONTENT;

ALTER TABLE BOARD
    RENAME COLUMN CONTENT_CLOB TO CONTENT;

COMMIT;

--  수정사항 2

ALTER TABLE BOARD
DROP CONSTRAINT SYS_C007130;

ALTER TABLE BOARD
DROP CONSTRAINT CK_BOARD_TYPE;

ALTER TABLE BOARD
    ADD CONSTRAINT CK_BOARD_TYPE
        CHECK (
            BOARD_TYPE IN (
                           'NOTICE',
                           'QNA',
                           'FREE',
                           'INFO',
                           'FAQ'
                )
            );

COMMIT;

DELETE FROM BOARD
WHERE BOARD_TYPE = 'MISSING';

UPDATE BOARD
SET BOARD_TYPE = 'FREE'
WHERE BOARD_TYPE = 'MISSING';

COMMIT;


