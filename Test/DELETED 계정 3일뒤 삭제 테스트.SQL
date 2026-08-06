/* ==================================================
   장기 미로그인 회원 스케줄러 테스트

   테스트 회원:
   MEMBER_ID = 435

   현재 스케줄러 실행 순서:

   1. markBlockedMembersAsDeleted()
      BLOCKED → DELETED

   2. blockInactiveMembers()
      ACTIVE → BLOCKED

   3. hardDeleteExpiredInactiveMembers()
      삭제 예정 시간이 지난 DELETED 회원 실제 삭제
================================================== */


/* ==================================================
   1. 테스트 회원 초기화

   정상 회원 상태로 되돌리고
   마지막 로그인 시각을 13개월 전으로 설정한다.

   blockInactiveMembers() 실행 조건:

   STATUS = ACTIVE
   MEMBER_STATUS = ACTIVE
   ROLE != ADMIN
   LAST_LOGIN_AT 또는 CREATED_AT이 12개월 이상 지남
================================================== */

UPDATE APP_MEMBER
SET
    STATUS = 'ACTIVE',
    MEMBER_STATUS = 'ACTIVE',
    LAST_LOGIN_AT = ADD_MONTHS(LOCALTIMESTAMP, -13),
    BLOCKED_AT = NULL,
    DELETE_REQUESTED_AT = NULL,
    DELETED_AT = NULL,
    HARD_DELETE_AT = NULL,
    DELETE_REASON = NULL,
    UPDATED_AT = LOCALTIMESTAMP
WHERE MEMBER_ID = 435
  AND ROLE != 'ADMIN';

COMMIT;


/* 초기화 결과 확인 */

SELECT
    MEMBER_ID,
    LOGIN_ID,
    ROLE,
    STATUS,
    MEMBER_STATUS,
    LAST_LOGIN_AT,
    BLOCKED_AT,
    DELETE_REQUESTED_AT,
    DELETED_AT,
    HARD_DELETE_AT,
    DELETE_REASON
FROM APP_MEMBER
WHERE MEMBER_ID = 435;


/* ==================================================
   2. 장기 미로그인 차단 처리 확인

   스케줄러에서 호출되는 메서드:

   blockInactiveMembers()

   fixedDelay = 1000이면
   COMMIT 후 약 1~2초 뒤 조회한다.

   기대 결과:

   STATUS = BLOCKED
   MEMBER_STATUS = ACTIVE
   BLOCKED_AT = 스케줄러 실행 시각
================================================== */

SELECT
    MEMBER_ID,
    LOGIN_ID,
    ROLE,
    STATUS,
    MEMBER_STATUS,
    LAST_LOGIN_AT,
    BLOCKED_AT
FROM APP_MEMBER
WHERE MEMBER_ID = 435;


/* ==================================================
   3. 차단된 지 31일 지난 상태로 변경

   markBlockedMembersAsDeleted() 실행 조건:

   STATUS = BLOCKED
   MEMBER_STATUS = ACTIVE
   ROLE != ADMIN
   BLOCKED_AT이 현재보다 30일 이상 이전

   중요:
   여기서는 아직 DELETED 상태가 아니므로
   WHERE 조건은 BLOCKED / ACTIVE가 맞다.
================================================== */

UPDATE APP_MEMBER
SET
    BLOCKED_AT = LOCALTIMESTAMP - INTERVAL '31' DAY,
    UPDATED_AT = LOCALTIMESTAMP
WHERE MEMBER_ID = 435
  AND STATUS = 'BLOCKED'
  AND MEMBER_STATUS = 'ACTIVE'
  AND ROLE != 'ADMIN';

COMMIT;


/* 차단 시각 변경 결과 확인 */

SELECT
    MEMBER_ID,
    LOGIN_ID,
    ROLE,
    STATUS,
    MEMBER_STATUS,
    BLOCKED_AT,
    LOCALTIMESTAMP AS CURRENT_TIME
