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

    // 🌟 1. 링크 공유하기 복사 기능
    const btnShare = document.getElementById('btnShare');
    if (btnShare) {
        btnShare.addEventListener('click', function() {
            // 현재 페이지의 URL 주소 복사
            navigator.clipboard.writeText(window.location.href)
                .then(() => {
                    alert('병원 링크가 복사되었습니다!\n원하는 곳에 붙여넣기(Ctrl+V) 하세요.');
                })
                .catch(err => {
                    console.error('복사 실패:', err);
                    alert('링크 복사에 실패했습니다.');
                });
        });
    }

    // 🌟 2. 스크롤 스파이 (Scroll Spy) 연동
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


    // 리뷰 글자수 카운터
    const reviewContent = document.getElementById('reviewContent');
    const charCount = document.getElementById('charCount');
    if (reviewContent && charCount) {
        reviewContent.addEventListener('input', function() {
            charCount.textContent = this.value.length + ' / 1000자';
        });
    }

    // 리뷰 150자 더보기/접기 토글
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

    // DB 연동 찜하기 기능 (+ 카운트 숫자 변경)
    document.querySelectorAll('.btn-zzim-toggle').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const hospitalId = this.dataset.id;
            fetch('/hospital/api/zzim', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ hospitalId: hospitalId })
            })
                .then(res => res.json())
                .then(data => {
                    if(data.isSuccess) {
                        // 리스트 하트, 뷰 상단 하트, 모바일 고정바 하트까지 모두 동시 변경!
                        document.querySelectorAll(`.btn-zzim-toggle[data-id="${hospitalId}"]`).forEach(el => {
                            if(data.isZzim) {
                                el.classList.add('text-rose-600', 'active');
                                el.classList.remove('text-slate-300', 'text-rose-400');
                            } else {
                                el.classList.remove('text-rose-600', 'active');
                                // 엘리먼트 위치에 따라 기본색 복구
                                if(el.classList.contains('top-right')) el.classList.add('text-slate-300');
                                else el.classList.add('text-rose-400');
                            }
                            const countSpan = el.querySelector('.count');
                            if(countSpan) countSpan.textContent = data.zzimCount;
                        });
                    } else {
                        alert("로그인 후 이용 부탁드립니다.");
                        location.href = '/member/login';
                    }
                })
                .catch(err => console.error("찜하기 통신 에러:", err));
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

            if(content === '') {
                alert('리뷰 내용을 입력해주세요!');
                return;
            }

            fetch('/hospital/api/review', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({
                    hospitalId: hospitalId,
                    rating: rating,
                    content: content
                })
            })
                .then(res => res.json())
                .then(data => {
                    if(data.isSuccess) {
                        alert('리뷰가 등록되었습니다!');
                        window.location.reload();
                    } else {
                        alert("로그인 후 이용 부탁드립니다.");
                        location.href = '/member/login';
                    }
                })
                .catch(err => console.error("리뷰 등록 에러:", err));
        });
    }
});