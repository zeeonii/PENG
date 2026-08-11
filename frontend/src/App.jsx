import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/Login/LoginPage.jsx";
import HomePage from "./pages/Home/HomePage.jsx";
import ProjectListPage from "./pages/Project/ProjectList/ProjectListPage.jsx";
import ProjectCreatePage from "./pages/Project/ProjectCreate/ProjectCreatePage.jsx";
import ProjectHomePage from "./pages/Project/ProjectDetail/Home/ProjectHomePage.jsx";
import NotePage from "./pages/Note/NotePage.jsx";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/home" element={<HomePage />} />
      <Route path="/projects" element={<ProjectListPage />} />
      <Route path="/projects/new" element={<ProjectCreatePage />} />
      <Route path="/projects/:projectId" element={<ProjectHomePage />} />
      <Route path="/notes" element={<NotePage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
