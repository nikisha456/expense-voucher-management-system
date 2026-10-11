import { Link } from 'react-router-dom';
import { ArrowDown, ArrowUp, Receipt } from 'lucide-react';
import { StatusBadge, EmptyState, Skeleton } from './ui';
import { formatDate, money } from '../utils/format';

const COLUMNS = [
  { key: 'voucherNumber', label: 'Voucher' },
  { key: 'employeeName', label: 'Employee' },
  { key: 'departmentName', label: 'Department' },
  { key: 'expenseCategory', label: 'Category' },
  { key: 'expenseDate', label: 'Expense date' },
  { key: 'amount', label: 'Amount' },
  { key: 'status', label: 'Status' },
];

export default function VoucherTable({ rows, loading, sortBy, sortDir, onSort, meta, page, onPage }) {
  if (loading) {
    return (
      <div className="space-y-3 rounded-2xl bg-white p-4 dark:bg-slate-900">
        {Array.from({ length: 6 }).map((_, index) => <Skeleton key={index} className="h-12" />)}
      </div>
    );
  }

  if (!rows.length) {
    return <EmptyState icon={Receipt} title="No vouchers found" message="Try another filter or create a new voucher when your role allows it." />;
  }

  return (
    <div className="overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
      <div className="hidden max-h-[640px] overflow-auto md:block">
        <table className="min-w-full text-sm">
          <thead className="sticky top-0 bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500 dark:bg-slate-800">
            <tr>
              {COLUMNS.map((column) => (
                <th key={column.key} className="px-4 py-3 font-semibold">
                  <button className="inline-flex items-center gap-1" onClick={() => onSort(column.key)}>
                    {column.label}
                    {sortBy === column.key && (sortDir === 'asc' ? <ArrowUp size={14} /> : <ArrowDown size={14} />)}
                  </button>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id} className="border-t border-slate-100 transition hover:bg-indigo-50/50 dark:border-slate-800 dark:hover:bg-slate-800/60">
                <td className="px-4 py-3 font-medium">
                  <Link className="text-indigo-600 hover:underline dark:text-indigo-300" to={`/vouchers/${row.id}`}>{row.voucherNumber}</Link>
                  <div className="text-xs text-slate-500">{row.expenseTitle}</div>
                </td>
                <td className="px-4 py-3">{row.employeeName}</td>
                <td className="px-4 py-3">{row.departmentName}</td>
                <td className="px-4 py-3">{row.categoryLabel}</td>
                <td className="px-4 py-3">{formatDate(row.expenseDate)}</td>
                <td className="px-4 py-3 font-medium">{money(row.amount)}</td>
                <td className="px-4 py-3"><StatusBadge status={row.status} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="space-y-3 p-3 md:hidden">
        {rows.map((row) => (
          <Link key={row.id} to={`/vouchers/${row.id}`} className="block rounded-2xl border border-slate-200 p-4 dark:border-slate-800">
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="font-semibold">{row.voucherNumber}</p>
                <p className="text-sm text-slate-500">{row.expenseTitle}</p>
              </div>
              <StatusBadge status={row.status} />
            </div>
            <p className="mt-3 text-sm">{row.employeeName} · {row.departmentName}</p>
            <p className="mt-1 text-sm font-medium">{money(row.amount)} · {formatDate(row.expenseDate)}</p>
          </Link>
        ))}
      </div>
      <div className="flex items-center justify-between border-t border-slate-100 px-4 py-3 text-sm dark:border-slate-800">
        <span className="text-slate-500">Page {page + 1} of {Math.max(meta?.totalPages || 1, 1)} · {meta?.totalElements || 0} vouchers</span>
        <div className="flex gap-2">
          <button className="rounded-lg px-3 py-1.5 ring-1 ring-slate-200 disabled:opacity-40 dark:ring-slate-700" disabled={page <= 0} onClick={() => onPage(page - 1)}>Previous</button>
          <button className="rounded-lg px-3 py-1.5 ring-1 ring-slate-200 disabled:opacity-40 dark:ring-slate-700" disabled={page + 1 >= (meta?.totalPages || 1)} onClick={() => onPage(page + 1)}>Next</button>
        </div>
      </div>
    </div>
  );
}
