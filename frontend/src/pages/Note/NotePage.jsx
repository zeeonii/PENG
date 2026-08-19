import { useEffect, useMemo, useState } from "react";
import MainLayout from "../../layouts/MainLayout.jsx";
import Avatar from "../../components/Avatar.jsx";
import Button from "../../components/Button.jsx";
import { useUser } from "../../contexts/UserContext.jsx";
import { getProjects, getProjectMembers } from "../../api/project.js";
import { getMessages, sendMessage, markMessageAsRead } from "../../api/message.js";
import { memberDisplayName } from "../../utils/member.js";

export default function NotePage() {
  const { user } = useUser();

  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState(null);
  const [contacts, setContacts] = useState([]);
  const [messages, setMessages] = useState([]);
  const [activeId, setActiveId] = useState(null);
  const [draft, setDraft] = useState("");
  const [loading, setLoading] = useState(true);
  const [contactsProjectId, setContactsProjectId] = useState(null);
  const contactsLoading = contactsProjectId !== selectedProjectId;
  const [error, setError] = useState(null);
  const [sending, setSending] = useState(false);

  // 프로젝트 목록 + 내 전체 메시지 로드
  useEffect(() => {
    if (!user) return;
    let ignore = false;

    Promise.all([getProjects(), getMessages()])
      .then(([projectsRes, messagesRes]) => {
        if (ignore) return;
        setProjects(projectsRes.data);
        setMessages(messagesRes.data);
        setSelectedProjectId((current) => current ?? projectsRes.data[0]?.id ?? null);
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

  // 선택된 프로젝트의 팀원(연락 가능한 사람) 로드
  useEffect(() => {
    if (!selectedProjectId || !user) return;
    let ignore = false;

    getProjectMembers(selectedProjectId)
      .then(({ data }) => {
        if (ignore) return;
        const others = data.filter((member) => member.memberId !== user.id);
        setContacts(others);
        setActiveId(others[0]?.memberId ?? null);
        setContactsProjectId(selectedProjectId);
      })
      .catch(() => {
        if (!ignore) {
          setContacts([]);
          setActiveId(null);
          setContactsProjectId(selectedProjectId);
        }
      });

    return () => {
      ignore = true;
    };
  }, [selectedProjectId, user]);

  const active = useMemo(
    () => contacts.find((contact) => contact.memberId === activeId) ?? null,
    [contacts, activeId],
  );

  const conversation = useMemo(() => {
    if (!activeId || !user || !selectedProjectId) return [];
    return messages
      .filter(
        (message) =>
          message.projectId === selectedProjectId &&
          ((message.senderId === user.id && message.receiverId === activeId) ||
            (message.senderId === activeId && message.receiverId === user.id)),
      )
      .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
  }, [messages, activeId, user, selectedProjectId]);

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
    if (!text || sending || !activeId || !selectedProjectId) return;

    setSending(true);
    try {
      const { data } = await sendMessage({ projectId: selectedProjectId, receiverId: activeId, originalText: text });
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
      <div className="mx-auto max-w-6xl">
        <header className="mb-7"><p className="mb-2 text-xs font-semibold tracking-[0.16em] text-muted">MESSAGES</p><h1 className="text-2xl font-bold text-primary">메시지</h1><p className="mt-2 text-sm text-muted">프로젝트별로 팀원과 맥락을 이어가세요.</p></header>

        {error && <p className="mb-4 text-sm text-danger">{error.message ?? "문제가 발생했어요."}</p>}

        <div className="flex flex-col gap-6 md:flex-row">
          <nav className="flex shrink-0 gap-2 overflow-auto md:w-48 md:flex-col md:overflow-visible">
            {projects.map((project) => {
              const isActive = project.id === selectedProjectId;
              return (
                <button
                  key={project.id}
                  type="button"
                  onClick={() => setSelectedProjectId(project.id)}
                  className={`min-w-[140px] border-l-2 px-3 py-1.5 text-left text-sm md:min-w-0 ${
                    isActive
                      ? "border-primary font-semibold text-primary"
                      : "border-transparent text-muted hover:text-primary"
                  }`}
                >
                  {project.name}
                </button>
              );
            })}
            {projects.length === 0 && (
              <p className="px-3 text-sm text-muted">참여 중인 프로젝트가 없어요.</p>
            )}
          </nav>

          <div className="min-w-0 flex-1">
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
                  {!contactsLoading && contacts.length === 0 && (
                    <p className="p-3 text-sm text-muted">이 프로젝트엔 메시지를 보낼 팀원이 없어요.</p>
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
                          const showTranslation =
                            message.translatedText && message.translatedText !== message.originalText;
                          return (
                            <div key={message.id} className={mine ? "ml-auto max-w-sm" : "max-w-sm"}>
                              <div className={`rounded-xl p-3 text-sm ${mine ? "rounded-tr-none bg-primary text-white" : "rounded-tl-none bg-white text-primary shadow-sm"}`}>
                                {message.originalText}
                              </div>
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
        </div>
      </div>
    </MainLayout>
  );
}
