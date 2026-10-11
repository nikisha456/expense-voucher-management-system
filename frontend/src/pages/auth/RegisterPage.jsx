import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Eye, EyeOff, Moon, Sun, WalletCards } from 'lucide-react';
import toast from 'react-hot-toast';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { registerSchema } from '../../validations/voucherSchema';
import { DEPARTMENTS } from '../../utils/constants';
import { errorMessage, fieldErrors } from '../../utils/format';
import { Button, Field, fieldClass } from '../../components/ui';

function passwordStrength(password) {
  if (!password) return { score: 0, label: '' };
  let score = 0;
  if (password.length >= 8) score += 1;
  if (/[a-z]/.test(password)) score += 1;
  if (/[A-Z]/.test(password)) score += 1;
  if (/\d/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;
  if (score <= 2) return { score: 1, label: 'Weak' };
  if (score <= 4) return { score: 2, label: 'Fair' };
  return { score: 3, label: 'Strong' };
}

const strengthColor = ['bg-slate-200', 'bg-rose-500', 'bg-amber-500', 'bg-emerald-500'];

export default function RegisterPage() {
  const { user } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const form = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      name: '',
      email: '',
      employeeId: '',
      department: '',
      password: '',
      confirmPassword: '',
    },
  });
  const password = form.watch('password');
  const strength = passwordStrength(password);

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  async function onSubmit(values) {
    setLoading(true);
    try {
      await api.post('/api/auth/register', {
        name: values.name.trim(),
        email: values.email.trim(),
        password: values.password,
        confirmPassword: values.confirmPassword,
        employeeId: values.employeeId?.trim() || null,
        department: values.department,
      });
      toast.success('Account created successfully, please sign in');
      navigate('/login');
    } catch (error) {
      const fields = fieldErrors(error);
      Object.entries(fields).forEach(([key, message]) => form.setError(key, { message }));
      if (error.response?.status === 409) {
        form.setError('email', { message: errorMessage(error) });
      }
      toast.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="relative grid min-h-screen lg:grid-cols-2">
      <button type="button" className="absolute right-4 top-4 z-10 rounded-xl bg-white/90 p-2 text-slate-700 shadow-sm dark:bg-slate-900 dark:text-slate-100" aria-label="Toggle color theme" onClick={toggleTheme}>
        {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
      </button>
      <section className="relative hidden overflow-hidden bg-gradient-to-br from-indigo-700 via-indigo-600 to-teal-600 p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="absolute -right-16 top-16 h-56 w-56 rounded-full bg-white/10" />
        <div className="absolute bottom-10 left-10 h-40 w-40 rounded-full bg-teal-300/20" />
        <div className="relative flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/15">
            <WalletCards />
          </div>
          <div>
            <p className="text-xl font-semibold">ExpenseFlow</p>
            <p className="text-sm text-indigo-100">ABC Company</p>
          </div>
        </div>
        <div className="relative max-w-md">
          <h1 className="text-4xl font-semibold leading-tight">Expense vouchers, from draft to reimbursement.</h1>
          <ul className="mt-6 space-y-3 text-sm text-indigo-50">
            <li>Employees draft, sign, and track every claim.</li>
            <li>The Director approves or rejects with a recorded reason.</li>
            <li>Accounts reviews the full register and prints the voucher.</li>
          </ul>
        </div>
        <p className="relative text-sm text-indigo-100">New accounts are created as employees.</p>
      </section>
      <section className="flex items-start justify-center overflow-y-auto px-4 py-10 sm:items-center">
        <form onSubmit={form.handleSubmit(onSubmit)} className="my-4 w-full max-w-md rounded-3xl bg-white p-6 shadow-card ring-1 ring-slate-200 dark:bg-slate-900 dark:ring-slate-800 sm:p-8">
          <div className="mb-6 flex items-center gap-3 lg:hidden">
            <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-indigo-600 text-white"><WalletCards size={18} /></div>
            <p className="font-semibold">ExpenseFlow</p>
          </div>
          <h2 className="text-2xl font-semibold">Create account</h2>
          <p className="mt-1 text-sm text-slate-500">Register as an ABC Company employee.</p>
          <div className="mt-6 space-y-4">
            <Field label="Full Name" error={form.formState.errors.name?.message}>
              <input className={fieldClass} autoComplete="name" {...form.register('name')} />
            </Field>
            <Field label="Email" error={form.formState.errors.email?.message}>
              <input className={fieldClass} type="email" autoComplete="email" {...form.register('email')} />
            </Field>
            <Field label="Employee ID (optional)" error={form.formState.errors.employeeId?.message}>
              <input className={fieldClass} {...form.register('employeeId')} />
            </Field>
            <Field label="Department" error={form.formState.errors.department?.message}>
              <select className={fieldClass} {...form.register('department')}>
                <option value="">Select department</option>
                {DEPARTMENTS.map((department) => <option key={department}>{department}</option>)}
              </select>
            </Field>
            <div className="block space-y-1.5 text-sm">
              <label className="font-medium text-slate-700 dark:text-slate-200" htmlFor="register-password">Password</label>
              <div className="relative">
                <input id="register-password" className={`${fieldClass} pr-11`} type={showPassword ? 'text' : 'password'} autoComplete="new-password" {...form.register('password')} />
                <button type="button" className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500" aria-label={showPassword ? 'Hide password' : 'Show password'} onClick={() => setShowPassword((value) => !value)}>
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {password && (
                <div className="mt-2" aria-live="polite">
                  <div className="grid grid-cols-3 gap-1">
                    {[1, 2, 3].map((step) => (
                      <span key={step} className={`h-1.5 rounded-full ${strength.score >= step ? strengthColor[strength.score] : 'bg-slate-200 dark:bg-slate-700'}`} />
                    ))}
                  </div>
                  <p className="mt-1 text-xs text-slate-500">Password strength: {strength.label}</p>
                </div>
              )}
              {form.formState.errors.password?.message && <span className="block text-xs text-rose-600" role="alert">{form.formState.errors.password.message}</span>}
            </div>
            <div className="block space-y-1.5 text-sm">
              <label className="font-medium text-slate-700 dark:text-slate-200" htmlFor="register-confirm-password">Confirm Password</label>
              <div className="relative">
                <input id="register-confirm-password" className={`${fieldClass} pr-11`} type={showConfirm ? 'text' : 'password'} autoComplete="new-password" {...form.register('confirmPassword')} />
                <button type="button" className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500" aria-label={showConfirm ? 'Hide confirm password' : 'Show confirm password'} onClick={() => setShowConfirm((value) => !value)}>
                  {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {form.formState.errors.confirmPassword?.message && <span className="block text-xs text-rose-600" role="alert">{form.formState.errors.confirmPassword.message}</span>}
            </div>
          </div>
          <Button className="mt-6 w-full" type="submit" loading={loading}>Create account</Button>
          <p className="mt-4 text-center text-sm text-slate-500">
            Already have an account? <Link className="font-semibold text-indigo-600 hover:text-indigo-500" to="/login">Sign in</Link>
          </p>
        </form>
      </section>
    </div>
  );
}
