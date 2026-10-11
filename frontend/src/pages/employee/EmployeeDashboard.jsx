import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { BadgeCheck, Clock3, FileEdit, Receipt, Wallet, XCircle } from 'lucide-react';
import { Cell, Pie, PieChart, Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { greeting, money, todayLabel } from '../../utils/format';
import { PageHeader, Skeleton, StatusBadge } from '../../components/ui';

const COLORS = ['#64748b', '#3b82f6', '#f59e0b', '#10b981', '#e11d48'];

export default function EmployeeDashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);

  useEffect(() => {
    api.get('/api/dashboard/employee').then((response) => setData(response.data.data));
  }, []);

  if (!data) {
    return <div className="grid gap-4 md:grid-cols-3">{Array.from({ length: 6 }).map((_, index) => <Skeleton key={index} className="h-28" />)}</div>;
  }

  const cards = [
    { label: 'Total Vouchers', value: data.totalVouchers, icon: Receipt, tone: 'from-indigo-500 to-indigo-700' },
    { label: 'Draft Vouchers', value: data.draftVouchers, icon: FileEdit, tone: 'from-slate-500 to-slate-700' },
    { label: 'Pending Approval', value: data.pendingApproval, icon: Clock3, tone: 'from-amber-500 to-orange-600' },
    { label: 'Approved Vouchers', value: data.approvedVouchers, icon: BadgeCheck, tone: 'from-emerald-500 to-teal-600' },
    { label: 'Rejected Vouchers', value: data.rejectedVouchers, icon: XCircle, tone: 'from-rose-500 to-rose-700' },
    { label: 'Total Amount Claimed', value: money(data.totalAmountClaimed), icon: Wallet, tone: 'from-teal-500 to-cyan-700', hint: data.amountChangePercent == null ? 'No prior month' : `${data.amountChangePercent}% vs last month` },
  ];

  return (
    <div>
      <PageHeader title={greeting(user?.name)} subtitle={todayLabel()} />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {cards.map((card) => <Stat key={card.label} {...card} />)}
      </div>
      <div className="mt-6 grid gap-4 xl:grid-cols-5">
        <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 xl:col-span-3 dark:bg-slate-900 dark:ring-slate-800">
          <h2 className="font-semibold">Monthly spend</h2>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.monthlySpend}>
                <XAxis dataKey="label" stroke="#94a3b8" fontSize={12} />
                <YAxis stroke="#94a3b8" fontSize={12} />
                <Tooltip />
                <Bar dataKey="amount" fill="#4f46e5" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </section>
        <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 xl:col-span-2 dark:bg-slate-900 dark:ring-slate-800">
          <h2 className="font-semibold">Status mix</h2>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={data.statusDistribution} dataKey="count" nameKey="label" innerRadius={55} outerRadius={85}>
                  {data.statusDistribution.map((entry, index) => <Cell key={entry.status} fill={COLORS[index % COLORS.length]} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </section>
      </div>
      <section className="mt-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
        <h2 className="font-semibold">Recent vouchers</h2>
        <div className="mt-4 overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead className="text-left text-xs uppercase tracking-wide text-slate-500">
              <tr><th className="py-2">Number</th><th>Title</th><th>Amount</th><th>Status</th></tr>
            </thead>
            <tbody>
              {data.recentVouchers.map((voucher) => (
                <tr key={voucher.id} className="border-t border-slate-100 dark:border-slate-800">
                  <td className="py-3"><Link className="font-medium text-indigo-600" to={`/vouchers/${voucher.id}`}>{voucher.voucherNumber}</Link></td>
                  <td>{voucher.expenseTitle}</td>
                  <td>{money(voucher.amount)}</td>
                  <td><StatusBadge status={voucher.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

function Stat({ label, value, icon: Icon, tone, hint }) {
  return (
    <article className="relative overflow-hidden rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
      <div className={`absolute -right-6 -top-8 h-24 w-24 rounded-full bg-gradient-to-br ${tone} opacity-20`} />
      <Icon className="text-indigo-600" size={18} />
      <p className="mt-4 text-sm text-slate-500">{label}</p>
      <p className="mt-1 text-2xl font-semibold">{value}</p>
      {hint && <p className="mt-1 text-xs text-teal-600">{hint}</p>}
    </article>
  );
}
