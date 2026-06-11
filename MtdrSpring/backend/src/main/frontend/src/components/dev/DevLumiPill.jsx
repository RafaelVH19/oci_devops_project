import { useEffect, useRef, useState } from 'react';
import { ArrowUp } from 'lucide-react';
import Markdown from 'react-markdown';
import { useOracleUser } from '../../hooks/useOracleUser';

function uid() {
  return `msg-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

async function askLumi(message, history, identity) {
  const endpoints = [import.meta.env.VITE_GENAI_API_URL, '/api/genai/chat'].filter(Boolean);
  for (const endpoint of endpoints) {
    try {
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          message,
          history: history.slice(-10).map((item) => ({ role: item.role, content: item.content })),
          userRole: identity?.role ?? null,
          userName: identity?.userName ?? null,
        }),
      });
      if (!response.ok) continue;
      const data = await response.json();
      const text = data.reply || data.message || data.output || data.text || data.response;
      if (typeof text === 'string' && text.trim()) {
        return { text: text.trim(), workspaceChanged: data.workspaceChanged === true };
      }
    } catch {
      // try next endpoint
    }
  }
  return {
    text: 'I could not reach Lumi right now. Please try again in a moment.',
    workspaceChanged: false,
  };
}

/**
 * Floating Lumi pill for the developer view.
 * Collapsed: small pill with the Lumi logo centered.
 * Hover: expands into a composer. Sending shows a "thinking" shimmer,
 * then the pill extends upward with the conversation. Click outside collapses it.
 */
export default function DevLumiPill({ onWorkspaceChanged }) {
  const { displayName, role } = useOracleUser();
  const rootRef = useRef(null);
  const inputRef = useRef(null);
  const messagesEndRef = useRef(null);

  const [expanded, setExpanded] = useState(false);
  const [panelOpen, setPanelOpen] = useState(false);
  const [draft, setDraft] = useState('');
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState([]);

  // Collapse back to the small pill when clicking outside.
  useEffect(() => {
    function handlePointerDown(event) {
      if (rootRef.current && !rootRef.current.contains(event.target)) {
        setPanelOpen(false);
        setExpanded(false);
      }
    }
    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        setPanelOpen(false);
        setExpanded(false);
      }
    }
    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, []);

  useEffect(() => {
    if (panelOpen) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }
  }, [messages, loading, panelOpen]);

  const openComposer = () => {
    setExpanded(true);
    if (messages.length > 0) setPanelOpen(true);
  };

  const handleMouseLeave = () => {
    if (loading || panelOpen || draft.trim()) return;
    setExpanded(false);
  };

  const sendMessage = async () => {
    const text = draft.trim();
    if (!text || loading) return;

    const userMessage = { id: uid(), role: 'user', content: text };
    const nextMessages = [...messages, userMessage];
    setMessages(nextMessages);
    setDraft('');
    setLoading(true);

    try {
      const { text: reply, workspaceChanged } = await askLumi(text, nextMessages, {
        role,
        userName: displayName,
      });
      setMessages((prev) => [...prev, { id: uid(), role: 'assistant', content: reply }]);
      setPanelOpen(true);
      if (workspaceChanged) {
        onWorkspaceChanged?.();
        window.dispatchEvent(new CustomEvent('lumen:workspace-changed'));
      }
    } finally {
      setLoading(false);
      inputRef.current?.focus();
    }
  };

  return (
    <div
      ref={rootRef}
      className="fixed bottom-5 left-1/2 z-[140] flex -translate-x-1/2 flex-col items-center"
    >
      {panelOpen && messages.length > 0 && (
        <div className="dev-lumi-panel-enter mb-2 w-[min(28rem,calc(100vw-2rem))] overflow-hidden rounded-2xl border border-[#2A1814]/10 bg-white shadow-[0_18px_50px_-20px_rgba(42,24,20,0.45)]">
          <div className="max-h-[45vh] overflow-y-auto px-4 py-4">
            <div className="flex flex-col gap-3">
              {messages.map((message) =>
                message.role === 'user' ? (
                  <div
                    key={message.id}
                    className="lumi-message-enter ml-auto max-w-[80%] rounded-2xl bg-[#f2f2f2] px-4 py-2 text-sm text-[#2A1814]"
                  >
                    {message.content}
                  </div>
                ) : (
                  <div
                    key={message.id}
                    className="lumi-message-enter flex max-w-[90%] items-start gap-2 text-sm leading-relaxed text-[#2A1814]"
                  >
                    <img src="/lumi.svg" alt="" aria-hidden className="mt-0.5 h-5 w-5 shrink-0" />
                    <div className="lumi-markdown min-w-0">
                      <Markdown>{message.content}</Markdown>
                    </div>
                  </div>
                )
              )}
              {loading && (
                <div className="flex items-center gap-2 text-sm">
                  <img src="/lumi.svg" alt="" aria-hidden className="h-5 w-5 shrink-0" />
                  <span className="dev-lumi-thinking-text">Lumi is thinking…</span>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>
          </div>
        </div>
      )}

      <div
        onMouseEnter={openComposer}
        onMouseLeave={handleMouseLeave}
        onClick={openComposer}
        className={`dev-lumi-pill flex items-center overflow-hidden rounded-full border border-[#2A1814]/10 bg-white shadow-[0_12px_36px_-14px_rgba(42,24,20,0.5)] ${
          expanded ? 'dev-lumi-pill--open' : 'dev-lumi-pill--closed'
        }`}
      >
        <img
          src="/lumi.svg"
          alt="Lumi"
          aria-hidden
          className={`h-7 w-7 shrink-0 transition-all duration-300 ${
            expanded ? 'ml-2.5' : 'mx-auto'
          }`}
        />

        <div
          className={`flex min-w-0 items-center gap-2 transition-opacity duration-200 ${
            expanded
              ? 'flex-1 pr-1.5 opacity-100'
              : 'pointer-events-none w-0 flex-none overflow-hidden opacity-0'
          }`}
        >
          {loading && !panelOpen ? (
            <span className="dev-lumi-thinking-text ml-2 whitespace-nowrap text-sm">
              Lumi is thinking…
            </span>
          ) : (
            <>
              <input
                ref={inputRef}
                value={draft}
                onChange={(event) => setDraft(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter' && !event.shiftKey) {
                    event.preventDefault();
                    sendMessage();
                  }
                }}
                placeholder="Ask Lumi"
                aria-label="Ask Lumi"
                className="ml-2 w-full bg-transparent text-sm text-[#2A1814] placeholder:text-[#6B6560] focus:outline-none"
              />
              <button
                type="button"
                onClick={sendMessage}
                disabled={!draft.trim() || loading}
                aria-label="Send message"
                className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#2A1814] text-white transition hover:bg-[#1d110e] disabled:opacity-40"
              >
                <ArrowUp className="h-4 w-4" strokeWidth={2.5} />
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
