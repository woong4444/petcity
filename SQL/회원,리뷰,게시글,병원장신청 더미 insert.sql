/* =====================================================
   0. 더미 회원 INSERT
   - 이미 있으면 중복 INSERT 안 됨
===================================================== */

INSERT INTO APP_MEMBER (
    MEMBER_ID,
    NICKNAME,
    EMAIL,
    PHONE,
    ROLE,
    LOGIN_ID,
    PASSWORD,
    EMAIL_VERIFIED,
    STATUS,
    MEMBER_STATUS
)
SELECT
    SEQ_APP_MEMBER.NEXTVAL,
    '테스트유저01',
    'user01@test.com',
    '010-1111-1111',
    'USER',
    'dummy_user01',
    '1234',
    'N',
    'ACTIVE',
    'ACTIVE'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM APP_MEMBER WHERE LOGIN_ID = 'dummy_user01'
);

INSERT INTO APP_MEMBER (
    MEMBER_ID,
    NICKNAME,
    EMAIL,
    PHONE,
    ROLE,
    LOGIN_ID,
    PASSWORD,
    EMAIL_VERIFIED,
    STATUS,
    MEMBER_STATUS
)
SELECT
    SEQ_APP_MEMBER.NEXTVAL,
    '테스트유저02',
    'user02@test.com',
    '010-2222-2222',
    'USER',
    'dummy_user02',
    '1234',
    'N',
    'ACTIVE',
    'ACTIVE'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM APP_MEMBER WHERE LOGIN_ID = 'dummy_user02'
);

INSERT INTO APP_MEMBER (
    MEMBER_ID,
    NICKNAME,
    EMAIL,
    PHONE,
    ROLE,
    LOGIN_ID,
    PASSWORD,
    EMAIL_VERIFIED,
    STATUS,
    MEMBER_STATUS
)
SELECT
    SEQ_APP_MEMBER.NEXTVAL,
    '병원장신청자01',
    'owner01@test.com',
    '010-3333-3333',
    'USER',
    'dummy_owner01',
    '1234',
    'N',
    'ACTIVE',
    'ACTIVE'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM APP_MEMBER WHERE LOGIN_ID = 'dummy_owner01'
);

INSERT INTO APP_MEMBER (
    MEMBER_ID,
    NICKNAME,
    EMAIL,
    PHONE,
    ROLE,
    LOGIN_ID,
    PASSWORD,
    EMAIL_VERIFIED,
    STATUS,
    MEMBER_STATUS
)
SELECT
    SEQ_APP_MEMBER.NEXTVAL,
    '병원장신청자02',
    'owner02@test.com',
    '010-4444-4444',
    'USER',
    'dummy_owner02',
    '1234',
    'N',
    'ACTIVE',
    'ACTIVE'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM APP_MEMBER WHERE LOGIN_ID = 'dummy_owner02'
);

/* =====================================================
   1. 병원 리뷰 더미 INSERT
   - HOSPITAL_ID 1~5가 있다고 가정
   - 병원이 없으면 해당 INSERT는 0건 처리됨
===================================================== */

INSERT INTO HOSPITAL_REVIEW (
    REVIEW_ID,
    HOSPITAL_ID,
    MEMBER_ID,
    RATING,
    CONTENT,
    IMAGE_URL
)
SELECT
    SEQ_HOSPITAL_REVIEW.NEXTVAL,
    h.HOSPITAL_ID,
    m.MEMBER_ID,
    4.5,
    '선생님이 친절하고 설명을 자세히 해주셔서 좋았습니다.',
    NULL
FROM HOSPITAL h
         CROSS JOIN APP_MEMBER m
WHERE h.HOSPITAL_ID = 1
  AND m.LOGIN_ID = 'dummy_user01';

INSERT INTO HOSPITAL_REVIEW (
    REVIEW_ID,
    HOSPITAL_ID,
    MEMBER_ID,
    RATING,
    CONTENT,
    IMAGE_URL
)
SELECT
    SEQ_HOSPITAL_REVIEW.NEXTVAL,
    h.HOSPITAL_ID,
    m.MEMBER_ID,
    5.0,
    '시설이 깔끔하고 대기 시간이 짧아서 만족했습니다.',
    NULL
FROM HOSPITAL h
         CROSS JOIN APP_MEMBER m
WHERE h.HOSPITAL_ID = 2
  AND m.LOGIN_ID = 'dummy_user02';

INSERT INTO HOSPITAL_REVIEW (
    REVIEW_ID,
    HOSPITAL_ID,
    MEMBER_ID,
    RATING,
    CONTENT,
    IMAGE_URL
)
SELECT
    SEQ_HOSPITAL_REVIEW.NEXTVAL,
    h.HOSPITAL_ID,
    m.MEMBER_ID,
    3.5,
    '진료는 괜찮았는데 주말에는 사람이 조금 많았습니다.',
    NULL
