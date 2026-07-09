document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("hospitalFilterForm");
    if (!form) return;

    const ajaxUrl = form.dataset.ajaxUrl || "/hospital/list/ajax";
    const pageUrl = form.dataset.pageUrl || "/hospital/list";

    const keywordInput = form.querySelector("input[name='keyword']");
    const pageInput = document.getElementById("pageInput");

    // 전체 체크박스
    const seoulAll = document.getElementById("seoulAll");
    const serviceAll = document.getElementById("serviceAll");

    // 개별 체크박스 리스트
    const districtChecks = form.querySelectorAll("input[name='districts']");
    const serviceChecks = form.querySelectorAll("input[name='serviceIds']");
    const animalRadios = form.querySelectorAll("input[name='animalId']");

    const resetButton = document.getElementById("resetButton");

    let abortController = null;

    // 파라미터 수집 (아무것도 선택 안된 상태 = 전체 검색)
    function makeParams(includeKeyword) {
        const params = new URLSearchParams();

        if (pageInput && pageInput.value) {
            params.append("page", pageInput.value);
        }

        // 동물 분류
        const checkedAnimal = form.querySelector("input[name='animalId']:checked");
        if (checkedAnimal && checkedAnimal.value !== "") {
            params.append("animalId", checkedAnimal.value);
        }

        // 진료 과목
        const checkedServices = form.querySelectorAll("input[name='serviceIds']:checked");
        checkedServices.forEach(function (svc) {
            if (svc.value !== "") params.append("serviceIds", svc.value);
        });

        // 지역 분류
        const checkedDistricts = form.querySelectorAll("input[name='districts']:checked");
        checkedDistricts.forEach(function (district) {
            if (district.value !== "") params.append("districts", district.value);
        });

        if (includeKeyword && keywordInput && keywordInput.value.trim() !== "") {
            params.append("keyword", keywordInput.value.trim());
        }

        return params;
    }

    // Ajax 비동기 호출
    function loadHospitalList(includeKeyword) {
        const params = makeParams(includeKeyword);
        const queryString = params.toString();

        const requestUrl = queryString ? ajaxUrl + "?" + queryString : ajaxUrl;
        const browserUrl = queryString ? pageUrl + "?" + queryString : pageUrl;
        const resultArea = document.getElementById("hospitalResultArea");

        if (resultArea) resultArea.classList.add("is-loading");

        if (abortController) abortController.abort();
        abortController = new AbortController();

        fetch(requestUrl, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" },
            signal: abortController.signal
        })
            .then(response => {
                if (!response.ok) throw new Error("병원 목록을 불러오지 못했습니다.");
                return response.text();
            })
            .then(html => {
                const oldResultArea = document.getElementById("hospitalResultArea");
                if (oldResultArea) oldResultArea.outerHTML = html;
                window.history.pushState(null, "", browserUrl);
            })
            .catch(error => {
                if (error.name === "AbortError") return;
                console.error(error);
                alert("병원 목록을 갱신하는 중 오류가 발생했습니다.");
            })
            .finally(() => {
                const newResultArea = document.getElementById("hospitalResultArea");
                if (newResultArea) newResultArea.classList.remove("is-loading");
            });
    }

    /* =======================================
       🌟 [UI 로직] 동물 분류 라디오버튼 재클릭 해제
    ======================================= */
    animalRadios.forEach(radio => {
        // 초기에 체크되어 있으면 상태 기록
        if (radio.checked) radio.dataset.wasChecked = "true";

        radio.addEventListener("click", function () {
            if (this.dataset.wasChecked === "true") {
                // 이미 체크된 걸 다시 누르면 해제
                this.checked = false;
                this.dataset.wasChecked = "false";
            } else {
                // 새로 체크하면 다른 것들은 false로, 자신은 true로
                animalRadios.forEach(r => r.dataset.wasChecked = "false");
                this.dataset.wasChecked = "true";
            }
        });
    });

    /* =======================================
       🌟 [UI 로직] 지역 / 진료과목 실시간 검색 및 전체 버튼 연동
    ======================================= */

    // 1. 지역 '전체' 체크박스 클릭
    if (seoulAll) {
        seoulAll.addEventListener("change", function () {
            if (this.checked) {
                // 전체를 누르면 하위 개별 지역은 모두 해제
                districtChecks.forEach(c => c.checked = false);
            }
            if (pageInput) pageInput.value = 1;
            loadHospitalList(true); // 바로 검색
        });
    }

    // 2. 지역 '개별' 체크박스 클릭
    districtChecks.forEach(function (check) {
        check.addEventListener("change", function () {
            if (this.checked && seoulAll) {
                // 개별 항목 누르면 전체 해제
                seoulAll.checked = false;
            }
            if (pageInput) pageInput.value = 1;
            loadHospitalList(true); // 바로 검색
        });
    });

    // 3. 진료과목 '전체' 체크박스 클릭
    if (serviceAll) {
        serviceAll.addEventListener("change", function () {
            if (this.checked) {
                serviceChecks.forEach(c => c.checked = false);
            }
            if (pageInput) pageInput.value = 1;
            loadHospitalList(true); // 바로 검색
        });
    }

    // 4. 진료과목 '개별' 체크박스 클릭
    serviceChecks.forEach(function (check) {
        check.addEventListener("change", function () {
            if (this.checked && serviceAll) {
                serviceAll.checked = false;
            }
            if (pageInput) pageInput.value = 1;
            loadHospitalList(true); // 바로 검색
        });
    });


    /* =======================================
       🌟 검색 / 초기화 / 페이징
    ======================================= */

    // 검색 버튼 클릭
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        if (pageInput) pageInput.value = 1;
        loadHospitalList(true);
    });

    // 초기화 버튼 클릭 (모두 해제)
    if (resetButton) {
        resetButton.addEventListener("click", function (event) {
            event.preventDefault();

            // 모든 체크박스, 라디오 해제
            form.querySelectorAll("input[type='radio'], input[type='checkbox']").forEach(c => {
                c.checked = false;
                if (c.type === 'radio') c.dataset.wasChecked = "false";
            });

            if (keywordInput) keywordInput.value = "";
            if (pageInput) pageInput.value = 1;

            loadHospitalList(false); // 리셋된 상태(모두 빈 상태)로 전체 검색
        });
    }

    // 페이징 번호 클릭
    document.addEventListener("click", function (event) {
        const pageLink = event.target.closest(".page-link");
        if (pageLink) {
            const parentLi = pageLink.parentElement;
            if (!parentLi.classList.contains("disabled") && !parentLi.classList.contains("active")) {
                event.preventDefault();
                if (pageInput) {
                    pageInput.value = pageLink.dataset.page;
                    loadHospitalList(true);
                }
            }
        }
    });

    /* =======================================
       🌟 [UI 로직] 박스 접기 / 펼치기 토글
    ======================================= */
    const toggleButtons = document.querySelectorAll(".btn-toggle-filter");
    toggleButtons.forEach(button => {
        button.addEventListener("click", function() {
            const contentWrap = this.parentElement.previousElementSibling;

            if(this.dataset.state === "open") {
                // 접기
                contentWrap.classList.add("is-minimized");
                this.dataset.state = "closed";
                this.textContent = "+ 펼치기";
            } else {
                // 펼치기
                contentWrap.classList.remove("is-minimized");
                this.dataset.state = "open";
                this.textContent = "- 접기";
            }
        });
    });

    window.addEventListener("popstate", function () {
        window.location.reload();
    });
});