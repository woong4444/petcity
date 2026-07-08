/* =====================================================
   13. 게시판 댓글 테이블
===================================================== */

CREATE TABLE BOARD_COMMENT (
                               COMMENT_ID NUMBER PRIMARY KEY,

                               BOARD_ID NUMBER NOT NULL,
                               MEMBER_ID NUMBER NOT NULL,

                               CONTENT VARCHAR2(1000 CHAR) NOT NULL,

                               CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                               UPDATED_AT TIMESTAMP,

                               CONSTRAINT FK_COMMENT_BOARD
                                   FOREIGN KEY (BOARD_ID)
                                       REFERENCES BOARD(BOARD_ID)
                                       ON DELETE CASCADE,

                               CONSTRAINT FK_COMMENT_MEMBER
                                   FOREIGN KEY (MEMBER_ID)
                                       REFERENCES APP_MEMBER(MEMBER_ID)
                                       ON DELETE CASCADE
);

CREATE SEQUENCE SEQ_BOARD_COMMENT
    START WITH 1
    INCREMENT BY 1
    NOCACHE;