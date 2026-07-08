/* =====================================================
   0. 기존 테이블 / 시퀀스 삭제
===================================================== */

BEGIN
FOR t IN (
        SELECT table_name
        FROM user_tables
        WHERE table_name IN (
            'BOARD_IMAGE',
            'BOARD_COMMENT',
            'BOARD',
            'HOSPITAL_OWNER_REQUEST',
            'FAVORITE_HOSPITAL',
            'HOSPITAL_LIKE',
            'HOSPITAL_REVIEW',
            'HOSPITAL_SERVICE',
            'HOSPITAL_ANIMAL',
            'MEMBER_PET',
            'HOSPITAL',
            'MEDICAL_SERVICE',
            'ANIMAL_TYPE',
            'APP_MEMBER'
        )
    ) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
END LOOP;
END;
/

BEGIN
FOR s IN (
        SELECT sequence_name
        FROM user_sequences
        WHERE sequence_name IN (
            'SEQ_APP_MEMBER',
            'SEQ_ANIMAL_TYPE',
            'SEQ_MEDICAL_SERVICE',
            'SEQ_HOSPITAL',
            'SEQ_HOSPITAL_REVIEW',
            'SEQ_HOSPITAL_OWNER_REQUEST',
            'SEQ_BOARD',
            'SEQ_BOARD_COMMENT',
            'SEQ_BOARD_IMAGE',
            'SEQ_MEMBER_PET'
        )
    ) LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
END LOOP;
END;
/



















