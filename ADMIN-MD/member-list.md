# 회원 선택 기능 JavaScript 학습 정리

## 1. 현재 구현 상태

현재 HTML과 JavaScript 연결 기준으로 다음 기능이 구현되어 있다.

```text
회원 한 명씩 선택
현재 페이지 회원 전체 선택
일부 회원만 선택했을 때 전체 선택 체크박스 중간 표시
페이지 이동 후 기존 선택 상태 복원
선택한 전체 회원 수 표시
선택 초기화
현재 페이지 회원이 모두 선택되었을 때 안내 영역 표시
```

아직 구현하지 않은 기능:

```text
전체 회원 n명 선택 버튼의 실제 동작
선택 회원 일괄 차단
선택 회원 일괄 삭제 대기
선택 회원 일괄 복구
```

현재 HTML의 `selectEveryMember` 버튼은 나중에 전체 회원 선택 기능을 연결하기 위해 미리 만들어둔 상태다.

---

# 2. 현재 JavaScript 전체 코드

```javascript
document.addEventListener("DOMContentLoaded", function () {

    const STORAGE_KEY = "selectedMemberIds";

    const selectAllMembers =
        document.querySelector("#selectAllMembers");

    const memberCheckboxes =
        Array.from(
            document.querySelectorAll(".member-checkbox")
        );

    const selectedMemberCount =
        document.querySelector("#selectedMemberCount");

    const clearSelectedMembers =
        document.querySelector("#clearSelectedMembers");

    const allSelectionNotice =
        document.querySelector("#allSelectionNotice");

    const selectedMemberIds =
        loadSelectedMemberIds();

    restoreCheckboxState();
    updateSelectAllState();
    updateSelectedCount();
    updateAllSelectionNotice();

    if (selectAllMembers !== null) {

        selectAllMembers.addEventListener(
            "change",
            function () {

                memberCheckboxes.forEach(
                    function (checkbox) {

                        checkbox.checked =
                            selectAllMembers.checked;

                        if (checkbox.checked) {

                            selectedMemberIds.add(
                                checkbox.value
                            );

                        } else {

                            selectedMemberIds.delete(
                                checkbox.value
                            );
                        }
                    }
                );

                saveSelectedMemberIds();
                updateSelectAllState();
                updateSelectedCount();
                updateAllSelectionNotice();
            }
        );
    }

    memberCheckboxes.forEach(
        function (checkbox) {

            checkbox.addEventListener(
                "change",
                function () {

                    if (checkbox.checked) {

                        selectedMemberIds.add(
                            checkbox.value
                        );

                    } else {

                        selectedMemberIds.delete(
                            checkbox.value
                        );
                    }

                    saveSelectedMemberIds();
                    updateSelectAllState();
                    updateSelectedCount();
                    updateAllSelectionNotice();
                }
            );
        }
    );

    if (clearSelectedMembers !== null) {

        clearSelectedMembers.addEventListener(
            "click",
            function () {

                selectedMemberIds.clear();

                sessionStorage.removeItem(
                    STORAGE_KEY
                );

                memberCheckboxes.forEach(
                    function (checkbox) {

                        checkbox.checked = false;
                    }
                );

                updateSelectAllState();
                updateSelectedCount();
                updateAllSelectionNotice();
            }
        );
    }

    function loadSelectedMemberIds() {

        const savedValue =
            sessionStorage.getItem(STORAGE_KEY);

        if (savedValue === null) {
            return new Set();
        }

        try {

            const memberIds =
                JSON.parse(savedValue);

            return new Set(
                memberIds.map(String)
            );

        } catch (error) {

            sessionStorage.removeItem(
                STORAGE_KEY
            );

            return new Set();
        }
    }

    function saveSelectedMemberIds() {

        const memberIds =
            Array.from(selectedMemberIds);

        sessionStorage.setItem(
            STORAGE_KEY,
            JSON.stringify(memberIds)
        );
    }

    function restoreCheckboxState() {

        memberCheckboxes.forEach(
            function (checkbox) {

                checkbox.checked =
                    selectedMemberIds.has(
                        checkbox.value
                    );
            }
        );
    }

    function updateSelectAllState() {

        if (selectAllMembers === null) {
            return;
        }

        const checkedCount =
            memberCheckboxes.filter(
                function (checkbox) {

                    return checkbox.checked;
                }
            ).length;

        selectAllMembers.checked =
            memberCheckboxes.length > 0
            && checkedCount === memberCheckboxes.length;

        selectAllMembers.indeterminate =
            checkedCount > 0
            && checkedCount < memberCheckboxes.length;
    }

    function updateSelectedCount() {

        if (selectedMemberCount === null) {
            return;
        }

        selectedMemberCount.textContent =
            selectedMemberIds.size;
    }

    function updateAllSelectionNotice() {

        if (allSelectionNotice === null) {
            return;
        }

        const checkedCount =
            memberCheckboxes.filter(
                function (checkbox) {

                    return checkbox.checked;
                }
            ).length;

        const isCurrentPageAllSelected =
            memberCheckboxes.length > 0
            && checkedCount === memberCheckboxes.length;

        allSelectionNotice.hidden =
            !isCurrentPageAllSelected;
    }
});
```

