/**
 * 디자인 확정 시 교체 필요: 색상 값은 tailwind.config.js의 임시 토큰 기준입니다.
 *
 * 사용 예시:
 * <Button onClick={handleCreate}>프로젝트 생성</Button>
 * <Button variant="secondary" onClick={handleCancel}>취소</Button>
 * <Button type="submit">전송</Button>
 */

const VARIANT_STYLES = {
  primary: "bg-active text-white hover:bg-[#E9784C]",
  secondary:
    "bg-white text-primary border border-border hover:bg-secondary",
};

export default function Button({
  variant = "primary",
  type = "button",
  onClick,
  children,
  className = "",
  ...props
}) {
  return (
    <button
      type={type}
      onClick={onClick}
      className={`rounded-md px-4 py-2 text-sm font-medium transition-colors ${VARIANT_STYLES[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
