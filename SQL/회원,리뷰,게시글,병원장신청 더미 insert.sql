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