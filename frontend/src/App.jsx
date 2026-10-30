import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Layout from './components/layout';
import ProtectedRoute from './auth/ProtectedRoute';
import { getPrimaryRole } from './auth/keycloak';
import RoleHome from './pages/RoleHome';
import Unauthorized from './pages/Unauthorized';
import AvailableSubjectsPage from './pages/student/AvailableSubjectsPage';
import MyEnrollmentsPage from './pages/student/MyEnrollmentsPage';
import MySectionsPage from './pages/teacher/MySectionsPage';
import SectionGradesPage from './pages/teacher/SectionGradesPage';
import AcademicPeriodsPage from './pages/admin/AcademicPeriodsPage';
import CareersPage from './pages/admin/CareersPage';
import StudyPlansPage from './pages/admin/StudyPlansPage';
import SubjectsPage from './pages/admin/SubjectsPage';
import TeachersPage from './pages/admin/TeachersPage';
import StudentsPage from './pages/admin/StudentsPage';
import SectionsPage from './pages/admin/SectionsPage';
import MatriculationsPage from './pages/admin/MatriculationsPage';

const theme = createTheme({
  palette: {
    primary: { main: '#0b5c8a' },
    secondary: { main: '#f2a900' },
  },
});

const ADMIN = ['ADMIN'];
const TEACHER = ['TEACHER'];
const STUDENT = ['STUDENT'];

/** Wraps a page with the role guard, keeping the route list flat and readable. */
const protect = (roles, element) => (
  <ProtectedRoute roles={roles}>{element}</ProtectedRoute>
);

export default function App() {
  const role = getPrimaryRole();
  const home =
    role === 'ADMIN' ? '/admin'
      : role === 'TEACHER' ? '/teacher'
        : role === 'STUDENT' ? '/student'
          : '/unauthorized';

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <Layout>
          <Routes>
            <Route path="/" element={<Navigate to={home} replace />} />
            <Route path="/unauthorized" element={<Unauthorized />} />

            {/* ADMIN */}
            <Route path="/admin" element={protect(ADMIN, <RoleHome />)} />
            <Route path="/admin/careers" element={protect(ADMIN, <CareersPage />)} />
            <Route path="/admin/study-plans" element={protect(ADMIN, <StudyPlansPage />)} />
            <Route path="/admin/subjects" element={protect(ADMIN, <SubjectsPage />)} />
            <Route path="/admin/teachers" element={protect(ADMIN, <TeachersPage />)} />
            <Route path="/admin/students" element={protect(ADMIN, <StudentsPage />)} />
            <Route path="/admin/academic-periods" element={protect(ADMIN, <AcademicPeriodsPage />)} />
            <Route path="/admin/sections" element={protect(ADMIN, <SectionsPage />)} />
            <Route path="/admin/matriculations" element={protect(ADMIN, <MatriculationsPage />)} />

            {/* TEACHER */}
            <Route path="/teacher" element={protect(TEACHER, <RoleHome />)} />
            <Route path="/teacher/sections" element={protect(TEACHER, <MySectionsPage />)} />
            <Route path="/teacher/sections/:sectionId/grades" element={protect(TEACHER, <SectionGradesPage />)} />

            {/* STUDENT */}
            <Route path="/student" element={protect(STUDENT, <RoleHome />)} />
            <Route path="/student/available" element={protect(STUDENT, <AvailableSubjectsPage />)} />
            <Route path="/student/enrollments" element={protect(STUDENT, <MyEnrollmentsPage />)} />

            <Route path="*" element={<Navigate to={home} replace />} />
          </Routes>
        </Layout>
      </BrowserRouter>
    </ThemeProvider>
  );
}
