/**
 * 설정 > 프로필 · 언어 설정
 *
 * 국가/시간대는 백엔드가 자유 텍스트로 저장합니다(예: "KR", "Asia/Seoul").
 * 와이어프레임의 한글 드롭다운과 형식이 달라 텍스트 입력으로 대체했습니다.
 */

import { useState } from "react";
import SettingsLayout from "../SettingsLayout.jsx";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import { updateMe } from "../../../api/member.js";
import { useUser } from "../../../contexts/UserContext.jsx";
import { LANGUAGE_OPTIONS } from "../../../api/mock/settingsData.js";

function Toggle({ checked, onChange, label }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-sm text-primary">{label}</span>
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        aria-label={label}
        onClick={() => onChange(!checked)}
        className={`h-6 w-11 shrink-0 rounded-full p-0.5 transition-colors ${
          checked ? "bg-primary" : "bg-border"
        }`}
      >
        <span
          className={`block h-5 w-5 rounded-full bg-white transition-transform ${
            checked ? "translate-x-5" : "translate-x-0"
          }`}
        />
      </button>
    </div>
  );
}

export default function ProfilePage() {
  const { user, loading, refresh } = useUser();
  const [form, setForm] = useState({
    language: "KR",
    country: "",
    timezone: "",
    duty: "",
    showOriginalText: true,
    showTranslatedText: true,
  });
  const [loadedUser, setLoadedUser] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [saved, setSaved] = useState(false);

  // user가 새로 로드되면(로그인 직후 등) 폼 값을 그 값으로 맞춥니다.
  // (렌더링 중 상태 조정 — https://ko.react.dev/learn/you-might-not-need-an-effect)
  if (user && user !== loadedUser) {
    setLoadedUser(user);
    setForm({
      language: user.language ?? "KR",
      country: user.country ?? "",
      timezone: user.timezone ?? "",
      duty: user.duty ?? "",
      showOriginalText: user.showOriginalText ?? true,
      showTranslatedText: user.showTranslatedText ?? true,
    });
  }

  const updateField = (key, value) => setForm((prev) => ({ ...prev, [key]: value }));

  // 원문과 번역문 중 최소 하나는 항상 표시되어야 합니다.
  const updateDisplay = (key, value) => {
    const next = { ...form, [key]: value };
    if (!next.showOriginalText && !next.showTranslatedText) return;
    setForm(next);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    setSaved(false);
    try {
      await updateMe(form);
      await refresh();
      setSaved(true);
    } catch {
      setError("저장에 실패했어요.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <SettingsLayout>
        <p className="text-sm text-muted">불러오는 중...</p>
      </SettingsLayout>
    );
  }

  return (
    <SettingsLayout>
      <h1 className="text-2xl font-bold text-primary">프로필 · 언어 설정</h1>

      <form onSubmit={handleSubmit} className="mt-6 max-w-md space-y-6">
        <label className="block">
          <span className="text-sm text-primary">국가</span>
          <span className="mt-2 block">
            <Input
              value={form.country}
              onChange={(event) => updateField("country", event.target.value)}
              placeholder="예: KR"
            />
          </span>
        </label>

        <label className="block">
          <span className="text-sm text-primary">시간대</span>
          <span className="mt-2 block">
            <Input
              value={form.timezone}
              onChange={(event) => updateField("timezone", event.target.value)}
              placeholder="예: Asia/Seoul"
            />
          </span>
        </label>

        <div>
          <span className="text-sm text-primary">사용 언어</span>
          <div className="mt-2 flex gap-2">
            {LANGUAGE_OPTIONS.map(({ value, label }) => (
              <Button
                key={value}
                type="button"
                variant={form.language === value ? "primary" : "secondary"}
                onClick={() => updateField("language", value)}
              >
                {label}
              </Button>
            ))}
          </div>
        </div>

        <label className="block">
          <span className="text-sm text-primary">담당 업무</span>
          <span className="mt-2 block">
            <Input
              value={form.duty}
              onChange={(event) => updateField("duty", event.target.value)}
              placeholder="예: 백엔드 개발"
            />
          </span>
        </label>

        <section className="border-t border-border pt-6">
          <h2 className="text-sm font-semibold text-primary">
            Cultural Translation 표시 설정
          </h2>

          <div className="mt-4 space-y-3">
            <Toggle
              label="원문 표시"
              checked={form.showOriginalText}
              onChange={(value) => updateDisplay("showOriginalText", value)}
            />
            <Toggle
              label="번역문 표시"
              checked={form.showTranslatedText}
              onChange={(value) => updateDisplay("showTranslatedText", value)}
            />
          </div>

          <p className="mt-3 text-xs leading-5 text-muted">
            원문과 번역문 중 최소 하나는 항상 표시되어야 합니다. 둘 다 끄는 설정은
            허용되지 않습니다.
          </p>
        </section>

        {error && <p className="text-sm text-danger">{error}</p>}
        {saved && <p className="text-sm text-success">저장했어요.</p>}

        <div className="flex justify-end">
          <Button type="submit" disabled={saving}>
            {saving ? "저장 중..." : "저장"}
          </Button>
        </div>
      </form>
    </SettingsLayout>
  );
}
