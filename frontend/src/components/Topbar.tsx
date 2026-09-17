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
            Complaints
          </NavLink>
          {canManage && (
            <NavLink to="/create" className={navClass}>
              New complaint
            </NavLink>
          )}
          {canManage && (
            <NavLink to="/users" className={navClass}>
              Users
            </NavLink>
          )}
          <NavLink to="/archive" className={navClass}>
            Archive
          </NavLink>
          <NavLink to="/tracking" className={navClass}>
            Shipment tracking
          </NavLink>
          <NavLink to="/profile" className={navClass}>
            Profile
          </NavLink>
          <a onClick={handleLogout}>Log out</a>
          <a onClick={toggleDarkMode} title="Toggle theme">
            {isDark ? "☀️" : "🌙"}
          </a>
        </nav>
      </div>
      <div className="searchbar">
        <input
          type="text"
          placeholder="Search complaints..."
          value={searchTerm}
          onChange={(e) => onSearchTermChange(e.target.value)}
        />
        <select
          value={productTypeFilter}
          onChange={(e) => onProductTypeFilterChange(e.target.value as ProductType | "")}
        >
          <option value="">All types</option>
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
