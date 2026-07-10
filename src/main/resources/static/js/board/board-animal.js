document.addEventListener("DOMContentLoaded", function () {
    const parentAnimalSelect = document.getElementById("parentAnimalSelect");
    const childAnimalSelect = document.getElementById("childAnimalSelect");

    if (!parentAnimalSelect || !childAnimalSelect) {
        return;
    }

    parentAnimalSelect.addEventListener("change", function () {
        const parentId = this.value;

        childAnimalSelect.innerHTML = '<option value="">품종·종류 선택</option>';

        if (!parentId) {
            childAnimalSelect.innerHTML = '<option value="">먼저 반려동물을 선택하세요</option>';
            return;
        }

        fetch("/board/animal/children?parentId=" + parentId)
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("품종·종류 조회 실패");
                }

                return response.json();
            })
            .then(function (data) {
                childAnimalSelect.innerHTML = '<option value="">품종·종류 선택</option>';

                data.forEach(function (animal) {
                    const option = document.createElement("option");

                    option.value = animal.animalId;
                    option.textContent = animal.animalName;

                    childAnimalSelect.appendChild(option);
                });
            })
            .catch(function (error) {
                console.error(error);
                alert("품종·종류를 불러오지 못했습니다.");
            });
    });
});