import { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, X } from 'lucide-react';

const STORAGE_KEY = 'dev-lumi-promo-dismissed';
const EXIT_MS = 220;

export default function DevLumiPromoToast() {
  const [open, setOpen] = useState(false);
  const [exiting, setExiting] = useState(false);
  const exitTimerRef = useRef(null);

  useEffect(() => {
    if (sessionStorage.getItem(STORAGE_KEY)) return undefined;
    const showTimer = window.setTimeout(() => setOpen(true), 400);
    return () => window.clearTimeout(showTimer);
  }, []);

  const clearExitTimer = useCallback(() => {
    if (exitTimerRef.current) {
      window.clearTimeout(exitTimerRef.current);
      exitTimerRef.current = null;
    }
  }, []);

  const dismiss = useCallback(() => {
    if (!open || exiting) return;
    setExiting(true);
    sessionStorage.setItem(STORAGE_KEY, '1');
    clearExitTimer();
    exitTimerRef.current = window.setTimeout(() => {
      setOpen(false);
      setExiting(false);
    }, EXIT_MS);
  }, [open, exiting, clearExitTimer]);

  useEffect(() => () => clearExitTimer(), [clearExitTimer]);

  if (!open && !exiting) return null;

  return (
    <div
      className={`pointer-events-none fixed right-4 top-4 z-[130] flex w-[min(40rem,calc(100vw-2rem))] justify-end sm:right-6 sm:top-6 ${
        exiting ? 'app-toast-exit' : 'app-toast-enter'
      }`}
      role="complementary"
      aria-label="Lumi assistant suggestion"
    >
      <div className="pointer-events-auto w-full rounded-xl border border-[#2A1814]/10 bg-white shadow-[0_12px_40px_-16px_rgba(42,24,20,0.35)]">
        <div className="relative flex items-center gap-3 py-3 pl-3.5 pr-10 sm:gap-4 sm:py-3.5 sm:pl-4 sm:pr-11">
          <button
            type="button"
            onClick={dismiss}
            className="absolute right-2 top-1/2 -translate-y-1/2 rounded-full p-1.5 text-[#6B6560] transition hover:bg-[#2A1814]/[0.04] hover:text-[#2A1814] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#c74634]"
            aria-label="Dismiss"
          >
            <X className="h-4 w-4" />
          </button>

          <img
            src="/lumi.svg"
            alt=""
            className="h-10 w-10 shrink-0 self-center sm:h-11 sm:w-11"
            aria-hidden
          />

          <div className="flex min-w-0 flex-1 items-center gap-3 border-l-2 border-[#c74634]/40 pl-3 sm:gap-4 sm:pl-4">
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold leading-snug tracking-tight text-[#2A1814] sm:text-[0.9375rem]">
                Need a hand with your sprint?
              </p>
              <p className="mt-0.5 text-xs leading-snug text-[#6B6560] sm:text-sm">
                Break down work, triage bugs, or summarize your backlog—in plain language.
              </p>
            </div>

            <Link
              to="/lumi"
              onClick={dismiss}
              className="inline-flex shrink-0 items-center justify-center gap-1.5 rounded-full border border-[#2A1814]/15 px-4 py-2 text-xs font-medium text-[#2A1814] transition hover:border-[#c74634]/35 hover:bg-[#c74634]/[0.06] hover:text-[#c74634] sm:gap-2 sm:px-5 sm:text-sm"
            >
              <img src="/lumi-ico-b.svg" alt="" className="h-3.5 w-3.5 shrink-0 sm:h-4 sm:w-4" aria-hidden />
              Open Lumi
              <ArrowRight className="h-3.5 w-3.5 text-[#c74634] sm:h-4 sm:w-4" aria-hidden />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
