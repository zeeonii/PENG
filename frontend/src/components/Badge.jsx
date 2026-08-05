/**
 * 디자인 확정 시 교체 필요: 색상 값은 tailwind.config.js의 임시 토큰 기준입니다.
 *
 * 사용 예시:
 * <Badge variant="active">진행중</Badge>
 * <Badge variant="success">완료</Badge>
 * <Badge variant="danger">영향도 높음</Badge>
 * <Badge>관리자</Badge>
 */

const VARIANT_STYLES = {
  default: "bg-secondary text-primary",
  success: "bg-success/10 text-success",
  active: "bg-active/10 text-active",
  danger: "bg-danger/10 text-danger",
};

export default function Badge({ variant = "default", children }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${VARIANT_STYLES[variant]}`}
    >
      {children}
    </span>
  );
}
