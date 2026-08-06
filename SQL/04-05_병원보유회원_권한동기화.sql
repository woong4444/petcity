
/* ============================================================================
   병원 보유 수에 따른 회원 권한 동기화

   병원 1개 이상: OWNER
   병원 0개     : USER
   ADMIN        : 변경하지 않음
============================================================================ */

UPDATE APP_MEMBER M
SET ROLE = CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM HOSPITAL H
                   WHERE H.OWNER_ID = M.MEMBER_ID
               )
               THEN 'OWNER'
               ELSE 'USER'
           END,
    UPDATED_AT = LOCALTIMESTAMP
WHERE ROLE IN ('USER', 'OWNER');

COMMIT;

SELECT M.MEMBER_ID,
       M.LOGIN_ID,
       M.ROLE,
       COUNT(H.HOSPITAL_ID) AS HOSPITAL_COUNT
FROM APP_MEMBER M
LEFT JOIN HOSPITAL H
       ON H.OWNER_ID = M.MEMBER_ID
WHERE M.ROLE IN ('USER', 'OWNER')
GROUP BY M.MEMBER_ID, M.LOGIN_ID, M.ROLE
ORDER BY M.MEMBER_ID;
