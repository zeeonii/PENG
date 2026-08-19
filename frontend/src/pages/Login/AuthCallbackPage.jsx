import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useUser } from "../../contexts/UserContext.jsx";

/**
 * Google OAuth 로그인 완료 후 백엔드가 돌려보내는 화면입니다.
 * 세션 쿠키를 받은 뒤 내 정보를 조회하고 홈으로 이동합니다.
 */
export default function AuthCallbackPage() {
  const navigate = useNavigate();
  const { refresh } = useUser();
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    let cancelled = false;

    const completeLogin = async () => {
      const user = await refresh();

      if (cancelled) return;

      if (user) {
        navigate("/home", { replace: true });
      } else {
        setHasError(true);
      }
    };

    completeLogin();

    return () => {
      cancelled = true;
    };
  }, [navigate, refresh]);

  if (hasError) {
    return (
      <main className="grid min-h-screen place-items-center bg-secondary p-6">
        <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-sm">
          <p className="text-2xl font-bold text-primary">로그인에 실패했어요.</p>
          <p className="mt-3 text-sm leading-6 text-muted">
            잠시 후 다시 시도해주세요. 문제가 계속되면 팀에 알려주세요.
          </p>
          <button
            type="button"
            onClick={() => navigate("/login", { replace: true })}
            className="mt-6 rounded-md bg-primary px-4 py-2 text-sm font-semibold text-accent"
          >
            로그인 화면으로 돌아가기
          </button>
        </div>
      </main>
    );
  }

  return (
    <main className="grid min-h-screen place-items-center bg-secondary p-6">
      <div className="text-center">
        <p className="text-2xl font-bold text-primary">MORROW.</p>
        <p className="mt-4 text-sm text-muted">로그인 정보를 확인하는 중이에요.</p>
      </div>
    </main>
  );
}
