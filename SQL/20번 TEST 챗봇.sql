/* =====================================================
   챗봇 카테고리 테이블
===================================================== */

CREATE TABLE CHATBOT_CATEGORY (
                                  CATEGORY_ID NUMBER
                                      CONSTRAINT PK_CHATBOT_CATEGORY PRIMARY KEY,

                                  CATEGORY_CODE VARCHAR2(50 CHAR) NOT NULL,

                                  CATEGORY_NAME VARCHAR2(100 CHAR) NOT NULL,

                                  DESCRIPTION VARCHAR2(300 CHAR),

                                  DISPLAY_ORDER NUMBER DEFAULT 0 NOT NULL,

                                  ENABLED CHAR(1) DEFAULT 'Y' NOT NULL,

                                  CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

                                  UPDATED_AT TIMESTAMP,

                                  CONSTRAINT UQ_CHATBOT_CATEGORY_CODE
                                      UNIQUE (CATEGORY_CODE),

                                  CONSTRAINT CK_CHATBOT_CATEGORY_ENABLED
                                      CHECK (ENABLED IN ('Y', 'N')),

                                  CONSTRAINT CK_CHATBOT_CATEGORY_ORDER
                                      CHECK (DISPLAY_ORDER >= 0)
);


/* =====================================================
   챗봇 카테고리 시퀀스
===================================================== */

CREATE SEQUENCE SEQ_CHATBOT_CATEGORY
    START WITH 1
    INCREMENT BY 1
    NOCACHE;

/* =====================================================
   챗봇 FAQ 노출 설정 테이블

   BOARD_ID:
   BOARD 테이블의 FAQ 게시글 번호

   CATEGORY_ID:
   챗봇에서 표시할 카테고리

   DISPLAY_ORDER:
   해당 카테고리 안에서 질문이 표시되는 순서

   ENABLED:
   챗봇 노출 여부
===================================================== */

CREATE TABLE CHATBOT_FAQ_SETTING (
                                     BOARD_ID NUMBER
                                         CONSTRAINT PK_CHATBOT_FAQ_SETTING PRIMARY KEY,

                                     CATEGORY_ID NUMBER NOT NULL,

                                     DISPLAY_ORDER NUMBER DEFAULT 0 NOT NULL,

                                     ENABLED CHAR(1) DEFAULT 'Y' NOT NULL,

                                     CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

                                     UPDATED_AT TIMESTAMP,

                                     CONSTRAINT FK_CHATBOT_FAQ_BOARD
                                         FOREIGN KEY (BOARD_ID)
                                             REFERENCES BOARD(BOARD_ID)
                                             ON DELETE CASCADE,

                                     CONSTRAINT FK_CHATBOT_FAQ_CATEGORY
                                         FOREIGN KEY (CATEGORY_ID)
                                             REFERENCES CHATBOT_CATEGORY(CATEGORY_ID)
                                             ON DELETE CASCADE,

                                     CONSTRAINT CK_CHATBOT_FAQ_ENABLED
                                         CHECK (ENABLED IN ('Y', 'N')),

                                     CONSTRAINT CK_CHATBOT_FAQ_ORDER
                                         CHECK (DISPLAY_ORDER >= 0)
);

CREATE INDEX IDX_CHATBOT_FAQ_CATEGORY
    ON CHATBOT_FAQ_SETTING (
                            CATEGORY_ID,
                            ENABLED,
                            DISPLAY_ORDER
        );


/* 1. 회원 관련 */

INSERT INTO CHATBOT_CATEGORY (
    CATEGORY_ID,
    CATEGORY_CODE,
    CATEGORY_NAME,
    DESCRIPTION,
    DISPLAY_ORDER,
    ENABLED
)
VALUES (
           SEQ_CHATBOT_CATEGORY.NEXTVAL,
           'MEMBER',
           '회원 관련',
           '회원가입, 로그인, 회원정보와 관련된 질문',
           1,
           'Y'
       );


/* 2. 동물병원 검색 */

INSERT INTO CHATBOT_CATEGORY (
    CATEGORY_ID,
    CATEGORY_CODE,
    CATEGORY_NAME,
    DESCRIPTION,
    DISPLAY_ORDER,
    ENABLED
)
VALUES (
           SEQ_CHATBOT_CATEGORY.NEXTVAL,
           'HOSPITAL_SEARCH',
           '동물병원 검색',
           '병원 검색과 맞춤 검색에 관련된 질문',
           2,
           'Y'
       );


/* 3. 병원장 신청 */

INSERT INTO CHATBOT_CATEGORY (
    CATEGORY_ID,
    CATEGORY_CODE,
    CATEGORY_NAME,
    DESCRIPTION,
    DISPLAY_ORDER,
    ENABLED
)
VALUES (
           SEQ_CHATBOT_CATEGORY.NEXTVAL,
           'OWNER_REQUEST',
           '병원장 신청',
           '병원장 신청과 승인 상태에 관련된 질문',
           3,
           'Y'
       );


/* 4. 병원 정보 관리 */

INSERT INTO CHATBOT_CATEGORY (
    CATEGORY_ID,
    CATEGORY_CODE,
    CATEGORY_NAME,
    DESCRIPTION,
    DISPLAY_ORDER,
    ENABLED
)
VALUES (
           SEQ_CHATBOT_CATEGORY.NEXTVAL,
           'HOSPITAL_MANAGEMENT',
           '병원 정보 관리',
           '병원 정보 수정, 휴업, 폐업과 관련된 질문',
           4,
           'Y'
       );


/* 5. 반려동물 관리 */

