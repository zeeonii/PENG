/**
 * 로그인 사용자 정보를 앱 전체에서 공유하는 저장소입니다.
 *
 * 사용 예시:
 * import { useUser } from "../contexts/UserContext.jsx";
 *
 * const { user, loading, refresh, clear } = useUser();
 *
 * if (loading) return <p>불러오는 중...</p>;
 * if (!user) return <p>로그인이 필요합니다.</p>;
 * return <h1>좋은 아침이에요, {user.name}님</h1>;
 *
 * user는 GET /accounts/me 응답이며, 비로그인 상태에서는 null입니다.
 * 로그인 직후에는 refresh()를 호출해 사용자 정보를 다시 받아오세요.
 */

import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { getMe } from "../api/member.js";

const UserContext = createContext(null);

export function UserProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await getMe();
      setUser(data);
      return data;
    } catch {
      // 비로그인 상태이거나 서버에 연결할 수 없는 경우
      setUser(null);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const clear = useCallback(() => setUser(null), []);

  // 앱 시작 시 한 번 로그인 상태를 확인합니다.
  useEffect(() => {
    let ignore = false;

    getMe()
      .then(({ data }) => {
        if (!ignore) setUser(data);
      })
      .catch(() => {
        // 비로그인 상태이거나 서버에 연결할 수 없는 경우
        if (!ignore) setUser(null);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, []);

  return (
    <UserContext.Provider value={{ user, loading, refresh, clear }}>
      {children}
    </UserContext.Provider>
  );
}

export function useUser() {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error("useUser는 UserProvider 안에서만 사용할 수 있습니다.");
  }
  return context;
}
