export const DEPARTMENTS = [
  'Engineering',
  'Finance',
  'HR',
  'Sales',
  'Marketing',
  'Operations',
  'IT Support',
  'Legal',
  'Administration',
];

export const CATEGORIES = [
  { value: 'TRAVEL', label: 'Travel' },
  { value: 'FOOD_MEALS', label: 'Food & Meals' },
  { value: 'ACCOMMODATION', label: 'Accommodation' },
  { value: 'OFFICE_SUPPLIES', label: 'Office Supplies' },
  { value: 'SOFTWARE_SUBSCRIPTIONS', label: 'Software & Subscriptions' },
  { value: 'CLIENT_ENTERTAINMENT', label: 'Client Entertainment' },
  { value: 'FUEL_TRANSPORT', label: 'Fuel & Transport' },
  { value: 'TRAINING', label: 'Training' },
  { value: 'COMMUNICATION', label: 'Communication' },
  { value: 'MISCELLANEOUS', label: 'Miscellaneous' },
];

export const STATUSES = [
  { value: 'DRAFT', label: 'Draft' },
  { value: 'SUBMITTED', label: 'Submitted' },
  { value: 'PENDING_APPROVAL', label: 'Pending Approval' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'REJECTED', label: 'Rejected' },
];

export const STATUS_STYLES = {
  DRAFT: 'bg-slate-100 text-slate-700 ring-slate-200 dark:bg-slate-800 dark:text-slate-200 dark:ring-slate-700',
  SUBMITTED: 'bg-blue-50 text-blue-700 ring-blue-200 dark:bg-blue-950 dark:text-blue-200 dark:ring-blue-900',
  PENDING_APPROVAL: 'bg-amber-50 text-amber-800 ring-amber-200 dark:bg-amber-950 dark:text-amber-200 dark:ring-amber-900',
  APPROVED: 'bg-emerald-50 text-emerald-700 ring-emerald-200 dark:bg-emerald-950 dark:text-emerald-200 dark:ring-emerald-900',
  REJECTED: 'bg-rose-50 text-rose-700 ring-rose-200 dark:bg-rose-950 dark:text-rose-200 dark:ring-rose-900',
};

export const DEMO_ACCOUNTS = [
  { role: 'Director', email: 'director@abc.com', password: 'Password@123' },
  { role: 'Accounts', email: 'accounts@abc.com', password: 'Password@123' },
  { role: 'Employee', email: 'ananya.iyer@abc.com', password: 'Password@123' },
];

export function categoryLabel(value) {
  return CATEGORIES.find((item) => item.value === value)?.label || value || '—';
}

export function statusLabel(value) {
  return STATUSES.find((item) => item.value === value)?.label || value || '—';
}
