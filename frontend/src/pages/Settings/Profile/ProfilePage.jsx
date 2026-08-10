/**
 * 설정 > 프로필 · 언어 설정
 *
 * 저장은 현재 mock 동작이며, 실제 연동 시 PATCH /accounts/me 로 교체합니다.
 *
 * Cultural Translation 표시 설정은 원문과 번역문 중 최소 하나가 항상 켜져 있어야 하며,
 * 마지막 하나를 끄려고 하면 무시됩니다.
 */

import { useState } from "react";
import SettingsLayout from "../SettingsLayout.jsx";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import {
  currentMember,
  translationDisplay,
  COUNTRY_OPTIONS,
  TIMEZONE_OPTIONS,
  LANGUAGE_OPTIONS,
} from "../../../api/mock/settingsData.js";

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
  const [profile, setProfile] = useState(currentMember);
  const [display, setDisplay] = useState(translationDisplay);

  const updateProfile = (key, value) =>
    setProfile((prev) => ({ ...prev, [key]: value }));

  // 원문과 번역문 중 최소 하나는 항상 표시되어야 합니다.
  const updateDisplay = (key, value) => {
    const next = { ...display, [key]: value };
    if (!next.showOriginal && !next.showTranslated) return;
    setDisplay(next);
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    // 실제 연동 시 PATCH /accounts/me 호출로 교체
  };

  return (
    <SettingsLayout>
      <h1 className="text-2xl font-bold text-primary">프로필 · 언어 설정</h1>

      <form onSubmit={handleSubmit} className="mt-6 max-w-md space-y-6">
        <label className="block">
          <span className="text-sm text-primary">국가</span>
          <select
            value={profile.country}
            onChange={(event) => updateProfile("country", event.target.value)}
            className="mt-2 w-full rounded-md border border-border bg-white px-3 py-2 text-sm text-primary"
          >
            {COUNTRY_OPTIONS.map((option) => (
              <option key={option}>{option}</option>
            ))}
          </select>
        </label>

        <label className="block">
          <span className="text-sm text-primary">시간대</span>
          <select
            value={profile.timezone}
            onChange={(event) => updateProfile("timezone", event.target.value)}
            className="mt-2 w-full rounded-md border border-border bg-white px-3 py-2 text-sm text-primary"
          >
            {TIMEZONE_OPTIONS.map((option) => (
              <option key={option}>{option}</option>
            ))}
          </select>
        </label>

        <div>
          <span className="text-sm text-primary">사용 언어</span>
          <div className="mt-2 flex gap-2">
            {LANGUAGE_OPTIONS.map(({ value, label }) => (
              <Button
                key={value}
                variant={profile.language === value ? "primary" : "secondary"}
                onClick={() => updateProfile("language", value)}
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
              value={profile.duty}
              onChange={(event) => updateProfile("duty", event.target.value)}
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
              checked={display.showOriginal}
              onChange={(value) => updateDisplay("showOriginal", value)}
            />
            <Toggle
              label="번역문 표시"
              checked={display.showTranslated}
              onChange={(value) => updateDisplay("showTranslated", value)}
            />
          </div>

          <p className="mt-3 text-xs leading-5 text-muted">
            원문과 번역문 중 최소 하나는 항상 표시되어야 합니다. 둘 다 끄는 설정은
            허용되지 않습니다.
          </p>
        </section>

        <div className="flex justify-end">
          <Button type="submit">저장</Button>
        </div>
      </form>
    </SettingsLayout>
  );
}
