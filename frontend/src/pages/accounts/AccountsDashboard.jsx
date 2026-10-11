import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { BadgeCheck, Clock3, Receipt, Wallet, XCircle } from 'lucide-react';
import { Bar, BarChart, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { greeting, money, todayLabel } from '../../utils/format';
import { PageHeader, Skeleton, StatusBadge } from '../../components/ui';

export default function AccountsDashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);

  useEffect(() => {
    api.get('/api/dashboard/accounts').then((response) => setData(response.data.data));
  }, []);

  if (!data) return <div className="grid gap-4 md:grid-cols-3">{Array.from({ length: 6 }).map((_, index) => <Skeleton key={index} className="h-28" />)}</div>;

  const cards = [
    ['Total Vouchers', data.totalVouchers, Receipt],
    ['Pending Approval', data.pendingApproval, Clock3],
    ['Approved Vouchers', data.approvedVouchers, BadgeCheck],
    ['Rejected Vouchers', data.rejectedVouchers, XCircle],
    ['Total Approved Expense', money(data.totalApprovedExpenseAmount), Wallet],
  ];

  return (
    <div>
      <PageHeader title={greeting(user?.name)} subtitle={`Accounts overview · ${todayLabel()}`} />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        {cards.map(([label, value, Icon]) => (
          <article key={label} className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
            <Icon size={18} className="text-teal-600" />
            <p className="mt-4 text-sm text-slate-500">{label}</p>
            <p className="mt-1 text-xl font-semibold">{value}</p>
          </article>
        ))}
      </div>
      <div className="mt-6 grid gap-4 xl:grid-cols-2">
        <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
          <h2 className="font-semibold">Department-wise approved amount</h2>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.departmentApproved} layout="vertical" margin={{ left: 24 }}>
                <XAxis type="number" stroke="#94a3b8" fontSize={12} />
                <YAxis type="category" dataKey="label" width={110} stroke="#94a3b8" fontSize={12} />
                <Tooltip />
                <Bar dataKey="amount" fill="#4f46e5" radius={[0, 8, 8, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </section>
        <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
          <h2 className="font-semibold">Monthly reimbursement trend</h2>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data.monthlyReimbursement}>
                <XAxis dataKey="label" stroke="#94a3b8" fontSize={12} />
                <YAxis stroke="#94a3b8" fontSize={12} />
                <Tooltip />
                <Line type="monotone" dataKey="amount" stroke="#0d9488" strokeWidth={3} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </section>
      </div>
      <section className="mt-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
        <h2 className="font-semibold">Recent approved vouchers</h2>
        <div className="mt-4 space-y-3">
          {data.recentApproved.map((voucher) => (
            <Link key={voucher.id} to={`/vouchers/${voucher.id}`} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-200 px-4 py-3 text-sm dark:border-slate-800">
              <span>
                <span className="font-medium">{voucher.voucherNumber}</span>
                <span className="ml-2 text-slate-500">{voucher.employeeName} · {voucher.departmentName}</span>
              </span>
              <span className="flex items-center gap-3">
                <span className="font-semibold">{money(voucher.amount)}</span>
                <StatusBadge status={voucher.status} />
              </span>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
