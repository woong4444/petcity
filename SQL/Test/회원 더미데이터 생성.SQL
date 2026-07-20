/* =========================================================
   기존 test001 ~ test100 계정 삭제

   이미 테스트 회원이 존재하는 경우
   LOGIN_ID 중복 오류를 방지하기 위한 SQL이다.

   admin 계정은 삭제되지 않는다.
   ========================================================= */

DELETE FROM APP_MEMBER
WHERE REGEXP_LIKE(
              LOGIN_ID,
              '^test(00[1-9]|0[1-9][0-9]|100)$'
      );

COMMIT;


/* =========================================================
   PetCity 테스트 회원 100명 생성

   기존 admin 계정은 건드리지 않는다.

   로그인 아이디:
   test001 ~ test100

   로그인 비밀번호:
   전부 1234

   역할:
   USER 80명
   OWNER 20명

   상태:
   ACTIVE / ACTIVE   90명
   DELETED / DELETED 10명
   ========================================================= */

DECLARE
/* 비밀번호 1234의 BCrypt 암호문 */
V_PASSWORD CONSTANT VARCHAR2(200) :=
        '$2a$10$CIU/6.QNVORtdY41VTyVje2cmIKfzPYud.sZ8GGmCO8dtqWid0CEi';

    V_ROLE                APP_MEMBER.ROLE%TYPE;
    V_STATUS              APP_MEMBER.STATUS%TYPE;
    V_MEMBER_STATUS       APP_MEMBER.MEMBER_STATUS%TYPE;

    V_LAST_LOGIN_AT       APP_MEMBER.LAST_LOGIN_AT%TYPE;
    V_BLOCKED_AT          APP_MEMBER.BLOCKED_AT%TYPE;
    V_DELETE_REQUESTED_AT APP_MEMBER.DELETE_REQUESTED_AT%TYPE;
    V_DELETED_AT          APP_MEMBER.DELETED_AT%TYPE;
    V_HARD_DELETE_AT      APP_MEMBER.HARD_DELETE_AT%TYPE;
    V_DELETE_REASON       APP_MEMBER.DELETE_REASON%TYPE;

BEGIN
FOR I IN 1..100 LOOP

        /* -------------------------------------------------
           역할 설정

           5의 배수:
           OWNER

           나머지:
           USER

           결과:
           OWNER 20명
           USER 80명
           ------------------------------------------------- */

        IF MOD(I, 5) = 0 THEN
            V_ROLE := 'OWNER';
ELSE
            V_ROLE := 'USER';
END IF;


        /* -------------------------------------------------
           차단 및 삭제 관련 값 초기화
           ------------------------------------------------- */

        V_BLOCKED_AT := NULL;
        V_DELETE_REQUESTED_AT := NULL;
        V_DELETED_AT := NULL;
        V_HARD_DELETE_AT := NULL;
        V_DELETE_REASON := NULL;


        /* -------------------------------------------------
           test001 ~ test090

           정상 활동 회원 90명

           STATUS:
           ACTIVE

           MEMBER_STATUS:
           ACTIVE

           마지막 로그인은 최근 90일 이내로 설정해서
           1년 미로그인 차단 대상이 되지 않게 한다.
           ------------------------------------------------- */

        IF I <= 90 THEN

            V_STATUS := 'ACTIVE';
            V_MEMBER_STATUS := 'ACTIVE';

            V_LAST_LOGIN_AT :=
                LOCALTIMESTAMP
                - NUMTODSINTERVAL(
                    MOD(I, 90),
                    'DAY'
                );


        /* -------------------------------------------------
           test091 ~ test100

           삭제 상태 회원 10명

           STATUS:
           DELETED

           MEMBER_STATUS:
           DELETED

           HARD_DELETE_AT은 현재보다 30일 뒤로 설정해서
           테스트 스케줄러가 바로 삭제하지 않게 한다.
           ------------------------------------------------- */

ELSE

            V_STATUS := 'DELETED';
            V_MEMBER_STATUS := 'DELETED';

            V_LAST_LOGIN_AT :=
                ADD_MONTHS(
                    LOCALTIMESTAMP,
                    -13
                );

            V_DELETE_REQUESTED_AT :=
                LOCALTIMESTAMP - INTERVAL '1' DAY;

            V_DELETED_AT :=
                LOCALTIMESTAMP - INTERVAL '1' DAY;

            V_HARD_DELETE_AT :=
                LOCALTIMESTAMP + INTERVAL '30' DAY;

            V_DELETE_REASON :=
                '테스트용 삭제 상태 회원';

END IF;


        /* -------------------------------------------------
           테스트 회원 INSERT
           ------------------------------------------------- */

INSERT INTO APP_MEMBER (
    MEMBER_ID,
    LOGIN_ID,
    PASSWORD,
    NICKNAME,
    EMAIL,
    PHONE,
    ROLE,
    CREATED_AT,
    UPDATED_AT,
    PROFILE_IMAGE,
    EMAIL_VERIFIED,
    STATUS,
    LAST_LOGIN_AT,
    MEMBER_STATUS,
    DELETE_REQUESTED_AT,
    HARD_DELETE_AT,
    DELETED_AT,
    BLOCKED_AT,
    DELETE_REASON
)
VALUES (
           SEQ_APP_MEMBER.NEXTVAL,

           /* test001 ~ test100 */
           'test' || LPAD(I, 3, '0'),

           /* 실제 로그인 비밀번호는 1234 */
           V_PASSWORD,

           /* 테스트회원001 ~ 테스트회원100 */
           '테스트회원' || LPAD(I, 3, '0'),

           /* test001@petcity.test */
           'test' || LPAD(I, 3, '0')
               || '@petcity.test',

           /* 010-9000-0001 ~ 010-9000-0100 */
           '010-9000-' || LPAD(I, 4, '0'),

           V_ROLE,

           /* 생성일은 회원별로 다르게 설정 */
           LOCALTIMESTAMP
               - NUMTODSINTERVAL(
                   MOD(I, 180),
                   'DAY'
                 ),

           LOCALTIMESTAMP,

           NULL,

           /* 모든 테스트 회원 이메일 인증 완료 */
           'Y',

           V_STATUS,
           V_LAST_LOGIN_AT,
           V_MEMBER_STATUS,
           V_DELETE_REQUESTED_AT,
           V_HARD_DELETE_AT,
           V_DELETED_AT,
           V_BLOCKED_AT,
           V_DELETE_REASON
       );

END LOOP;

COMMIT;

DBMS_OUTPUT.PUT_LINE(
        '테스트 회원 test001 ~ test100 생성 완료'
    );

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
