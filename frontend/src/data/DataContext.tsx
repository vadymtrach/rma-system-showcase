import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { getComplaints } from "../api/complaints";
import { getUsers } from "../api/users";
import { useAuth } from "../auth/AuthContext";
import type { Complaint, User } from "../types";

interface DataContextValue {
  complaints: Complaint[];
  users: User[];
  refreshComplaints: () => Promise<void>;
  refreshUsers: () => Promise<void>;
}

const DataContext = createContext<DataContextValue | null>(null);

export function DataProvider({ children }: { children: ReactNode }) {
  const { currentUser, onDataChanged } = useAuth();
  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [users, setUsers] = useState<User[]>([]);

  const canManageUsers = currentUser?.role === "ADMIN" || currentUser?.role === "SERVICE";

  const refreshComplaints = useCallback(async () => {
    try {
      setComplaints(await getComplaints());
    } catch (e) {
      console.error(e);
    }
  }, []);

  const refreshUsers = useCallback(async () => {
    if (!canManageUsers) return;
    try {
      setUsers(await getUsers());
    } catch (e) {
      console.error(e);
    }
  }, [canManageUsers]);

  useEffect(() => {
    refreshComplaints();
    refreshUsers();
    // Initial load on login/mount, then WS "REFRESH" events drive updates below.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => onDataChanged(refreshComplaints), [onDataChanged, refreshComplaints]);

  return (
    <DataContext.Provider value={{ complaints, users, refreshComplaints, refreshUsers }}>
      {children}
    </DataContext.Provider>
  );
}

export function useData(): DataContextValue {
  const ctx = useContext(DataContext);
  if (!ctx) throw new Error("useData must be used within a DataProvider");
  return ctx;
}
