-- [상각_07-19 / 데이터 적용 / 실행 순서 2]
-- 구분: 기존 병원 데이터 수정 SQL
-- 기능: 팀 제공 병원 이미지와 HOSPITAL.IMAGE_URL 매칭
-- 병원 ID와 static/images/hospital 파일명을 기준으로 대표 이미지를 매칭합니다.
-- 파일이 없는 병원은 다른 병원 사진이 노출되지 않도록 NULL 처리합니다.
-- 상각_07-19: 팀 제공 병원 이미지와 HOSPITAL 데이터 경로 매칭
UPDATE HOSPITAL
SET IMAGE_URL = CASE
    WHEN HOSPITAL_ID IN (4,16,52,54,57,81,86,95,97,105,114,115)
        THEN '/images/hospital/' || HOSPITAL_ID || '.JPG'
    WHEN HOSPITAL_ID IN (26,29,59,67,102)
        THEN '/images/hospital/' || HOSPITAL_ID || '.png'
    WHEN HOSPITAL_ID = 37
        THEN '/images/hospital/37.jpeg'
    WHEN HOSPITAL_ID BETWEEN 1 AND 125
         AND HOSPITAL_ID NOT IN (30,35,36,62,66,72,84,87,96,104)
        THEN '/images/hospital/' || HOSPITAL_ID || '.jpg'
    ELSE NULL
END
WHERE HOSPITAL_ID BETWEEN 1 AND 125;

COMMIT;

-- 검증: 경로가 설정된 병원 115개, 이미지 없음 10개가 정상입니다.
SELECT
    COUNT(*) AS TOTAL_COUNT,
    COUNT(IMAGE_URL) AS IMAGE_COUNT,
    SUM(CASE WHEN IMAGE_URL IS NULL THEN 1 ELSE 0 END) AS NO_IMAGE_COUNT
FROM HOSPITAL
WHERE HOSPITAL_ID BETWEEN 1 AND 125;
