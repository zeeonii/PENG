/** Notion 로고를 단순화한 아이콘입니다. 디자인 확정 시 교체 필요. */
export default function NotionIcon({ className = "h-9 w-9" }) {
  return (
    <svg viewBox="0 0 36 36" className={className} role="img" aria-label="Notion">
      <rect x="2" y="2" width="32" height="32" rx="7" fill="#111827" />
      <path
        d="M11 10.5h3.4l7.6 11.4V10.5h2.6v15h-3.3l-7.7-11.5V25.5H11V10.5z"
        fill="#FFFFFF"
      />
    </svg>
  );
}
