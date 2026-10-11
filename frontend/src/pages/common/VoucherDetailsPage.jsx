import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import toast from 'react-hot-toast';
import { Download, Printer } from 'lucide-react';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { rejectSchema } from '../../validations/voucherSchema';
import { errorMessage, formatDateTime } from '../../utils/format';
import { Button, Modal, PageHeader, fieldClass } from '../../components/ui';
import VoucherDocument from '../../components/VoucherDocument';
import SignatureUpload from '../../components/SignatureUpload';

const STEPS = [
  { key: 'DRAFT', label: 'Draft' },
  { key: 'SUBMITTED', label: 'Submitted' },
  { key: 'PENDING_APPROVAL', label: 'Pending Approval' },
  { key: 'APPROVED', label: 'Approved' },
];

export default function VoucherDetailsPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [voucher, setVoucher] = useState(null);
  const [approveOpen, setApproveOpen] = useState(false);
  const [rejectOpen, setRejectOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [file, setFile] = useState(null);
  const [useSaved, setUseSaved] = useState(false);
  const [busy, setBusy] = useState('');
  const rejectForm = useForm({ resolver: zodResolver(rejectSchema), defaultValues: { reason: '' } });

  function load() {
    return api.get(`/api/vouchers/${id}`).then((response) => setVoucher(response.data.data));
  }

  useEffect(() => {
    load().catch((error) => {
      toast.error(errorMessage(error));
      navigate('/vouchers');
    });
  }, [id]);

  async function submitDraft() {
    setBusy('submit');
    try {
      await api.post(`/api/vouchers/${id}/submit`);
      toast.success('Submitted for approval');
      await load();
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setBusy('');
    }
  }

  async function removeDraft() {
    setBusy('delete');
    try {
      await api.delete(`/api/vouchers/${id}`);
      toast.success('Draft deleted');
      navigate('/vouchers');
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setBusy('');
    }
  }

  async function approve() {
    if (!file && !useSaved) {
      toast.error('Director signature is required before approval');
      return;
    }
    setBusy('approve');
    try {
      const body = new FormData();
      if (file) body.append('signature', file);
      if (useSaved) body.append('useSaved', 'true');
      await api.post(`/api/vouchers/${id}/approve`, body);
      toast.success('Voucher approved');
      setApproveOpen(false);
      await load();
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setBusy('');
    }
  }

  async function reject(values) {
    setBusy('reject');
    try {
      await api.post(`/api/vouchers/${id}/reject`, values);
      toast.success('Voucher rejected');
      setRejectOpen(false);
      await load();
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setBusy('');
    }
  }

  async function downloadPdf() {
    setBusy('pdf');
    try {
      const response = await api.get(`/api/vouchers/${id}/pdf`, { responseType: 'blob' });
      const url = URL.createObjectURL(response.data);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${voucher.voucherNumber}.pdf`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setBusy('');
    }
  }

  if (!voucher) return <p className="text-sm text-slate-500">Loading voucher…</p>;

  const owner = user?.role === 'EMPLOYEE';
  const director = user?.role === 'DIRECTOR';
  const finalStep = voucher.status === 'REJECTED' ? { key: 'REJECTED', label: 'Rejected' } : STEPS[3];
  const reached = new Set((voucher.history || []).map((item) => item.toStatus));
  reached.add(voucher.status);

  return (
    <div>
      <PageHeader
        title={voucher.voucherNumber}
        subtitle={voucher.expenseTitle}
        actions={
          <>
            {owner && voucher.editable && <Link to={`/vouchers/${id}/edit`}><Button variant="secondary">Edit draft</Button></Link>}
            {owner && voucher.submittable && <Button loading={busy === 'submit'} onClick={submitDraft}>Submit</Button>}
            {owner && voucher.deletable && <Button variant="danger" onClick={() => setDeleteOpen(true)}>Delete</Button>}
            {director && voucher.status === 'PENDING_APPROVAL' && <Button onClick={() => setApproveOpen(true)}>Approve</Button>}
            {director && voucher.status === 'PENDING_APPROVAL' && <Button variant="danger" onClick={() => setRejectOpen(true)}>Reject</Button>}
            <Button variant="secondary" onClick={() => window.print()}><Printer size={16} /> Print</Button>
            <Button variant="teal" loading={busy === 'pdf'} onClick={downloadPdf}><Download size={16} /> Download PDF</Button>
          </>
        }
      />
      <div className="no-print mb-6">
        <ol className="grid gap-3 md:grid-cols-4">
          {[STEPS[0], STEPS[1], STEPS[2], finalStep].map((step, index) => {
            const done = reached.has(step.key) || (step.key === 'APPROVED' && voucher.status === 'APPROVED');
            const current = voucher.status === step.key;
            return (
              <li key={step.key} className={`rounded-2xl border px-4 py-3 text-sm ${current ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950/40' : 'border-slate-200 dark:border-slate-800'} ${step.key === 'REJECTED' ? 'border-rose-300 bg-rose-50 dark:bg-rose-950/40' : ''}`}>
                <p className="text-xs text-slate-500">Step {index + 1}</p>
                <p className="font-semibold">{step.label}</p>
                <p className="text-xs text-slate-500">{done ? 'Recorded' : 'Waiting'}</p>
              </li>
            );
          })}
        </ol>
        <ol className="mt-4 space-y-3">
          {(voucher.history || []).map((item) => (
            <li key={item.id} className="rounded-xl bg-white px-4 py-3 text-sm shadow-sm ring-1 ring-slate-200 dark:bg-slate-900 dark:ring-slate-800">
              <span className="font-medium">{item.changedByName}</span> moved this voucher
              {item.fromStatus ? ` from ${item.fromStatus}` : ''} to {item.toStatus}.
              <span className="mt-1 block text-xs text-slate-500">{formatDateTime(item.changedAt)}{item.remarks ? ` · ${item.remarks}` : ''}</span>
            </li>
          ))}
        </ol>
      </div>
      <VoucherDocument voucher={voucher} />
      <Modal open={approveOpen} title="Approve voucher" onClose={() => setApproveOpen(false)}>
        <p className="mb-3 text-sm text-slate-500">Upload your director signature. Employee-entered details stay unchanged.</p>
        <SignatureUpload
          file={file}
          onFile={(next) => { setFile(next); setUseSaved(false); }}
          savedAvailable={Boolean(user?.signatureFileName)}
          usingSaved={useSaved}
          onUseSaved={() => { setUseSaved(true); setFile(null); }}
        />
        <div className="mt-4 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setApproveOpen(false)}>Cancel</Button>
          <Button loading={busy === 'approve'} onClick={approve}>Approve</Button>
        </div>
      </Modal>
      <Modal open={rejectOpen} title="Reject voucher" onClose={() => setRejectOpen(false)}>
        <form onSubmit={rejectForm.handleSubmit(reject)} className="space-y-3">
          <label className="block text-sm font-medium">Rejection reason
            <textarea className={`${fieldClass} mt-1.5`} rows={4} {...rejectForm.register('reason')} />
          </label>
          {rejectForm.formState.errors.reason && <p className="text-xs text-rose-600">{rejectForm.formState.errors.reason.message}</p>}
          <p className="text-xs text-slate-500">{(rejectForm.watch('reason') || '').trim().length} / 10 minimum characters</p>
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setRejectOpen(false)}>Cancel</Button>
            <Button variant="danger" type="submit" loading={busy === 'reject'}>Reject</Button>
          </div>
        </form>
      </Modal>
      <Modal open={deleteOpen} title="Delete this draft?" onClose={() => setDeleteOpen(false)}>
        <p className="text-sm text-slate-500">This permanently removes the draft voucher.</p>
        <div className="mt-4 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setDeleteOpen(false)}>Cancel</Button>
          <Button variant="danger" loading={busy === 'delete'} onClick={removeDraft}>Delete</Button>
        </div>
      </Modal>
    </div>
  );
}
