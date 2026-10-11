import { Link } from 'react-router-dom';
import { ShieldOff } from 'lucide-react';
import { Button } from '../../components/ui';

export default function ForbiddenPage() {
  return (
    <div className="grid min-h-screen place-items-center px-4">
      <div className="max-w-md text-center">
        <ShieldOff className="mx-auto text-rose-500" size={42} />
        <h1 className="mt-4 text-3xl font-semibold">403</h1>
        <p className="mt-2 text-sm text-slate-500">Your role does not have access to this page.</p>
        <Link to="/dashboard"><Button className="mt-6">Back to dashboard</Button></Link>
      </div>
    </div>
  );
}
