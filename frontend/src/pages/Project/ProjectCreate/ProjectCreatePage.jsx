import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import MainLayout from "../../../layouts/MainLayout.jsx";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import { createProject, projects, saveProject } from "../../../api/mock/projectData.js";

const emptyMember = { email: "", country: "대한민국", timezone: "GMT+9 서울" };

export default function ProjectCreatePage() {
  const navigate = useNavigate();
  const { projectId } = useParams();
  const existingProject = projects.find((project) => project.id === projectId);
  const isEditing = Boolean(existingProject);

  const [projectName, setProjectName] = useState(existingProject?.title ?? "");
  const [startDate, setStartDate] = useState(existingProject?.startDate ?? "");
  const [endDate, setEndDate] = useState(existingProject?.endDate ?? "");
  const [members, setMembers] = useState([emptyMember]);
  const [isNotionConnected, setIsNotionConnected] = useState(false);

  const updateMember = (index, key, value) => {
    setMembers((current) =>
      current.map((member, memberIndex) =>
        memberIndex === index ? { ...member, [key]: value } : member,
      ),
    );
  };

  const removeMember = (index) => {
    if (members.length === 1) return;
    setMembers((current) => current.filter((_, memberIndex) => memberIndex !== index));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    if (isEditing) {
      const formatDate = (date) => date.replaceAll("-", ".");
      saveProject({
        ...existingProject,
        title: projectName,
        startDate,
        endDate,
        period:
          startDate && endDate
            ? `${formatDate(startDate)} — ${formatDate(endDate)}`
            : existingProject.period,
        updated: "방금 전",
      });
      navigate(`/projects/${projectId}`);
      return;
    }

    const formatDate = (date) => date.replaceAll("-", ".");
    const invitedMembers = members
      .map((member) => member.email.trim().split("@")[0])
      .filter(Boolean);
    const newProject = createProject({
      id: `project-${Date.now()}`,
      title: projectName,
      description: "새로 만든 프로젝트입니다.",
      status: "진행중",
      members: invitedMembers.length > 0 ? ["김승언", ...invitedMembers] : ["김승언"],
      updated: "방금 전",
      tasks: 0,
      period:
        startDate && endDate
          ? `${formatDate(startDate)} — ${formatDate(endDate)}`
          : "기간 미정",
      startDate,
      endDate,
    });
    navigate(`/projects/${newProject.id}`);
  };

  return (
    <MainLayout>
      <div className="mx-auto max-w-3xl">
        <header className="mb-8">
          <p className="mb-2 text-xs font-semibold tracking-[0.16em] text-muted">
            PROJECT
          </p>
          <h1 className="text-2xl font-bold text-primary">
            {isEditing ? "프로젝트 수정" : "새 프로젝트 만들기"}
          </h1>
          <p className="mt-2 text-sm text-muted">
            {isEditing
              ? "프로젝트 정보와 팀원 구성을 수정할 수 있습니다."
              : "프로젝트를 만들고 팀원과 함께 협업을 시작하세요."}
          </p>
        </header>

        <form onSubmit={handleSubmit} className="space-y-7 rounded-xl border border-border bg-white p-6 sm:p-8">
          <section>
            <h2 className="mb-4 text-base font-semibold text-primary">프로젝트 정보</h2>
            <label className="block text-sm font-medium text-primary">
              프로젝트명
              <span className="ml-1 text-danger">*</span>
              <Input
                placeholder="예: 글로벌 협업 프로젝트"
                value={projectName}
                onChange={(event) => setProjectName(event.target.value)}
                required
                className="mt-2"
              />
            </label>

            <div className="mt-5">
              <p className="text-sm font-medium text-primary">프로젝트 기간</p>
              <div className="mt-2 grid gap-3 sm:grid-cols-2">
                <label className="text-xs text-muted">
                  시작일
                  <Input
                    type="date"
                    value={startDate}
                    onChange={(event) => setStartDate(event.target.value)}
                    className="mt-1"
                  />
                </label>
                <label className="text-xs text-muted">
                  종료일
                  <Input
                    type="date"
                    min={startDate || undefined}
                    value={endDate}
                    onChange={(event) => setEndDate(event.target.value)}
                    className="mt-1"
                  />
                </label>
              </div>
            </div>
          </section>

          <section className="border-t border-border pt-7">
            <div className="mb-4 flex items-center justify-between gap-3">
              <div>
                <h2 className="text-base font-semibold text-primary">팀원 초대</h2>
                <p className="mt-1 text-xs text-muted">필요한 팀원을 추가하고 국가와 시간대를 설정하세요.</p>
              </div>
              <Button
                variant="secondary"
                className="shrink-0 px-3 py-1.5 text-xs"
                onClick={() => setMembers((current) => [...current, emptyMember])}
              >
                + 팀원 추가
              </Button>
            </div>

            <div className="space-y-2">
              {members.map((member, index) => (
                <div key={index} className="grid gap-2 sm:grid-cols-[minmax(0,1fr)_130px_120px_auto]">
                  <Input
                    type="email"
                    placeholder="이메일 주소"
                    value={member.email}
                    onChange={(event) => updateMember(index, "email", event.target.value)}
                  />
                  <select
                    value={member.country}
                    onChange={(event) => {
                      const country = event.target.value;
                      updateMember(index, "country", country);
                      updateMember(
                        index,
                        "timezone",
                        country === "대한민국" ? "GMT+9 서울" : "GMT-8 샌프란시스코",
                      );
                    }}
                    className="w-full rounded-md border border-border bg-white px-3 py-2 text-sm text-primary focus:outline-none focus:ring-2 focus:ring-active/50"
                  >
                    <option>대한민국</option>
                    <option>미국</option>
                  </select>
                  <select
                    value={member.timezone}
                    onChange={(event) => updateMember(index, "timezone", event.target.value)}
                    className="w-full rounded-md border border-border bg-white px-3 py-2 text-sm text-primary focus:outline-none focus:ring-2 focus:ring-active/50"
                  >
                    <option>GMT+9 서울</option>
                    <option>GMT-8 샌프란시스코</option>
                  </select>
                  <Button
                    variant="secondary"
                    className="px-3 py-2 text-xs"
                    onClick={() => removeMember(index)}
                    disabled={members.length === 1}
                  >
                    삭제
                  </Button>
                </div>
              ))}
            </div>
          </section>

          <section className="border-t border-border pt-7">
            <h2 className="text-base font-semibold text-primary">Notion 워크스페이스 연동</h2>
            <div className="mt-4 flex items-center justify-between gap-4 rounded-lg border border-border p-4">
              <div>
                <p className="text-sm font-medium text-primary">Notion</p>
                <p className="mt-1 text-xs text-muted">
                  프로젝트 관련 문서를 함께 동기화합니다.
                </p>
              </div>
              <Button
                variant="secondary"
                onClick={() => setIsNotionConnected((current) => !current)}
              >
                {isNotionConnected ? "연동됨" : "연동하기"}
              </Button>
            </div>
          </section>

          <div className="flex justify-end gap-2 border-t border-border pt-6">
            <Button variant="secondary" onClick={() => navigate(-1)}>
              취소
            </Button>
            <Button type="submit">
              {isEditing ? "수정 완료" : "프로젝트 생성"}
            </Button>
          </div>
        </form>
      </div>
    </MainLayout>
  );
}
