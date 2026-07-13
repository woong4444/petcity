/* =====================================================
   14. 게시판 이미지 테이블
=====================================================1 */

CREATE TABLE BOARD_IMAGE (
                             IMAGE_ID NUMBER PRIMARY KEY,

                             BOARD_ID NUMBER NOT NULL,

                             ORIGINAL_NAME VARCHAR2(300 CHAR),
                             SAVED_NAME VARCHAR2(300 CHAR),
                             IMAGE_URL VARCHAR2(1000 CHAR),

                             CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

                             CONSTRAINT FK_IMAGE_BOARD
                                 FOREIGN KEY (BOARD_ID)
                                     REFERENCES BOARD(BOARD_ID)
                                     ON DELETE CASCADE
);

CREATE SEQUENCE SEQ_BOARD_IMAGE
    START WITH 1
    INCREMENT BY 1
    NOCACHE;

COMMIT;


-- 추가 항목
ALTER TABLE BOARD_IMAGE
    ADD LINK_URL VARCHAR2(1000 CHAR);

COMMIT;