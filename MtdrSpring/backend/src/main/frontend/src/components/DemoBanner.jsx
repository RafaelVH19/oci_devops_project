import { Link } from 'react-router-dom';

export default function DemoBanner() {
  return (
    <div className="fixed inset-x-0 top-0 z-[100] border-b border-[#c74634]/20 bg-[#fff8f6] px-4 py-2 text-center text-sm text-[#2A1814] shadow-sm">
      <span className="font-medium text-[#c74634]">Demo preview</span>
      <span className="mx-2 text-[#6B6560]">·</span>
      Sample data only — no backend required.
      <span className="mx-2 hidden text-[#6B6560] sm:inline">·</span>
      <Link to="/app" className="font-medium text-[#c74634] hover:underline">
        Dev view
      </Link>
      <span className="mx-1 text-[#6B6560]">|</span>
      <Link to="/dashboard" className="font-medium text-[#c74634] hover:underline">
        Dashboard
      </Link>
    </div>
  );
}
