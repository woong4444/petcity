INSERT INTO APP_MEMBER (
    MEMBER_ID,
    NICKNAME,
    EMAIL,
    PHONE,
    ROLE,
    CREATED_AT,
    LOGIN_ID,
    PASSWORD,
    EMAIL_VERIFIED,
    STATUS,
    LAST_LOGIN_AT,
    MEMBER_STATUS,
    DELETE_REQUESTED_AT,
    HARD_DELETE_AT,
    DELETED_AT
)
SELECT
    SEQ_APP_MEMBER.NEXTVAL,

    /* 닉네임 */
    '테스트회원' || LPAD(test_no, 3, '0'),

    /* 이메일 */
    'test_user' || LPAD(test_no, 3, '0') || '@test.com',

    /* 전화번호 */
    '010-9000-' || LPAD(test_no, 4, '0'),

    /* 10명마다 병원장 권한 */
    CASE
        WHEN MOD(test_no, 10) = 0 THEN 'OWNER'
        ELSE 'USER'
        END,

    /* 가입일: 최근 90일 사이로 분산 */
    SYSTIMESTAMP
        - NUMTODSINTERVAL(
            MOD(test_no, 90),
            'DAY'
          ),

    /* 로그인 아이디 */
    'test_user' || LPAD(test_no, 3, '0'),

    /* 비밀번호 */
    '1234',

    /* 일부 회원만 이메일 인증 */
    CASE
        WHEN MOD(test_no, 3) = 0 THEN 'Y'
        ELSE 'N'
        END,

    /* 계정 상태 */
    CASE
        /* 삭제 완료 회원 */
        WHEN MOD(test_no, 25) = 0 THEN 'DELETED'

        /* 일부 차단 회원 */
        WHEN MOD(test_no, 10) = 0 THEN 'BLOCKED'

        /* 나머지 정상 회원 */
        ELSE 'ACTIVE'
        END,

    /* 최근 로그인 시간
       7의 배수 회원은 로그인 기록 없음 */
    CASE
        WHEN MOD(test_no, 7) = 0 THEN NULL

        ELSE SYSTIMESTAMP
            - NUMTODSINTERVAL(
                     MOD(test_no, 30),
                     'DAY'
              )
            - NUMTODSINTERVAL(
                     MOD(test_no, 24),
                     'HOUR'
              )
        END,

    /* 회원 탈퇴 상태 */
    CASE
        /* 삭제 완료 */
        WHEN MOD(test_no, 25) = 0 THEN 'DELETED'

        /* 삭제 대기 */
        WHEN MOD(test_no, 12) = 0 THEN 'DELETE_PENDING'

        /* 정상 */
        ELSE 'ACTIVE'
        END,

    /* 탈퇴 요청 시간 */
    CASE
        WHEN MOD(test_no, 25) = 0
            THEN SYSTIMESTAMP
            - NUMTODSINTERVAL(3, 'DAY')

        WHEN MOD(test_no, 12) = 0
            THEN SYSTIMESTAMP
            - NUMTODSINTERVAL(1, 'DAY')

        ELSE NULL
        END,

    /* 완전 삭제 예정 시간 */
    CASE
        WHEN MOD(test_no, 25) = 0
            THEN SYSTIMESTAMP
            - NUMTODSINTERVAL(1, 'DAY')

        WHEN MOD(test_no, 12) = 0
            THEN SYSTIMESTAMP
            + NUMTODSINTERVAL(1, 'DAY')

        ELSE NULL
        END,

    /* 삭제 완료 시간 */
    CASE
        WHEN MOD(test_no, 25) = 0
            THEN SYSTIMESTAMP
            - NUMTODSINTERVAL(1, 'DAY')

        ELSE NULL
        END

FROM (
         SELECT LEVEL AS test_no
         FROM DUAL
             CONNECT BY LEVEL <= 105
     ) test_data

/* 이미 생성한 테스트 계정은 중복 생성하지 않음 */
WHERE NOT EXISTS (
    SELECT 1
    FROM APP_MEMBER member
    WHERE member.LOGIN_ID =
          'test_user' || LPAD(test_data.test_no, 3, '0')
);

COMMIT;