FROM HOSPITAL h
         CROSS JOIN APP_MEMBER m
WHERE h.HOSPITAL_ID = 3
  AND m.LOGIN_ID = 'dummy_user01';

INSERT INTO HOSPITAL_REVIEW (
    REVIEW_ID,
    HOSPITAL_ID,
    MEMBER_ID,
    RATING,
    CONTENT,
    IMAGE_URL
)
SELECT
    SEQ_HOSPITAL_REVIEW.NEXTVAL,
    h.HOSPITAL_ID,
    m.MEMBER_ID,
    4.0,
    '강아지 예방접종으로 방문했는데 전체적으로 만족합니다.',
    NULL
FROM HOSPITAL h
         CROSS JOIN APP_MEMBER m
WHERE h.HOSPITAL_ID = 4
  AND m.LOGIN_ID = 'dummy_user02';

/* =====================================================
   3. 병원장 승인 대기 더미 INSERT
   - STATUS = PENDING 이면 어드민 대시보드 승인 대기 수에 잡힘
===================================================== */

INSERT INTO HOSPITAL_OWNER_REQUEST (
    REQUEST_ID,
    MEMBER_ID,
    HOSPITAL_ID,
    BUSINESS_NUMBER,
    DOCUMENT_URL,
    STATUS,
    REJECT_REASON
)
SELECT
    SEQ_HOSPITAL_OWNER_REQUEST.NEXTVAL,
    m.MEMBER_ID,
    h.HOSPITAL_ID,
    '111-22-33333',
    '/documents/owner-request/dummy-owner01.pdf',
    'PENDING',
    NULL
FROM APP_MEMBER m
         CROSS JOIN HOSPITAL h
WHERE m.LOGIN_ID = 'dummy_owner01'
  AND h.HOSPITAL_ID = 1;

INSERT INTO HOSPITAL_OWNER_REQUEST (
    REQUEST_ID,
    MEMBER_ID,
    HOSPITAL_ID,
    BUSINESS_NUMBER,
    DOCUMENT_URL,
    STATUS,
    REJECT_REASON
)
SELECT
    SEQ_HOSPITAL_OWNER_REQUEST.NEXTVAL,
    m.MEMBER_ID,
    h.HOSPITAL_ID,
    '222-33-44444',
    '/documents/owner-request/dummy-owner02.pdf',
    'PENDING',
    NULL
FROM APP_MEMBER m
         CROSS JOIN HOSPITAL h
WHERE m.LOGIN_ID = 'dummy_owner02'
  AND h.HOSPITAL_ID = 2;

INSERT INTO HOSPITAL_OWNER_REQUEST (
    REQUEST_ID,
    MEMBER_ID,
    HOSPITAL_ID,
    BUSINESS_NUMBER,
    DOCUMENT_URL,
    STATUS,
    REJECT_REASON
)
SELECT
    SEQ_HOSPITAL_OWNER_REQUEST.NEXTVAL,
    m.MEMBER_ID,
    h.HOSPITAL_ID,
    '333-44-55555',
    '/documents/owner-request/dummy-owner03.pdf',
    'APPROVED',
    NULL
FROM APP_MEMBER m
         CROSS JOIN HOSPITAL h
WHERE m.LOGIN_ID = 'dummy_user01'
  AND h.HOSPITAL_ID = 3;

INSERT INTO HOSPITAL_OWNER_REQUEST (
    REQUEST_ID,
    MEMBER_ID,
    HOSPITAL_ID,
    BUSINESS_NUMBER,
    DOCUMENT_URL,
    STATUS,
    REJECT_REASON
)
SELECT
    SEQ_HOSPITAL_OWNER_REQUEST.NEXTVAL,
    m.MEMBER_ID,
    h.HOSPITAL_ID,
    '444-55-66666',
    '/documents/owner-request/dummy-owner04.pdf',
    'REJECTED',
    '사업자등록증 확인이 필요합니다.'
FROM APP_MEMBER m
         CROSS JOIN HOSPITAL h
WHERE m.LOGIN_ID = 'dummy_user02'
  AND h.HOSPITAL_ID = 4;

COMMIT;



-- 자유게시판 게시글 더미데이터입니다!!!!!!!!!!!!!!!!!!
DECLARE
V_MEMBER_ID APP_MEMBER.MEMBER_ID%TYPE;
    V_ANIMAL_ID ANIMAL_TYPE.ANIMAL_ID%TYPE;
BEGIN

    /* 존재하는 회원 한 명 선택 */