INSERT INTO CHATBOT_CATEGORY (
    CATEGORY_ID,
    CATEGORY_CODE,
    CATEGORY_NAME,
    DESCRIPTION,
    DISPLAY_ORDER,
    ENABLED
)
VALUES (
           SEQ_CHATBOT_CATEGORY.NEXTVAL,
           'PET_MANAGEMENT',
           '반려동물 관리',
           '내 반려동물 등록과 관리에 관련된 질문',
           5,
           'Y'
       );


/* 6. 게시판 이용 */

INSERT INTO CHATBOT_CATEGORY (
    CATEGORY_ID,
    CATEGORY_CODE,
    CATEGORY_NAME,
    DESCRIPTION,
    DISPLAY_ORDER,
    ENABLED
)
VALUES (
           SEQ_CHATBOT_CATEGORY.NEXTVAL,
           'COMMUNITY',
           '게시판 이용',
           '게시판과 수의사 상담 이용에 관련된 질문',
           6,
           'Y'
       );

COMMIT;

/* =====================================================
   기존 BOARD FAQ를 챗봇 카테고리와 연결

   TITLE을 기준으로 기존 FAQ 게시글을 찾아서
   CHATBOT_FAQ_SETTING에 등록한다.
===================================================== */

MERGE INTO CHATBOT_FAQ_SETTING TARGET

    USING (
        SELECT
            B.BOARD_ID,
            CC.CATEGORY_ID,
            FAQ_DATA.DISPLAY_ORDER

        FROM BOARD B

                 INNER JOIN (
            /* 회원 관련 */

            SELECT
                '회원가입을 하지 않아도 동물병원을 검색할 수 있나요?'
                         AS TITLE,
                'MEMBER' AS CATEGORY_CODE,
                1 AS DISPLAY_ORDER
            FROM DUAL

            UNION ALL

            SELECT
                '회원탈퇴를 하면 작성한 게시글과 댓글은 어떻게 되나요?',
                'MEMBER',
                2
            FROM DUAL


            /* 동물병원 검색 */

            UNION ALL

            SELECT
                '현재 위치를 불러오지 못할 때는 어떻게 해야 하나요?',
                'HOSPITAL_SEARCH',
                1
            FROM DUAL

            UNION ALL

            SELECT
                '병원과의 거리는 어떤 기준으로 계산되나요?',
                'HOSPITAL_SEARCH',
                2
            FROM DUAL

            UNION ALL

            SELECT
                '영업 중으로 표시된 병원은 바로 방문해도 되나요?',
                'HOSPITAL_SEARCH',
                3
            FROM DUAL

            UNION ALL

            SELECT
                '파충류나 조류 같은 특수동물 진료 병원은 어떻게 찾나요?',
                'HOSPITAL_SEARCH',
                4
            FROM DUAL

            UNION ALL

            SELECT
                '병원을 즐겨찾기에 저장하려면 어떻게 해야 하나요?',
                'HOSPITAL_SEARCH',
                5
            FROM DUAL


            /* 병원장 신청 */

            UNION ALL

            SELECT
                '병원장 권한은 어떻게 신청하나요?',
                'OWNER_REQUEST',
                1
            FROM DUAL


            /* 병원 정보 관리 */

            UNION ALL

            SELECT
                '병원 정보가 실제 정보와 다르게 표시됩니다.',
                'HOSPITAL_MANAGEMENT',
                1
            FROM DUAL


            /* 반려동물 관리 */

            UNION ALL

            SELECT
                '내 반려동물 정보는 어디에서 등록하나요?',
                'PET_MANAGEMENT',
                1
            FROM DUAL


            /* 게시판 이용 */

            UNION ALL

            SELECT
                '병원 리뷰는 누구나 작성할 수 있나요?',
                'COMMUNITY',
                1
            FROM DUAL

            UNION ALL

            SELECT
                '작성한 리뷰를 수정하거나 삭제할 수 있나요?',
                'COMMUNITY',
                2
            FROM DUAL

            UNION ALL

            SELECT
                '수의사상담 게시판은 어떻게 이용하나요?',
                'COMMUNITY',
                3
            FROM DUAL

            UNION ALL

            SELECT
                '수의사상담 답변만 보고 병원에 가지 않아도 되나요?',
                'COMMUNITY',
                4
            FROM DUAL

            UNION ALL

            SELECT
                '반려동물이 응급상황일 때 상담 게시판을 이용해도 되나요?',
                'COMMUNITY',
                5
            FROM DUAL

        ) FAQ_DATA
                            ON B.TITLE = FAQ_DATA.TITLE

                 INNER JOIN CHATBOT_CATEGORY CC
                            ON CC.CATEGORY_CODE = FAQ_DATA.CATEGORY_CODE

        WHERE B.BOARD_TYPE = 'FAQ'

    ) SOURCE

    ON (
        TARGET.BOARD_ID = SOURCE.BOARD_ID
        )

    WHEN MATCHED THEN

        UPDATE SET
            TARGET.CATEGORY_ID = SOURCE.CATEGORY_ID,
            TARGET.DISPLAY_ORDER = SOURCE.DISPLAY_ORDER,
            TARGET.ENABLED = 'Y',
            TARGET.UPDATED_AT = SYSTIMESTAMP

    WHEN NOT MATCHED THEN

        INSERT (
                BOARD_ID,
                CATEGORY_ID,
                DISPLAY_ORDER,
                ENABLED,
                CREATED_AT
            )
            VALUES (
                       SOURCE.BOARD_ID,
                       SOURCE.CATEGORY_ID,
                       SOURCE.DISPLAY_ORDER,
                       'Y',
                       SYSTIMESTAMP
                   );

COMMIT;
