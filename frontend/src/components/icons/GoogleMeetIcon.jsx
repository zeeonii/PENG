/** Google Meet 로고를 단순화한 아이콘입니다. 디자인 확정 시 교체 필요. */
export default function GoogleMeetIcon({ className = "h-9 w-9" }) {
  return (
    <svg viewBox="0 0 36 36" className={className} role="img" aria-label="Google Meet">
      <rect x="2" y="2" width="32" height="32" rx="7" fill="#FFFFFF" stroke="#E2E5EA" />
      <path d="M11 12h9.5a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H11z" fill="#00832D" />
      <path d="M11 12v12H9.5A2.5 2.5 0 0 1 7 21.5v-7A2.5 2.5 0 0 1 9.5 12z" fill="#0066DA" />
      <path d="M22.5 15.5 27 12.3a1 1 0 0 1 1.6.8v9.8a1 1 0 0 1-1.6.8l-4.5-3.2z" fill="#00AC47" />
      <path d="M22.5 15.5v5l-2-1.4v-2.2z" fill="#FFBA00" />
      <path d="M9.5 12H11v3H7v-.5A2.5 2.5 0 0 1 9.5 12z" fill="#00AC47" />
      <path d="M20.5 24H11v-3h11.5a2 2 0 0 1-2 3z" fill="#E52B26" />
    </svg>
  );
}
