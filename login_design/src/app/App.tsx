import { useState } from "react";
import { Eye, EyeOff, ArrowLeft, Mail, CheckCircle } from "lucide-react";
import { ImageWithFallback } from "@/app/components/figma/ImageWithFallback";
import logoImg from "@/imports/ChatGPT_Image_10_______2026__03_46_40__.png";

type Screen = "login" | "signup" | "forgot" | "forgot-sent";

function GoogleIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
    </svg>
  );
}

function InputField({
  icon,
  label,
  type = "text",
  value,
  onChange,
  placeholder,
  dir = "ltr",
  trailing,
}: {
  icon: React.ReactNode;
  label: string;
  type?: string;
  value: string;
  onChange: (v: string) => void;
  placeholder: string;
  dir?: "ltr" | "rtl";
  trailing?: React.ReactNode;
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-foreground/70 text-xs font-semibold uppercase tracking-widest">
        {label}
      </label>
      <div
        className="flex items-center gap-3 px-4 py-3.5 rounded-xl border transition-all duration-200"
        style={{
          background: "#1a1a1a",
          borderColor: value ? "rgba(229,19,42,0.5)" : "rgba(255,255,255,0.08)",
        }}
      >
        <span className="w-4 h-4 text-muted-foreground shrink-0">{icon}</span>
        <input
          type={type}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          dir={dir}
          className="flex-1 bg-transparent text-foreground placeholder:text-muted-foreground outline-none text-sm"
        />
        {trailing}
      </div>
    </div>
  );
}

