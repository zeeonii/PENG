import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/Login/LoginPage.jsx";
import AuthCallbackPage from "./pages/Login/AuthCallbackPage.jsx";
import HomePage from "./pages/Home/HomePage.jsx";
import ProjectListPage from "./pages/Project/ProjectList/ProjectListPage.jsx";
import ProjectCreatePage from "./pages/Project/ProjectCreate/ProjectCreatePage.jsx";
import ProjectHomePage from "./pages/Project/ProjectDetail/Home/ProjectHomePage.jsx";
import NotePage from "./pages/Note/NotePage.jsx";
import IntegrationManagePage from "./pages/Settings/IntegrationManage/IntegrationManagePage.jsx";
import ProfilePage from "./pages/Settings/Profile/ProfilePage.jsx";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/auth/callback" element={<AuthCallbackPage />} />
      <Route path="/home" element={<HomePage />} />
      <Route path="/projects" element={<ProjectListPage />} />
      <Route path="/projects/new" element={<ProjectCreatePage />} />
      <Route path="/projects/:projectId/edit" element={<ProjectCreatePage />} />
      <Route path="/projects/:projectId" element={<ProjectHomePage />} />
      <Route path="/notes" element={<NotePage />} />
      <Route path="/settings" element={<IntegrationManagePage />} />
      <Route path="/settings/profile" element={<ProfilePage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
