import { useState, type FormEvent } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { Logo } from "../components/Logo";
import { PasswordInput } from "../components/PasswordInput";

export function LoginPage() {
  const { currentUser, loading, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  if (!loading && currentUser) return <Navigate to="/" replace />;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    if (!email.trim() || !password) {
      setError("Enter your email and password.");
      return;
    }
    try {
      await login(email.trim(), password);
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Authentication failed.");
    }
  };

  return (
    <div className="login-wrap">
      <form className="login-box form-box" onSubmit={handleSubmit}>
        <div className="logo">
          <Logo size={32} />
          HARDWARE RMA SYSTEM
        </div>
        <label>Email</label>
        <input
          type="text"
          placeholder="user@rma-service.local"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <label>Password</label>
        <PasswordInput value={password} onChange={setPassword} placeholder="••••••••" autoComplete="current-password" />
        <div className="form-actions">
          <button type="submit" className="btn btn-green" style={{ width: "100%" }}>
            Log in
          </button>
        </div>
        <div className="error-msg">{error}</div>
      </form>
    </div>
  );
}
