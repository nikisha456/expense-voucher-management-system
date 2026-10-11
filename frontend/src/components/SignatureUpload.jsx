import { useEffect, useState } from 'react';
import { Upload } from 'lucide-react';
import { Button } from './ui';

const MAX_BYTES = 2 * 1024 * 1024;

export function validateSignatureFile(file) {
  if (!file) return 'Signature file is required';
  const name = file.name.toLowerCase();
  if (!name.endsWith('.png') && !name.endsWith('.jpg') && !name.endsWith('.jpeg')) {
    return 'Signature must be a PNG or JPG image';
  }
  if (file.size > MAX_BYTES) return 'Signature file must be 2 MB or smaller';
  return '';
}

export default function SignatureUpload({ file, onFile, savedAvailable, onUseSaved, usingSaved, disabled, allowSaved = true }) {
  const [preview, setPreview] = useState('');
  const [dragOver, setDragOver] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!file) {
      setPreview('');
      return undefined;
    }
    const url = URL.createObjectURL(file);
    setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  function accept(nextFile) {
    const message = validateSignatureFile(nextFile);
    setError(message);
    if (!message) onFile(nextFile);
  }

  return (
    <div className="space-y-3">
      <div
        onDragOver={(event) => {
          event.preventDefault();
          setDragOver(true);
        }}
        onDragLeave={() => setDragOver(false)}
        onDrop={(event) => {
          event.preventDefault();
          setDragOver(false);
          const next = event.dataTransfer.files?.[0];
          if (next) accept(next);
        }}
        className={`rounded-2xl border border-dashed px-4 py-6 text-center transition ${dragOver ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950/40' : 'border-slate-300 dark:border-slate-700'}`}
      >
        {preview && !usingSaved ? (
          <img src={preview} alt="Signature preview" className="mx-auto mb-3 h-16 object-contain" />
        ) : (
          <Upload className="mx-auto mb-2 text-indigo-500" size={22} />
        )}
        <p className="text-sm font-medium">{usingSaved ? 'Saved signature will be used' : 'Drag and drop a signature image'}</p>
        <p className="mt-1 text-xs text-slate-500">PNG or JPG, up to 2 MB</p>
        <label className="mt-3 inline-flex cursor-pointer rounded-xl bg-slate-100 px-3 py-2 text-xs font-semibold text-slate-700 dark:bg-slate-800 dark:text-slate-200">
          Browse file
          <input
            className="sr-only"
            type="file"
            accept="image/png,image/jpeg"
            disabled={disabled}
            onChange={(event) => {
              const next = event.target.files?.[0];
              if (next) accept(next);
            }}
          />
        </label>
      </div>
      {error && <p className="text-xs text-rose-600" role="alert">{error}</p>}
      {allowSaved && (
        <>
          <Button variant="secondary" disabled={disabled || !savedAvailable} onClick={onUseSaved}>
            Use saved signature
          </Button>
          {!savedAvailable && <p className="text-xs text-slate-500">Upload a signature on your profile to reuse it.</p>}
        </>
      )}
    </div>
  );
}
