# teamline Frontend

Borderless Teamwork with AI — 프론트엔드

## 실행 방법

```bash
cd frontend
npm install
npm run dev
```

빌드:

```bash
npm run build
```

## 기술 스택

- React + Vite
- Tailwind CSS v3
- react-router-dom

## 폴더 구조

```
src/
  components/     # 공통 컴포넌트 (Badge, Button, Input, Avatar, Sidebar, Tab)
  layouts/        # 여러 페이지가 공유하는 화면 틀 (MainLayout: Sidebar + 콘텐츠 영역)
  pages/          # 화면별 페이지 (현재 빈 구조, 담당자별로 채워나갈 예정)
    Login/
    Home/
    Project/
      ProjectList/
      ProjectCreate/
      ProjectDetail/
        Home/
        Briefing/
        QnA/
        Members/
        Integration/
    Note/
    Settings/
      IntegrationManage/
      Profile/
  api/
    mock/         # 목업 데이터
  routes/         # 라우트 정의 (현재 빈 구조)
  hooks/          # 커스텀 훅 (현재 빈 구조)
  assets/         # 이미지·폰트 등 정적 리소스 (현재 빈 구조)
  App.jsx
  main.jsx
  index.css
```

## 공통 컴포넌트 사용법

각 컴포넌트 파일(`src/components/*.jsx`) 상단 주석에 사용 예시가 있습니다. 사용 전에 해당 파일을 먼저 확인해주세요.

- `Badge` — 상태/역할 표시 라벨 (`variant`: default/success/active/danger)
- `Button` — `variant`: primary/secondary
- `Input` — placeholder/value/onChange/type props
- `Avatar` — `src` 있으면 이미지, 없으면 `name` 이니셜 또는 회색 원
- `Sidebar` — 좌측 고정 메뉴, `useLocation` 기반으로 현재 경로 하이라이트
- `Tab` — `tabs` 배열(`{ label, content }`) props로 전달, 클릭 시 해당 content 렌더링

## 레이아웃 사용법

사이드바가 있는 화면(홈, 프로젝트, 쪽지, 설정)은 `MainLayout`으로 감싸서 사용합니다.

```jsx
import MainLayout from "../../layouts/MainLayout.jsx";

export default function HomePage() {
  return (
    <MainLayout>
      <h1>오늘의 브리핑</h1>
    </MainLayout>
  );
}
```

사이드바가 없는 로그인 화면은 `MainLayout` 없이 작성합니다.

## 참고

디자인이 아직 미확정이라 색상은 `tailwind.config.js`의 임시 토큰(무채색 기준)을 사용합니다. 각 컴포넌트 파일 상단에 "디자인 확정 시 교체 필요" 주석이 있는 부분(로고, AI 아바타 등)은 디자인 확정 후 교체가 필요합니다.

디자인 값을 바꿀 때는 `tailwind.config.js`의 `theme.extend.colors` 값만 수정하면 전체 컴포넌트에 자동 반영됩니다.
