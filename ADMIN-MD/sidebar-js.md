# 관리자 사이드바 JavaScript 정리

## 전체 코드

```javascript
document.addEventListener("DOMContentLoaded", function () {

    const adminLayout = document.querySelector(".admin-layout");
    const sidebarToggle = document.querySelector(".sidebar-toggle");

    if (adminLayout === null || sidebarToggle === null) {
        return;
    }

    const savedSidebarState =
        localStorage.getItem("petcityAdminSidebar");

    if (savedSidebarState === "collapsed") {
        adminLayout.classList.add("is-sidebar-collapsed");
        updateButtonState(true);
    }

    sidebarToggle.addEventListener("click", function () {

        adminLayout.classList.toggle("is-sidebar-collapsed");

        const isCollapsed =
            adminLayout.classList.contains("is-sidebar-collapsed");

        if (isCollapsed) {
            localStorage.setItem(
                "petcityAdminSidebar",
                "collapsed"
            );
        } else {
            localStorage.setItem(
                "petcityAdminSidebar",
                "expanded"
            );
        }

        updateButtonState(isCollapsed);
    });

    function updateButtonState(isCollapsed) {

        sidebarToggle.setAttribute(
            "aria-expanded",
            String(!isCollapsed)
        );

        sidebarToggle.setAttribute(
            "aria-label",
            isCollapsed
                ? "사이드바 펼치기"
                : "사이드바 접기"
        );
    }
});
```

## 역할

관리자 사이드바를 접고 펼친다. 현재 상태를 `localStorage`에 저장하므로 새로고침하거나 다른 관리자 페이지로 이동해도 마지막 상태가 유지된다.

```text
버튼 클릭
→ 접힘 클래스 추가 또는 제거
→ 현재 상태 확인
→ localStorage 저장
→ 버튼 접근성 정보 변경
```

## 주요 코드 설명

### `DOMContentLoaded`

```javascript
document.addEventListener("DOMContentLoaded", function () {
```

HTML 구조가 모두 만들어진 다음 JavaScript를 실행한다. 요소가 만들어지기 전에 `querySelector()`를 실행해서 `null`이 되는 문제를 막는다.

### `querySelector()`

```javascript
const adminLayout =
    document.querySelector(".admin-layout");

const sidebarToggle =
    document.querySelector(".sidebar-toggle");
```

HTML에서 해당 클래스를 가진 첫 번째 요소를 찾는다. `.`은 클래스 선택자다.

### 요소 존재 검사

```javascript
if (adminLayout === null || sidebarToggle === null) {
    return;
}
```

둘 중 하나라도 없으면 이후 코드를 실행하지 않는다. `||`는 OR 조건이다.

### `localStorage.getItem()`

```javascript
const savedSidebarState =
    localStorage.getItem("petcityAdminSidebar");
```

브라우저에 저장된 값을 읽는다.

```text
키: petcityAdminSidebar
값: collapsed 또는 expanded
```

### 이전 상태 복원

```javascript
if (savedSidebarState === "collapsed") {
    adminLayout.classList.add("is-sidebar-collapsed");
    updateButtonState(true);
}
```

이전에 접혀 있었다면 접힘 클래스를 다시 추가한다.

### 클릭 이벤트

```javascript
sidebarToggle.addEventListener("click", function () {
```

버튼을 클릭할 때 내부 코드를 실행한다.

### `classList.toggle()`

```javascript
adminLayout.classList.toggle("is-sidebar-collapsed");
```

클래스가 없으면 추가하고, 있으면 제거한다.

```text
펼침 → 접힘
접힘 → 펼침
```

### `classList.contains()`

```javascript
const isCollapsed =
    adminLayout.classList.contains("is-sidebar-collapsed");
```

현재 접힘 클래스가 있는지 확인한다.

```text
true  → 접힌 상태
false → 펼친 상태
```

### `localStorage.setItem()`

```javascript
localStorage.setItem(
    "petcityAdminSidebar",
    "collapsed"
);
```

현재 상태를 브라우저에 저장한다.

### `updateButtonState()`

```javascript
function updateButtonState(isCollapsed) {
```

사이드바 상태에 맞춰 버튼의 `aria-expanded`, `aria-label`을 변경한다.

### `setAttribute()`

```javascript
sidebarToggle.setAttribute(
    "aria-expanded",
    String(!isCollapsed)
);
```

HTML 속성을 추가하거나 수정한다.

### 삼항 연산자

```javascript
isCollapsed
    ? "사이드바 펼치기"
    : "사이드바 접기"
```

기본 형태:

```javascript
조건 ? 참일 때 값 : 거짓일 때 값
```

## 전체 실행 순서

```text
1. HTML 로딩 완료
2. admin-layout과 sidebar-toggle 찾기
3. 저장된 사이드바 상태 읽기
4. 이전에 접혀 있었다면 접힘 상태 복원
5. 버튼 클릭 감지
6. 접힘 클래스 토글
7. 현재 상태 확인
8. localStorage 저장
9. 버튼 접근성 속성 변경
```

## 메서드 요약

```javascript
document.querySelector(".class");
```

요소 하나를 찾는다.

```javascript
element.addEventListener("click", function () {});
```

이벤트를 등록한다.

```javascript
element.classList.add("active");
```

클래스를 추가한다.

```javascript
element.classList.remove("active");
```

클래스를 제거한다.

```javascript
element.classList.toggle("active");
```

클래스를 추가하거나 제거한다.

```javascript
element.classList.contains("active");
```

클래스가 있는지 확인한다.

```javascript
localStorage.setItem("key", "value");
```

값을 저장한다.

```javascript
localStorage.getItem("key");
```

저장된 값을 읽는다.

```javascript
element.setAttribute("속성명", "값");
```

HTML 속성을 설정한다.

## 브라우저에서 확인

```text
F12
→ Application
→ Local Storage
→ http://localhost:8080
```

아래 값이 보이면 정상이다.

```text
petcityAdminSidebar = collapsed
```

또는:

```text
petcityAdminSidebar = expanded
```