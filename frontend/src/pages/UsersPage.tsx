import { useState } from "react";
import * as usersApi from "../api/users";
import { Modal } from "../components/Modal";
import { useData } from "../data/DataContext";
import { ROLE_LABELS, type Role, type User } from "../types";

type ModalKind = "create" | "edit" | null;

export function UsersPage() {
  const { users, refreshUsers } = useData();
  const [modal, setModal] = useState<ModalKind>(null);
  const [activeUser, setActiveUser] = useState<User | null>(null);
  const [modalError, setModalError] = useState("");

  function openCreate() {
    setModalError("");
    setActiveUser(null);
    setModal("create");
  }
  function openEdit(user: User) {
    setModalError("");
    setActiveUser(user);
    setModal("edit");
  }
  function closeModal() {
    setModal(null);
    setActiveUser(null);
  }

  async function handleCreate(input: usersApi.UserCreateInput) {
    try {
      await usersApi.createUser(input);
      closeModal();
      await refreshUsers();
    } catch (e) {
      setModalError(e instanceof Error ? e.message : "Wystąpił błąd.");
    }
  }

  async function handleEdit(id: number, input: usersApi.UserUpdateInput) {
    try {
      await usersApi.updateUser(id, input);
      closeModal();
      await refreshUsers();
    } catch (e) {
      setModalError(e instanceof Error ? e.message : "Wystąpił błąd.");
    }
  }

  async function toggleStatus(user: User) {
    const action = user.active ? "zablokować" : "aktywować";
    if (!confirm(`Czy na pewno chcesz ${action} konto użytkownika ${user.fullName}?`)) return;
    try {
      await usersApi.updateUserStatus(user.id, !user.active);
      await refreshUsers();
    } catch (e) {
      alert(e instanceof Error ? e.message : "Wystąpił błąd.");
    }
  }

  return (
    <section>
      <h2>Zarządzanie personelem</h2>
      <div className="userbar">
        <span>{users.length} kont w systemie</span>
        <div>
          <button className="btn" onClick={() => refreshUsers()}>
            Odśwież
          </button>
          <button className="btn btn-green" onClick={openCreate}>
            + Nowy użytkownik
          </button>
        </div>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Email</th>
              <th>Imię i nazwisko</th>
              <th>Rola</th>
              <th>Status konta</th>
              <th>Operacje</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className={user.active ? undefined : "user-inactive"}>
                <td>{user.email}</td>
                <td>{user.fullName}</td>
                <td>{ROLE_LABELS[user.role] ?? user.role}</td>
                <td>{user.active ? "Aktywne" : "Zablokowane"}</td>
                <td className="actions-cell">
                  <button className="btn btn-small" onClick={() => openEdit(user)}>
                    Edycja
                  </button>
                  <button
                    className={`btn btn-small ${user.active ? "btn-red" : "btn-green"}`}
                    onClick={() => toggleStatus(user)}
                  >
                    {user.active ? "Dezaktywuj" : "Aktywuj"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {modal === "create" && (
        <Modal onClose={closeModal}>
          <CreateUserForm error={modalError} onCancel={closeModal} onSubmit={handleCreate} />
        </Modal>
      )}
      {modal === "edit" && activeUser && (
        <Modal onClose={closeModal}>
          <EditUserForm
            user={activeUser}
            error={modalError}
            onCancel={closeModal}
            onSubmit={(input) => handleEdit(activeUser.id, input)}
          />
        </Modal>
      )}
    </section>
  );
}

function RoleSelect({ value, onChange }: { value: Role; onChange: (role: Role) => void }) {
  return (
    <select value={value} onChange={(e) => onChange(e.target.value as Role)}>
      {(Object.keys(ROLE_LABELS) as Role[]).map((key) => (
        <option key={key} value={key}>
          {ROLE_LABELS[key]}
        </option>
      ))}
    </select>
  );
}

function CreateUserForm({
  error,
  onCancel,
  onSubmit,
}: {
  error: string;
  onCancel: () => void;
  onSubmit: (input: usersApi.UserCreateInput) => void;
}) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fullName, setFullName] = useState("");
  const [role, setRole] = useState<Role>("EMPLOYEE");
  const [localError, setLocalError] = useState("");

  function submit() {
    if (!email.trim() || !password || !fullName.trim()) {
      setLocalError("Wypełnij wszystkie pola.");
      return;
    }
    if (password.length < 6) {
      setLocalError("Hasło musi zawierać co najmniej 6 znaków.");
      return;
    }
    onSubmit({ email: email.trim(), password, fullName: fullName.trim(), role });
  }

  return (
    <>
      <h3>Dodaj nowego pracownika</h3>
      <label>Email firmowy</label>
      <input
        type="text"
        placeholder="uzytkownik@rma-service.local"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />
      <label>Hasło początkowe</label>
      <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
      <label>Imię i nazwisko</label>
      <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} />
      <label>Rola systemowa</label>
      <RoleSelect value={role} onChange={setRole} />
      <div className="form-actions">
        <button className="btn btn-green" onClick={submit}>
          Zapisz
        </button>
        <button className="btn" onClick={onCancel}>
          Anuluj
        </button>
      </div>
      <div className="error-msg">{localError || error}</div>
    </>
  );
}

function EditUserForm({
  user,
  error,
  onCancel,
  onSubmit,
}: {
  user: User;
  error: string;
  onCancel: () => void;
  onSubmit: (input: usersApi.UserUpdateInput) => void;
}) {
  const [email, setEmail] = useState(user.email);
  const [fullName, setFullName] = useState(user.fullName);
  const [role, setRole] = useState<Role>(user.role);
  const [localError, setLocalError] = useState("");

  function submit() {
    if (!email.trim() || !fullName.trim()) {
      setLocalError("Wypełnij wszystkie pola.");
      return;
    }
    onSubmit({ email: email.trim(), fullName: fullName.trim(), role });
  }

  return (
    <>
      <h3>Modyfikacja konta</h3>
      <label>Email firmowy</label>
      <input type="text" value={email} onChange={(e) => setEmail(e.target.value)} />
      <label>Imię i nazwisko</label>
      <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} />
      <label>Rola systemowa</label>
      <RoleSelect value={role} onChange={setRole} />
      <div className="form-actions">
        <button className="btn btn-green" onClick={submit}>
          Zapisz
        </button>
        <button className="btn" onClick={onCancel}>
          Anuluj
        </button>
      </div>
      <div className="error-msg">{localError || error}</div>
    </>
  );
}
