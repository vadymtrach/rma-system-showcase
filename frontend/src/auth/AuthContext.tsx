import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import * as authApi from "../api/client";
import { getMyProfile } from "../api/users";
import { connectWebSocket, disconnectWebSocket } from "../ws";
import type { User } from "../types";

interface AuthContextValue {
  currentUser: User | null;
  loading: boolean;
  loginError: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  onDataChanged: (listener: () => void) => () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [loginError, setLoginError] = useState<string | null>(null);
  const [listeners] = useState<Set<() => void>>(new Set());

  const onDataChanged = useCallback(
    (listener: () => void) => {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
    [listeners],
  );

  const startSession = useCallback(
    (user: User) => {
      setCurrentUser(user);
      connectWebSocket(() => listeners.forEach((l) => l()));
    },
    [listeners],
  );

  useEffect(() => {
    getMyProfile()
      .then(startSession)
      .catch(() => setCurrentUser(null))
      .finally(() => setLoading(false));
    // Restoring the session on first load only, mirroring the old app's
    // DOMContentLoaded handler that re-checks the session cookie.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      setLoginError(null);
      try {
        await authApi.login(email, password);
        const user = await getMyProfile();
        startSession(user);
      } catch (e) {
        setLoginError(e instanceof Error ? e.message : "Błąd uwierzytelniania.");
        throw e;
      }
    },
    [startSession],
  );

  const logout = useCallback(async () => {
    await authApi.logout();
    disconnectWebSocket();
    setCurrentUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ currentUser, loading, loginError, login, logout, onDataChanged }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
