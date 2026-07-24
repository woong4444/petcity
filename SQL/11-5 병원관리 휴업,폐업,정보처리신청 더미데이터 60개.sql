
--test031 아이디 병원장으로 바꾸기--

UPDATE APP_MEMBER
SET ROLE = 'OWNER'
WHERE LOGIN_ID = 'test031';

COMMIT;


--병원 20개 생성 --
INSERT INTO HOSPITAL (
    HOSPITAL_ID,
    OWNER_ID,
    NAME,
    ADDRESS,
    DETAIL_ADDRESS,
    PHONE,
    LATITUDE,
    LONGITUDE,
    OPEN_TIME,
    CLOSE_TIME,
    BREAK_TIME,
    CLOSED_DAYS,
    STATUS,
    DESCRIPTION,
    IMAGE_URL,
    WEBSITE_URL,
    DISTRICT,
    DOCTOR_INFO,
    OWNER_NAME,
    NOTE,
    CREATED_AT,
    UPDATED_AT
)
SELECT
    SEQ_HOSPITAL.NEXTVAL,
    M.MEMBER_ID,
    '[더미] 테스트동물병원 ' || LPAD(N.NO, 2, '0'),
    '서울특별시 서초구 테스트로 ' || (100 + N.NO),
    N.NO || '01호',
    '02-5000-' || LPAD(N.NO, 4, '0'),
    37.490000 + (N.NO / 10000),
    127.030000 + (N.NO / 10000),
    '09:00',
    '20:00',
    '13:00~14:00',
    '일요일',
    'OPEN',
    '[더미] 병원 관리 요청 기능 테스트용 병원입니다.',
    NULL,
    'https://dummy' || LPAD(N.NO, 2, '0') || '.petcity.test',
    '서초구',
    '[더미] 테스트 수의사 2명',
    M.NICKNAME,
    '[더미] 자동 생성 병원',
    SYSTIMESTAMP,
    SYSTIMESTAMP
FROM APP_MEMBER M
         CROSS JOIN (
    SELECT LEVEL AS NO
    FROM DUAL
    CONNECT BY LEVEL <= 20
) N
WHERE M.LOGIN_ID = 'test031'
  AND NOT EXISTS (
    SELECT 1
    FROM HOSPITAL H
    WHERE H.OWNER_ID = M.MEMBER_ID
      AND H.NAME = '[더미] 테스트동물병원 ' || LPAD(N.NO, 2, '0')
);

COMMIT;

-- 병원  정보수정·휴업·폐업 요청 60개
INSERT INTO HOSPITAL_UPDATE_REQUEST (
    HOSPITAL_ID,
    MEMBER_ID,
    REQUEST_TYPE,
    APPLICANT_NAME,
    BUSINESS_NUMBER,
    DOCUMENT_URL,
    HOSPITAL_NAME,
    HOSPITAL_PHONE,
    HOSPITAL_ADDRESS,
    HOSPITAL_DETAIL_ADDRESS,
    HOSPITAL_DISTRICT,
    HOSPITAL_WEBSITE_URL,
    HOSPITAL_LATITUDE,
    HOSPITAL_LONGITUDE,
    MEDICAL_SUBJECTS,
    OPEN_TIME,
    CLOSE_TIME,
    BREAK_TIME,
    CLOSED_DAYS,
    HOSPITAL_DOCTOR_INFO,
    HOSPITAL_DESCRIPTION,
    HOSPITAL_IMAGE_URL,
    HOSPITAL_NOTE,
    TEMP_CLOSE_START_AT,
    TEMP_CLOSE_END_AT,
    REQUEST_REASON,
    STATUS,
    CREATED_AT
)
SELECT
    H.HOSPITAL_ID,
    H.OWNER_ID,
    R.REQUEST_TYPE,
    H.OWNER_NAME,
    '900-10-' || LPAD(H.NO, 4, '0'),
    '/upload/dummy/license-' || LPAD(H.NO, 2, '0') || '.pdf',
    H.NAME,
    H.PHONE,
    H.ADDRESS,
    H.DETAIL_ADDRESS,
    H.DISTRICT,
    H.WEBSITE_URL,
    H.LATITUDE,
    H.LONGITUDE,
    H.MEDICAL_SUBJECTS,
    H.OPEN_TIME,
    H.CLOSE_TIME,
    H.BREAK_TIME,
    H.CLOSED_DAYS,
    H.DOCTOR_INFO,
    H.DESCRIPTION,
    H.IMAGE_URL,
    H.NOTE,

    CASE
        WHEN R.REQUEST_TYPE = 'TEMP_CLOSE'
            THEN SYSTIMESTAMP + NUMTODSINTERVAL(H.NO * 10, 'DAY')
        ELSE NULL
        END,

    CASE
        WHEN R.REQUEST_TYPE = 'TEMP_CLOSE'
            THEN SYSTIMESTAMP + NUMTODSINTERVAL((H.NO * 10) + 5, 'DAY')
        ELSE NULL
        END,

    CASE
        WHEN R.REQUEST_TYPE = 'UPDATE'
            THEN '[더미] 병원 정보 수정 요청'
        WHEN R.REQUEST_TYPE = 'TEMP_CLOSE'
            THEN '[더미] 리모델링 공사로 인한 휴업 요청'
        WHEN R.REQUEST_TYPE = 'CLOSE'
            THEN '[더미] 관리자 목록 테스트용 폐업 요청'
        END,

    'PENDING',

    SYSTIMESTAMP - NUMTODSINTERVAL(
            (H.NO * 3) +
            CASE
                WHEN R.REQUEST_TYPE = 'UPDATE' THEN 1
                WHEN R.REQUEST_TYPE = 'TEMP_CLOSE' THEN 2
                ELSE 3
                END,
            'MINUTE'
                   )

FROM (
         SELECT
             H.*,
             ROW_NUMBER() OVER (ORDER BY H.HOSPITAL_ID) AS NO
         FROM HOSPITAL H
             JOIN APP_MEMBER M
         ON M.MEMBER_ID = H.OWNER_ID
         WHERE M.LOGIN_ID = 'test031'
           AND H.NAME LIKE '[더미] 테스트동물병원 %'
     ) H
         CROSS JOIN (
    SELECT 'UPDATE' AS REQUEST_TYPE FROM DUAL
    UNION ALL
    SELECT 'TEMP_CLOSE' FROM DUAL
    UNION ALL
    SELECT 'CLOSE' FROM DUAL
) R
WHERE NOT EXISTS (
    SELECT 1
    FROM HOSPITAL_UPDATE_REQUEST U
    WHERE U.HOSPITAL_ID = H.HOSPITAL_ID
      AND U.REQUEST_TYPE = R.REQUEST_TYPE
      AND U.REQUEST_REASON LIKE '[더미]%'
);

COMMIT;