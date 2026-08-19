import { useEffect, useMemo, useState } from "react";
import MainLayout from "../../layouts/MainLayout.jsx";
import Avatar from "../../components/Avatar.jsx";
import Button from "../../components/Button.jsx";
import { useUser } from "../../contexts/UserContext.jsx";
import { getProjects, getProjectMembers } from "../../api/project.js";
import { getMessages, sendMessage, markMessageAsRead } from "../../api/message.js";
import { memberDisplayName } from "../../utils/member.js";

// /notes 라우트엔 프로젝트 개념이 없지만 메시지 API는 프로젝트 단위라, 첫 번째 프로젝트를 기준으로 연결합니다.
export default function NotePage() {
  const { user } = useUser();

  const [projectId, setProjectId] = useState(null);
  const [contacts, setContacts] = useState([]);
  const [messages, setMessages] = useState([]);
  const [activeId, setActiveId] = useState(null);
  const [draft, setDraft] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sending, setSending] = useState(false);

  useEffect(() => {
    if (!user) return;
    let ignore = false;

    Promise.all([getProjects(), getMessages()])
      .then(async ([projectsRes, messagesRes]) => {
        const firstProject = projectsRes.data[0];
        if (!firstProject) {
          if (!ignore) {
            setContacts([]);
            setMessages(messagesRes.data);
          }
          return;
        }

        const { data: members } = await getProjectMembers(firstProject.id);
        if (ignore) return;

        const others = members.filter((member) => member.memberId !== user.id);
        setProjectId(firstProject.id);
        setContacts(others);
        setMessages(messagesRes.data);
        setActiveId((current) => current ?? others[0]?.memberId ?? null);
      })
      .catch((err) => {
        if (!ignore) setError(err);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [user]);

  const active = useMemo(
    () => contacts.find((contact) => contact.memberId === activeId) ?? null,
    [contacts, activeId],
  );

  const conversation = useMemo(() => {
    if (!activeId || !user) return [];
    return messages
      .filter(
        (message) =>
          (message.senderId === user.id && message.receiverId === activeId) ||
          (message.senderId === activeId && message.receiverId === user.id),
      )
      .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
  }, [messages, activeId, user]);

  useEffect(() => {
    const unread = conversation.filter((message) => !message.isRead && message.receiverId === user?.id);
    if (unread.length === 0) return;

    Promise.all(unread.map((message) => markMessageAsRead(message.id))).then(() => {
      setMessages((prev) =>
        prev.map((message) =>
          unread.some((u) => u.id === message.id) ? { ...message, isRead: true } : message,
        ),
      );
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [conversation]);

  async function send(event) {
    event?.preventDefault();
    const text = draft.trim();
    if (!text || sending || !activeId || !projectId) return;

    setSending(true);
    try {
      const { data } = await sendMessage({ projectId, receiverId: activeId, originalText: text });
      setMessages((prev) => [...prev, data]);
      setDraft("");
    } catch {
      setError(new Error("메시지 전송에 실패했어요."));
    } finally {
      setSending(false);
    }
  }

  if (loading) {
    return (
      <MainLayout>
        <p className="text-sm text-muted">불러오는 중...</p>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="mx-auto max-w-5xl">
        <header className="mb-7"><p className="mb-2 text-xs font-semibold tracking-[0.16em] text-muted">MESSAGES</p><h1 className="text-2xl font-bold text-primary">메시지</h1><p className="mt-2 text-sm text-muted">팀원과 프로젝트 맥락을 이어가세요.</p></header>

        {error && <p className="mb-4 text-sm text-danger">{error.message ?? "문제가 발생했어요."}</p>}

        <div className="grid min-h-[560px] overflow-hidden rounded-xl border border-border bg-white md:grid-cols-[250px_1fr]">
          <aside className="border-b border-border p-4 md:border-b-0 md:border-r">
            <h2 className="font-semibold text-primary">팀원에게 메시지 보내기</h2>
            <div className="mt-4 flex gap-2 overflow-auto md:block md:space-y-1">
              {contacts.map((contact) => (
                <button
                  key={contact.memberId}
                  type="button"
                  onClick={() => setActiveId(contact.memberId)}
                  className={`flex min-w-[170px] items-center gap-3 rounded-lg p-3 text-left md:w-full ${
                    activeId === contact.memberId ? "bg-secondary" : "hover:bg-secondary"
                  }`}
                >
                  <Avatar name={memberDisplayName(contact)} />
                  <span>
                    <strong className="block text-sm text-primary">{memberDisplayName(contact)}</strong>
                    {contact.role && <small className="text-xs text-muted">{contact.role}</small>}
                  </span>
                </button>
              ))}
              {contacts.length === 0 && (
                <p className="p-3 text-sm text-muted">메시지를 보낼 팀원이 없어요.</p>
              )}
            </div>
          </aside>

          <section className="flex min-h-[480px] flex-col">
            {active ? (
              <>
                <header className="flex items-center gap-3 border-b border-border p-5">
                  <Avatar name={memberDisplayName(active)} />
                  <span><strong className="block text-sm text-primary">{memberDisplayName(active)}</strong></span>
                </header>
                <div className="flex-1 space-y-4 bg-secondary/50 p-5">
                  {conversation.length === 0 ? (
                    <p className="pt-16 text-center text-sm text-muted">{memberDisplayName(active)}님에게 첫 메시지를 보내보세요.</p>
                  ) : (
                    conversation.map((message) => {
                      const mine = message.senderId === user.id;
                      const hasTranslation =
                        message.translatedText && message.translatedText !== message.originalText;
                      // 설정 > 프로필의 Cultural Translation 표시 설정을 따른다.
                      // 백엔드가 둘 다 false를 거부하지만, 혹시 몰라 방어적으로 최소 원문은 보여준다.
                      const showOriginal = user.showOriginalText ?? true;
                      const showTranslation = (user.showTranslatedText ?? true) && hasTranslation;
                      return (
                        <div key={message.id} className={mine ? "ml-auto max-w-sm" : "max-w-sm"}>
                          {(showOriginal || !showTranslation) && (
                            <div className={`rounded-xl p-3 text-sm ${mine ? "rounded-tr-none bg-primary text-white" : "rounded-tl-none bg-white text-primary shadow-sm"}`}>
                              {message.originalText}
                            </div>
                          )}
                          {showTranslation && (
                            <p className={`mt-1 text-xs text-muted ${mine ? "text-right" : ""}`}>번역 · {message.translatedText}</p>
                          )}
                        </div>
                      );
                    })
                  )}
                </div>
                <form onSubmit={send} className="border-t border-border p-4">
                  <textarea
                    value={draft}
                    onChange={(event) => setDraft(event.target.value)}
                    onKeyDown={(event) => {
                      if (event.key === "Enter" && !event.shiftKey) {
                        event.preventDefault();
                        send();
                      }
                    }}
                    placeholder={`${memberDisplayName(active)}님에게 메시지 보내기`}
                    disabled={sending}
                    className="h-20 w-full resize-none rounded-md border border-border p-3 text-sm outline-none focus:ring-2 focus:ring-active/40"
                  />
                  <div className="mt-3 flex items-center justify-between">
                    <span className="text-xs text-muted">Enter 전송 · Shift + Enter 줄바꿈</span>
                    <Button type="submit" disabled={sending}>{sending ? "보내는 중..." : "메시지 보내기"}</Button>
                  </div>
                </form>
              </>
            ) : (
              <p className="flex flex-1 items-center justify-center text-sm text-muted">왼쪽에서 대화 상대를 선택하세요.</p>
            )}
          </section>
        </div>
      </div>
    </MainLayout>
  );
}
