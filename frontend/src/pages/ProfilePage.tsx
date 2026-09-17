import { useEffect, useState } from "react";
import { changeMyPassword, getMyProfile } from "../api/users";
import { useAuth } from "../auth/AuthContext";
import type { User } from "../types";

export function ProfilePage() {
  const { currentUser } = useAuth();
  const [profile, setProfile] = useState<User | null>(currentUser);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");

  useEffect(() => {
    getMyProfile()
      .then(setProfile)
      .catch((e) => alert(e instanceof Error ? e.message : "Wystąpił błąd."));
  }, []);

  async function submitPasswordChange() {
    setError("");
    setInfo("");
    if (!currentPassword || !newPassword || !confirmPassword) {
      setError("Wszystkie pola są wymagane.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("Hasła nie są identyczne.");
      return;
    }
    try {
      await changeMyPassword(currentPassword, newPassword);
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setInfo("Hasło zostało pomyślnie zaktualizowane.");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Wystąpił błąd.");
    }
  }

  return (
    <section id="view-profile">
      <h2>Twój profil</h2>
      <div className="profile-box">
        <p>
          <strong>Identyfikator (Email):</strong> <span>{profile?.email ?? "-"}</span>
        </p>
        <p>
          <strong>Użytkownik:</strong> <span>{profile?.fullName ?? "-"}</span>
        </p>
        <p>
          <strong>Uprawnienia:</strong> <span>{profile?.role ?? "-"}</span>
        </p>
        <hr />
        <h3 style={{ marginTop: 0 }}>Aktualizacja hasła</h3>
        <label>Obecne hasło</label>
        <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} />
        <label>Nowe hasło</label>
        <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
        <label>Powtórz nowe hasło</label>
        <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} />
        <div className="form-actions">
          <button className="btn btn-green" onClick={submitPasswordChange}>
            Zapisz nowe hasło
          </button>
        </div>
        <div className="error-msg">{error}</div>
        <div className="info-msg" style={{ color: "#16a34a" }}>
          {info}
        </div>
      </div>
    </section>
  );
}
