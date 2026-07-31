-- 상각_07-30: 회원 탈퇴 사유를 한글 500자까지 저장할 수 있도록 문자 기준으로 보정
-- 기존 컬럼이 VARCHAR2(500 BYTE)인 DB와 컬럼이 없는 DB에서 모두 실행할 수 있습니다.

DECLARE
    v_column_count NUMBER;
    v_char_used    USER_TAB_COLUMNS.CHAR_USED%TYPE;
    v_char_length  USER_TAB_COLUMNS.CHAR_LENGTH%TYPE;
BEGIN
    SELECT COUNT(*)
      INTO v_column_count
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'APP_MEMBER'
       AND COLUMN_NAME = 'DELETE_REASON';

    IF v_column_count = 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE APP_MEMBER ADD (DELETE_REASON VARCHAR2(500 CHAR))';
    ELSE
        SELECT CHAR_USED,
               CHAR_LENGTH
          INTO v_char_used,
               v_char_length
          FROM USER_TAB_COLUMNS
         WHERE TABLE_NAME = 'APP_MEMBER'
           AND COLUMN_NAME = 'DELETE_REASON';

        IF NVL(v_char_used, 'B') <> 'C'
           OR v_char_length <> 500 THEN
            EXECUTE IMMEDIATE
                'ALTER TABLE APP_MEMBER MODIFY (DELETE_REASON VARCHAR2(500 CHAR))';
        END IF;
    END IF;
END;
/

-- 적용 결과 확인
SELECT COLUMN_NAME,
       DATA_TYPE,
       DATA_LENGTH,
       CHAR_LENGTH,
       CHAR_USED
  FROM USER_TAB_COLUMNS
 WHERE TABLE_NAME = 'APP_MEMBER'
   AND COLUMN_NAME = 'DELETE_REASON';
