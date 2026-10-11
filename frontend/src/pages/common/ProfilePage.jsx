import { useState } from 'react';
import toast from 'react-hot-toast';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { errorMessage } from '../../utils/format';
import { Button, PageHeader } from '../../components/ui';
import SignatureImage from '../../components/SignatureImage';
import SignatureUpload, { validateSignatureFile } from '../../components/SignatureUpload';

export default function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);

  async function upload() {
    const message = validateSignatureFile(file);
    if (message) {
      toast.error(message);
      return;
    }
    setLoading(true);
    try {
      const body = new FormData();
      body.append('file', file);
      const response = await api.post('/api/users/me/signature', body);
      refreshUser(response.data.data);
      setFile(null);
      toast.success('Signature saved');
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader title="Profile" subtitle="Your identity and saved signature." />
      <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200/80 dark:bg-slate-900 dark:ring-slate-800">
        <dl className="grid gap-4 sm:grid-cols-2">
          <Info label="Name" value={user?.name} />
          <Info label="Email" value={user?.email} />
          <Info label="Role" value={user?.role} />
          <Info label="Employee ID" value={user?.employeeCode || '—'} />
          <Info label="Department" value={user?.department || '—'} />
        </dl>
        <div className="mt-6">
          <p className="text-sm font-medium">Saved signature</p>
          <div className="mt-3 rounded-2xl border border-slate-200 p-4 dark:border-slate-800">
            <SignatureImage fileName={user?.signatureFileName} alt="Saved signature" />
          </div>
        </div>
        <div className="mt-6">
          <SignatureUpload file={file} onFile={setFile} allowSaved={false} savedAvailable={false} usingSaved={false} onUseSaved={() => {}} />
          <Button className="mt-4" loading={loading} disabled={!file} onClick={upload}>Upload signature</Button>
        </div>
      </section>
    </div>
  );
}

function Info({ label, value }) {
  return (
    <div>
      <dt className="text-xs uppercase tracking-wide text-slate-500">{label}</dt>
      <dd className="mt-1 font-medium">{value}</dd>
    </div>
  );
}
