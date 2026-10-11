import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { BadgeCheck, Clock3, IndianRupee, XCircle } from 'lucide-react';
import { Bar, BarChart, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { formatDateTime, greeting, money, todayLabel } from '../../utils/format';
import { Button, PageHeader, Skeleton, StatusBadge } from '../../components/ui';

export default function DirectorDashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);

  useEffect(() => {
    api.get('/api/dashboard/director').then((response) => setData(response.data.data));
  }, []);

  if (!data) return <div className="grid gap-4 md:grid-cols-4">{Array.from({ length: 4 }).map((_, index) => <Skeleton key={index} className="h-28" />)}</div>;

  const cards = [
    { label: 'Pending Approval', value: data.pendingApprovalCount, icon: Clock3 },
    { label: 'Approved Today', value: data.approvedToday, icon: BadgeCheck },
    { label: 'Rejected Today', value: data.rejectedToday, icon: XCircle },
    { label: 'Total Pending Amount', value: money(data.totalPendingAmount), icon: IndianRupee },
  ];

  return (
    <div>
      <PageHeader title={greeting(user?.name)} subtitle={`Director overview · ${todayLabel()}`} />
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {cards.map((card) => (
          <article key={card.label} className="rounded-2xl bg-gradient-to-br from-indigo-600 to-teal-600 p-5 text-white shadow-card">
            <card.icon size={18} />
            <p className="mt-4 text-sm text-indigo-100">{card.label}</p>
            <p className="mt-1 text-2xl font-semibold">{card.value}</p>
          </article>
        ))}
      </div>
      <div className="mt-6 grid gap-4 xl:grid-cols-2">
        <ChartCard title="Category-wise approved spend">
          <BarChart data={data.categorySpend}>
            <XAxis dataKey="label" hide />
            <YAxis stroke="#94a3b8" fontSize={12} />
            <Tooltip />
            <Bar dataKey="amount" fill="#0d9488" radius={[8, 8, 0, 0]} />
          </BarChart>
        </ChartCard>
        <ChartCard title="Approvals trend">
          <LineChart data={data.approvalsTrend}>
            <XAxis dataKey="label" stroke="#94a3b8" fontSize={12} />
            <YAxis allowDecimals={false} stroke="#94a3b8" fontSize={12} />
            <Tooltip />
            <Line type="monotone" dataKey="count" stroke="#4f46e5" strokeWidth={3} dot={false} />
          </LineChart>
        </ChartCard>
      </div>
      <div className="mt-6 grid gap-4 xl:grid-cols-2">
        <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-semibold">Review now</h2>
            <Link to="/approvals"><Button variant="secondary">Open queue</Button></Link>
          </div>
          <div className="space-y-3">
            {data.pendingReview.map((voucher) => (
              <Link key={voucher.id} to={`/vouchers/${voucher.id}`} className="flex items-center justify-between rounded-xl border border-slate-200 px-3 py-3 text-sm hover:border-indigo-300 dark:border-slate-800">
                <span>
                  <span className="block font-medium">{voucher.voucherNumber}</span>
                  <span className="text-slate-500">{voucher.employeeName} · {voucher.expenseTitle}</span>
                </span>
                <span className="font-semibold">{money(voucher.amount)}</span>
              </Link>
            ))}
          </div>
        </section>
        <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
          <h2 className="font-semibold">Recent voucher activity</h2>
          <ol className="mt-4 space-y-4">
            {data.recentActivity.map((item, index) => (
              <li key={`${item.voucherId}-${index}`} className="flex gap-3 text-sm">
                <StatusBadge status={item.toStatus} />
                <div>
                  <p><span className="font-medium">{item.actorName}</span> {item.action} <Link className="text-indigo-600" to={`/vouchers/${item.voucherId}`}>{item.voucherNumber}</Link></p>
                  <p className="text-xs text-slate-500">{formatDateTime(item.changedAt)}</p>
                </div>
              </li>
            ))}
          </ol>
        </section>
      </div>
    </div>
  );
}

function ChartCard({ title, children }) {
  return (
    <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
      <h2 className="mb-2 font-semibold">{title}</h2>
      <div className="h-72">
        <ResponsiveContainer width="100%" height="100%">{children}</ResponsiveContainer>
      </div>
    </section>
  );
}