---

# 3. HTML과 JavaScript 연결 관계

## 현재 페이지 전체 선택 체크박스

HTML:

```html
<input type="checkbox"
       id="selectAllMembers"
       aria-label="현재 페이지 회원 전체 선택">
```

JavaScript:

```javascript
const selectAllMembers =
    document.querySelector("#selectAllMembers");
```

---

## 회원별 체크박스

HTML:

```html
<input type="checkbox"
       class="member-checkbox"
       th:value="${member.memberId}">
```

JavaScript:

```javascript
const memberCheckboxes =
    Array.from(
        document.querySelectorAll(".member-checkbox")
    );
```

각 체크박스의 `value`에는 회원 번호가 들어간다.

예:

```html
<input type="checkbox"
       class="member-checkbox"
       value="25">
```

JavaScript에서 읽으면:

```javascript
checkbox.value
```

결과:

```text
"25"
```

숫자가 아니라 문자열이다.

---

## 선택 회원 수

HTML:

```html
<strong id="selectedMemberCount">
    0
</strong>
```

JavaScript:

```javascript
selectedMemberCount.textContent =
    selectedMemberIds.size;
```

회원 5명을 선택하면 화면의 숫자가 `5`로 바뀐다.

---

## 선택 초기화 버튼

HTML:

```html
<button type="button"
        id="clearSelectedMembers">
    선택 초기화
</button>
```

JavaScript:

```javascript
clearSelectedMembers.addEventListener(
    "click",
    function () {
        ...
    }
);
```

---

## 현재 페이지 전체 선택 안내

HTML:

```html
<div id="allSelectionNotice"
     hidden>
```

JavaScript:

```javascript
allSelectionNotice.hidden =
    !isCurrentPageAllSelected;
```

현재 페이지 회원이 모두 선택되면 `hidden`이 `false`가 되어 화면에 표시된다.

---

# 4. 전체 실행 흐름

## 페이지가 처음 열릴 때

```text
DOMContentLoaded 실행
→ HTML 요소 찾기
→ sessionStorage에서 선택 회원 번호 읽기
→ 현재 페이지 체크박스 상태 복원
→ 전체 선택 체크박스 상태 계산
→ 선택 회원 수 표시
→ 전체 선택 안내 영역 표시 여부 계산
```

---

## 회원 한 명을 선택할 때

```text
체크박스 change 이벤트 실행
→ checked 값 확인
→ 선택이면 Set에 회원 번호 추가
→ 해제이면 Set에서 회원 번호 삭제
→ sessionStorage 저장
→ 전체 선택 체크박스 갱신
→ 선택 회원 수 갱신
→ 안내 영역 갱신
```

