INSERT INTO APP_MEMBER (
    MEMBER_ID,
    LOGIN_ID,
    PASSWORD,
    NICKNAME,
    ROLE,
    EMAIL_VERIFIED,
    STATUS,
    MEMBER_STATUS,
    CREATED_AT,
    UPDATED_AT
)
VALUES (
           SEQ_APP_MEMBER.NEXTVAL,
           'admin',
           '$2a$10$CIU/6.QNVORtdY41VTyVje2cmIKfzPYud.sZ8GGmCO8dtqWid0CEi',
           '관리자',
           'ADMIN',
           'Y',
           'ACTIVE',
           'ACTIVE',
           LOCALTIMESTAMP,
           LOCALTIMESTAMP
       );

COMMIT;