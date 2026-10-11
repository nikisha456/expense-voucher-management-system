import { useEffect, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Bell, ClipboardCheck, LayoutDashboard, LogOut, Menu, Moon, Plus, Receipt, Sun, UserRound, WalletCards, X } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { api } from '../api/client';
import { initials } from '../utils/format';

const MENUS = {
  EMPLOYEE: [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/vouchers', label: 'My Vouchers', icon: Receipt },
    { to: '/vouchers/new', label: 'New Voucher', icon: Plus },
    { to: '/profile', label: 'Profile', icon: UserRound },
  ],
  DIRECTOR: [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/approvals', label: 'Pending Approvals', icon: ClipboardCheck },
    { to: '/vouchers', label: 'All Vouchers', icon: Receipt },
    { to: '/profile', label: 'Profile', icon: UserRound },
  ],
  ACCOUNTS: [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/vouchers', label: 'All Vouchers', icon: Receipt },
    { to: '/profile', label: 'Profile', icon: UserRound },
  ],
};

export default function AppLayout() {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [notesOpen, setNotesOpen] = useState(false);
  const [pendingCount, setPendingCount] = useState(0);
  const items = MENUS[user?.role] || [];

  useEffect(() => {
    if (user?.role !== 'DIRECTOR') return undefined;
    let active = true;
    api.get('/api/vouchers/pending', { params: { page: 0, size: 1 } })
      .then((response) => {
        if (active) setPendingCount(response.data.meta?.totalElements || 0);
      })
      .catch(() => {});
    return () => {
      active = false;
    };
  }, [user]);

  async function handleLogout() {
    await logout();
    navigate('/login');
  }

  const sidebar = (
    <div className="flex h-full flex-col">
      <div className="flex items-center gap-3 px-4 py-5">
        <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-indigo-600 text-white">
          <WalletCards size={20} />
        </div>
        {!collapsed && (
          <div>
            <p className="font-semibold leading-tight">ExpenseFlow</p>
            <p className="text-xs text-slate-500">ABC Company</p>
          </div>
        )}
      </div>
      <nav className="flex-1 space-y-1 px-3">
        {items.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/dashboard' || item.to === '/vouchers'}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) => `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition ${isActive ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30' : 'text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800'}`}
            >
              <Icon size={18} />
              {!collapsed && <span className="flex-1">{item.label}</span>}
              {!collapsed && item.to === '/approvals' && pendingCount > 0 && (
                <span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs text-amber-800">{pendingCount}</span>
              )}
            </NavLink>
          );
        })}
      </nav>
      <button className="m-3 hidden items-center gap-2 rounded-xl px-3 py-2 text-sm text-slate-500 hover:bg-slate-100 lg:flex dark:hover:bg-slate-800" onClick={() => setCollapsed((value) => !value)}>
        <Menu size={16} />
        {!collapsed && 'Collapse'}
      </button>
    </div>
  );

  return (
    <div className="min-h-screen lg:flex">
      <aside className={`no-print fixed inset-y-0 left-0 z-40 hidden border-r border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900 lg:block ${collapsed ? 'w-[84px]' : 'w-64'}`}>
        {sidebar}
      </aside>
      {mobileOpen && (
        <div className="no-print fixed inset-0 z-40 lg:hidden">
          <button className="absolute inset-0 bg-slate-950/40" aria-label="Close menu" onClick={() => setMobileOpen(false)} />
          <aside className="relative h-full w-72 bg-white dark:bg-slate-900">{sidebar}</aside>
        </div>
      )}
      <div className={`flex min-h-screen flex-1 flex-col ${collapsed ? 'lg:pl-[84px]' : 'lg:pl-64'}`}>
        <header className="no-print sticky top-0 z-30 flex h-16 items-center justify-between border-b border-slate-200 bg-white/90 px-4 backdrop-blur dark:border-slate-800 dark:bg-slate-950/90">
          <button className="rounded-xl p-2 lg:hidden" aria-label="Open menu" onClick={() => setMobileOpen(true)}>
            <Menu size={20} />
          </button>
          <div className="hidden text-sm text-slate-500 sm:block">Voucher operations for ABC Company</div>
          <div className="ml-auto flex items-center gap-2">
            <button className="rounded-xl p-2 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800" aria-label="Toggle color theme" onClick={toggleTheme}>
              {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <div className="relative">
              <button className="rounded-xl p-2 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800" aria-label="Notifications" onClick={() => setNotesOpen((value) => !value)}>
                <Bell size={18} />
              </button>
              {notesOpen && (
                <div className="absolute right-0 mt-2 w-64 rounded-2xl bg-white p-4 text-sm shadow-card ring-1 ring-slate-200 dark:bg-slate-900 dark:ring-slate-800">
                  No new notifications
                </div>
              )}
            </div>
            <div className="relative">
              <button className="flex items-center gap-2 rounded-full py-1 pl-1 pr-3 hover:bg-slate-100 dark:hover:bg-slate-800" onClick={() => setMenuOpen((value) => !value)}>
                <span className="flex h-8 w-8 items-center justify-center rounded-full bg-teal-600 text-xs font-semibold text-white">{initials(user?.name)}</span>
                <span className="hidden text-left sm:block">
                  <span className="block text-sm font-medium leading-tight">{user?.name}</span>
                  <span className="block text-[11px] uppercase tracking-wide text-indigo-600">{user?.role}</span>
                </span>
              </button>
              {menuOpen && (
                <div className="absolute right-0 mt-2 w-48 rounded-2xl bg-white p-2 shadow-card ring-1 ring-slate-200 dark:bg-slate-900 dark:ring-slate-800">
                  <button className="flex w-full items-center gap-2 rounded-xl px-3 py-2 text-sm hover:bg-slate-100 dark:hover:bg-slate-800" onClick={() => { setMenuOpen(false); navigate('/profile'); }}>
                    <UserRound size={16} /> Profile
                  </button>
                  <button className="flex w-full items-center gap-2 rounded-xl px-3 py-2 text-sm text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950" onClick={handleLogout}>
                    <LogOut size={16} /> Logout
                  </button>
                </div>
              )}
            </div>
            <button className="rounded-xl p-2 lg:hidden" aria-label="Close overlays" onClick={() => { setMenuOpen(false); setNotesOpen(false); }}>
              <X size={16} className="opacity-0" />
            </button>
          </div>
        </header>
        <main className="flex-1 px-4 py-6 sm:px-6 lg:px-8">
          <div className="animate-fade-up">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
