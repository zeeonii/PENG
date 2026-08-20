/**
 * 프로젝트 및 참여자 관련 API입니다.
 *
 * 사용 예시:
 * import { getProjects, getProject, getProjectMembers } from "../api/project.js";
 *
 * const { data } = await getProjects();
 * const { data } = await getProject(projectId);
 * await inviteProjectMember(projectId, { email, role });
 */

import client from "./client.js";

/** 내가 참여한 프로젝트 목록 */
export const getProjects = () => client.get("/projects");

/**
 * 프로젝트 생성
 * @param {{ name: string }} data
 */
export const createProject = (data) => client.post("/projects", data);

/** 프로젝트 상세 */
export const getProject = (projectId) => client.get(`/projects/${projectId}`);

/**
 * 프로젝트 상태 변경
 * @param {"PENDING"|"IN_PROGRESS"|"COMPLETED"} status
 */
export const updateProjectStatus = (projectId, status) =>
  client.patch(`/projects/${projectId}/status`, { status });

/** 프로젝트 이름 수정. 참여자면 누구나 가능. */
export const updateProjectName = (projectId, name) =>
  client.patch(`/projects/${projectId}/name`, { name });

/** 프로젝트 삭제. 생성자만 가능하며 되돌릴 수 없습니다. */
export const deleteProject = (projectId) => client.delete(`/projects/${projectId}`);

/** 프로젝트 참여자 목록 */
export const getProjectMembers = (projectId) =>
  client.get(`/projects/${projectId}/members`);

/**
 * 참여자 초대
 * @param {{ email: string, role?: string }} data
 */
export const inviteProjectMember = (projectId, data) =>
  client.post(`/projects/${projectId}/members`, data);

/** 참여자 내보내기 */
export const removeProjectMember = (projectId, memberId) =>
  client.delete(`/projects/${projectId}/members/${memberId}`);
