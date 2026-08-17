import { useEffect, useMemo, useState } from "react";
import MainLayout from "../../layouts/MainLayout.jsx";
import Avatar from "../../components/Avatar.jsx";
import Button from "../../components/Button.jsx";
import { getProjects, getProjectMembers } from "../../api/project.js";
import { getMessages, markMessageAsRead, sendMessage } from "../../api/message.js";
import { useUser } from "../../contexts/UserContext.jsx";

/**
 * ⚠️ 이 화면의 라우트(/notes)에는 프로젝트 정보가 없는데, 쪽지 전송 API는
 * projectId가 필수입니다. 임시로 내가 참여한 첫 번째 프로젝트의 팀원을
 * 연락처 목록으로 사용합니다. (근본적으로는 프로젝트 선택 UI가 필요해 보입니다)
 *
 * 원문/번역문 동시 표시는 프로필 설정과 연결되어야 하나 아직 저장 API가 없어,
 * translatedText가 있으면 항상 같이 보여주는 방식으로 두었습니다.
 */
export default function NotePage() {
  const { user } = useUser();
  const [projectId, setProjectId] = useState(null);
  const [contacts, setContacts] = useState([]);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [activeContact, setActiveContact] = useState(null);
  const [draft, setDraft] = useState("");
  const [sending, setSending] = useState(false);

  useEffect(() => {
    let ignore = false;

    getProjects()
      .then(({ data: projects }) => {
        const firstProject = projects[0];
        if (!firstProject) return null;
        setProjectId(firstProject.id);
        return Promise.all([getProjectMembers(firstProject.id), getMessages()]);
      })
      .then((result) => {
        if (ignore || !result) return;
        const [membersRes, messagesRes] = result;
        const others = membersRes.data.filter((member) => member.memberId !== user?.id);
        setContacts(others);
        setActiveContact(others[0] ?? null);
        setMessages(messagesRes.data);
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
  }, [user?.id]);

  const conversation = useMemo(() => {
    if (!activeContact) return [];
    return messages.filter(
      (message) =>
        (message.senderId === activeContact.memberId && message.receiverId === user?.id) ||
        (message.receiverId === activeContact.memberId && message.senderId === user?.id),
    );
  }, [messages, activeContact, user?.id]);

  useEffect(() => {
    const unread = conversation.filter(
      (message) => !message.isRead && message.receiverId === user?.id,
    );
    unread.forEach((message) => {
      markMessageAsRead(message.id).then(() => {
        setMessages((prev) =>
          prev.map((m) => (m.id === message.id ? { ...m, isRead: true } : m)),
        );
      });
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps -- 대화 전환 시 한 번만 읽음 처리
  }, [activeContact]);

  const send = async (event) => {
    event?.preventDefault();
    const text = draft.trim();
    if (!text || !activeContact || !projectId || sending) return;

    setSending(true);
    try {
      const { data } = await sendMessage({
        projectId,
        receiverId: activeContact.memberId,
        originalText: text,
      });
      setMessages((prev) => [...prev, data]);
      setDraft("");
    } catch {
      setError(new Error("메시지를 보내지 못했어요."));
    } finally {
      setSending(false);
    }
  };

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
        <header className="mb-7">
          <p className="mb-2 text-xs font-semibold tracking-[0.16em] text-muted">MESSAGES</p>
          <h1 className="text-2xl font-bold text-primary">메시지</h1>
          <p className="mt-2 text-sm text-muted">팀원과 프로젝트 맥락을 이어가세요.</p>
        </header>

        {error && <p className="mb-4 text-sm text-danger">{error.message}</p>}

        {contacts.length === 0 ? (
          <p className="rounded-xl border border-dashed border-border px-5 py-16 text-center text-sm text-muted">
            같이 대화할 팀원이 없어요.
          </p>
        ) : (
          <div className="grid min-h-[560px] overflow-hidden rounded-xl border border-border bg-white md:grid-cols-[250px_1fr]">
            <aside className="border-b border-border p-4 md:border-b-0 md:border-r">
              <h2 className="font-semibold text-primary">팀원에게 메시지 보내기</h2>
              <div className="mt-4 flex gap-2 overflow-auto md:block md:space-y-1">
                {contacts.map((contact) => (
                  <button
                    key={contact.memberId}
                    type="button"
                    onClick={() => setActiveContact(contact)}
                    className={`flex min-w-[170px] items-center gap-3 rounded-lg p-3 text-left md:w-full ${
                      activeContact?.memberId === contact.memberId ? "bg-secondary" : "hover:bg-secondary"
                    }`}
                  >
                    <Avatar name={contact.name} />
                    <span>
                      <strong className="block text-sm text-primary">{contact.name}</strong>
                      <small className="text-xs text-muted">
                        {[contact.role, contact.country].filter(Boolean).join(" · ")}
                      </small>
                    </span>
                  </button>
                ))}
              </div>
            </aside>

            <section className="flex min-h-[480px] flex-col">
              <header className="flex items-center gap-3 border-b border-border p-5">
                <Avatar name={activeContact?.name} />
                <span>
                  <strong className="block text-sm text-primary">{activeContact?.name}</strong>
                </span>
              </header>
              <div className="flex-1 space-y-4 bg-secondary/50 p-5">
                {conversation.length === 0 ? (
                  <p className="pt-16 text-center text-sm text-muted">
                    {activeContact?.name}님에게 첫 메시지를 보내보세요.
                  </p>
                ) : (
                  conversation.map((message) => {
                    const mine = message.senderId === user?.id;
                    return (
                      <div key={message.id} className={mine ? "ml-auto max-w-sm" : "max-w-sm"}>
                        <div
                          className={`rounded-xl p-3 text-sm ${
                            mine
                              ? "rounded-tr-none bg-primary text-white"
                              : "rounded-tl-none bg-white text-primary shadow-sm"
                          }`}
                        >
                          {message.originalText}
                        </div>
                        {message.translatedText && (
                          <p className={`mt-1 text-xs text-muted ${mine ? "text-right" : ""}`}>
                            번역 · {message.translatedText}
                          </p>
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
                  placeholder={`${activeContact?.name ?? ""}님에게 메시지 보내기`}
                  className="h-20 w-full resize-none rounded-md border border-border p-3 text-sm outline-none focus:ring-2 focus:ring-active/40"
                />
                <div className="mt-3 flex items-center justify-between">
                  <span className="text-xs text-muted">Enter 전송 · Shift + Enter 줄바꿈</span>
                  <Button type="submit" disabled={sending}>
                    {sending ? "보내는 중..." : "메시지 보내기"}
                  </Button>
                </div>
              </form>
            </section>
          </div>
        )}
      </div>
    </MainLayout>
  );
}
