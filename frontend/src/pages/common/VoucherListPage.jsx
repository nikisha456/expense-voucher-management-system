import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { useDebouncedValue } from '../../hooks/useDebounce';
import VoucherFilters from '../../components/VoucherFilters';
import VoucherTable from '../../components/VoucherTable';
import { PageHeader } from '../../components/ui';
import { errorMessage } from '../../utils/format';
import toast from 'react-hot-toast';

const EMPTY = {
  search: '',
  voucherNumber: '',
  employeeName: '',
  department: '',
  category: '',
  status: '',
  fromDate: '',
  toDate: '',
  minAmount: '',
  maxAmount: '',
};

export default function VoucherListPage({ mode = 'role' }) {
  const { user } = useAuth();
  const [params, setParams] = useSearchParams();
  const [rows, setRows] = useState([]);
  const [meta, setMeta] = useState(null);
  const [loading, setLoading] = useState(true);
  const pending = mode === 'pending';
  const endpoint = pending ? '/api/vouchers/pending' : user?.role === 'EMPLOYEE' ? '/api/vouchers/my' : '/api/vouchers';
  const title = pending ? 'Pending Approvals' : user?.role === 'EMPLOYEE' ? 'My Vouchers' : 'All Vouchers';

  const filters = {
    ...EMPTY,
    search: params.get('search') || '',
    voucherNumber: params.get('voucherNumber') || '',
    employeeName: params.get('employeeName') || '',
    department: params.get('department') || '',
    category: params.get('category') || '',
    status: pending ? 'PENDING_APPROVAL' : (params.get('status') || ''),
    fromDate: params.get('fromDate') || '',
    toDate: params.get('toDate') || '',
    minAmount: params.get('minAmount') || '',
    maxAmount: params.get('maxAmount') || '',
  };
  const [searchInput, setSearchInput] = useState(filters.search);
  const debouncedSearch = useDebouncedValue(searchInput, 400);
  const sortBy = params.get('sortBy') || 'createdAt';
  const sortDir = params.get('sortDir') || 'desc';
  const page = Number(params.get('page') || 0);

  useEffect(() => {
    setSearchInput(params.get('search') || '');
  }, [params]);

  useEffect(() => {
    const current = params.get('search') || '';
    if (debouncedSearch === current) return;
    const next = new URLSearchParams(params);
    if (debouncedSearch) next.set('search', debouncedSearch);
    else next.delete('search');
    next.set('page', '0');
    setParams(next, { replace: true });
  }, [debouncedSearch]);

  useEffect(() => {
    const query = Object.fromEntries([...params.entries()].filter(([, value]) => value !== ''));
    query.page = page;
    query.size = 10;
    query.sortBy = sortBy;
    query.sortDir = sortDir;
    if (pending) query.status = 'PENDING_APPROVAL';
    setLoading(true);
    api.get(endpoint, { params: query })
      .then((response) => {
        setRows(response.data.data || []);
        setMeta(response.data.meta);
      })
      .catch((error) => toast.error(errorMessage(error)))
      .finally(() => setLoading(false));
  }, [params, endpoint, pending, page, sortBy, sortDir]);

  function update(key, value) {
    if (key === 'search') {
      setSearchInput(value);
      return;
    }
    const next = new URLSearchParams(params);
    if (value) next.set(key, value);
    else next.delete(key);
    next.set('page', '0');
    setParams(next);
  }

  function sort(column) {
    const next = new URLSearchParams(params);
    const direction = sortBy === column && sortDir === 'asc' ? 'desc' : 'asc';
    next.set('sortBy', column);
    next.set('sortDir', direction);
    setParams(next);
  }

  return (
    <div>
      <PageHeader title={title} subtitle="Search, filter, and sort the voucher register." />
      <VoucherFilters
        filters={{ ...filters, search: searchInput }}
        hideStatus={pending}
        onChange={update}
        onClear={() => setParams(new URLSearchParams())}
      />
      <VoucherTable
        rows={rows}
        loading={loading}
        sortBy={sortBy}
        sortDir={sortDir}
        onSort={sort}
        meta={meta}
        page={page}
        onPage={(nextPage) => {
          const next = new URLSearchParams(params);
          next.set('page', String(nextPage));
          setParams(next);
        }}
      />
    </div>
  );
}
