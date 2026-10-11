import SignatureImage from './SignatureImage';
import { StatusBadge } from './ui';
import { formatDate, money } from '../utils/format';

export default function VoucherDocument({ voucher }) {
  return (
    <article id="voucher-document" className="rounded-2xl bg-white p-6 shadow-card ring-1 ring-slate-200 print:shadow-none dark:bg-slate-900 dark:ring-slate-800 print:dark:bg-white print:dark:text-black">
      <header className="border-b border-slate-200 pb-4 text-center dark:border-slate-800">
        <p className="text-xs font-semibold uppercase tracking-[0.22em] text-indigo-600">ABC Company</p>
        <h2 className="mt-1 text-2xl font-semibold">Expense Voucher</h2>
        <p className="text-sm text-slate-500">ExpenseFlow reimbursement document</p>
      </header>
      <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-xs uppercase tracking-wide text-slate-500">Voucher number</p>
          <p className="font-semibold">{voucher.voucherNumber}</p>
        </div>
        <StatusBadge status={voucher.status} />
      </div>
      <dl className="mt-5 grid gap-4 sm:grid-cols-2">
        <Item label="Voucher date" value={formatDate(voucher.voucherDate)} />
        <Item label="Expense date" value={formatDate(voucher.expenseDate)} />
        <Item label="Employee" value={voucher.employeeName} />
        <Item label="Employee ID" value={voucher.employeeCode || '—'} />
        <Item label="Department" value={voucher.departmentName} />
        <Item label="Category" value={voucher.categoryLabel} />
        <Item label="Expense title" value={voucher.expenseTitle} wide />
        <Item label="Description" value={voucher.expenseDescription || '—'} wide />
      </dl>
      <div className="mt-6 rounded-2xl bg-slate-50 p-4 dark:bg-slate-950">
        <p className="text-xs uppercase tracking-wide text-slate-500">Amount</p>
        <p className="text-2xl font-semibold text-indigo-700 dark:text-indigo-300">{money(voucher.amount)}</p>
        <p className="mt-1 text-sm text-slate-600 dark:text-slate-300">{voucher.amountInWords}</p>
        {voucher.approvalDate && <p className="mt-2 text-xs text-slate-500">Approval date: {formatDate(voucher.approvalDate)}</p>}
      </div>
      {voucher.rejectionReason && (
        <div className="mt-4 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800 dark:border-rose-900 dark:bg-rose-950/50 dark:text-rose-200">
          <p className="font-semibold">Rejection reason</p>
          <p className="mt-1">{voucher.rejectionReason}</p>
        </div>
      )}
      <div className="mt-6 grid gap-4 sm:grid-cols-3">
        <SignBox title="Employee" name={voucher.employeeName} fileName={voucher.employeeSignatureFileName} />
        <SignBox title="Director" name={voucher.decisionByName || 'Director'} fileName={voucher.directorSignatureFileName} />
        <SignBox title="Accounts" name="For reimbursement" />
      </div>
    </article>
  );
}

function Item({ label, value, wide }) {
  return (
    <div className={wide ? 'sm:col-span-2' : ''}>
      <dt className="text-xs uppercase tracking-wide text-slate-500">{label}</dt>
      <dd className="mt-1 text-sm font-medium">{value}</dd>
    </div>
  );
}

function SignBox({ title, name, fileName }) {
  return (
    <div className="rounded-2xl border border-slate-200 p-3 dark:border-slate-800">
      <p className="text-xs uppercase tracking-wide text-slate-500">{title}</p>
      <div className="mt-3 flex h-16 items-center">
        <SignatureImage fileName={fileName} alt={`${title} signature`} />
      </div>
      <p className="mt-2 border-t border-slate-200 pt-2 text-sm font-medium dark:border-slate-800">{name}</p>
    </div>
  );
}
