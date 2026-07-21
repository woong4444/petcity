DECLARE
TYPE COUNT_ARRAY IS TABLE OF PLS_INTEGER
        INDEX BY PLS_INTEGER;

    LOGIN_COUNTS COUNT_ARRAY;
    GUEST_COUNTS COUNT_ARRAY;

    VISIT_DAY DATE;
    LOGIN_INDEX PLS_INTEGER;
    MEMBER_COUNT PLS_INTEGER;
BEGIN
SELECT COUNT(*)
INTO MEMBER_COUNT
FROM APP_MEMBER
WHERE MEMBER_ID IS NOT NULL;

IF MEMBER_COUNT < 5 THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            '로그인 방문자 더미 데이터를 만들려면 MEMBER 테이블에 회원이 최소 5명 필요합니다.'
        );
END IF;

DELETE FROM SITE_VISIT_LOG
WHERE VISITOR_KEY LIKE 'DUMMY:%';

LOGIN_COUNTS(1) := 4;
    LOGIN_COUNTS(2) := 3;
    LOGIN_COUNTS(3) := 5;
    LOGIN_COUNTS(4) := 2;
    LOGIN_COUNTS(5) := 4;
    LOGIN_COUNTS(6) := 3;

    GUEST_COUNTS(1) := 8;
    GUEST_COUNTS(2) := 6;
    GUEST_COUNTS(3) := 10;
    GUEST_COUNTS(4) := 5;
    GUEST_COUNTS(5) := 7;
    GUEST_COUNTS(6) := 5;

FOR DAY_INDEX IN 1..6 LOOP

        VISIT_DAY := TRUNC(SYSDATE) - DAY_INDEX;
        LOGIN_INDEX := 0;

FOR MEMBER_RECORD IN (
            SELECT MEMBER_ID
            FROM APP_MEMBER
            WHERE MEMBER_ID IS NOT NULL
            ORDER BY MEMBER_ID
        ) LOOP

            EXIT WHEN LOGIN_INDEX >= LOGIN_COUNTS(DAY_INDEX);

            LOGIN_INDEX := LOGIN_INDEX + 1;

INSERT INTO SITE_VISIT_LOG (
    VISIT_ID,
    VISIT_DATE,
    VISITOR_TYPE,
    VISITOR_KEY,
    MEMBER_ID,
    SESSION_ID,
    IP_ADDRESS,
    USER_AGENT,
    VISITED_URL,
    FIRST_VISITED_AT,
    LAST_VISITED_AT,
    CREATED_AT,
    UPDATED_AT
)
VALUES (
           SEQ_SITE_VISIT_LOG.NEXTVAL,
           VISIT_DAY,
           'LOGIN',
           'DUMMY:LOGIN:'
               || TO_CHAR(VISIT_DAY, 'YYYYMMDD')
               || ':'
               || MEMBER_RECORD.MEMBER_ID,
           MEMBER_RECORD.MEMBER_ID,
           'DUMMY-LOGIN-SESSION-'
               || TO_CHAR(VISIT_DAY, 'YYYYMMDD')
               || '-'
               || MEMBER_RECORD.MEMBER_ID,
           '127.0.0.1',
           'PetCity Dummy Browser',
           '/admin/dummy-login',
           CAST(VISIT_DAY AS TIMESTAMP)
               + NUMTODSINTERVAL(
                   9 + LOGIN_INDEX,
                   'HOUR'
                 ),
           CAST(VISIT_DAY AS TIMESTAMP)
               + NUMTODSINTERVAL(
                   10 + LOGIN_INDEX,
                   'HOUR'
                 ),
           SYSTIMESTAMP,
           SYSTIMESTAMP
       );

END LOOP;

FOR GUEST_INDEX IN 1..GUEST_COUNTS(DAY_INDEX) LOOP

            INSERT INTO SITE_VISIT_LOG (
                VISIT_ID,
                VISIT_DATE,
                VISITOR_TYPE,
                VISITOR_KEY,
                MEMBER_ID,
                SESSION_ID,
                IP_ADDRESS,
                USER_AGENT,
                VISITED_URL,
                FIRST_VISITED_AT,
                LAST_VISITED_AT,
                CREATED_AT,
                UPDATED_AT
            )
            VALUES (
                SEQ_SITE_VISIT_LOG.NEXTVAL,
                VISIT_DAY,
                'GUEST',
                'DUMMY:GUEST:'
                    || TO_CHAR(VISIT_DAY, 'YYYYMMDD')
                    || ':'
                    || LPAD(GUEST_INDEX, 3, '0'),
                NULL,
                'DUMMY-GUEST-SESSION-'
                    || TO_CHAR(VISIT_DAY, 'YYYYMMDD')
                    || '-'
                    || LPAD(GUEST_INDEX, 3, '0'),
                '127.0.0.1',
                'PetCity Dummy Browser',
                '/admin/dummy-guest',
                CAST(VISIT_DAY AS TIMESTAMP)
                    + NUMTODSINTERVAL(8, 'HOUR')
                    + NUMTODSINTERVAL(
                        GUEST_INDEX * 10,
                        'MINUTE'
                    ),
                CAST(VISIT_DAY AS TIMESTAMP)
                    + NUMTODSINTERVAL(8, 'HOUR')
                    + NUMTODSINTERVAL(
                        GUEST_INDEX * 10 + 30,
                        'MINUTE'
                    ),
                SYSTIMESTAMP,
                SYSTIMESTAMP
            );

END LOOP;

END LOOP;

COMMIT;
END;

-- 확인 코드


SELECT
    TO_CHAR(
            VISIT_DATE,
            'YYYY-MM-DD'
    ) AS VISIT_DATE,

    COUNT(*) AS TOTAL_VISITOR_COUNT,

    SUM(
            CASE
                WHEN VISITOR_TYPE = 'LOGIN'
                    THEN 1
                ELSE 0
                END
    ) AS LOGIN_VISITOR_COUNT,

    SUM(
            CASE
                WHEN VISITOR_TYPE = 'GUEST'
                    THEN 1
                ELSE 0
                END
    ) AS GUEST_VISITOR_COUNT

FROM SITE_VISIT_LOG

WHERE VISITOR_KEY LIKE 'DUMMY:%'

GROUP BY VISIT_DATE

ORDER BY VISIT_DATE DESC;