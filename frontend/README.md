# Borderless Teamwork with AI — FE
## 실행 방법

```bash
cd frontend
npm install
cp .env.example .env    # 최초 1회
npm run dev
```

```bash
npm run build
npm run lint
```

## 환경 변수

`.env`는 git에 올라가지 않으므로 각자 만들어야 합니다. `.env.example`을 복사해서 사용하세요.

| 변수 | 설명 |
| --- | --- |
| `VITE_API_BASE_URL` | 백엔드 API 주소 |

기본값은 배포된 백엔드(`https://morrow.p-e.kr`)입니다. 로컬에서 백엔드를 직접 실행할 때만 `http://localhost:8080`으로 바꿔주세요.

Vite는 빌드 시점에 이 값을 코드에 포함시키므로, 값을 바꾼 뒤에는 개발 서버를 다시 실행해야 반영됩니다. 배포 환경에서는 Cloudflare Pages의 환경 변수 설정을 사용합니다.

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
  api/            # API 호출 함수 (client.js, member.js, project.js ...)
    mock/         # 목업 데이터
  contexts/       # UserContext (로그인 사용자 정보)
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

## API 호출

`src/api/`에 도메인별 호출 함수가 있습니다. 공통 설정(baseURL, 쿠키, CSRF)은 `client.js`에 모여 있으니 화면에서는 함수만 가져다 쓰면 됩니다.

```jsx
import { useEffect, useState } from "react";
import { getProjects } from "../api/project.js";

const [projects, setProjects] = useState([]);

useEffect(() => {
  getProjects().then((res) => setProjects(res.data));
}, []);
```

로그인은 OAuth 리다이렉트가 필요해서 axios가 아닌 페이지 이동을 사용합니다.

```jsx
import { goToGoogleLogin } from "../api/member.js";

<Button onClick={goToGoogleLogin}>Google로 계속하기</Button>
```

로그인 사용자 정보는 `UserContext`에서 꺼내 씁니다.

```jsx
import { useUser } from "../contexts/UserContext.jsx";

const { user, loading, refresh } = useUser();
```

로그인 직후에는 `refresh()`를 호출해 사용자 정보를 다시 받아오세요.

## 디자인 토큰

색상은 `tailwind.config.js`의 `theme.extend.colors`에 정의되어 있습니다. 화면 코드에서는 Tailwind 기본 색상 대신 이 토큰(`primary`, `muted`, `active` 등)을 사용해주세요.

## 브랜치 전략

`main`(배포) / `develop`(개발 통합) 구조입니다. 작업 브랜치는 `develop`에서 분기하고 `develop`을 base로 PR을 올립니다.

브랜치명: `feature/{이슈번호}-{설명}`
