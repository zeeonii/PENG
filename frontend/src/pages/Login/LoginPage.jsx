import { useNavigate } from "react-router-dom";
import Button from "../../components/Button.jsx";

export default function LoginPage() {
  const navigate = useNavigate();
  return (
    <main className="grid min-h-screen bg-white lg:grid-cols-2">
      <section className="relative hidden overflow-hidden bg-primary p-16 text-white lg:flex lg:flex-col">
        <span className="text-2xl font-bold">teamline<span className="text-active">.</span></span>
        <div className="my-auto"><p className="text-xs font-semibold tracking-[.2em] text-blue-200">BORDERLESS TEAMWORK</p><h1 className="mt-4 text-5xl font-bold leading-tight">밤사이 흩어진 맥락을<br />아침 한 번에.</h1><p className="mt-5 text-sm leading-6 text-gray-300">회의와 문서, 팀의 결정을 기억하는 AI 팀원과<br />더 자연스럽게 협업하세요.</p></div>
        <div className="rounded-xl border border-white/20 bg-white/10 p-5 text-sm"><p className="font-semibold">AI 팀원 · Overnight update</p><p className="mt-3 text-gray-200">로그인 정책이 변경되었습니다.<br />오늘 영향받는 작업은 2개예요.</p></div>
      </section>
      <section className="flex items-center justify-center p-6"><div className="w-full max-w-sm text-center"><div className="mb-8 text-2xl font-bold text-primary lg:hidden">teamline<span className="text-active">.</span></div><p className="text-xs font-semibold tracking-[.15em] text-active">WELCOME TO TEAMLINE</p><h2 className="mt-3 text-3xl font-bold text-primary">다시 만나 반가워요</h2><p className="mt-3 text-sm leading-6 text-muted">Google 계정으로 로그인하고<br />프로젝트의 오늘을 확인하세요.</p><Button className="mt-8 w-full border border-border bg-white !py-3 !text-primary" variant="secondary" onClick={() => navigate("/home")}><span className="font-bold text-active">G</span> Google로 계속하기</Button><p className="mt-4 text-xs text-muted">로그인 시 Google Meet 접근 권한이 함께 승인됩니다.</p></div></section>
    </main>
  );
}
