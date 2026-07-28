/* ========================================================
   [search.js]
   맞춤 동물병원 검색 화면
   - 반려동물 선택
   - 진료 과목 전체 선택
   - 진료 서비스 전체 선택
   - 서울 지역 전체 선택
   - 그 외 지역 전체 선택
   - AJAX 검색 결과 갱신
======================================================== */

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("customSearchForm");

    if (!form) {
        return;
    }

    const subjectAllSearch = document.getElementById(
        "subjectAllSearch"
    );

    const serviceAllSearch = document.getElementById(
        "serviceAllSearch"
    );

    const seoulAllSearch = document.getElementById(
        "seoulAllSearch"
    );

    const otherAllSearch = document.getElementById(
        "otherAllSearch"
    );

    const subjectChecks = form.querySelectorAll(
        ".step2-subject-chk"
    );

    const serviceChecks = form.querySelectorAll(
        ".step2-service-chk"
    );

    const seoulDistrictChecks = form.querySelectorAll(
        ".step3-district-chk"
    );

    const otherDistrictChecks = form.querySelectorAll(
        ".hidden-other-district-search"
    );

    /*
     * 현재 하위 항목의 선택 상태에 맞춰
     * 전체 선택 체크박스 상태를 갱신합니다.
     */
    function updateAllCheckbox(
        allCheckbox,
        itemCheckboxes
    ) {
        if (!allCheckbox || itemCheckboxes.length === 0) {
            return;
        }

        const checkedCount = Array.from(
            itemCheckboxes
        ).filter(checkbox => checkbox.checked).length;

        allCheckbox.checked =
            checkedCount === itemCheckboxes.length;

        allCheckbox.indeterminate =
            checkedCount > 0
            && checkedCount < itemCheckboxes.length;
    }

    /*
     * 전체 선택을 누르면 해당 그룹의 모든 항목을
     * 같은 상태로 변경합니다.
     */
    function setAllCheckboxes(
        allCheckbox,
        itemCheckboxes
    ) {
        itemCheckboxes.forEach(checkbox => {
            checkbox.checked = allCheckbox.checked;
        });

        allCheckbox.indeterminate = false;
    }

    /*
     * 화면 최초 진입 시 현재 선택 상태를 기준으로
     * 전체 선택 체크박스를 초기화합니다.
     */
    updateAllCheckbox(
        subjectAllSearch,
        subjectChecks
    );

    updateAllCheckbox(
        serviceAllSearch,
        serviceChecks
    );

    updateAllCheckbox(
        seoulAllSearch,
        seoulDistrictChecks
    );

    updateAllCheckbox(
        otherAllSearch,
        otherDistrictChecks
    );

    /*
     * 폼 내부 체크박스 변경 처리
     */
    form.addEventListener("change", function (event) {
        const target = event.target;

        /*
         * 진료 과목 전체 선택
         */
        if (target === subjectAllSearch) {
            setAllCheckboxes(
                subjectAllSearch,
                subjectChecks
            );
        }

        /*
         * 개별 진료 과목 선택
         */
        if (target.classList.contains(
            "step2-subject-chk"
        )) {
            updateAllCheckbox(
                subjectAllSearch,
                subjectChecks
            );
        }

        /*
         * 진료 옵션 및 시설 전체 선택
         */
        if (target === serviceAllSearch) {
            setAllCheckboxes(
                serviceAllSearch,
                serviceChecks
            );
        }

        /*
         * 개별 진료 옵션 및 시설 선택
         */
        if (target.classList.contains(
            "step2-service-chk"
        )) {
            updateAllCheckbox(
                serviceAllSearch,
                serviceChecks
            );
        }

        /*
         * 서울 지역 전체 선택
         */
        if (target === seoulAllSearch) {
            setAllCheckboxes(
                seoulAllSearch,
                seoulDistrictChecks
            );
        }

        /*
         * 개별 서울 지역 선택
         */
        if (target.classList.contains(
            "step3-district-chk"
        )) {
            updateAllCheckbox(
                seoulAllSearch,
                seoulDistrictChecks
            );
        }

        /*
         * 그 외 지역 전체 선택
         */
        if (target === otherAllSearch) {
            setAllCheckboxes(
                otherAllSearch,
                otherDistrictChecks
            );
        }

        /*
         * 선택값이 변경될 때마다 검색 결과를 갱신합니다.
         */
        fetchDynamicResults();
    });

    /*
     * 반려동물 선택
     */
    const petSelectButtons = document.querySelectorAll(
        ".btn-select-pet"
    );

    petSelectButtons.forEach(button => {
        button.addEventListener(
            "click",
            function (event) {
                event.preventDefault();

                const animalId = this.getAttribute(
                    "data-animal-id"
                );

                const subAnimalId =
                    this.getAttribute(
                        "data-sub-animal-id"
                    ) || "";

                const animalInput =
                    document.getElementById(
                        "searchAnimalId"
                    );

                const subAnimalInput =
                    document.getElementById(
                        "searchSubAnimalId"
                    );

                if (animalInput) {
                    animalInput.value = animalId;
                }

                if (subAnimalInput) {
                    subAnimalInput.value = subAnimalId;
                }

                /*
                 * 기존 반려동물 선택 표시를 제거합니다.
                 */
                document.querySelectorAll(
                    ".pet-card-wrap"
                ).forEach(card => {
                    card.classList.remove(
                        "border-sky-500",
                        "bg-sky-50"
                    );

                    card.classList.add(
                        "border-slate-200",
                        "bg-white"
                    );
                });

                /*
                 * 현재 선택한 반려동물을 표시합니다.
                 */
                const selectedCard = this.closest(
                    ".pet-card-wrap"
                );

                if (selectedCard) {
                    selectedCard.classList.remove(
                        "border-slate-200",
                        "bg-white"
                    );

                    selectedCard.classList.add(
                        "border-sky-500",
                        "bg-sky-50"
                    );
                }

                /*
                 * 진료 항목 선택 영역을 엽니다.
                 */
                const step2Content =
                    document.getElementById(
                        "content-step2"
                    );

                const step2Icon =
                    document.getElementById(
                        "icon-step2"
                    );

                if (step2Content) {
                    step2Content.classList.remove(
                        "hidden"
                    );
                }

                if (step2Icon) {
                    step2Icon.textContent = "−";
                }

                fetchDynamicResults();
            }
        );
    });
});


