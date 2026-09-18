import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from "react";
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
      connectWebSocket(
        () => listeners.forEach((l) => l()),
        // A 401 here triggers the unauthorized handler below, which stops reconnecting.
        () => getMyProfile().then(() => undefined, () => undefined),
      );
    },
    [listeners],
  );

  // Any 401 means the server-side session is gone (expired, or ended after an account change).
  // Clearing the user makes ProtectedRoute redirect to the login page.
  const currentUserRef = useRef<User | null>(null);
  useEffect(() => {
    currentUserRef.current = currentUser;
  }, [currentUser]);

  useEffect(() => {
    authApi.setUnauthorizedHandler(() => {
      if (currentUserRef.current) setLoginError("Your session has expired. Please log in again.");
      disconnectWebSocket();
      setCurrentUser(null);
    });
    return () => authApi.setUnauthorizedHandler(null);
  }, []);

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
        setLoginError(e instanceof Error ? e.message : "Authentication failed.");
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
