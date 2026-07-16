document.addEventListener("DOMContentLoaded", function () {

    // 카카오맵 렌더링
    if (typeof kakao !== 'undefined' && kakao.maps.services && lat && lng) {
        var tmX = (lat < lng) ? lat : lng;
        var tmY = (lat > lng) ? lat : lng;
        var geocoder = new kakao.maps.services.Geocoder();

        geocoder.transCoord(tmX, tmY, function(result, status) {
            if (status === kakao.maps.services.Status.OK) {
                var wgs84Y = result[0].y;
                var wgs84X = result[0].x;
                var mapContainer = document.getElementById('kakaoMap');
                var mapOption = { center: new kakao.maps.LatLng(wgs84Y, wgs84X), level: 3 };
                var map = new kakao.maps.Map(mapContainer, mapOption);

                var marker = new kakao.maps.Marker({ position: new kakao.maps.LatLng(wgs84Y, wgs84X) });
                marker.setMap(map);

                var iwContent = `<div style="padding:5px; text-align:center; font-weight:bold; font-size:13px; color:#111827;">${hName}</div>`;
                var infowindow = new kakao.maps.InfoWindow({ content : iwContent });
                infowindow.open(map, marker);
                document.getElementById('kakaoMapLink').href = `https://map.kakao.com/link/map/${hName},${wgs84Y},${wgs84X}`;
            }
        }, { input_coord: kakao.maps.services.Coords.TM, output_coord: kakao.maps.services.Coords.WGS84 });
    }

    // 링크 공유하기 복사 기능
    const btnShare = document.getElementById('btnShare');
    if (btnShare) {
        btnShare.addEventListener('click', function() {
            navigator.clipboard.writeText(window.location.href).then(() => {
                alert('병원 링크가 복사되었습니다!\n원하는 곳에 붙여넣기(Ctrl+V) 하세요.');
            }).catch(err => {
                console.error('복사 실패:', err);
                alert('링크 복사에 실패했습니다.');
            });
        });
    }

    // 스크롤 스파이 연동
    const tabs = document.querySelectorAll('.tab-btn');
    const sections = document.querySelectorAll('.view-section');
    const stickyHeaderOffset = 120;

    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const targetElem = document.querySelector(this.dataset.target);
            if (targetElem) {
                const offsetPosition = targetElem.offsetTop - stickyHeaderOffset;
                window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
            }
        });
    });

    window.addEventListener('scroll', () => {
        let current = '';
        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            if (window.scrollY >= sectionTop - stickyHeaderOffset - 10) {
                current = section.getAttribute('id');
            }
        });

        tabs.forEach(tab => {
            tab.classList.remove('bg-sky-500', 'text-white', 'shadow-md', 'active');
            tab.classList.add('text-slate-500', 'hover:bg-slate-50');
            if (tab.dataset.target === `#${current}`) {
                tab.classList.remove('text-slate-500', 'hover:bg-slate-50');
                tab.classList.add('bg-sky-500', 'text-white', 'shadow-md', 'active');
            }
        });
    });

    // 보호자 리뷰 글자수 카운터
    const reviewContent = document.getElementById('reviewContent');
    const charCount = document.getElementById('charCount');
    if (reviewContent && charCount) {
        reviewContent.addEventListener('input', function() {
            charCount.textContent = this.value.length + ' / 1000자';
        });
    }

    // 리뷰 & 답글 150자 더보기/접기 토글 공통 로직
    document.querySelectorAll('.btn-more-text').forEach(btn => {
        btn.addEventListener('click', function() {
            const contentDiv = this.previousElementSibling;
            if (this.textContent === '더보기') {
                contentDiv.textContent = contentDiv.dataset.full;
                this.textContent = '접기';
            } else {
                contentDiv.textContent = contentDiv.dataset.short;
                this.textContent = '더보기';
            }
        });
    });

    // 찜하기 기능
    document.querySelectorAll('.btn-zzim-toggle').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const hospitalId = this.dataset.id;
            fetch('/hospital/api/zzim', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ hospitalId: hospitalId })
            })
                .then(res => {
                    if (!res.ok) throw new Error("HTTP 상태 코드: " + res.status);
                    return res.json();
                })
                .then(data => {
                    if(data.isSuccess === true) {
                        document.querySelectorAll(`.btn-zzim-toggle[data-id="${hospitalId}"]`).forEach(el => {
                            if(data.isZzim) {
                                el.classList.add('text-rose-600', 'active');
                                el.classList.remove('text-slate-300', 'text-rose-400');
                            } else {
                                el.classList.remove('text-rose-600', 'active');
                                if(el.classList.contains('top-right')) el.classList.add('text-slate-300');
                                else el.classList.add('text-rose-400');
                            }
                            const countSpan = el.querySelector('.count');
                            if(countSpan) countSpan.textContent = data.zzimCount;
                        });
                    } else if(data.isSuccess === false) {
                        alert("세션이 만료되었습니다. 다시 로그인 해주세요.");
                        location.href = '/member/login';
                    }
                })
                .catch(err => {
                    console.error("찜하기 통신 에러:", err);
                    alert("서버 처리 중 오류가 발생했습니다.");
                });
        });
    });

    // 별점 렌더링
    const stars = document.querySelectorAll('#starRatingSelect span');
    const ratingInput = document.getElementById('reviewRating');
    if(stars.length > 0) {
        stars.forEach(star => {
            star.style.cursor = 'pointer';
            star.style.color = '#cbd5e1';
            star.addEventListener('click', function() {
                const val = parseInt(this.dataset.value);
                ratingInput.value = val;
                stars.forEach((s, index) => {
                    if(index < val) s.style.color = '#fbbf24';
                    else s.style.color = '#cbd5e1';
                });
            });
        });
        stars.forEach(s => s.style.color = '#fbbf24');
    }

    // DB 리뷰 등록
    const btnSubmitReview = document.getElementById('btnSubmitReview');
    if(btnSubmitReview) {
        btnSubmitReview.addEventListener('click', function() {
            const content = reviewContent.value.trim();
            const rating = ratingInput.value;
            const hospitalId = document.getElementById('reviewHospitalId').value;

            if(content === '') { alert('리뷰 내용을 입력해주세요!'); return; }

            fetch('/hospital/api/review', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ hospitalId: hospitalId, rating: rating, content: content })
            })
                .then(res => {
                    if (!res.ok) {
                        throw new Error("서버 응답 오류 (HTTP " + res.status + ")");
                    }
                    return res.json();
                })
                .then(data => {
                    if(data.isSuccess === true) {
                        alert('리뷰가 등록되었습니다!');
                        window.location.reload();
                    } else if(data.isSuccess === false) {
                        alert("세션이 만료되었습니다. 안전을 위해 다시 로그인 해주세요.");
                        location.href = '/member/login';
                    } else {
                        alert("응답 형식이 올바르지 않습니다.");
                    }
                })
                .catch(err => {
                    console.error("리뷰 등록 에러:", err);
                    alert("💥 앗! 리뷰 저장 중 DB 오류가 발생했습니다.\nIDE(스프링 부트) 콘솔창의 에러 로그를 확인해주세요!");
                });
        });
    }

    // ==============================================
    // 병원장/관리자 전용 리뷰 답글 1000자 제한 및 수정 로직
    // ==============================================

    // 답글 글자수 카운터
    document.querySelectorAll('.reply-textarea').forEach(textarea => {
        textarea.addEventListener('input', function() {
            const countSpan = this.closest('.reply-form-box').querySelector('.reply-char-count');
            if(countSpan) {
                countSpan.textContent = this.value.length + ' / 1000자';
            }
        });
    });

    // 신규 답글 폼 열기/닫기
    document.querySelectorAll('.btn-toggle-reply').forEach(btn => {
        btn.addEventListener('click', function() {
            const formBox = this.nextElementSibling;
            formBox.classList.toggle('hidden');
        });
    });

    // 기존 답글 수정 폼 열기
    document.querySelectorAll('.btn-edit-reply').forEach(btn => {
        btn.addEventListener('click', function() {
            const parentDiv = this.closest('.bg-slate-50');
            const displayArea = parentDiv.querySelector('.reply-display-area');
            const formBox = parentDiv.querySelector('.reply-form-box');

            // 기존 텍스트 숨기고 폼 띄우기
            displayArea.classList.add('hidden');
            formBox.classList.remove('hidden');

            // 폼 띄울 때 글자수 즉시 계산
            const textarea = formBox.querySelector('.reply-textarea');
            textarea.dispatchEvent(new Event('input'));
        });
    });

    // 기존 답글 수정 취소
    document.querySelectorAll('.btn-cancel-reply').forEach(btn => {
        btn.addEventListener('click', function() {
            const parentDiv = this.closest('.bg-slate-50');
            const displayArea = parentDiv.querySelector('.reply-display-area');
            const formBox = parentDiv.querySelector('.reply-form-box');

            // 폼 숨기고 기존 텍스트 다시 보이기
            displayArea.classList.remove('hidden');
            formBox.classList.add('hidden');
        });
    });

    // 답글 등록 및 수정 통신
    document.querySelectorAll('.btn-submit-reply').forEach(btn => {
        btn.addEventListener('click', function() {
            const reviewId = this.dataset.reviewId;
            const textarea = this.closest('.reply-form-box').querySelector('textarea');
            const replyContent = textarea.value.trim();

            if (replyContent === '') {
                alert('답글 내용을 입력해주세요.');
                textarea.focus();
                return;
            }

            if (!confirm('답글을 저장하시겠습니까?')) return;

            fetch('/hospital/api/review/reply', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ reviewId: reviewId, replyContent: replyContent })
            })
                .then(res => {
                    if (!res.ok) throw new Error("HTTP " + res.status);
                    return res.json();
                })
                .then(data => {
                    if(data.isSuccess === true) {
                        alert('답글이 저장되었습니다!');
                        window.location.reload();
                    } else {
                        alert(data.message || '세션이 만료되었거나 권한이 없습니다.');
                    }
                })
                .catch(err => {
                    console.error("답글 등록 통신 에러:", err);
                    alert("💥 앗! 답글 저장 중 오류가 발생했습니다.\nIDE(스프링 부트) 콘솔창을 확인해주세요.");
                });
        });
    });

});