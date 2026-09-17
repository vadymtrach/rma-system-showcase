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
      .catch((e) => alert(e instanceof Error ? e.message : "An error occurred."));
  }, []);

  async function submitPasswordChange() {
    setError("");
    setInfo("");
    if (!currentPassword || !newPassword || !confirmPassword) {
      setError("All fields are required.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    try {
      await changeMyPassword(currentPassword, newPassword);
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setInfo("Password successfully updated.");
    } catch (e) {
      setError(e instanceof Error ? e.message : "An error occurred.");
    }
  }

  return (
    <section id="view-profile">
      <h2>Your profile</h2>
      <div className="profile-box">
        <p>
          <strong>Identifier (Email):</strong> <span>{profile?.email ?? "-"}</span>
        </p>
        <p>
          <strong>User:</strong> <span>{profile?.fullName ?? "-"}</span>
        </p>
        <p>
          <strong>Permissions:</strong> <span>{profile?.role ?? "-"}</span>
        </p>
        <hr />
        <h3 style={{ marginTop: 0 }}>Change password</h3>
        <label>Current password</label>
        <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} />
        <label>New password</label>
        <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
        <label>Repeat new password</label>
        <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} />
        <div className="form-actions">
          <button className="btn btn-green" onClick={submitPasswordChange}>
            Save new password
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