SELECT MIN(MEMBER_ID)
INTO V_MEMBER_ID
FROM APP_MEMBER;

/* 품종·종류에 해당하는 하위 동물 한 개 선택 */
SELECT MIN(ANIMAL_ID)
INTO V_ANIMAL_ID
FROM ANIMAL_TYPE
WHERE PARENT_ID IS NOT NULL;

IF V_MEMBER_ID IS NULL THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'APP_MEMBER 테이블에 회원이 없습니다.'
        );
END IF;

    IF V_ANIMAL_ID IS NULL THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'ANIMAL_TYPE 테이블에 하위 동물이 없습니다.'
        );
END IF;

FOR I IN 1..65 LOOP

        INSERT INTO BOARD (
            BOARD_ID,
            MEMBER_ID,
            BOARD_TYPE,
            TITLE,
            CONTENT,
            ANIMAL_ID,
            HIT,
            CREATED_AT,
            UPDATED_AT
        )
        VALUES (
            SEQ_BOARD.NEXTVAL,
            V_MEMBER_ID,
            'FREE',

            CASE
                WHEN MOD(I, 3) = 0
                    THEN '[페이징테스트] 강아지 산책 이야기 '
                WHEN MOD(I, 5) = 0
                    THEN '[페이징테스트] 고양이 자랑 '
                ELSE '[페이징테스트] 자유게시판 더미 글 '
            END || LPAD(I, 2, '0'),

            TO_CLOB(
                CASE
                    WHEN MOD(I, 2) = 0
                        THEN '페이징 테스트를 위한 강아지 관련 더미 내용입니다. 번호: '
                    ELSE '페이징 테스트를 위한 반려동물 자유토크 내용입니다. 번호: '
                END || I
            ),

            V_ANIMAL_ID,
            MOD(I * 3, 100),

            /* 글마다 작성 시간이 조금씩 다르게 저장 */
            SYSTIMESTAMP
                - NUMTODSINTERVAL(65 - I, 'MINUTE'),

            SYSTIMESTAMP
                - NUMTODSINTERVAL(65 - I, 'MINUTE')
        );

END LOOP;

COMMIT;
END;


-- faq 질문 더미데이터

DECLARE
V_ADMIN_ID NUMBER;
BEGIN

    /*
        관리자 회원 한 명 조회
    */
SELECT MEMBER_ID
INTO V_ADMIN_ID
FROM APP_MEMBER
WHERE ROLE = 'ADMIN'
  AND ROWNUM = 1;


