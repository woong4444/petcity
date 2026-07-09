document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("hospitalFilterForm");
    if (!form) return;

    const ajaxUrl = form.dataset.ajaxUrl || "/hospital/list/ajax";
    const pageUrl = form.dataset.pageUrl || "/hospital/list";

    const keywordInput = form.querySelector("input[name='keyword']");
    const pageInput = document.getElementById("pageInput");

    const seoulAll = document.getElementById("seoulAll");
    const serviceAll = document.getElementById("serviceAll");

    const districtChecks = form.querySelectorAll("input[name='districts']");
    const serviceChecks = form.querySelectorAll("input[name='serviceIds']");
    const animalRadios = form.querySelectorAll("input[name='animalId']");

    const resetButton = document.getElementById("resetButton");

    let abortController = null;

    function makeParams(includeKeyword) {
        const params = new URLSearchParams();

        if (pageInput && pageInput.value) {
            params.append("page", pageInput.value);
        }

        const checkedAnimal = form.querySelector("input[name='animalId']:checked");
        if (checkedAnimal && checkedAnimal.value !== "") {
            params.append("animalId", checkedAnimal.value);
        }

        if (serviceAll && !serviceAll.checked) {
            const checkedServices = form.querySelectorAll("input[name='serviceIds']:checked");
            checkedServices.forEach(function (svc) {
                if (svc.value !== "") params.append("serviceIds", svc.value);
            });
        }

        if (seoulAll && !seoulAll.checked) {
            const checkedDistricts = form.querySelectorAll("input[name='districts']:checked");
            checkedDistricts.forEach(function (district) {
                if (district.value !== "") params.append("districts", district.value);
            });
        }

        if (includeKeyword && keywordInput && keywordInput.value.trim() !== "") {
            params.append("keyword", keywordInput.value.trim());
        }

        return params;
    }

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

    animalRadios.forEach(radio => {
        if (radio.checked) radio.dataset.wasChecked = "true";

        radio.addEventListener("click", function () {
            if (this.dataset.wasChecked === "true") {
                this.checked = false;
                this.dataset.wasChecked = "false";
            } else {
                animalRadios.forEach(r => r.dataset.wasChecked = "false");
                this.dataset.wasChecked = "true";
            }
        });
    });

    if (seoulAll) {
        seoulAll.addEventListener("change", function () {
            districtChecks.forEach(c => c.checked = this.checked);
            if (pageInput) pageInput.value = 1;
            loadHospitalList(true);
        });
    }

    districtChecks.forEach(function (check) {
        check.addEventListener("change", function () {
            if (seoulAll) {
                const total = districtChecks.length;
                const checkedCount = form.querySelectorAll("input[name='districts']:checked").length;
                seoulAll.checked = (total === checkedCount);
            }
            if (pageInput) pageInput.value = 1;
            loadHospitalList(true);
        });
    });

    if (serviceAll) {
        serviceAll.addEventListener("change", function () {
            serviceChecks.forEach(c => c.checked = this.checked);
            if (pageInput) pageInput.value = 1;
            loadHospitalList(true);
        });
    }

    serviceChecks.forEach(function (check) {
        check.addEventListener("change", function () {
            if (serviceAll) {
                const total = serviceChecks.length;
                const checkedCount = form.querySelectorAll("input[name='serviceIds']:checked").length;
                serviceAll.checked = (total === checkedCount);
            }
            if (pageInput) pageInput.value = 1;
            loadHospitalList(true);
        });
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        if (pageInput) pageInput.value = 1;
        loadHospitalList(true);
    });

    if (resetButton) {
        resetButton.addEventListener("click", function (event) {
            event.preventDefault();

            form.querySelectorAll("input[type='radio'], input[type='checkbox']").forEach(c => {
                c.checked = false;
                if (c.type === 'radio') c.dataset.wasChecked = "false";
            });

            if (keywordInput) keywordInput.value = "";
            if (pageInput) pageInput.value = 1;

            loadHospitalList(false);
        });
    }

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

    const toggleButtons = document.querySelectorAll(".btn-toggle-filter");
    toggleButtons.forEach(button => {
        button.addEventListener("click", function() {
            const contentWrap = this.parentElement.previousElementSibling;

            if(this.dataset.state === "open") {
                contentWrap.classList.add("is-minimized");
                this.dataset.state = "closed";
                this.textContent = "+ 펼치기";
            } else {
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