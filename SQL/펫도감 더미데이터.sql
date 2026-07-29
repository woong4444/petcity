DECLARE
V_ADMIN_ID APP_MEMBER.MEMBER_ID%TYPE;
    V_BOARD_ID BOARD.BOARD_ID%TYPE;

    PROCEDURE ADD_PET_GUIDE(
        P_ANIMAL_ID IN NUMBER,
        P_TITLE IN VARCHAR2,
        P_CONTENT IN CLOB,
        P_IMAGE_URL IN VARCHAR2,
        P_LINK_URL IN VARCHAR2,
        P_HIT IN NUMBER,
        P_DAYS_AGO IN NUMBER
    ) IS
BEGIN
        V_BOARD_ID := SEQ_BOARD.NEXTVAL;

INSERT INTO BOARD (
    BOARD_ID, MEMBER_ID, BOARD_TYPE, TITLE, CONTENT,
    ANIMAL_ID, HIT, CREATED_AT, UPDATED_AT
) VALUES (
             V_BOARD_ID, V_ADMIN_ID, 'INFO', P_TITLE, P_CONTENT,
             P_ANIMAL_ID, P_HIT,
             SYSTIMESTAMP - NUMTODSINTERVAL(P_DAYS_AGO, 'DAY'),
             SYSTIMESTAMP - NUMTODSINTERVAL(P_DAYS_AGO, 'DAY')
         );

INSERT INTO BOARD_IMAGE (
    IMAGE_ID, BOARD_ID, ORIGINAL_NAME, SAVED_NAME, IMAGE_URL, LINK_URL
) VALUES (
             SEQ_BOARD_IMAGE.NEXTVAL,
             V_BOARD_ID,
             'pet-guide-' || V_BOARD_ID || '.jpg',
             'pet-guide-' || V_BOARD_ID || '.jpg',
             P_IMAGE_URL,
             P_LINK_URL
         );
END;

BEGIN
    /* 활성 관리자 1명을 작성자로 사용 */
SELECT MEMBER_ID
INTO V_ADMIN_ID
FROM (
         SELECT MEMBER_ID
         FROM APP_MEMBER
         WHERE ROLE = 'ADMIN'
           AND STATUS = 'ACTIVE'
           AND MEMBER_STATUS = 'ACTIVE'
         ORDER BY MEMBER_ID
     )
WHERE ROWNUM = 1;