/* 1 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '회원가입을 하지 않아도 동물병원을 검색할 수 있나요?',
           TO_CLOB(q'[
            <p>네. 동물병원 목록 조회와 기본 검색은 로그인하지 않아도 이용할 수 있습니다.</p>
            <p>다만 병원 즐겨찾기, 리뷰 작성, 반려동물 정보 관리 같은 회원 전용 기능은 로그인이 필요합니다.</p>
        ]'),
           NULL
       );


/* 2 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '현재 위치를 불러오지 못할 때는 어떻게 해야 하나요?',
           TO_CLOB(q'[
            <p>브라우저 주소창 옆의 위치 권한을 확인하고 PetCity의 위치 접근을 허용해 주세요.</p>
            <p>위치 권한을 허용하지 않아도 지역이나 병원 이름을 직접 검색해 병원을 찾을 수 있습니다.</p>
        ]'),
           NULL
       );


/* 3 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '병원과의 거리는 어떤 기준으로 계산되나요?',
           TO_CLOB(q'[
            <p>사용자의 현재 위치 좌표와 병원 좌표를 기준으로 거리를 계산합니다.</p>
            <p>표시되는 거리는 실제 도로 이동 거리와 다를 수 있으므로 참고용으로 이용해 주세요.</p>
        ]'),
           NULL
       );


/* 4 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '영업 중으로 표시된 병원은 바로 방문해도 되나요?',
           TO_CLOB(q'[
            <p>영업 상태는 등록된 영업시간 정보를 기준으로 표시됩니다.</p>
            <p>공휴일, 임시휴무, 진료 일정 변경이 반영되지 않았을 수 있으므로 방문 전 병원에 전화로 확인하는 것을 권장합니다.</p>
        ]'),
           NULL
       );


/* 5 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '병원 정보가 실제 정보와 다르게 표시됩니다.',
           TO_CLOB(q'[
            <p>병원 정보는 공공데이터와 병원에서 제공한 정보를 바탕으로 표시됩니다.</p>
            <p>이전, 폐업, 전화번호 변경처럼 실제 정보와 다른 내용이 있다면 관리자에게 알려 주세요. 확인 후 수정하겠습니다.</p>
        ]'),
           NULL
       );


/* 6 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '파충류나 조류 같은 특수동물 진료 병원은 어떻게 찾나요?',
           TO_CLOB(q'[
            <p>병원 검색 화면에서 진료 동물 종류를 파충류, 조류, 소동물 등으로 선택해 검색해 주세요.</p>
            <p>병원마다 실제 진료 가능 동물과 진료 일정이 다를 수 있으므로 방문 전 전화 문의가 필요합니다.</p>
        ]'),
           NULL
       );


/* 7 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '병원을 즐겨찾기에 저장하려면 어떻게 해야 하나요?',
           TO_CLOB(q'[
            <p>로그인 후 병원 목록이나 상세 화면의 하트 모양 즐겨찾기 버튼을 눌러 주세요.</p>
            <p>저장한 병원은 마이페이지의 즐겨찾기 목록에서 다시 확인할 수 있습니다.</p>
        ]'),
           NULL
       );


/* 8 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '병원 리뷰는 누구나 작성할 수 있나요?',
           TO_CLOB(q'[
            <p>리뷰를 작성하려면 PetCity 회원으로 로그인해야 합니다.</p>
            <p>다른 이용자에게 도움이 될 수 있도록 실제 이용 경험을 바탕으로 작성해 주세요.</p>
        ]'),
           NULL
       );


/* 9 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '작성한 리뷰를 수정하거나 삭제할 수 있나요?',
           TO_CLOB(q'[
            <p>본인이 작성한 리뷰는 수정하거나 삭제할 수 있습니다.</p>
            <p>욕설, 광고, 개인정보 노출 등 운영정책에 위반되는 리뷰는 관리자에 의해 제한될 수 있습니다.</p>
        ]'),
           NULL
       );


/* 10 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '수의사상담 게시판은 어떻게 이용하나요?',
           TO_CLOB(q'[
            <p>수의사상담 게시판에서 반려동물 종류와 증상을 작성해 질문을 등록할 수 있습니다.</p>
            <p>등록된 질문에는 병원장 권한을 가진 회원이나 관리자가 답변할 수 있습니다.</p>
        ]'),
           NULL
       );


/* 11 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '수의사상담 답변만 보고 병원에 가지 않아도 되나요?',
           TO_CLOB(q'[
            <p>수의사상담 게시판의 답변은 일반적인 참고 정보이며, 직접적인 진단이나 처방을 대신할 수 없습니다.</p>
            <p>증상이 지속되거나 상태가 심각하다면 가까운 동물병원에서 직접 진료를 받아 주세요.</p>
        ]'),
           NULL
       );


/* 12 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '반려동물이 응급상황일 때 상담 게시판을 이용해도 되나요?',
           TO_CLOB(q'[
            <p>호흡곤란, 심한 출혈, 경련, 의식 저하, 독성물질 섭취 같은 응급상황에서는 게시판 답변을 기다리면 안 됩니다.</p>
            <p>즉시 가까운 응급 진료 가능 동물병원에 전화한 뒤 방문해 주세요.</p>
        ]'),
           NULL
       );


/* 13 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '병원장 권한은 어떻게 신청하나요?',
           TO_CLOB(q'[
            <p>로그인 후 병원장 권한 신청 화면에서 담당 병원을 선택하고 필요한 정보를 입력해 주세요.</p>
            <p>관리자가 제출된 내용을 확인하고 승인하면 병원장 권한을 사용할 수 있습니다.</p>
        ]'),
           NULL
       );


/* 14 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '내 반려동물 정보는 어디에서 등록하나요?',
           TO_CLOB(q'[
            <p>로그인 후 마이페이지의 반려동물 관리 메뉴에서 이름, 종류, 성별, 생년월일, 체중 등의 정보를 등록할 수 있습니다.</p>
            <p>등록된 정보는 마이페이지에서 수정하거나 삭제할 수 있습니다.</p>
        ]'),
           NULL
       );


/* 15 */
INSERT INTO BOARD (
    BOARD_ID,
    MEMBER_ID,
    BOARD_TYPE,
    TITLE,
    CONTENT,
    ANIMAL_ID
)
VALUES (
           SEQ_BOARD.NEXTVAL,
           V_ADMIN_ID,
           'FAQ',
           '회원탈퇴를 하면 작성한 게시글과 댓글은 어떻게 되나요?',
           TO_CLOB(q'[
            <p>회원탈퇴 시 회원 계정과 연결된 게시글, 댓글 등 일부 작성 정보가 함께 삭제될 수 있습니다.</p>
            <p>탈퇴 전 보관이 필요한 내용이 있다면 미리 확인해 주세요.</p>
        ]'),
           NULL
       );

COMMIT;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'ROLE이 ADMIN인 관리자 회원이 없습니다.'
        );
END;
/