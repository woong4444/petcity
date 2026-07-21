/* =====================================================
   1. 기존 SITE_VISIT_LOG 테이블 삭제

   현재 데이터가 없는 상태이므로
   방문자 통계 용도에 맞게 다시 생성한다.
===================================================== */

BEGIN
EXECUTE IMMEDIATE
    'DROP TABLE SITE_VISIT_LOG CASCADE CONSTRAINTS PURGE';

EXCEPTION
    WHEN OTHERS THEN

        /* 테이블이 존재하지 않는 경우는 무시 */
        IF SQLCODE != -942 THEN
            RAISE;
END IF;
END;



/* =====================================================
   2. 기존 시퀀스 삭제
===================================================== */

BEGIN
EXECUTE IMMEDIATE
    'DROP SEQUENCE SEQ_SITE_VISIT_LOG';

EXCEPTION
    WHEN OTHERS THEN

        /* 시퀀스가 존재하지 않는 경우는 무시 */
        IF SQLCODE != -2289 THEN
            RAISE;
END IF;
END;



/* =====================================================
   3. 방문자 기록 테이블 생성

   하루에 동일한 방문자는 한 행만 저장한다.

   로그인 방문자:
   VISITOR_KEY = Login:회원번호

   비로그인 방문자:
   VISITOR_KEY = NotLogin:세션아이디
===================================================== */

CREATE TABLE SITE_VISIT_LOG (

    /* 방문 기록 고유 번호 */
                                VISIT_ID NUMBER
                                    CONSTRAINT PK_SITE_VISIT_LOG
                                        PRIMARY KEY,


    /* 방문 날짜
       시간은 제거하고 날짜만 저장한다. */
                                VISIT_DATE DATE
                                    DEFAULT TRUNC(SYSDATE)
                                    NOT NULL,


    /* 방문자 종류
       LOGIN = 로그인 회원
       GUEST = 비로그인 방문자 */
                                VISITOR_TYPE VARCHAR2(20)
        NOT NULL,


    /* 하루 동안 방문자를 구분하는 고유값

       로그인:
       Login:214

       비로그인:
       NotLogin:세션아이디
    */
                                VISITOR_KEY VARCHAR2(300)
        NOT NULL,


    /* 로그인 회원 번호
       비로그인 방문자는 NULL */
                                MEMBER_ID NUMBER,


    /* 방문 당시 HTTP 세션 번호 */
                                SESSION_ID VARCHAR2(200)
        NOT NULL,


    /* 최초 방문 IP */
                                IP_ADDRESS VARCHAR2(100),


    /* 브라우저 및 기기 정보 */
                                USER_AGENT VARCHAR2(1000),


    /* 해당 날짜에 최초로 접속한 주소 */
                                VISITED_URL VARCHAR2(1000),


    /* 해당 날짜의 최초 방문 시간 */
                                FIRST_VISITED_AT TIMESTAMP
                                    NOT NULL,


    /* 해당 날짜의 마지막 방문 시간 */
                                LAST_VISITED_AT TIMESTAMP
                                    NOT NULL,


    /* DB에 방문 기록을 저장한 시간 */
                                CREATED_AT TIMESTAMP
                                    DEFAULT SYSTIMESTAMP
                                    NOT NULL,


    /* DB 기록이 수정된 시간 */
                                UPDATED_AT TIMESTAMP
                                    DEFAULT SYSTIMESTAMP
                                    NOT NULL,


    /* 방문자 종류 제한 */
                                CONSTRAINT CK_SITE_VISIT_LOG_TYPE
                                    CHECK (
                                        VISITOR_TYPE IN (
                                                         'LOGIN',
                                                         'GUEST'
                                            )
                                        ),


    /* 로그인 회원은 MEMBER_ID 필수
       비로그인 방문자는 MEMBER_ID가 NULL */
                                CONSTRAINT CK_SITE_VISIT_LOG_MEMBER
                                    CHECK (
                                        (
                                            VISITOR_TYPE = 'LOGIN'
                                                AND MEMBER_ID IS NOT NULL
                                            )
                                            OR
                                        (
                                            VISITOR_TYPE = 'GUEST'
                                                AND MEMBER_ID IS NULL
                                            )
                                        ),


    /* 같은 날짜에 동일 방문자를 중복 저장하지 않음 */
                                CONSTRAINT UQ_SITE_VISIT_LOG_DAILY
                                    UNIQUE (
                                            VISIT_DATE,
                                            VISITOR_KEY
                                        )
);

CREATE SEQUENCE SEQ_SITE_VISIT_LOG
    START WITH 1
    INCREMENT BY 1
    NOCACHE
NOCYCLE;


CREATE INDEX IDX_SITE_VISIT_LOG_DATE_TYPE
    ON SITE_VISIT_LOG (
                       VISIT_DATE,
                       VISITOR_TYPE
        );


CREATE INDEX IDX_SITE_VISIT_LOG_MEMBER
    ON SITE_VISIT_LOG (
                       MEMBER_ID
        );