export default function App() {
  const [screen, setScreen] = useState<Screen>("login");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [confirm, setConfirm] = useState("");
  const [resetEmail, setResetEmail] = useState("");

  const emailIcon = (
    <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} className="w-4 h-4">
      <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
    </svg>
  );
  const lockIcon = (
    <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} className="w-4 h-4">
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
    </svg>
  );
  const userIcon = (
    <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} className="w-4 h-4">
      <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
    </svg>
  );

  return (
    <div
      className="min-h-screen w-full flex items-center justify-center bg-background"
      style={{ fontFamily: "'Inter', sans-serif" }}
    >
      {/* Phone frame */}
      <div
        className="relative w-[390px] h-[844px] bg-card rounded-[40px] overflow-hidden shadow-2xl flex flex-col"
        style={{ boxShadow: "0 40px 100px rgba(229,19,42,0.18), 0 0 0 1px rgba(255,255,255,0.06)" }}
      >
        {/* Status bar */}
        <div className="flex justify-between items-center px-8 pt-4 pb-2">
          <span className="text-foreground/60 text-xs font-medium">9:41</span>
          <div className="flex gap-1 items-center">
            <div className="w-4 h-2.5 rounded-sm border border-foreground/40 relative">
              <div className="absolute inset-0.5 right-1 bg-foreground/60 rounded-sm" />
              <div className="absolute right-0.5 top-1/2 -translate-y-1/2 w-0.5 h-1 bg-foreground/40 rounded-r-sm -mr-0.5" />
            </div>
          </div>
        </div>

        {/* Top glow */}
        <div
          className="absolute top-0 left-1/2 -translate-x-1/2 w-80 h-80 rounded-full pointer-events-none"
          style={{ background: "radial-gradient(circle, rgba(229,19,42,0.22) 0%, transparent 70%)", top: "-60px" }}
        />

        {/* Logo */}
        <div className="flex flex-col items-center pt-10 pb-6 px-8 relative z-10">
          <ImageWithFallback
            src={logoImg}
            alt="TubeTogether logo"
            className="w-24 h-24 object-cover mb-2"
            style={{ borderRadius: "28%" }}
          />
        </div>

        {/* ── FORGOT PASSWORD screen ── */}
        {(screen === "forgot" || screen === "forgot-sent") && (
          <div className="flex-1 px-8 flex flex-col relative z-10">
            {/* Back button */}
            <button
              onClick={() => setScreen("login")}
              className="flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-8 self-start"
            >
              <ArrowLeft className="w-4 h-4" />
              <span className="text-sm">رجوع</span>
            </button>

            {screen === "forgot" ? (
              <>
                {/* Icon */}
                <div
                  className="w-16 h-16 rounded-2xl flex items-center justify-center mb-6 self-start"
                  style={{ background: "rgba(229,19,42,0.12)", border: "1px solid rgba(229,19,42,0.25)" }}
                >
                  <Mail className="w-7 h-7" style={{ color: "#e5132a" }} />
                </div>

                <h2
                  className="text-2xl text-foreground mb-2"
                  style={{ fontFamily: "'Outfit', sans-serif", fontWeight: 800 }}
                >
                  نسيت كلمة المرور؟
                </h2>
                <p className="text-muted-foreground text-sm mb-8 leading-relaxed">
                  أدخل بريدك الإلكتروني وسنرسل لك رابط لإعادة تعيين كلمة المرور.
                </p>

                <InputField
                  icon={emailIcon}
                  label="البريد الإلكتروني"
                  type="email"
                  value={resetEmail}
                  onChange={setResetEmail}
                  placeholder="example@email.com"
                />

                <button
                  onClick={() => resetEmail && setScreen("forgot-sent")}
                  className="w-full py-4 rounded-xl text-white font-bold text-base mt-6 transition-all duration-200 active:scale-95"
                  style={{
                    fontFamily: "'Outfit', sans-serif",
                    background: resetEmail
                      ? "linear-gradient(135deg, #c1001f 0%, #e5132a 50%, #ff1744 100%)"
                      : "#2a2a2a",
                    boxShadow: resetEmail ? "0 8px 30px rgba(229,19,42,0.4)" : "none",
                    color: resetEmail ? "#fff" : "#555",
                  }}
                >
                  إرسال رابط الإعادة
                </button>
              </>
            ) : (
              /* Sent confirmation */
              <div className="flex-1 flex flex-col items-center justify-center text-center gap-4 pb-16">
                <div
                  className="w-20 h-20 rounded-full flex items-center justify-center mb-2"
                  style={{ background: "rgba(229,19,42,0.12)", border: "2px solid rgba(229,19,42,0.3)" }}
                >
                  <CheckCircle className="w-9 h-9" style={{ color: "#e5132a" }} />
                </div>
                <h2
                  className="text-2xl text-foreground"
                  style={{ fontFamily: "'Outfit', sans-serif", fontWeight: 800 }}
                >
                  تم الإرسال!
                </h2>
                <p className="text-muted-foreground text-sm leading-relaxed max-w-[260px]">
                  أرسلنا رابط إعادة التعيين إلى
                  <br />
                  <span className="text-foreground font-medium">{resetEmail}</span>
                  <br />
                  تفقد بريدك الإلكتروني.
                </p>
                <button
                  onClick={() => { setScreen("login"); setResetEmail(""); }}
                  className="mt-4 w-full py-4 rounded-xl text-white font-bold text-base transition-all duration-200 active:scale-95"
                  style={{
                    fontFamily: "'Outfit', sans-serif",
                    background: "linear-gradient(135deg, #c1001f 0%, #e5132a 50%, #ff1744 100%)",
                    boxShadow: "0 8px 30px rgba(229,19,42,0.4)",
                  }}
                >
                  العودة لتسجيل الدخول
                </button>
              </div>
            )}
          </div>
        )}

        {/* ── LOGIN / SIGNUP screens ── */}
        {screen !== "forgot" && screen !== "forgot-sent" && (
          <>
            {/* Tab switcher */}
            <div className="mx-8 mb-6 bg-secondary rounded-xl p-1 flex relative z-10">
              <button
                onClick={() => setScreen("login")}
                className="flex-1 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200"
                style={{
                  fontFamily: "'Outfit', sans-serif",
                  background: screen === "login" ? "linear-gradient(135deg, #e5132a, #ff1744)" : "transparent",
                  color: screen === "login" ? "#fff" : "#888",
                }}
              >
                تسجيل الدخول
              </button>
              <button
                onClick={() => setScreen("signup")}
                className="flex-1 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200"
                style={{
                  fontFamily: "'Outfit', sans-serif",
                  background: screen === "signup" ? "linear-gradient(135deg, #e5132a, #ff1744)" : "transparent",
                  color: screen === "signup" ? "#fff" : "#888",
                }}
              >
                إنشاء حساب
              </button>
            </div>

            {/* Form */}
            <div className="flex-1 px-8 flex flex-col gap-3 relative z-10 overflow-y-auto" style={{ scrollbarWidth: "none" }}>
              {screen === "signup" && (
                <InputField
                  icon={userIcon}
                  label="الاسم الكامل"
                  value={name}
                  onChange={setName}
                  placeholder="مصفى ماجد"
                  dir="rtl"
                />
              )}

              <InputField
                icon={emailIcon}
                label="البريد الإلكتروني"
                type="email"
                value={email}
                onChange={setEmail}
                placeholder="example@email.com"
              />

              <InputField
                icon={lockIcon}
                label="كلمة المرور"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={setPassword}
                placeholder="••••••••"
                trailing={
                  <button
                    onClick={() => setShowPassword(!showPassword)}
                    className="text-muted-foreground hover:text-foreground transition-colors"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                }
              />

              {screen === "signup" && (
                <InputField
                  icon={
                    <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} className="w-4 h-4">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                    </svg>
                  }
                  label="تأكيد كلمة المرور"
                  type={showConfirm ? "text" : "password"}
                  value={confirm}
                  onChange={setConfirm}
                  placeholder="••••••••"
                  trailing={
                    <button
                      onClick={() => setShowConfirm(!showConfirm)}
                      className="text-muted-foreground hover:text-foreground transition-colors"
                    >
                      {showConfirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  }
                />
              )}

              {screen === "login" && (
                <div className="flex justify-end">
                  <button
                    onClick={() => setScreen("forgot")}
                    className="text-xs font-medium transition-opacity hover:opacity-70"
                    style={{ color: "#e5132a" }}
                  >
                    نسيت كلمة المرور؟
                  </button>
                </div>
              )}

              <button
                className="w-full py-4 rounded-xl text-white font-bold text-base mt-1 transition-all duration-200 active:scale-95"
                style={{
                  fontFamily: "'Outfit', sans-serif",
                  background: "linear-gradient(135deg, #c1001f 0%, #e5132a 50%, #ff1744 100%)",
                  boxShadow: "0 8px 30px rgba(229,19,42,0.4)",
                }}
              >
                {screen === "login" ? "تسجيل الدخول" : "إنشاء الحساب"}
              </button>

              <div className="flex items-center gap-3 my-1">
                <div className="flex-1 h-px" style={{ background: "rgba(255,255,255,0.08)" }} />
                <span className="text-muted-foreground text-xs">أو</span>
                <div className="flex-1 h-px" style={{ background: "rgba(255,255,255,0.08)" }} />
              </div>

              <button
                className="w-full py-3.5 rounded-xl flex items-center justify-center gap-3 border font-semibold text-sm transition-all duration-200 active:scale-95 hover:border-white/20"
                style={{
                  fontFamily: "'Outfit', sans-serif",
                  background: "#1a1a1a",
                  borderColor: "rgba(255,255,255,0.1)",
                  color: "#f5f5f5",
                }}
              >
                <GoogleIcon />
                الاستمرار عبر Google
              </button>

              <p className="text-center text-muted-foreground text-xs mt-2 pb-8">
                {screen === "login" ? "ليس لديك حساب؟ " : "لديك حساب بالفعل؟ "}
                <button
                  onClick={() => setScreen(screen === "login" ? "signup" : "login")}
                  className="font-semibold"
                  style={{ color: "#e5132a" }}
                >
                  {screen === "login" ? "إنشاء حساب" : "تسجيل الدخول"}
                </button>
              </p>
            </div>
          </>
        )}

        {/* Bottom indicator */}
        <div className="flex justify-center pb-3">
          <div className="w-32 h-1 rounded-full bg-foreground/20" />
        </div>
      </div>
    </div>
  );
}