---

## 현재 페이지 전체 선택

```text
헤더 체크박스 선택
→ 현재 페이지의 모든 체크박스를 반복
→ 모두 checked = true
→ 모든 회원 번호를 Set에 추가
→ sessionStorage 저장
→ 안내 영역 표시
```

---

## 페이지 이동

```text
1페이지에서 회원 2명 선택
→ sessionStorage에 회원 번호 저장
→ 2페이지로 이동
→ JavaScript 다시 실행
→ 저장된 회원 번호 읽기
→ 2페이지에서 회원 3명 추가 선택
→ 총 선택 인원은 5명
→ 다시 1페이지로 돌아가면 기존 2명 체크 복원
```

---

# 5. JavaScript 내장 객체와 메서드

## `document`

현재 HTML 문서를 나타내는 브라우저 내장 객체다.

```javascript
document.querySelector("#selectAllMembers");
```

---

## `addEventListener()`

HTML 요소에 이벤트를 등록한다.

```javascript
element.addEventListener(
    "click",
    function () {
    }
);
```

대표적인 이벤트:

```text
click
change
input
submit
keydown
DOMContentLoaded
```

---

## `querySelector()`

조건에 맞는 첫 번째 요소 하나를 찾는다.

```javascript
document.querySelector("#selectedMemberCount");
```

찾지 못하면:

```text
null
```

을 반환한다.

---

## `querySelectorAll()`

조건에 맞는 요소를 모두 찾는다.

```javascript
document.querySelectorAll(".member-checkbox");
```

반환값은 `NodeList`다.

---

## `Array.from()`

배열과 비슷한 값을 실제 배열로 변환한다.

```javascript
Array.from(
    document.querySelectorAll(".member-checkbox")
);
```

배열로 변환하면 다음 메서드를 사용할 수 있다.

```text
forEach()
filter()
map()
every()
```

---

# 6. 배열 메서드

## `forEach()`

배열의 모든 요소를 한 번씩 실행한다.

```javascript
memberCheckboxes.forEach(
    function (checkbox) {

        checkbox.checked = false;
    }
);
```

새로운 배열을 만들지는 않는다.

---

## `filter()`

조건을 만족하는 요소만 모아 새로운 배열을 만든다.

```javascript
const checkedCheckboxes =
    memberCheckboxes.filter(
        function (checkbox) {

            return checkbox.checked;
        }
    );
```

선택된 체크박스만 남는다.

---

## `map()`

배열의 각 값을 다른 값으로 바꿔 새로운 배열을 만든다.

```javascript
memberIds.map(String);
```

결과:

```text
[1, 2, 3]
→ ["1", "2", "3"]
```

---

# 7. `Set` 객체

`Set`은 같은 값을 중복해서 저장하지 않는 JavaScript 내장 객체다.

```javascript
const selectedMemberIds =
    new Set();
```

회원 선택 목록에 적합한 이유:

```text
같은 회원 번호가 중복 저장되지 않음
특정 회원 번호 존재 여부 확인 가능
특정 회원 번호 삭제 가능
전체 초기화 가능
```

---

## `Set.add()`

값을 추가한다.

```javascript
selectedMemberIds.add("10");
```

---

## `Set.delete()`

값을 삭제한다.

```javascript
selectedMemberIds.delete("10");
```

---

## `Set.has()`

값이 들어 있는지 확인한다.

```javascript
selectedMemberIds.has("10");
```

결과:

```text
있음  → true
없음  → false
```

---

## `Set.clear()`

모든 값을 삭제한다.

```javascript
selectedMemberIds.clear();
```

---

## `Set.size`

저장된 값의 개수를 반환한다.

```javascript
selectedMemberIds.size;
```

`size`는 함수가 아니라 속성이다.

