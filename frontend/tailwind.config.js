/**
 * 디자인 미확정 상태의 임시 토큰입니다.
 * 디자인 확정 시 아래 색상 값만 교체하면 전체 컴포넌트에 자동 반영됩니다.
 */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        primary: "#111827",
        secondary: "#F3F4F6",
        border: "#E5E7EB",
        success: "#16A34A",
        active: "#2563EB",
        danger: "#DC2626",
        muted: "#6B7280",
      },
    },
  },
  plugins: [],
};
