import { goToGoogleLogin } from "../../api/member.js";
import Button from "../../components/Button.jsx";

export default function LoginPage() {
  

  return (
    <main className="grid min-h-screen bg-secondary lg:grid-cols-2">
      <section className="relative hidden overflow-hidden bg-primary-dark p-16 text-accent lg:flex lg:flex-col">
        <span className="text-2xl font-bold">MORROW<span className="text-active">.</span></span>
        <div className="my-auto">
          <p className="text-xs font-semibold tracking-[.2em] text-accent/80">A DAY WITH MORROW</p>
          <h1 className="mt-4 text-5xl font-bold leading-tight text-white">
            밤사이 흩어진 맥락을<br />아침 한 번에.
          </h1>
          <p className="mt-5 text-sm leading-6 text-white/70">
            회의와 문서, 팀의 결정을 기억하는 REMI와<br />더 자연스럽게 협업하세요.
          </p>
        </div>
      </section>

      <section className="flex items-center justify-center bg-white p-6">
        <div className="w-full max-w-sm text-center">
          <div className="mb-8 text-2xl font-bold text-primary lg:hidden">MORROW<span className="text-active">.</span></div>
          <p className="text-xs font-semibold tracking-[.15em] text-primary">WELCOME TO MORROW</p>
          <h2 className="mt-3 text-3xl font-bold text-active">다시 만나 반가워요</h2>
          <p className="mt-3 text-sm leading-6 text-muted">Google 계정으로 로그인하고<br />프로젝트의 오늘을 확인하세요.</p>
          <Button
            className="mt-8 w-full border border-border bg-white !py-3 !text-primary"
            variant="secondary"
            onClick={goToGoogleLogin}
          >
            <span className="mr-1 inline-block bg-[conic-gradient(from_-45deg,_#4285F4_0_25%,_#34A853_0_50%,_#FBBC05_0_75%,_#EA4335_0_100%)] bg-clip-text font-bold text-transparent">G</span>
            <span className="text-[#202124]">Google로 계속하기</span>
          </Button>
          <p className="mt-4 text-xs text-muted">로그인 시 Google Meet 접근 권한이 함께 승인됩니다.</p>
        </div>
      </section>
    </main>
  );
}
