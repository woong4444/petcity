-- 13개의 사진을 167개 병원에 번갈아 가면서 예쁘게 매핑합니다.
UPDATE HOSPITAL
SET IMAGE_URL = '/images/hospital/' || (MOD(HOSPITAL_ID, 13) + 1) || '.jpg';

-- 반드시 저장(COMMIT) 해주세요!
COMMIT;