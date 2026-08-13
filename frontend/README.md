# Borderless Teamwork with AI — FE
## 실행 방법

```bash
cd frontend
npm install
npm run dev
```

```bash
npm run build
npm run lint
```

## 기술 스택

- React 18 + Vite 5
- Tailwind CSS v3
- react-router-dom 6 / axios / Node 20

## 폴더 구조

```
src/
  components/     # 공통 컴포넌트 (Badge, Button, Input, Avatar, Sidebar, Tab)
  layouts/        # MainLayout (Sidebar + 콘텐츠 영역)
  pages/          # 화면별 페이지
    Login/  Home/  Note/
    Project/
      ProjectList/  ProjectCreate/
      ProjectDetail/
        Home/  Briefing/  QnA/  Members/  Integration/
    Settings/
      SettingsLayout.jsx    # 설정 공통 레이아웃 (좌측 서브 메뉴)
      IntegrationManage/  Profile/
  api/mock/       # 목업 데이터
  assets/         # 이미지·폰트 (로고 확정 시 사용)
  App.jsx         # 라우트 정의
```

라우팅은 `App.jsx`에 직접 작성합니다. 페이지 추가 시 `<Route>` 한 줄을 함께 추가해주세요.

## 화면 경로

| 화면 | 경로 |
| --- | --- |
| 로그인 | `/login` |
| 홈 | `/home` |
| 프로젝트 목록 | `/projects` |
| 프로젝트 생성 | `/projects/new` |
| 프로젝트 상세 | `/projects/:projectId` |
| 쪽지 | `/notes` |
| 설정 · 연동 관리 | `/settings` |
| 설정 · 프로필 · 언어 | `/settings/profile` |

프로젝트 상세는 `Tab`으로 홈 / AI 브리핑 상세 / 컨텍스트 Q&A / 팀원 관리 / 연동 상태를 전환합니다.

## 컴포넌트 사용법

각 컴포넌트 파일 상단 주석에 사용 예시가 있습니다.

사이드바가 있는 화면은 `MainLayout`으로 감쌉니다. 로그인 화면은 감싸지 않습니다.

```jsx
<MainLayout>
  <h1>오늘의 브리핑</h1>
</MainLayout>
```

설정 화면은 `SettingsLayout`으로 감쌉니다. (내부에서 `MainLayout`을 사용)

## mock 데이터

| 파일 | 사용 화면 |
| --- | --- |
| `api/mock/projectData.js` | 홈, 프로젝트 목록·상세 |
| `api/mock/projectDetailData.js` | 프로젝트 상세 탭 |
| `api/mock/settingsData.js` | 설정 |

필드명은 `docs/openapi.yaml`을 따릅니다.

## 디자인 토큰

색상은 `tailwind.config.js`의 `theme.extend.colors`에 정의되어 있습니다. 화면 코드에서는 Tailwind 기본 색상 대신 이 토큰(`primary`, `muted`, `active` 등)을 사용해주세요.

## 브랜치 전략

`main`(배포) / `develop`(개발 통합) 구조입니다. 작업 브랜치는 `develop`에서 분기하고 `develop`을 base로 PR을 올립니다.

브랜치명: `feature/{이슈번호}-{설명}`
