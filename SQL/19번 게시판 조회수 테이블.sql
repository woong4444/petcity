CREATE TABLE BOARD_VIEW_HISTORY (
                                    BOARD_ID NUMBER NOT NULL,
                                    MEMBER_ID NUMBER NOT NULL,
                                    VIEWED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

                                    CONSTRAINT PK_BOARD_VIEW_HISTORY
                                        PRIMARY KEY (BOARD_ID, MEMBER_ID),

                                    CONSTRAINT FK_BOARD_VIEW_HISTORY_BOARD
                                        FOREIGN KEY (BOARD_ID)
                                            REFERENCES BOARD(BOARD_ID)
                                            ON DELETE CASCADE,

                                    CONSTRAINT FK_BOARD_VIEW_HISTORY_MEMBER
                                        FOREIGN KEY (MEMBER_ID)
                                            REFERENCES APP_MEMBER(MEMBER_ID)
                                            ON DELETE CASCADE
);

COMMENT ON TABLE BOARD_VIEW_HISTORY
IS '게시글 회원별 조회 이력';

COMMENT ON COLUMN BOARD_VIEW_HISTORY.BOARD_ID
IS '게시글 번호';

COMMENT ON COLUMN BOARD_VIEW_HISTORY.MEMBER_ID
IS '조회한 회원 번호';

COMMENT ON COLUMN BOARD_VIEW_HISTORY.VIEWED_AT
IS '최초 조회일';