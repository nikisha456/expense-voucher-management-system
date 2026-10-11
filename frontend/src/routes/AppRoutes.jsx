import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import AppLayout from '../layouts/AppLayout';
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import EmployeeDashboard from '../pages/employee/EmployeeDashboard';
import DirectorDashboard from '../pages/director/DirectorDashboard';
import AccountsDashboard from '../pages/accounts/AccountsDashboard';
import VoucherFormPage from '../pages/employee/VoucherFormPage';
import VoucherListPage from '../pages/common/VoucherListPage';
import VoucherDetailsPage from '../pages/common/VoucherDetailsPage';
import ProfilePage from '../pages/common/ProfilePage';
import ForbiddenPage from '../pages/common/ForbiddenPage';
import NotFoundPage from '../pages/common/NotFoundPage';
import { useAuth } from '../context/AuthContext';

function DashboardRouter() {
  const { user } = useAuth();
  if (user?.role === 'DIRECTOR') return <DirectorDashboard />;
  if (user?.role === 'ACCOUNTS') return <AccountsDashboard />;
  return <EmployeeDashboard />;
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forbidden" element={<ForbiddenPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardRouter />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/vouchers" element={<VoucherListPage />} />
          <Route path="/vouchers/:id" element={<VoucherDetailsPage />} />
          <Route element={<ProtectedRoute roles={['EMPLOYEE']} />}>
            <Route path="/vouchers/new" element={<VoucherFormPage />} />
            <Route path="/vouchers/:id/edit" element={<VoucherFormPage />} />
          </Route>
          <Route element={<ProtectedRoute roles={['DIRECTOR']} />}>
            <Route path="/approvals" element={<VoucherListPage mode="pending" />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
