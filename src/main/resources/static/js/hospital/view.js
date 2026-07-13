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

    // 탭 스크롤
    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const targetElem = document.querySelector(this.dataset.target);
            if (targetElem) {
                targetElem.scrollIntoView({ behavior: 'smooth' });
                tabs.forEach(t => t.classList.remove('active'));
                this.classList.add('active');
            }
        });
    });

    // 🌟 리뷰 글자수 카운터
    const reviewContent = document.getElementById('reviewContent');
    const charCount = document.getElementById('charCount');
    if (reviewContent && charCount) {
        reviewContent.addEventListener('input', function() {
            charCount.textContent = this.value.length + ' / 1000자';
        });
    }

    // 🌟 리뷰 150자 더보기/접기 토글
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

    // 🌟 DB 연동 찜하기 기능 (+ 카운트 숫자 변경)
    const zzimBtn = document.querySelector('.btn-detail-zzim');
    if(zzimBtn) {
        zzimBtn.addEventListener('click', function() {
            const hospitalId = this.dataset.id;
            fetch('/hospital/api/zzim', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ hospitalId: hospitalId })
            })
                .then(res => res.json())
                .then(data => {
                    if(data.isSuccess) {
                        if(data.isZzim) this.classList.add('active');
                        else this.classList.remove('active');

                        const countSpan = this.querySelector('.count');
                        if(countSpan) countSpan.textContent = data.zzimCount;
                    } else {
                        alert("로그인 후 이용 부탁드립니다.");
                        location.href = '/member/login';
                    }
                })
                .catch(err => console.error("찜하기 통신 에러:", err));
        });
    }

    // 🌟 별점 렌더링
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

    // 🌟 DB 리뷰 등록
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