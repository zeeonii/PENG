/**
 * 디자인 확정 시 교체 필요: 색상 값은 tailwind.config.js의 임시 토큰 기준입니다.
 *
 * 사용 예시:
 * <Input
 *   type="email"
 *   placeholder="이메일 주소"
 *   value={email}
 *   onChange={(e) => setEmail(e.target.value)}
 * />
 */

export default function Input({
  type = "text",
  placeholder,
  value,
  onChange,
  ...props
}) {
  return (
    <input
      type={type}
      placeholder={placeholder}
      value={value}
      onChange={onChange}
      className="w-full rounded-md border border-border px-3 py-2 text-sm text-primary placeholder:text-muted focus:outline-none focus:ring-2 focus:ring-active/50"
      {...props}
    />
  );
}
