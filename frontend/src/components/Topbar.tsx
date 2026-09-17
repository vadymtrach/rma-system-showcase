import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { useTheme } from "../theme/ThemeContext";
import { PRODUCT_LABELS, type ProductType } from "../types";

interface TopbarProps {
  searchTerm: string;
  onSearchTermChange: (value: string) => void;
  productTypeFilter: ProductType | "";
  onProductTypeFilterChange: (value: ProductType | "") => void;
}

export function Topbar({
  searchTerm,
  onSearchTermChange,
  productTypeFilter,
  onProductTypeFilterChange,
}: TopbarProps) {
  const { currentUser, logout } = useAuth();
  const { isDark, toggleDarkMode } = useTheme();
  const navigate = useNavigate();
  const canManage = currentUser?.role === "ADMIN" || currentUser?.role === "SERVICE";

  const navClass = ({ isActive }: { isActive: boolean }) => (isActive ? "active" : undefined);

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  return (
    <header className="topbar">
      <div className="brand">
        <div className="logo" onClick={() => navigate("/")} style={{ cursor: "pointer" }}>
          RMA WORKSPACE
        </div>
        <nav className="navlinks">
          <NavLink to="/" end className={navClass}>
            Zgłoszenia
          </NavLink>
          {canManage && (
            <NavLink to="/create" className={navClass}>
              Nowe zgłoszenie
            </NavLink>
          )}
          {canManage && (
            <NavLink to="/users" className={navClass}>
              Użytkownicy
            </NavLink>
          )}
          <NavLink to="/archive" className={navClass}>
            Archiwum
          </NavLink>
          <NavLink to="/tracking" className={navClass}>
            Śledzenie przesyłki
          </NavLink>
          <NavLink to="/profile" className={navClass}>
            Profil
          </NavLink>
          <a onClick={handleLogout}>Wyloguj</a>
          <a onClick={toggleDarkMode} title="Zmień motyw">
            {isDark ? "☀️" : "🌙"}
          </a>
        </nav>
      </div>
      <div className="searchbar">
        <input
          type="text"
          placeholder="Szukaj zgłoszenia..."
          value={searchTerm}
          onChange={(e) => onSearchTermChange(e.target.value)}
        />
        <select
          value={productTypeFilter}
          onChange={(e) => onProductTypeFilterChange(e.target.value as ProductType | "")}
        >
          <option value="">Wszystkie typy</option>
          {(Object.keys(PRODUCT_LABELS) as ProductType[]).map((key) => (
            <option key={key} value={key}>
              {PRODUCT_LABELS[key]}
            </option>
          ))}
        </select>
      </div>
    </header>
  );
}
