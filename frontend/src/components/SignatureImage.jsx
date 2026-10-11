import { useEffect, useState } from 'react';
import { api } from '../api/client';

export default function SignatureImage({ fileName, alt = 'Signature', className = 'h-16 max-w-[180px] object-contain' }) {
  const [src, setSrc] = useState(null);

  useEffect(() => {
    if (!fileName) {
      setSrc(null);
      return undefined;
    }
    let active = true;
    let objectUrl;
    api.get(`/api/files/signatures/${fileName}`, { responseType: 'blob' })
      .then((response) => {
        if (!active) return;
        objectUrl = URL.createObjectURL(response.data);
        setSrc(objectUrl);
      })
      .catch(() => {
        if (active) setSrc(null);
      });
    return () => {
      active = false;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [fileName]);

  if (!fileName) return <span className="text-xs text-slate-400">Not signed</span>;
  if (!src) return <span className="text-xs text-slate-400">Loading signature…</span>;
  return <img src={src} alt={alt} className={className} />;
}
