import { useMemo, useState } from "react";
import MainLayout from "../../layouts/MainLayout.jsx";
import Avatar from "../../components/Avatar.jsx";
import Button from "../../components/Button.jsx";

const STORAGE_KEY = "remi-messages";
const contacts = [
  { name: "김지원", role: "백엔드 개발 · 한국(KST)", language: "한국어" },
  { name: "Sarah Lee", role: "디자이너 · 미국(PST)", language: "English" },
  { name: "Alex Kim", role: "리서처 · 미국(PST)", language: "English" },
  { name: "라연헌", role: "PM · 한국(KST)", language: "한국어" },
];

const initialMessages = {
  "김지원": [
    { mine: false, text: "API 명세 정리본을 확인 부탁드려요.", translation: "Please review the API specification." },
    { mine: true, text: "확인 후 오늘 안에 의견 남길게요.", translation: "I'll leave feedback today." },
  ],
  "Sarah Lee": [
    { mine: false, text: "Could you share the design review by tomorrow?", translation: "내일까지 디자인 검토 내용을 공유해 주실 수 있을까요?" },
  ],
  "Alex Kim": [],
  "라연헌": [],
};

function loadMessages() {
  try {
    return JSON.parse(window.localStorage.getItem(STORAGE_KEY)) ?? initialMessages;
  } catch {
    return initialMessages;
  }
}

export default function NotePage() {
  const [active, setActive] = useState(contacts[0]);
  const [draft, setDraft] = useState("");
  const [conversation, setConversation] = useState(loadMessages);
  const messages = useMemo(() => conversation[active.name] ?? [], [conversation, active.name]);

  function send(event) {
    event?.preventDefault();
    const text = draft.trim();
    if (!text) return;
    const updated = {
      ...conversation,
      [active.name]: [...messages, { mine: true, text, translation: active.language === "English" ? "AI 번역 예정" : "원문 메시지" }],
    };
    setConversation(updated);
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
    setDraft("");
  }

  return (
    <MainLayout>
      <div className="mx-auto max-w-5xl">
        <header className="mb-7"><p className="mb-2 text-xs font-semibold tracking-[0.16em] text-muted">MESSAGES</p><h1 className="text-2xl font-bold text-primary">메시지</h1><p className="mt-2 text-sm text-muted">팀원과 프로젝트 맥락을 이어가세요.</p></header>

        <div className="grid min-h-[560px] overflow-hidden rounded-xl border border-border bg-white md:grid-cols-[250px_1fr]">
          <aside className="border-b border-border p-4 md:border-b-0 md:border-r">
            <h2 className="font-semibold text-primary">팀원에게 메시지 보내기</h2>
            <div className="mt-4 flex gap-2 overflow-auto md:block md:space-y-1">
              {contacts.map((contact) => (
                <button key={contact.name} type="button" onClick={() => setActive(contact)} className={`flex min-w-[170px] items-center gap-3 rounded-lg p-3 text-left md:w-full ${active.name === contact.name ? "bg-secondary" : "hover:bg-secondary"}`}>
                  <Avatar name={contact.name} />
                  <span><strong className="block text-sm text-primary">{contact.name}</strong><small className="text-xs text-muted">{contact.role}</small></span>
                </button>
              ))}
            </div>
          </aside>

          <section className="flex min-h-[480px] flex-col">
            <header className="flex items-center gap-3 border-b border-border p-5"><Avatar name={active.name} /><span><strong className="block text-sm text-primary">{active.name}</strong><small className="text-xs text-success">● 온라인</small></span></header>
            <div className="flex-1 space-y-4 bg-secondary/50 p-5">
              {messages.length === 0 ? <p className="pt-16 text-center text-sm text-muted">{active.name}님에게 첫 메시지를 보내보세요.</p> : messages.map((message, index) => (
                <div key={index} className={message.mine ? "ml-auto max-w-sm" : "max-w-sm"}>
                  <div className={`rounded-xl p-3 text-sm ${message.mine ? "rounded-tr-none bg-primary text-white" : "rounded-tl-none bg-white text-primary shadow-sm"}`}>{message.text}</div>
                  {active.language === "English" && <p className={`mt-1 text-xs text-muted ${message.mine ? "text-right" : ""}`}>번역 · {message.translation}</p>}
                </div>
              ))}
            </div>
            <form onSubmit={send} className="border-t border-border p-4">
              <textarea value={draft} onChange={(event) => setDraft(event.target.value)} onKeyDown={(event) => { if (event.key === "Enter" && !event.shiftKey) { event.preventDefault(); send(); } }} placeholder={`${active.name}님에게 메시지 보내기`} className="h-20 w-full resize-none rounded-md border border-border p-3 text-sm outline-none focus:ring-2 focus:ring-active/40" />
              <div className="mt-3 flex items-center justify-between"><span className="text-xs text-muted">Enter 전송 · Shift + Enter 줄바꿈</span><Button type="submit">메시지 보내기</Button></div>
            </form>
          </section>
        </div>
      </div>
    </MainLayout>
  );
}
