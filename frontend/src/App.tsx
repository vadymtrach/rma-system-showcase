import { Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { AppLayout } from "./components/AppLayout";
import { ArchivePage } from "./pages/ArchivePage";
import { ComplaintsPage } from "./pages/ComplaintsPage";
import { CreateComplaintPage } from "./pages/CreateComplaintPage";
import { LoginPage } from "./pages/LoginPage";
import { ProfilePage } from "./pages/ProfilePage";
import { TrackingPage } from "./pages/TrackingPage";
import { UsersPage } from "./pages/UsersPage";
import { ThemeProvider } from "./theme/ThemeContext";

export function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route index element={<ComplaintsPage />} />
              <Route path="create" element={<CreateComplaintPage />} />
              <Route path="users" element={<UsersPage />} />
              <Route path="archive" element={<ArchivePage />} />
              <Route path="tracking" element={<TrackingPage />} />
              <Route path="profile" element={<ProfilePage />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </ThemeProvider>
  );
}