/* ========================================================
   AJAX 검색 결과 갱신
======================================================== */

let hospitalSearchAbortController = null;

function fetchDynamicResults() {
    const form = document.getElementById(
        "customSearchForm"
    );

    if (!form) {
        return;
    }

    const resultArea = document.getElementById(
        "ajaxDynamicResult"
    );

    if (!resultArea) {
        return;
    }

    const formData = new FormData(form);
    const searchParams = new URLSearchParams(formData);

    resultArea.classList.remove("hidden");

    resultArea.innerHTML = `
        <div class="text-center py-10
                    text-slate-500 font-bold">
            맞춤 병원을 불러오는 중입니다...
        </div>
    `;

    /*
     * 이전 검색 요청이 끝나기 전에 새로운 검색이 발생하면
     * 이전 요청을 취소합니다.
     */
    if (hospitalSearchAbortController) {
        hospitalSearchAbortController.abort();
    }

    hospitalSearchAbortController =
        new AbortController();

    fetch(
        "/hospital/list/ajax?"
        + searchParams.toString(),
        {
            method: "GET",
            headers: {
                "X-Requested-With":
                    "XMLHttpRequest"
            },
            signal:
            hospitalSearchAbortController.signal
        }
    )
        .then(response => {
            if (!response.ok) {
                throw new Error(
                    "병원 검색 요청에 실패했습니다."
                );
            }

            return response.text();
        })
        .then(html => {
            resultArea.innerHTML = html;

            resultArea.classList.add(
                "bg-white",
                "p-6",
                "rounded-xl",
                "border",
                "border-slate-200",
                "shadow-sm"
            );
        })
        .catch(error => {
            if (error.name === "AbortError") {
                return;
            }

            console.error(error);

            resultArea.innerHTML = `
                <div class="text-center py-10
                            text-red-500 font-bold">
                    오류가 발생했습니다.
                    다시 시도해주세요.
                </div>
            `;
        });
}