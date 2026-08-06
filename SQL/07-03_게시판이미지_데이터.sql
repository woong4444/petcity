/* 선행 실행: 01-00, 07-00, 07-01 */

/* 03_BOARD 이미지 데이터.sql */

MERGE INTO BOARD_IMAGE i
USING (
    WITH IMAGE_DATA (
        BOARD_TYPE, BOARD_TITLE, BOARD_WRITER_LOGIN,
        ORIGINAL_NAME, SAVED_NAME, IMAGE_URL, CREATED_AT, LINK_URL
    ) AS (
        SELECT 'INFO', 'fdfd', 'admin',
               '83032551-ea07-4cd7-8a89-484d2e8c71a6.png',
               'd5740833-0c06-43be-9616-f46f7fab8e22.png',
               '/upload/board/d5740833-0c06-43be-9616-f46f7fab8e22.png',
               TIMESTAMP '2026-07-10 11:14:11', 'https://www.naver.com'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', 'ㄹㅇㄴㅁㄹ', 'admin',
               '83032551-ea07-4cd7-8a89-484d2e8c71a6.png',
               'c764f5ea-80ff-44c5-92fb-bbb2c0a46837.png',
               '/upload/board/c764f5ea-80ff-44c5-92fb-bbb2c0a46837.png',
               TIMESTAMP '2026-07-10 15:18:08', 'https://www.naver.com'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', 'fdsfsdf', 'admin',
               'ChatGPT Image 2026년 7월 15일 오후 08_50_14.png',
               '6cc2d96a-ade4-4bae-a1d8-cd88853fd167.png',
               '/upload/board/6cc2d96a-ade4-4bae-a1d8-cd88853fd167.png',
               TIMESTAMP '2026-07-21 17:05:33', 'https://www.naver.com'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', 'fdf', 'admin',
               '생성된 이미지 1 (2).png',
               '4d236911-78e1-43a0-bd12-4f3c808efe61.png',
               '/upload/board/4d236911-78e1-43a0-bd12-4f3c808efe61.png',
               TIMESTAMP '2026-07-24 10:51:45', 'https://www.naver.com'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', 'fasdfafds', 'admin',
               '생성된 이미지 1.png',
               '41a296f5-7398-47d9-9a64-8e832abfe7fa.png',
               '/upload/board/41a296f5-7398-47d9-8e832abfe7fa.png',
               TIMESTAMP '2026-07-24 14:02:28', 'https://www.naver.com'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', 'fdsaf', 'admin',
               '생성된 이미지 1.png',
               '4abc13d0-5d59-4ab9-8bd0-19fc6dc39dfe.png',
               '/upload/board/4abc13d0-5d59-4ab9-8bd0-19fc6dc39dfe.png',
               TIMESTAMP '2026-07-24 14:03:02', 'https://www.naver.com'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', 'fasd', 'admin',
               '생성된 이미지 1 (2).png',
               'ad57ca2a-2c52-48d1-ae8e-b266a58253d1.png',
               '/upload/board/ad57ca2a-2c52-48d1-ae8e-b266a58253d1.png',
               TIMESTAMP '2026-07-24 15:56:24', 'https://www.naver.com'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', 'fdsfsdafsadf', 'admin',
               '생성된 이미지 1.png',
               '4f6e0da9-95a4-433a-b389-705ecc0e8ec2.png',
               '/upload/board/4f6e0da9-95a4-433a-b389-705ecc0e8ec2.png',
               TIMESTAMP '2026-07-24 18:06:50', 'https://www.naver.com'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 말티즈 눈물자국 관리 방법', 'admin',
               'pet-guide-190.jpg', 'pet-guide-190.jpg',
               'https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://www.akc.org/expert-advice/health/'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 골든리트리버 산책량과 관절 관리', 'admin',
               'pet-guide-191.jpg', 'pet-guide-191.jpg',
               'https://images.unsplash.com/photo-1558788353-f76d92427f16?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://www.akc.org/expert-advice/health/'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 보더콜리의 지루함 줄이는 놀이 5가지', 'admin',
               'pet-guide-192.jpg', 'pet-guide-192.jpg',
               'https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://www.akc.org/expert-advice/lifestyle/'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 코리안숏헤어, 실내 생활 환경 만들기', 'admin',
               'pet-guide-193.jpg', 'pet-guide-193.jpg',
               'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://catfriendly.com/'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 먼치킨 체형에 맞는 생활 안전 수칙', 'admin',
               'pet-guide-194.jpg', 'pet-guide-194.jpg',
               'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://catfriendly.com/'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 러시안블루와 조용한 교감 시간', 'admin',
               'pet-guide-195.jpg', 'pet-guide-195.jpg',
               'https://images.unsplash.com/photo-1495360010541-f48722b34f7d?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://catfriendly.com/'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 도마뱀 사육장 온도와 습도 체크', 'admin',
               'pet-guide-196.jpg', 'pet-guide-196.jpg',
               'https://images.unsplash.com/photo-1505009258427-7160a95f7d2c?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://arav.org/'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 거북이 수질 관리와 일광욕 공간', 'admin',
               'pet-guide-197.jpg', 'pet-guide-197.jpg',
               'https://images.unsplash.com/photo-1496196614460-48988a57fccf?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://arav.org/'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 햄스터 케이지 청소 주기와 방법', 'admin',
               'pet-guide-198.jpg', 'pet-guide-198.jpg',
               'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://www.hamstersociety.sg/'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 토끼가 먹으면 안 되는 음식 정리', 'admin',
               'pet-guide-199.jpg', 'pet-guide-199.jpg',
               'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://rabbit.org/'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 친칠라 모래목욕, 이렇게 준비하세요', 'admin',
               'pet-guide-200.jpg', 'pet-guide-200.jpg',
               'https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://www.rspca.org.uk/adviceandwelfare/pets/rodents/chinchillas'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 금붕어 어항 물갈이의 기본', 'admin',
               'pet-guide-201.jpg', 'pet-guide-201.jpg',
               'https://images.unsplash.com/photo-1524704654690-b56c05c78a00?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://www.aquariumcoop.com/blogs/aquarium'
        FROM DUAL
        UNION ALL
        SELECT 'INFO', '[펫도감 더미] 열대어 합사 전 확인해야 할 3가지', 'admin',
               'pet-guide-202.jpg', 'pet-guide-202.jpg',
               'https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=900&q=80',
               TIMESTAMP '2026-07-29 11:43:46', 'https://www.aquariumcoop.com/blogs/aquarium'
        FROM DUAL
    )
    SELECT
        b.BOARD_ID,
        d.ORIGINAL_NAME,
        d.SAVED_NAME,
        d.IMAGE_URL,
        d.CREATED_AT,
        d.LINK_URL
    FROM IMAGE_DATA d
    JOIN BOARD b
        ON b.BOARD_TYPE = d.BOARD_TYPE
       AND b.TITLE = d.BOARD_TITLE
    JOIN APP_MEMBER writer
        ON writer.MEMBER_ID = b.MEMBER_ID
       AND writer.LOGIN_ID = d.BOARD_WRITER_LOGIN
) s
ON (
    i.BOARD_ID = s.BOARD_ID
    AND i.SAVED_NAME = s.SAVED_NAME
)
WHEN MATCHED THEN
    UPDATE SET
        i.ORIGINAL_NAME = s.ORIGINAL_NAME,
        i.IMAGE_URL = s.IMAGE_URL,
        i.CREATED_AT = s.CREATED_AT,
        i.LINK_URL = s.LINK_URL
WHEN NOT MATCHED THEN
    INSERT (
        IMAGE_ID, BOARD_ID, ORIGINAL_NAME, SAVED_NAME,
        IMAGE_URL, CREATED_AT, LINK_URL
    )
    VALUES (
        SEQ_BOARD_IMAGE.NEXTVAL, s.BOARD_ID, s.ORIGINAL_NAME, s.SAVED_NAME,
        s.IMAGE_URL, s.CREATED_AT, s.LINK_URL
    );

COMMIT;
SELECT COUNT(*) AS BOARD_IMAGE_COUNT FROM BOARD_IMAGE;