/* 강아지 */
ADD_PET_GUIDE(
        6,
        '[펫도감 더미] 말티즈 눈물자국 관리 방법',
        TO_CLOB(q'[<h2>말티즈 눈물자국, 생활 관리부터 시작해요</h2><p>눈 주변은 미지근한 물에 적신 부드러운 거즈로 닦고, 물기를 충분히 말려 주세요.</p><p>눈곱의 색이나 양이 갑자기 달라졌다면 자가 처치보다 동물병원 상담을 권장합니다.</p>]'),
        'https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&w=900&q=80',
        'https://www.akc.org/expert-advice/health/',
        128, 2
    );

    ADD_PET_GUIDE(
        8,
        '[펫도감 더미] 골든리트리버 산책량과 관절 관리',
        TO_CLOB(q'[<h2>활동량이 많은 대형견의 건강 루틴</h2><p>산책 전후로 가벼운 스트레칭 시간을 만들고, 더운 날에는 낮 시간을 피해 주세요.</p><p>절뚝거림이나 활동량 감소가 계속되면 관절 검진이 필요할 수 있습니다.</p>]'),
        'https://images.unsplash.com/photo-1558788353-f76d92427f16?auto=format&fit=crop&w=900&q=80',
        'https://www.akc.org/expert-advice/health/',
        95, 5
    );

    ADD_PET_GUIDE(
        9,
        '[펫도감 더미] 보더콜리의 지루함 줄이는 놀이 5가지',
        TO_CLOB(q'[<h2>똑똑한 반려견에게는 두뇌 활동도 필요해요</h2><p>노즈워크, 간단한 트릭 연습, 숨은 장난감 찾기를 짧게 나누어 진행해 보세요.</p><p>한 번에 오래 하기보다 매일 꾸준히 하는 것이 좋습니다.</p>]'),
        'https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=900&q=80',
        'https://www.akc.org/expert-advice/lifestyle/',
        76, 9
    );

    /* 고양이 */
    ADD_PET_GUIDE(
        14,
        '[펫도감 더미] 코리안숏헤어, 실내 생활 환경 만들기',
        TO_CLOB(q'[<h2>고양이가 편안한 집은 높은 공간에서 시작돼요</h2><p>캣타워나 선반처럼 안전하게 올라갈 수 있는 공간과 숨을 곳을 마련해 주세요.</p><p>화장실은 조용한 곳에 두고 매일 청결하게 관리하는 것이 좋습니다.</p>]'),
        'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=900&q=80',
        'https://catfriendly.com/',
        143, 1
    );

    ADD_PET_GUIDE(
        16,
        '[펫도감 더미] 먼치킨 체형에 맞는 생활 안전 수칙',
        TO_CLOB(q'[<h2>낮은 높이의 계단과 미끄럼 방지가 중요해요</h2><p>소파와 침대 주변에는 반려동물 계단을 두고, 자주 뛰어내리는 곳에는 미끄럼 방지 매트를 깔아 주세요.</p><p>걷는 모습이 평소와 다르다면 빠르게 병원에 문의하세요.</p>]'),
        'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=900&q=80',
        'https://catfriendly.com/',
        84, 7
    );

    ADD_PET_GUIDE(
        18,
        '[펫도감 더미] 러시안블루와 조용한 교감 시간',
        TO_CLOB(q'[<h2>낯선 환경에서는 먼저 기다려 주세요</h2><p>고양이가 스스로 다가올 때까지 낮은 목소리로 말을 걸고, 짧은 놀이로 관계를 만들어 보세요.</p><p>억지로 안거나 숨는 공간을 막는 행동은 피해주세요.</p>]'),
        'https://images.unsplash.com/photo-1495360010541-f48722b34f7d?auto=format&fit=crop&w=900&q=80',
        'https://catfriendly.com/',
        67, 12
    );

    /* 파충류 */
    ADD_PET_GUIDE(
        22,
        '[펫도감 더미] 도마뱀 사육장 온도와 습도 체크',
        TO_CLOB(q'[<h2>종마다 다른 적정 환경을 먼저 확인하세요</h2><p>온도계와 습도계를 사육장 양쪽에 설치하면 환경 변화를 더 쉽게 확인할 수 있습니다.</p><p>먹이 섭취량이나 탈피 상태가 달라질 때는 사육 환경을 함께 점검해 주세요.</p>]'),
        'https://images.unsplash.com/photo-1505009258427-7160a95f7d2c?auto=format&fit=crop&w=900&q=80',
        'https://arav.org/',
        58, 4
    );

    ADD_PET_GUIDE(
        23,
        '[펫도감 더미] 거북이 수질 관리와 일광욕 공간',
        TO_CLOB(q'[<h2>깨끗한 물과 쉴 수 있는 육지 공간을 함께</h2><p>먹이 찌꺼기는 바로 제거하고, 여과기 상태와 물 온도를 정기적으로 확인해 주세요.</p><p>종에 맞는 UVB 조명과 안전한 일광욕 공간도 필요합니다.</p>]'),
        'https://images.unsplash.com/photo-1496196614460-48988a57fccf?auto=format&fit=crop&w=900&q=80',
        'https://arav.org/',
        46, 11
    );

    /* 설치류 */
    ADD_PET_GUIDE(
        29,
        '[펫도감 더미] 햄스터 케이지 청소 주기와 방법',
        TO_CLOB(q'[<h2>집 전체를 한 번에 바꾸지 않아도 돼요</h2><p>매일 젖은 깔짚과 먹이 찌꺼기를 제거하고, 전체 청소는 케이지 상태에 맞춰 진행하세요.</p><p>기존 둥지 재료를 일부 남겨두면 환경 변화 스트레스를 줄이는 데 도움이 됩니다.</p>]'),
        'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?auto=format&fit=crop&w=900&q=80',
        'https://www.hamstersociety.sg/',
        112, 3
    );

    ADD_PET_GUIDE(
        30,
        '[펫도감 더미] 토끼가 먹으면 안 되는 음식 정리',
        TO_CLOB(q'[<h2>건초는 기본, 간식은 소량만</h2><p>토끼는 충분한 양의 건초와 깨끗한 물이 필요합니다. 새로운 채소는 아주 적은 양부터 반응을 살펴보며 급여하세요.</p><p>식욕이 줄거나 배변량이 달라졌다면 빠른 진료가 필요할 수 있습니다.</p>]'),
        'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=900&q=80',
        'https://rabbit.org/',
        91, 6
    );

    ADD_PET_GUIDE(
        31,
        '[펫도감 더미] 친칠라 모래목욕, 이렇게 준비하세요',
        TO_CLOB(q'[<h2>친칠라 전용 모래를 짧은 시간 제공해요</h2><p>습기에 약한 친칠라는 물목욕 대신 전용 모래목욕이 필요합니다.</p><p>모래가 더러워지면 교체하고, 목욕통은 사용 후 치워두어 과도한 목욕을 예방해 주세요.</p>]'),
        'https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=900&q=80',
        'https://www.rspca.org.uk/adviceandwelfare/pets/rodents/chinchillas',
        52, 13
    );

    /* 어류 */
    ADD_PET_GUIDE(
        33,
        '[펫도감 더미] 금붕어 어항 물갈이의 기본',
        TO_CLOB(q'[<h2>한 번에 전부 바꾸기보다 부분 환수가 좋아요</h2><p>새 물은 수온을 맞추고 염소 제거제를 사용한 뒤, 기존 물과 조금씩 섞어 주세요.</p><p>먹이는 1~2분 안에 먹을 양만 급여해 수질 오염을 줄입니다.</p>]'),
        'https://images.unsplash.com/photo-1524704654690-b56c05c78a00?auto=format&fit=crop&w=900&q=80',
        'https://www.aquariumcoop.com/blogs/aquarium',
        73, 8
    );

    ADD_PET_GUIDE(
        35,
        '[펫도감 더미] 열대어 합사 전 확인해야 할 3가지',
        TO_CLOB(q'[<h2>수온, 성격, 크기를 먼저 비교하세요</h2><p>같은 수조에 넣기 전 적정 수온과 수질 조건이 비슷한지 확인해야 합니다.</p><p>새 개체는 가능하면 별도 공간에서 상태를 확인한 뒤 합사하는 것이 안전합니다.</p>]'),
        'https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=900&q=80',
        'https://www.aquariumcoop.com/blogs/aquarium',
        64, 10
    );

COMMIT;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20001, '활성 상태의 ADMIN 계정이 없어 펫도감 더미 데이터를 생성할 수 없습니다.');
WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/

/* 생성 확인용 쿼리 */
SELECT B.BOARD_ID,
       B.TITLE,
       A.ANIMAL_NAME,
       M.NICKNAME AS WRITER,
       B.HIT,
       B.CREATED_AT,
       BI.IMAGE_URL,
       BI.LINK_URL
FROM BOARD B
         JOIN APP_MEMBER M ON M.MEMBER_ID = B.MEMBER_ID
         LEFT JOIN ANIMAL_TYPE A ON A.ANIMAL_ID = B.ANIMAL_ID
         LEFT JOIN BOARD_IMAGE BI ON BI.BOARD_ID = B.BOARD_ID
WHERE B.BOARD_TYPE = 'INFO'
  AND B.TITLE LIKE '[펫도감 더미]%'
ORDER BY B.BOARD_ID DESC;
NOCYCLE;

commit;