const STORAGE_KEY = "morrow-projects";
const PREVIOUS_STORAGE_KEY = "remi-projects";

const defaultProjects = [
  {
    id: "teamline",
    title: "Borderless Teamwork MVP",
    description: "AI와 함께 만드는 글로벌 협업의 새로운 기준",
    status: "진행중",
    members: ["김승언", "안혜선", "김지원"],
    updated: "방금 전",
    tasks: 2,
    period: "2026.08.08 — 2026.08.30",
    startDate: "2026-08-08",
    endDate: "2026-08-30",
  },
  {
    id: "global-marketing",
    title: "글로벌 마케팅 캠페인",
    description: "서울·샌프란시스코 팀의 브랜드 론칭",
    status: "진행전",
    members: ["김승언", "Sarah Lee", "라연헌"],
    updated: "2시간 전",
    tasks: 3,
    period: "2026.08.12 — 2026.09.05",
    startDate: "2026-08-12",
    endDate: "2026-09-05",
  },
  {
    id: "research-sprint",
    title: "사용자 리서치 스프린트",
    description: "사용자 인터뷰와 인사이트 정리",
    status: "완료",
    members: ["김지원", "Alex Kim"],
    updated: "어제",
    tasks: 0,
    period: "2026.07.22 — 2026.08.02",
    startDate: "2026-07-22",
    endDate: "2026-08-02",
  },
];

function readProjects() {
  try {
    const stored = window.localStorage.getItem(STORAGE_KEY)
      ?? window.localStorage.getItem(PREVIOUS_STORAGE_KEY);
    return stored ? JSON.parse(stored) : defaultProjects;
  } catch {
    return defaultProjects;
  }
}

export let projects = readProjects();

export function saveProject(updatedProject) {
  projects = projects.map((project) =>
    project.id === updatedProject.id ? updatedProject : project,
  );
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(projects));
  return updatedProject;
}

export function createProject(newProject) {
  projects = [newProject, ...projects];
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(projects));
  return newProject;
}

export const statusVariant = {
  진행중: "active",
  진행전: "danger",
  완료: "success",
};
