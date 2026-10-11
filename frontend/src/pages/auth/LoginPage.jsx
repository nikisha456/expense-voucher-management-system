import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { Moon, Sun, WalletCards } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { DEMO_ACCOUNTS } from '../../utils/constants';
import { errorMessage } from '../../utils/format';
import { Button, fieldClass } from '../../components/ui';

export default function LoginPage() {
  const { user, login } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const [email, setEmail] = useState('ananya.iyer@abc.com');
  const [password, setPassword] = useState('Password@123');
  const [loading, setLoading] = useState(false);

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  async function onSubmit(event) {
    event.preventDefault();
    setLoading(true);
    try {
      await login(email, password);
      toast.success('Welcome back');
      navigate('/dashboard');
    } catch (error) {
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
        <p className="relative text-sm text-indigo-100">Demo password for every account: Password@123</p>
      </section>
      <section className="flex items-center justify-center px-4 py-10">
        <form onSubmit={onSubmit} className="w-full max-w-md rounded-3xl bg-white p-8 shadow-card ring-1 ring-slate-200 dark:bg-slate-900 dark:ring-slate-800">
          <div className="mb-6 flex items-center gap-3 lg:hidden">
            <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-indigo-600 text-white"><WalletCards size={18} /></div>
            <p className="font-semibold">ExpenseFlow</p>
          </div>
          <h2 className="text-2xl font-semibold">Sign in</h2>
          <p className="mt-1 text-sm text-slate-500">Use your ABC Company email to continue.</p>
          <label className="mt-6 block text-sm font-medium">Email
            <input className={`${fieldClass} mt-1.5`} type="email" required value={email} onChange={(event) => setEmail(event.target.value)} />
          </label>
          <label className="mt-4 block text-sm font-medium">Password
            <input className={`${fieldClass} mt-1.5`} type="password" required value={password} onChange={(event) => setPassword(event.target.value)} />
          </label>
          <Button className="mt-6 w-full" type="submit" loading={loading}>Sign in</Button>
          <p className="mt-4 text-center text-sm text-slate-500">
            Don't have an account? <Link className="font-semibold text-indigo-600 hover:text-indigo-500" to="/register">Register</Link>
          </p>
          <div className="mt-6 grid gap-2">
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Demo accounts</p>
            {DEMO_ACCOUNTS.map((account) => (
              <button
                key={account.email}
                type="button"
                className="rounded-xl border border-slate-200 px-3 py-2 text-left text-sm hover:border-indigo-300 hover:bg-indigo-50 dark:border-slate-700 dark:hover:bg-slate-800"
                onClick={() => { setEmail(account.email); setPassword(account.password); }}
              >
                <span className="font-medium">{account.role}</span>
                <span className="mt-0.5 block text-xs text-slate-500">{account.email}</span>
              </button>
            ))}
            <p className="text-xs text-slate-500">Also available: karan.patel@abc.com and meera.nair@abc.com</p>
          </div>
        </form>
      </section>
    </div>
  );
}
