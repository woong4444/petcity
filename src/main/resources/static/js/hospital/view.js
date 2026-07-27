document.addEventListener("DOMContentLoaded", function () {

    // 🌟 1. 최근 본 병원 로컬스토리지 저장 로직
    try {
        if (typeof hId !== 'undefined' && typeof hName !== 'undefined' && hId > 0) {
            let recentHospitals = JSON.parse(localStorage.getItem('petcity_recent') || '[]');

            const currentHospital = {
                id: hId,
                name: hName,
                img: (typeof hImg !== 'undefined' && hImg !== '') ? hImg : null
            };

            // 중복 시 기존 기록 삭제
            recentHospitals = recentHospitals.filter(h => String(h.id) !== String(hId));

            // 맨 앞에 새로 추가
            recentHospitals.unshift(currentHospital);

            // 3개까지만 유지
            recentHospitals = recentHospitals.slice(0, 3);

            localStorage.setItem('petcity_recent', JSON.stringify(recentHospitals));

            // 🌟 2. 저장 직후 퀵메뉴 즉시 새로고침! (head.js에 있는 함수 호출)
            if (typeof renderGlobalRecentHospitals === 'function') {
                renderGlobalRecentHospitals();
            }
        }
    } catch (e) {
        console.error("최근 본 병원 저장 중 오류 발생:", e);
    }

    // ==============================================
    // 카카오맵 렌더링 로직 등 (이전 코드 유지)
    // ==============================================
    if (typeof kakao !== 'undefined' && kakao.maps && kakao.maps.services) {
        kakao.maps.load(function () {
            const mapContainer = document.getElementById('kakaoMap');
            if (!mapContainer) return;

            let map;
            let targetPosition;

            function renderMap(finalLat, finalLng) {
                targetPosition = new kakao.maps.LatLng(finalLat, finalLng);
                const mapOption = {center: targetPosition, level: 3};
                map = new kakao.maps.Map(mapContainer, mapOption);

                const marker = new kakao.maps.Marker({position: targetPosition});
                marker.setMap(map);

                const iwContent = `<div style="padding:5px; text-align:center; font-weight:bold; font-size:13px; color:#111827; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${typeof hName !== 'undefined' ? hName : '병원'}</div>`;
                const infowindow = new kakao.maps.InfoWindow({content: iwContent});
                infowindow.open(map, marker);

                const mapLink = document.getElementById('kakaoMapLink');
                if (mapLink && typeof hName !== 'undefined') {
                    mapLink.href = `https://map.kakao.com/link/map/${hName},${finalLat},${finalLng}`;
                    mapLink.style.display = 'block';
                }

                setTimeout(function () {
                    map.relayout();
                    map.setCenter(targetPosition);
                }, 100);
            }

            if (lat > 1000 || lng > 1000) {
                const tmX = (lat < lng) ? lat : lng;
                const tmY = (lat > lng) ? lat : lng;
                const geocoder = new kakao.maps.services.Geocoder();

                geocoder.transCoord(tmX, tmY, function (result, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        renderMap(result[0].y, result[0].x);
                    } else {
                        renderMap(37.566826, 126.978656);
                    }
                }, {input_coord: kakao.maps.services.Coords.TM, output_coord: kakao.maps.services.Coords.WGS84});
            } else if (lat > 30 && lng > 120) {
                renderMap(lat, lng);
            } else if (typeof hAddress !== 'undefined' && hAddress.trim() !== '') {
                const geocoder = new kakao.maps.services.Geocoder();
                geocoder.addressSearch(hAddress, function (result, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        renderMap(result[0].y, result[0].x);
                    } else {
                        renderMap(37.566826, 126.978656);
                        const mapLink = document.getElementById('kakaoMapLink');
                        if (mapLink) mapLink.style.display = 'none';
                    }
                });
            } else {
                renderMap(37.566826, 126.978656);
            }

            const tabs = document.querySelectorAll('.tab-btn');
            tabs.forEach(tab => {
                tab.addEventListener('click', function () {
                    if (this.dataset.target === '#section-info') {
                        setTimeout(() => {
                            if (map && targetPosition) {
                                map.relayout();
                                map.setCenter(targetPosition);
                            }
                        }, 100);
                    }
                });
            });

            window.addEventListener('resize', function () {
                if (map && targetPosition) {
                    map.relayout();
                    map.setCenter(targetPosition);
                }
            });
        });
    }

    function getFetchHeaders() {
        const headers = {'Content-Type': 'application/x-www-form-urlencoded'};
        if (typeof csrfHeader !== 'undefined' && csrfHeader && typeof csrfToken !== 'undefined' && csrfToken) {
            headers[csrfHeader] = csrfToken;
        }
        return headers;
    }

    const btnShare = document.getElementById('btnShare');
    if (btnShare) {
        btnShare.addEventListener('click', function () {
            navigator.clipboard.writeText(window.location.href).then(() => {
                alert('병원 링크가 복사되었습니다!\n원하는 곳에 붙여넣기(Ctrl+V) 하세요.');
            }).catch(err => {
                alert('링크 복사에 실패했습니다.');
            });
        });
    }

    const tabs = document.querySelectorAll('.tab-btn');
    const sections = document.querySelectorAll('.view-section');
    const stickyHeaderOffset = 120;

    tabs.forEach(tab => {
        tab.addEventListener('click', function () {
            const targetElem = document.querySelector(this.dataset.target);
            if (targetElem) {
                const offsetPosition = targetElem.offsetTop - stickyHeaderOffset;
                window.scrollTo({top: offsetPosition, behavior: 'smooth'});
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
            tab.classList.remove('bg-slate-900', 'text-white', 'shadow-md', 'active');
            tab.classList.add('text-slate-500', 'hover:bg-slate-100');
            if (tab.dataset.target === `#${current}`) {
                tab.classList.remove('text-slate-500', 'hover:bg-slate-100');
                tab.classList.add('bg-slate-900', 'text-white', 'shadow-md', 'active');
            }
        });
    });

    const reviewContent = document.getElementById('reviewContent');
    const charCount = document.getElementById('charCount');
    if (reviewContent && charCount) {
        reviewContent.addEventListener('input', function () {
            charCount.textContent = this.value.length + ' / 1000자';
        });
    }

    document.querySelectorAll('.btn-more-text').forEach(btn => {
        btn.addEventListener('click', function () {
            const contentDiv = this.previousElementSibling;
            if (this.textContent === '+ 더보기') {
                contentDiv.textContent = contentDiv.dataset.full;
                this.textContent = '접기';
            } else {
                contentDiv.textContent = contentDiv.dataset.short;
                this.textContent = '+ 더보기';
            }
        });
    });

    document.querySelectorAll('.btn-zzim-toggle').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            const hospitalId = this.dataset.id;
            fetch('/hospital/api/zzim', {
                method: 'POST',
                headers: getFetchHeaders(),
                body: new URLSearchParams({hospitalId: hospitalId})
            })
                .then(res => res.json())
                .then(data => {
                    if (data.isSuccess === true) {
                        document.querySelectorAll(`.btn-zzim-toggle[data-id="${hospitalId}"]`).forEach(el => {
                            const emptyHeart = el.querySelector('.icon-heart-empty');
                            const filledHeart = el.querySelector('.icon-heart-filled');
                            if (data.isZzim) {
                                el.classList.add('text-rose-500', 'active');
                                el.classList.remove('text-slate-300', 'text-rose-400');
                                if (emptyHeart) { emptyHeart.classList.remove('block'); emptyHeart.classList.add('hidden'); }
                                if (filledHeart) { filledHeart.classList.remove('hidden'); filledHeart.classList.add('block'); }
                            } else {
                                el.classList.remove('text-rose-500', 'active');
                                el.classList.add('text-slate-300');
                                if (emptyHeart) { emptyHeart.classList.remove('hidden'); emptyHeart.classList.add('block'); }
                                if (filledHeart) { filledHeart.classList.remove('block'); filledHeart.classList.add('hidden'); }
                            }
                            const countSpan = el.querySelector('.count');
                            if (countSpan) countSpan.textContent = data.zzimCount;
                        });
                    } else {
                        alert("세션이 만료되었습니다. 다시 로그인 해주세요.");
                        location.href = '/member/login';
                    }
                })
                .catch(err => console.error("찜하기 통신 에러:", err));
        });
    });

    const stars = document.querySelectorAll('#starRatingSelect span');
    const ratingInput = document.getElementById('reviewRating');
    if (stars.length > 0) {
        stars.forEach(star => {
            star.style.cursor = 'pointer';
            star.style.color = '#cbd5e1';
            star.addEventListener('click', function () {
                const val = parseInt(this.dataset.value);
                ratingInput.value = val;
                stars.forEach((s, index) => {
                    if (index < val) s.style.color = '#fbbf24';
                    else s.style.color = '#cbd5e1';
                });
            });
        });
        stars.forEach(s => s.style.color = '#fbbf24');
    }

    const btnSubmitReview = document.getElementById('btnSubmitReview');
    if (btnSubmitReview) {
        btnSubmitReview.addEventListener('click', function () {
            const content = reviewContent.value.trim();
            const rating = ratingInput.value;
            const hospitalId = document.getElementById('reviewHospitalId').value;
            const memberId = document.getElementById('loginMemberId') ? document.getElementById('loginMemberId').value : '';

            if (content === '') {
                alert('리뷰 내용을 입력해주세요!');
                return;
            }

            fetch('/hospital/api/review', {
                method: 'POST',
                headers: getFetchHeaders(),
                body: new URLSearchParams({
                    hospitalId: hospitalId, memberId: memberId, rating: rating, content: content
                })
            })
                .then(async res => {
                    if (!res.ok) {
                        const errText = await res.text();
                        throw new Error(`서버 응답 오류 (${res.status}): ${errText}`);
                    }
                    return res.json();
                })
                .then(data => {
                    if (data.isSuccess === true) {
                        alert('리뷰가 등록되었습니다!');
                        window.location.reload();
                    } else {
                        alert(data.message || "리뷰 등록에 실패했습니다.");
                    }
                })
                .catch(err => alert("💥 리뷰 등록 실패:\n" + err.message));
        });
    }
});

function toggleReviewEdit(reviewId) {
    const contentBox = document.getElementById('reviewContentBox-' + reviewId);
    const editBox = document.getElementById('reviewEditBox-' + reviewId);

    if (editBox.classList.contains('hidden')) {
        contentBox.classList.add('hidden');
        editBox.classList.remove('hidden');
    } else {
        contentBox.classList.remove('hidden');
        editBox.classList.add('hidden');
    }
}