import { Link } from 'react-router-dom';
import { FileQuestion } from 'lucide-react';
import { Button } from '../../components/ui';

export default function NotFoundPage() {
  return (
    <div className="grid min-h-screen place-items-center px-4">
      <div className="max-w-md text-center">
        <FileQuestion className="mx-auto text-indigo-500" size={42} />
        <h1 className="mt-4 text-3xl font-semibold">Page not found</h1>
        <p className="mt-2 text-sm text-slate-500">That route is not part of ExpenseFlow.</p>
        <Link to="/dashboard"><Button className="mt-6">Go to dashboard</Button></Link>
      </div>
    </div>
  );
}
