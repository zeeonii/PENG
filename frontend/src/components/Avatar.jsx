/**
 * 디자인 확정 시 교체 필요: 이니셜/플레이스홀더 스타일은 임시입니다.
 *
 * 사용 예시:
 * <Avatar src={user.profileImageUrl} name="김지훈" />
 * <Avatar name="Sarah Lee" size="sm" />
 * <Avatar /> // 이미지도 이름도 없으면 회색 원
 */

import remiAvatar from "../assets/REMI.png";

const SIZE_STYLES = {
  sm: "h-6 w-6 text-xs",
  md: "h-8 w-8 text-sm",
  lg: "h-10 w-10 text-base",
};

function getInitial(name) {
  return name?.trim()?.[0]?.toUpperCase() ?? "";
}

export default function Avatar({ src, name, size = "md" }) {
  if (name === "REMI") {
    return (
      <img
        src={remiAvatar}
        alt="REMI"
        className={`${SIZE_STYLES[size]} shrink-0 rounded-full object-contain`}
      />
    );
  }

  if (src) {
    return (
      <img
        src={src}
        alt={name ?? "avatar"}
        className={`${SIZE_STYLES[size]} rounded-full object-cover`}
      />
    );
  }

  return (
    <span
      className={`${SIZE_STYLES[size]} inline-flex items-center justify-center rounded-full bg-secondary font-medium text-muted`}
    >
      {getInitial(name)}
    </span>
  );
}