```javascript
selectedMemberIds.size;   // 정상
selectedMemberIds.size(); // 오류
```

---

# 8. `sessionStorage`

현재 브라우저 탭에서 임시 데이터를 저장하는 내장 저장소다.

```text
새로고침해도 유지
페이지를 이동해도 유지
현재 탭을 닫으면 삭제
```

---

## `setItem()`

값을 저장한다.

```javascript
sessionStorage.setItem(
    "selectedMemberIds",
    '["1","2","3"]'
);
```

`sessionStorage`에는 문자열만 저장할 수 있다.

---

## `getItem()`

저장된 값을 읽는다.

```javascript
sessionStorage.getItem(
    "selectedMemberIds"
);
```

값이 없으면 `null`이 반환된다.

---

## `removeItem()`

저장된 값을 삭제한다.

```javascript
sessionStorage.removeItem(
    "selectedMemberIds"
);
```

---

# 9. JSON 메서드

## `JSON.stringify()`

배열이나 객체를 문자열로 변환한다.

```javascript
JSON.stringify(["1", "2", "3"]);
```

결과:

```text
["1","2","3"]
```

---

## `JSON.parse()`

JSON 문자열을 다시 배열이나 객체로 변환한다.

```javascript
JSON.parse('["1","2","3"]');
```

결과:

```javascript
["1", "2", "3"]
```

---

# 10. `try-catch`

오류가 발생할 수 있는 코드를 안전하게 처리한다.

```javascript
try {

    const memberIds =
        JSON.parse(savedValue);

} catch (error) {

    sessionStorage.removeItem(
        STORAGE_KEY
    );
}
```

잘못된 JSON 문자열이 저장되어 있어도 화면 전체가 멈추지 않도록 한다.

---

# 11. DOM 속성

## `checked`

체크박스가 선택되었는지 나타낸다.

```javascript
checkbox.checked;
```

결과:

```text
선택됨 → true
해제됨 → false
```

---

## `value`

HTML의 `value` 값을 가져온다.

```javascript
checkbox.value;
```

항상 문자열로 읽힌다.

---

## `indeterminate`

체크박스가 일부 선택 상태임을 표시한다.

```javascript
selectAllMembers.indeterminate = true;
```

전체 선택 체크박스에 체크 대신 중간 표시가 나타난다.

---

## `hidden`

요소를 숨기거나 표시한다.

```javascript
element.hidden = true;
```

숨김.

```javascript
element.hidden = false;
```

표시.

---

## `textContent`

요소 안의 글자를 변경한다.

```javascript
selectedMemberCount.textContent = 5;
```

---

# 12. 주요 연산자

## `===`

값과 자료형을 모두 비교한다.

```javascript
savedValue === null
```

---

## `&&`

두 조건이 모두 참인지 확인한다.

```javascript
checkedCount > 0
&& checkedCount < memberCheckboxes.length
```

---

## `!`

논리값을 반대로 바꾼다.

```javascript
!true
```

결과:

```text
false
```

---

# 13. 브라우저 저장값 확인

```text
F12
→ Application
→ Session Storage
→ http://localhost:8080
```

정상 저장 예:

```text
selectedMemberIds = ["3","8","15","27","31"]
```

---

# 14. 이후 구현 예정

현재 HTML에는 다음 버튼이 있다.

```html
<button id="selectEveryMember"
        type="button">
    전체회원 n명 선택
</button>
```

현재는 버튼 모양만 있고 동작은 아직 연결하지 않았다.

나중에는 다음 중 하나의 방식으로 구현할 수 있다.

```text
백엔드에서 전체 회원 ID 조회
현재 검색 결과 전체 선택 모드 저장
현재 필터 조건을 서버로 보내 일괄 처리
```

필터 기능이 추가된 뒤에는 단순 전체 회원보다:

```text
현재 검색 결과 전체 선택
```

방식으로 연결하는 것이 더 안전하다.