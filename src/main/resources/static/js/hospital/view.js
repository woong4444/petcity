document.addEventListener("DOMContentLoaded", function () {

    // 1. 카카오맵 렌더링 (DB의 TM좌표를 지도가 이해할 수 있게 WGS84로 변환)
    if (typeof kakao !== 'undefined' && kakao.maps.services && lat && lng) {

        // DB에 저장된 X,Y 꼬임 방어코드: 값이 더 큰 쪽(40만대)이 Y(위도매핑)이고, 작은 쪽(20만대)이 X(경도매핑)입니다.
        var tmX = (lat < lng) ? lat : lng;
        var tmY = (lat > lng) ? lat : lng;

        var geocoder = new kakao.maps.services.Geocoder();

        geocoder.transCoord(tmX, tmY, function(result, status) {
            if (status === kakao.maps.services.Status.OK) {
                var wgs84Y = result[0].y; // 변환된 실제 위도
                var wgs84X = result[0].x; // 변환된 실제 경도

                var mapContainer = document.getElementById('kakaoMap');
                var mapOption = {
                    center: new kakao.maps.LatLng(wgs84Y, wgs84X),
                    level: 3
                };

                var map = new kakao.maps.Map(mapContainer, mapOption);

                var markerPosition  = new kakao.maps.LatLng(wgs84Y, wgs84X);
                var marker = new kakao.maps.Marker({
                    position: markerPosition
                });
                marker.setMap(map);

                var iwContent = `<div style="padding:5px; text-align:center; font-weight:bold; font-size:13px; color:#111827;">${hName}</div>`;
                var infowindow = new kakao.maps.InfoWindow({
                    content : iwContent
                });
                infowindow.open(map, marker);

                // 길찾기 버튼 링크 동적 업데이트
                document.getElementById('kakaoMapLink').href = `https://map.kakao.com/link/map/${hName},${wgs84Y},${wgs84X}`;
            }
        }, {
            input_coord: kakao.maps.services.Coords.TM,
            output_coord: kakao.maps.services.Coords.WGS84
        });
    }

    // 2. 탭 메뉴 스크롤
    const tabs = document.querySelectorAll('.tab-btn');

    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const targetId = this.dataset.target;
            const targetElem = document.querySelector(targetId);

            if (targetElem) {
                targetElem.scrollIntoView({ behavior: 'smooth' });
                tabs.forEach(t => t.classList.remove('active'));
                this.classList.add('active');
            }
        });
    });
});