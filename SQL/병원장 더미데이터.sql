DECLARE
v_pwd VARCHAR2(500);
BEGIN
    -- 0. 기존 더미 owner 계정 및 매핑 깔끔하게 초기화
UPDATE HOSPITAL SET OWNER_ID = NULL WHERE OWNER_ID IN (SELECT MEMBER_ID FROM MEMBER WHERE USERNAME LIKE 'owner_%');
DELETE FROM MEMBER WHERE USERNAME LIKE 'owner_%';
COMMIT;

-- 1. 'test005'의 암호화된 비밀번호 가져오기 (없으면 '1234')
BEGIN
SELECT PASSWORD INTO v_pwd FROM MEMBER WHERE USERNAME = 'test005';
EXCEPTION WHEN NO_DATA_FOUND THEN
        v_pwd := '1234';
END;

    -- 2. 모든 병원을 돌면서 병원 ID와 똑같은 번호로 원장 계정을 만들고 1:1 매칭
FOR h IN (SELECT HOSPITAL_ID, NAME FROM HOSPITAL) LOOP

        -- 만약 MEMBER_ID 시퀀스나 기존 데이터와 충돌이 난다면 무시하고 넘어갑니다.
BEGIN
INSERT INTO MEMBER (
    MEMBER_ID,
    USERNAME,
    PASSWORD,
    NICKNAME,
    ROLE_ID
)
VALUES (
           h.HOSPITAL_ID, -- 병원 번호 그대로 회원 번호로 사용! (1번 병원 = 1번 회원)
           'owner_' || h.HOSPITAL_ID,
           v_pwd,         -- test005와 동일한 암호화 비밀번호 적용 (바로 로그인 가능)
           SUBSTR(h.NAME, 1, 10) || '원장',
           2              -- 병원장 권한 ID
       );
EXCEPTION WHEN DUP_VAL_ON_INDEX THEN
            NULL;
END;

        -- 3. 병원의 OWNER_ID를 병원 번호와 똑같이 지정
UPDATE HOSPITAL
SET OWNER_ID = h.HOSPITAL_ID
WHERE HOSPITAL_ID = h.HOSPITAL_ID;

END LOOP;

COMMIT;
END;