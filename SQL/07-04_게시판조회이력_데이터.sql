/* 선행 실행: 01-00, 07-00, 07-01 */

/*
  BOARD_ID를 고정하지 않는 공유용 조회수 더미 데이터입니다.
  BOARD_TYPE + TITLE + 게시글 작성자 LOGIN_ID로 실제 BOARD_ID를 조회합니다.
*/

MERGE INTO BOARD_VIEW_HISTORY h
USING (
    WITH VIEW_DATA (
        BOARD_TYPE, BOARD_TITLE, BOARD_WRITER_LOGIN, VIEWER_LOGIN, VIEWED_AT
    ) AS (
        SELECT 'NOTICE', 'ㄹㅇ', 'admin', 'admin', TIMESTAMP '2026-07-16 10:15:10.55574' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 자유게시판 더미 글 59', 'test085', 'admin', TIMESTAMP '2026-07-16 12:15:02.913495' FROM DUAL
        UNION ALL SELECT 'FREE', 'ㄹㄴㅇㅁㄹ', 'test024', 'test002', TIMESTAMP '2026-07-28 16:56:44.545988' FROM DUAL
        UNION ALL SELECT 'QNA', 'ㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹ', 'test112', 'test002', TIMESTAMP '2026-07-28 16:57:14.743534' FROM DUAL
        UNION ALL SELECT 'QNA', 'ㄹㅇㄴㄹㅇㅁㄴㄹ', 'test020', 'test002', TIMESTAMP '2026-07-28 16:57:22.711214' FROM DUAL
        UNION ALL SELECT 'NOTICE', 'PetCity 공지사항 테스트', 'admin', 'test002', TIMESTAMP '2026-07-28 16:58:04.022979' FROM DUAL
        UNION ALL SELECT 'QNA', 'saddfasdf', 'test050', 'test162', TIMESTAMP '2026-07-30 13:16:52.216096' FROM DUAL
        UNION ALL SELECT 'FREE', 'zzzz', 'test049', 'test162', TIMESTAMP '2026-07-30 13:17:03.231501' FROM DUAL
        UNION ALL SELECT 'NOTICE', 'fsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsda', 'admin', 'test162', TIMESTAMP '2026-07-30 13:17:08.598877' FROM DUAL
        UNION ALL SELECT 'NOTICE', '123213', 'admin', 'test002', TIMESTAMP '2026-07-30 13:17:31.609531' FROM DUAL
        UNION ALL SELECT 'FREE', 'zzzz', 'test049', 'test002', TIMESTAMP '2026-07-30 13:41:02.200852' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 자유게시판 더미 글 56', 'test082', 'test002', TIMESTAMP '2026-07-30 17:31:00.648674' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 강아지 산책 이야기 57', 'test083', 'admin', TIMESTAMP '2026-07-21 12:40:20.500443' FROM DUAL
        UNION ALL SELECT 'QNA', 'ㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹ', 'test112', 'admin', TIMESTAMP '2026-07-20 17:40:27.871862' FROM DUAL
        UNION ALL SELECT 'QNA', '개개ㅐㅐ', 'test118', 'test133', TIMESTAMP '2026-07-22 11:56:28.22288' FROM DUAL
        UNION ALL SELECT 'QNA', '개개ㅐㅐ', 'test118', 'test162', TIMESTAMP '2026-07-22 11:56:44.734001' FROM DUAL
        UNION ALL SELECT 'QNA', 'ㄹㅇㄴㄹㅇㅁㄴㄹ', 'test020', 'test162', TIMESTAMP '2026-07-22 11:58:21.065983' FROM DUAL
        UNION ALL SELECT 'FREE', '응가', 'test006', 'test162', TIMESTAMP '2026-07-27 09:04:31.682925' FROM DUAL
        UNION ALL SELECT 'NOTICE', '공지', 'admin', 'test162', TIMESTAMP '2026-07-27 09:05:08.547326' FROM DUAL
        UNION ALL SELECT 'QNA', 'ㄹ안머라ㅣㄴ머라ㅣㄴㅁ얼', 'test007', 'test162', TIMESTAMP '2026-07-27 16:45:35.890439' FROM DUAL
        UNION ALL SELECT 'FREE', 'ㄹㄴㅇㅁㄹ', 'test024', 'test162', TIMESTAMP '2026-07-29 11:03:36.386398' FROM DUAL
        UNION ALL SELECT 'INFO', '[펫도감 더미] 열대어 합사 전 확인해야 할 3가지', 'admin', 'test002', TIMESTAMP '2026-07-29 11:57:18.788192' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 강아지 산책 이야기 63', 'test089', 'test135', TIMESTAMP '2026-07-21 17:04:22.795907' FROM DUAL
        UNION ALL SELECT 'NOTICE', 'fd', 'admin', 'test135', TIMESTAMP '2026-07-21 17:04:56.788242' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 자유게시판 더미 글 58', 'test084', 'admin', TIMESTAMP '2026-07-15 10:05:20.323246' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 강아지 산책 이야기 63', 'test089', 'admin', TIMESTAMP '2026-07-15 10:05:39.231937' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 자유게시판 더미 글 62', 'test088', 'admin', TIMESTAMP '2026-07-15 11:23:56.57615' FROM DUAL
        UNION ALL SELECT 'NOTICE', 'PetCity 공지사항 테스트', 'admin', 'admin', TIMESTAMP '2026-07-15 11:25:03.055961' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 자유게시판 더미 글 59', 'test085', 'test162', TIMESTAMP '2026-07-27 10:12:33.022342' FROM DUAL
        UNION ALL SELECT 'NOTICE', '공지', 'admin', 'admin', TIMESTAMP '2026-07-21 09:48:34.840584' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 자유게시판 더미 글 62', 'test088', 'test162', TIMESTAMP '2026-07-24 16:07:02.011323' FROM DUAL
        UNION ALL SELECT 'NOTICE', 'fd', 'admin', 'test163', TIMESTAMP '2026-07-24 15:11:36.510421' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 강아지 산책 이야기 63', 'test089', 'test162', TIMESTAMP '2026-07-24 16:16:06.230065' FROM DUAL
        UNION ALL SELECT 'NOTICE', 'fd', 'admin', 'test162', TIMESTAMP '2026-07-24 16:07:07.023795' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 자유게시판 더미 글 56', 'test082', 'test162', TIMESTAMP '2026-07-24 16:16:58.5034' FROM DUAL
        UNION ALL SELECT 'QNA', 'fdsfdsf', 'test004', 'test162', TIMESTAMP '2026-07-24 16:25:54.688154' FROM DUAL
        UNION ALL SELECT 'NOTICE', 'ㄹㅇ', 'admin', 'test162', TIMESTAMP '2026-07-24 16:36:49.207355' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 강아지 산책 이야기 63', 'test089', 'test002', TIMESTAMP '2026-07-24 16:37:10.800247' FROM DUAL
        UNION ALL SELECT 'QNA', 'fdsfdsf', 'test004', 'test002', TIMESTAMP '2026-07-24 16:38:08.094717' FROM DUAL
        UNION ALL SELECT 'QNA', 'fdfdf', 'test021', 'test162', TIMESTAMP '2026-07-24 16:40:19.764285' FROM DUAL
        UNION ALL SELECT 'QNA', 'fdsfdsf', 'test004', 'test163', TIMESTAMP '2026-07-24 17:01:33.064129' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 강아지 산책 이야기 57', 'test083', 'test002', TIMESTAMP '2026-07-24 18:39:05.275991' FROM DUAL
        UNION ALL SELECT 'NOTICE', 'fsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsdafsaffsda', 'admin', 'test002', TIMESTAMP '2026-07-28 10:48:12.185061' FROM DUAL
        UNION ALL SELECT 'QNA', '강아지가 밥을 잘 안 먹어요', 'test004', 'test002', TIMESTAMP '2026-07-28 16:57:19.231711' FROM DUAL
        UNION ALL SELECT 'QNA', 'fsdafsadf', 'test009', 'test002', TIMESTAMP '2026-07-28 10:50:58.392194' FROM DUAL
        UNION ALL SELECT 'FREE', '[페이징테스트] 자유게시판 더미 글 59', 'test085', 'test002', TIMESTAMP '2026-07-28 16:56:33.19065' FROM DUAL
        UNION ALL SELECT 'NOTICE', '공지', 'admin', 'test002', TIMESTAMP '2026-07-28 16:58:00.913548' FROM DUAL
    )
    SELECT b.BOARD_ID, viewer.MEMBER_ID, d.VIEWED_AT
    FROM VIEW_DATA d
    JOIN BOARD b
        ON b.BOARD_TYPE = d.BOARD_TYPE
       AND b.TITLE = d.BOARD_TITLE
    JOIN APP_MEMBER board_writer
        ON board_writer.MEMBER_ID = b.MEMBER_ID
       AND board_writer.LOGIN_ID = d.BOARD_WRITER_LOGIN
    JOIN APP_MEMBER viewer
        ON viewer.LOGIN_ID = d.VIEWER_LOGIN
) s
ON (h.BOARD_ID = s.BOARD_ID AND h.MEMBER_ID = s.MEMBER_ID)
WHEN MATCHED THEN
    UPDATE SET h.VIEWED_AT = s.VIEWED_AT
WHEN NOT MATCHED THEN
    INSERT (BOARD_ID, MEMBER_ID, VIEWED_AT)
    VALUES (s.BOARD_ID, s.MEMBER_ID, s.VIEWED_AT);

COMMIT;
SELECT COUNT(*) AS BOARD_VIEW_HISTORY_COUNT FROM BOARD_VIEW_HISTORY;