FROM APP_MEMBER
WHERE MEMBER_ID = 435;


/* ==================================================
   4. 소프트 삭제 상태 전환 확인

   스케줄러에서 호출되는 메서드:

   markBlockedMembersAsDeleted()

   fixedDelay = 1000이면
   COMMIT 후 약 1~2초 뒤 조회한다.

   기대 결과:

   STATUS = DELETED
   MEMBER_STATUS = DELETED
   DELETE_REQUESTED_AT = 현재 시각
   DELETED_AT = 현재 시각
   HARD_DELETE_AT = 현재 시각 + 3일
   DELETE_REASON 값 저장
================================================== */

SELECT
    MEMBER_ID,
    LOGIN_ID,
    ROLE,
    STATUS,
    MEMBER_STATUS,
    BLOCKED_AT,
    DELETE_REQUESTED_AT,
    DELETED_AT,
    HARD_DELETE_AT,
    DELETE_REASON
FROM APP_MEMBER
WHERE MEMBER_ID = 435;


/* ==================================================
   5. 하드 삭제 예정 시각을 1분 전으로 변경

   hardDeleteExpiredInactiveMembers() 실행 조건:

   STATUS = DELETED
   MEMBER_STATUS = DELETED
   ROLE != ADMIN
   HARD_DELETE_AT IS NOT NULL
   HARD_DELETE_AT <= 현재 시각
================================================== */

UPDATE APP_MEMBER
SET
    HARD_DELETE_AT = LOCALTIMESTAMP - INTERVAL '1' MINUTE,
    UPDATED_AT = LOCALTIMESTAMP
WHERE MEMBER_ID = 435
  AND STATUS = 'DELETED'
  AND MEMBER_STATUS = 'DELETED'
  AND ROLE != 'ADMIN';

COMMIT;


/* ==================================================
   6. 하드 삭제 대상 조회

   현재 Mapper의 DELETE 조건과 동일하게 작성한다.

   이 조회에서 MEMBER_ID = 435가 나오면
   하드 삭제 대상 조건을 만족한 상태다.
================================================== */

SELECT
    MEMBER_ID,
    LOGIN_ID,
    ROLE,
    STATUS,
    MEMBER_STATUS,
    HARD_DELETE_AT,
    LOCALTIMESTAMP AS CURRENT_TIME
FROM APP_MEMBER
WHERE MEMBER_ID = 435
  AND STATUS = 'DELETED'
  AND MEMBER_STATUS = 'DELETED'
  AND ROLE != 'ADMIN'
  AND HARD_DELETE_AT IS NOT NULL
  AND HARD_DELETE_AT <= LOCALTIMESTAMP;


/* ==================================================
   7. 하드 삭제 실행 대기

   스케줄러에서 호출되는 메서드:

   hardDeleteExpiredInactiveMembers()

   fixedDelay = 1000이면
   COMMIT 후 약 1~2초 기다린다.

   실행되는 SQL:

   DELETE FROM APP_MEMBER
   WHERE STATUS = 'DELETED'
     AND MEMBER_STATUS = 'DELETED'
     AND ROLE != 'ADMIN'
     AND HARD_DELETE_AT IS NOT NULL
     AND HARD_DELETE_AT <= LOCALTIMESTAMP
================================================== */


/* ==================================================
   8. 실제 삭제 결과 확인

   결과가 0건이면 정상적으로 하드 삭제된 것이다.

   결과가 계속 남아 있다면 콘솔에서 다음을 확인한다.

   - ORA-02292 외래키 오류
   - DB 잠금
   - 하드 삭제 스케줄러 실행 여부
================================================== */

SELECT
    MEMBER_ID,
    LOGIN_ID,
    ROLE,
    STATUS,
    MEMBER_STATUS,
    HARD_DELETE_AT
FROM APP_MEMBER
WHERE MEMBER_ID = 435;