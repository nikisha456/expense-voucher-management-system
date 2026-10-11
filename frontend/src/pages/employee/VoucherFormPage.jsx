import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import toast from 'react-hot-toast';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { voucherSchema } from '../../validations/voucherSchema';
import { CATEGORIES, DEPARTMENTS } from '../../utils/constants';
import { amountInWords } from '../../utils/amountInWords';
import { errorMessage, fieldErrors, todayInputValue } from '../../utils/format';
import { Button, Field, Modal, PageHeader, fieldClass } from '../../components/ui';
import SignatureUpload, { validateSignatureFile } from '../../components/SignatureUpload';

export default function VoucherFormPage() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const { user } = useAuth();
  const [file, setFile] = useState(null);
  const [useSaved, setUseSaved] = useState(false);
  const [existingSignature, setExistingSignature] = useState(null);
  const [busy, setBusy] = useState('');
  const [confirmSubmit, setConfirmSubmit] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const form = useForm({
    resolver: zodResolver(voucherSchema),
    defaultValues: {
      voucherDate: todayInputValue(),
      expenseDate: todayInputValue(),
      departmentName: user?.department || '',
      expenseTitle: '',
      expenseCategory: '',
      expenseDescription: '',
      amount: '',
    },
  });

  useEffect(() => {
    if (!editing) return;
    api.get(`/api/vouchers/${id}`).then((response) => {
      const voucher = response.data.data;
      if (voucher.status !== 'DRAFT') {
        toast.error('Only draft vouchers can be edited');
        navigate(`/vouchers/${id}`);
        return;
      }
      form.reset({
        voucherDate: voucher.voucherDate,
        expenseDate: voucher.expenseDate,
        departmentName: voucher.departmentName,
        expenseTitle: voucher.expenseTitle,
        expenseCategory: voucher.expenseCategory,
        expenseDescription: voucher.expenseDescription || '',
        amount: String(voucher.amount),
      });
      setExistingSignature(voucher.employeeSignatureFileName);
    }).catch((error) => {
      toast.error(errorMessage(error));
      navigate('/vouchers');
    });
  }, [editing, id]);

  const amount = form.watch('amount');

  async function persist() {
    const values = form.getValues();
    const payload = {
      ...values,
      amount: Number(values.amount),
      expenseDescription: values.expenseDescription || null,
    };
    if (editing) {
      const response = await api.put(`/api/vouchers/${id}`, payload);
      return response.data.data;
    }
    const response = await api.post('/api/vouchers', payload);
    return response.data.data;
  }

  async function attachSignature(voucherId) {
    if (file) {
      const message = validateSignatureFile(file);
      if (message) throw new Error(message);
      const body = new FormData();
      body.append('file', file);
      await api.post(`/api/vouchers/${voucherId}/signature`, body);
      return;
    }
    if (useSaved) {
      await api.post(`/api/vouchers/${voucherId}/signature/use-profile`);
    }
  }

  async function onSave() {
    const valid = await form.trigger();
    if (!valid) return;
    setBusy('save');
    try {
      const voucher = await persist();
      if (file || useSaved) await attachSignature(voucher.id);
      toast.success('Draft saved');
      navigate(`/vouchers/${voucher.id}`);
    } catch (error) {
      applyErrors(error);
    } finally {
      setBusy('');
    }
  }

  async function onSubmit() {
    const valid = await form.trigger();
    if (!valid) return;
    if (!file && !useSaved && !existingSignature) {
      toast.error('Employee signature is required before submission');
      return;
    }
    setBusy('submit');
    try {
      const voucher = await persist();
      if (file || useSaved) await attachSignature(voucher.id);
      await api.post(`/api/vouchers/${voucher.id}/submit`);
      toast.success('Submitted for approval');
      navigate(`/vouchers/${voucher.id}`);
    } catch (error) {
      applyErrors(error);
    } finally {
      setBusy('');
      setConfirmSubmit(false);
    }
  }

  async function onDelete() {
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

  function applyErrors(error) {
    const fields = fieldErrors(error);
    Object.entries(fields).forEach(([key, message]) => form.setError(key, { message }));
    if (!Object.keys(fields).length) toast.error(errorMessage(error));
  }

  return (
    <div>
      <PageHeader title={editing ? 'Edit draft voucher' : 'Create voucher'} subtitle="Save a draft or submit it once the signature is attached." />
      <form className="grid gap-6 xl:grid-cols-[1.4fr_0.8fr]" onSubmit={(event) => event.preventDefault()}>
        <section className="space-y-5 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
          <h2 className="font-semibold">Basic information</h2>
          <div className="grid gap-4 md:grid-cols-2">
            <Field label="Voucher date" error={form.formState.errors.voucherDate?.message}>
              <input type="date" className={fieldClass} {...form.register('voucherDate')} />
            </Field>
            <Field label="Expense date" error={form.formState.errors.expenseDate?.message}>
              <input type="date" className={fieldClass} {...form.register('expenseDate')} />
            </Field>
            <Field label="Department" error={form.formState.errors.departmentName?.message}>
              <select className={fieldClass} {...form.register('departmentName')}>
                <option value="">Select department</option>
                {DEPARTMENTS.map((department) => <option key={department}>{department}</option>)}
              </select>
            </Field>
            <Field label="Expense category" error={form.formState.errors.expenseCategory?.message}>
              <select className={fieldClass} {...form.register('expenseCategory')}>
                <option value="">Select category</option>
                {CATEGORIES.map((category) => <option key={category.value} value={category.value}>{category.label}</option>)}
              </select>
            </Field>
            <Field label="Expense title" error={form.formState.errors.expenseTitle?.message}>
              <input className={fieldClass} {...form.register('expenseTitle')} />
            </Field>
            <Field label="Amount (INR)" error={form.formState.errors.amount?.message}>
              <input className={fieldClass} inputMode="decimal" {...form.register('amount')} />
            </Field>
          </div>
          {amount && <p className="text-sm text-slate-500">{amountInWords(amount)}</p>}
          <Field label="Description" error={form.formState.errors.expenseDescription?.message}>
            <textarea className={fieldClass} rows={4} {...form.register('expenseDescription')} />
          </Field>
          <div className="rounded-2xl bg-slate-50 p-4 text-sm dark:bg-slate-950">
            <p className="font-medium">Employee</p>
            <p className="mt-1 text-slate-500">{user?.name} · {user?.employeeCode || 'No employee ID'} · {user?.email}</p>
          </div>
        </section>
        <section className="space-y-4 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
          <h2 className="font-semibold">Employee signature</h2>
          <p className="text-sm text-slate-500">A draft can be saved without a signature. Submission requires one.</p>
          {existingSignature && !file && !useSaved && <p className="text-sm text-emerald-600">A signature is already attached to this draft.</p>}
          <SignatureUpload
            file={file}
            onFile={(next) => { setFile(next); setUseSaved(false); }}
            savedAvailable={Boolean(user?.signatureFileName)}
            usingSaved={useSaved}
            onUseSaved={() => { setUseSaved(true); setFile(null); toast.success('Saved signature selected'); }}
          />
          <div className="flex flex-wrap gap-2 pt-2">
            <Button variant="secondary" loading={busy === 'save'} onClick={onSave}>Save as Draft</Button>
            <Button loading={busy === 'submit'} onClick={() => setConfirmSubmit(true)}>Submit for Approval</Button>
            {editing && <Button variant="danger" onClick={() => setConfirmDelete(true)}>Delete draft</Button>}
          </div>
        </section>
      </form>
      <Modal open={confirmSubmit} title="Submit this voucher?" onClose={() => setConfirmSubmit(false)}>
        <p className="text-sm text-slate-500">After submission the voucher becomes read-only and moves to pending approval.</p>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setConfirmSubmit(false)}>Cancel</Button>
          <Button loading={busy === 'submit'} onClick={onSubmit}>Submit</Button>
        </div>
      </Modal>
      <Modal open={confirmDelete} title="Delete this draft?" onClose={() => setConfirmDelete(false)}>
        <p className="text-sm text-slate-500">This permanently removes the draft voucher.</p>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setConfirmDelete(false)}>Cancel</Button>
          <Button variant="danger" loading={busy === 'delete'} onClick={onDelete}>Delete</Button>
        </div>
      </Modal>
    </div>
  );
}
