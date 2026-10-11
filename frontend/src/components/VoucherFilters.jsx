import { CATEGORIES, DEPARTMENTS, STATUSES } from '../utils/constants';
import { fieldClass, Button } from './ui';

export default function VoucherFilters({ filters, onChange, onClear, hideStatus = false }) {
  const chips = [
    ['voucherNumber', 'Number'],
    ['employeeName', 'Employee'],
    ['department', 'Department'],
    ['category', 'Category'],
    ['status', 'Status'],
    ['fromDate', 'From'],
    ['toDate', 'To'],
    ['minAmount', 'Min'],
    ['maxAmount', 'Max'],
    ['search', 'Search'],
  ].filter(([key, label]) => filters[key] && !(hideStatus && key === 'status'));

  return (
    <div className="mb-4 space-y-3 rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        <input className={fieldClass} placeholder="Keyword search" aria-label="Keyword search" value={filters.search} onChange={(event) => onChange('search', event.target.value)} />
        <input className={fieldClass} placeholder="Voucher number" aria-label="Voucher number" value={filters.voucherNumber} onChange={(event) => onChange('voucherNumber', event.target.value)} />
        <input className={fieldClass} placeholder="Employee name" aria-label="Employee name" value={filters.employeeName} onChange={(event) => onChange('employeeName', event.target.value)} />
        <select className={fieldClass} aria-label="Department" value={filters.department} onChange={(event) => onChange('department', event.target.value)}>
          <option value="">All departments</option>
          {DEPARTMENTS.map((department) => <option key={department}>{department}</option>)}
        </select>
        <select className={fieldClass} aria-label="Category" value={filters.category} onChange={(event) => onChange('category', event.target.value)}>
          <option value="">All categories</option>
          {CATEGORIES.map((category) => <option key={category.value} value={category.value}>{category.label}</option>)}
        </select>
        {!hideStatus && (
          <select className={fieldClass} aria-label="Status" value={filters.status} onChange={(event) => onChange('status', event.target.value)}>
            <option value="">All statuses</option>
            {STATUSES.map((status) => <option key={status.value} value={status.value}>{status.label}</option>)}
          </select>
        )}
        <input className={fieldClass} type="date" aria-label="From date" value={filters.fromDate} onChange={(event) => onChange('fromDate', event.target.value)} />
        <input className={fieldClass} type="date" aria-label="To date" value={filters.toDate} onChange={(event) => onChange('toDate', event.target.value)} />
        <input className={fieldClass} type="number" min="0" step="0.01" placeholder="Min amount" aria-label="Minimum amount" value={filters.minAmount} onChange={(event) => onChange('minAmount', event.target.value)} />
        <input className={fieldClass} type="number" min="0" step="0.01" placeholder="Max amount" aria-label="Maximum amount" value={filters.maxAmount} onChange={(event) => onChange('maxAmount', event.target.value)} />
      </div>
      <div className="flex flex-wrap items-center gap-2">
        {chips.map(([key, label]) => (
          <button key={key} className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700 dark:bg-indigo-950 dark:text-indigo-200" onClick={() => onChange(key, '')}>
            {label}: {filters[key]} ×
          </button>
        ))}
        <Button variant="ghost" onClick={onClear}>Clear filters</Button>
      </div>
    </div>
  );
}
