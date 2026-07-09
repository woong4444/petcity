document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("hospitalFilterForm");

    if (!form) {
        return;
    }

    const ajaxUrl = form.dataset.ajaxUrl || "/hospital/list/ajax";
    const pageUrl = form.dataset.pageUrl || "/hospital/list";

    const keywordInput = form.querySelector("input[name='keyword']");
    const pageInput = document.getElementById("pageInput"); // 추가됨
    const seoulAll = document.getElementById("seoulAll");
    const resetButton = document.getElementById("resetButton");

    let abortController = null;

    /*
        현재 선택된 동물, 진료과목, 지역, 검색어를 URLSearchParams로 만든다.

        includeKeyword가 true일 때만 검색어를 포함한다.
        그래서 검색어는 검색 버튼을 눌렀을 때만 적용된다.
    */
    function makeParams(includeKeyword) {
        const params = new URLSearchParams();

        // 페이지 번호 (추가됨)
        if (pageInput && pageInput.value) {
            params.append("page", pageInput.value);
        }

        // 동물 분류
        const checkedAnimal = form.querySelector("input[name='animalId']:checked");

        if (checkedAnimal && checkedAnimal.value !== "") {
            params.append("animalId", checkedAnimal.value);
        }

        // 진료 과목 분류 (추가됨)
        const checkedService = form.querySelector("input[name='serviceId']:checked");

        if (checkedService && checkedService.value !== "") {
            params.append("serviceId", checkedService.value);
        }

        // 지역 분류
        const checkedDistricts = form.querySelectorAll("input[name='districts']:checked");

        checkedDistricts.forEach(function (district) {
            if (district.value !== "") {
                params.append("districts", district.value);
            }
        });

        // 검색어
        if (includeKeyword && keywordInput && keywordInput.value.trim() !== "") {
            params.append("keyword", keywordInput.value.trim());
        }

        return params;
    }

    /*
        Ajax로 병원 목록 영역만 다시 가져온다.
    */
    function loadHospitalList(includeKeyword) {
        const params = makeParams(includeKeyword);
        const queryString = params.toString();

        const requestUrl = queryString
            ? ajaxUrl + "?" + queryString
            : ajaxUrl;

        const browserUrl = queryString
            ? pageUrl + "?" + queryString
            : pageUrl;

        const resultArea = document.getElementById("hospitalResultArea");

        if (resultArea) {
            resultArea.classList.add("is-loading");
        }

        // 이전 요청이 끝나기 전에 또 클릭하면 이전 요청 취소
        if (abortController) {
            abortController.abort();
        }

        abortController = new AbortController();

        fetch(requestUrl, {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            },
            signal: abortController.signal
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("병원 목록을 불러오지 못했습니다.");
                }

                return response.text();
            })
            .then(function (html) {
                const oldResultArea = document.getElementById("hospitalResultArea");

                if (oldResultArea) {
                    oldResultArea.outerHTML = html;
                }

                // 주소창도 현재 필터 상태에 맞게 바꿔줌
                window.history.pushState(null, "", browserUrl);
            })
            .catch(function (error) {
                if (error.name === "AbortError") {
                    return;
                }

                console.error(error);
                alert("병원 목록을 불러오는 중 오류가 발생했습니다.");
            })
            .finally(function () {
                const newResultArea = document.getElementById("hospitalResultArea");

                if (newResultArea) {
                    newResultArea.classList.remove("is-loading");
                }
            });
    }

    /*
        카테고리 선택 시에는 검색어를 자동 적용하지 않음.
        그래서 keyword input 값을 비우고, 페이지도 1로 초기화하여 병원 목록을 갱신한다.
    */
    function loadByCategory() {
        if (keywordInput) {
            keywordInput.value = "";
        }

        if (pageInput) {
            pageInput.value = 1; // 필터 변경 시 1페이지로
        }

        loadHospitalList(false);
    }

    /*
        동물 & 진료과목 라디오 버튼 변경 시 바로 Ajax 검색
    */
    const radioInputs = form.querySelectorAll("input[name='animalId'], input[name='serviceId']");

    radioInputs.forEach(function (radio) {
        radio.addEventListener("change", function () {
            loadByCategory();
        });
    });

    /*
        서울 전체 클릭 시:
        - 모든 지역 체크 해제
        - 전체 지역으로 Ajax 검색
    */
    if (seoulAll) {
        seoulAll.addEventListener("change", function () {
            const districtChecks = form.querySelectorAll("input[name='districts']");

            if (seoulAll.checked) {
                districtChecks.forEach(function (check) {
                    check.checked = false;
                });
            }

            loadByCategory();
        });
    }

    /*
        지역 체크박스 변경 시:
        - 서울 전체 체크 해제
        - 선택된 지역이 하나도 없으면 서울 전체 체크
        - 바로 Ajax 검색
    */
    const districtChecks = form.querySelectorAll("input[name='districts']");

    districtChecks.forEach(function (check) {
        check.addEventListener("change", function () {
            if (seoulAll) {
                seoulAll.checked = false;
            }

            const checkedDistrictCount =
                form.querySelectorAll("input[name='districts']:checked").length;

            if (checkedDistrictCount === 0 && seoulAll) {
                seoulAll.checked = true;
            }

            loadByCategory();
        });
    });

    /*
        검색 버튼 눌렀을 때만 검색어 포함해서 Ajax 검색
    */
    form.addEventListener("submit", function (event) {
        event.preventDefault();

        if (pageInput) {
            pageInput.value = 1; // 검색 시 1페이지로
        }

        loadHospitalList(true);
    });

    /*
        초기화도 Ajax로 처리
    */
    if (resetButton) {
        resetButton.addEventListener("click", function (event) {
            event.preventDefault();

            // 동물 전체 선택
            const allAnimalRadio = form.querySelector("input[name='animalId'][value='']");
            if (allAnimalRadio) {
                allAnimalRadio.checked = true;
            }

            // 진료 과목 전체 선택 (추가됨)
            const allServiceRadio = form.querySelector("input[name='serviceId'][value='']");
            if (allServiceRadio) {
                allServiceRadio.checked = true;
            }

            // 지역 전체 선택
            districtChecks.forEach(function (check) {
                check.checked = false;
            });

            if (seoulAll) {
                seoulAll.checked = true;
            }

            // 검색어 및 페이지 초기화
            if (keywordInput) {
                keywordInput.value = "";
            }
            if (pageInput) {
                pageInput.value = 1;
            }

            loadHospitalList(false);
        });
    }

    /*
        페이징 버튼 클릭 시 처리 (추가됨)
    */
    document.addEventListener("click", function (event) {
        const pageLink = event.target.closest(".page-link");

        if (pageLink) {
            const parentLi = pageLink.parentElement;

            // disabled(이전/다음 끝)이거나 active(현재 페이지)가 아닐 때만 이동
            if (!parentLi.classList.contains("disabled") && !parentLi.classList.contains("active")) {
                event.preventDefault();

                if (pageInput) {
                    pageInput.value = pageLink.dataset.page;
                    loadHospitalList(true); // 현재 검색 상태를 유지한 채 페이지 이동
                }
            }
        }
    });

    /*
        뒤로가기/앞으로가기 했을 때는 간단하게 새로고침
    */
    window.addEventListener("popstate", function () {
        window.location.reload();
    });
});