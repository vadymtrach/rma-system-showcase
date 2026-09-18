import { act, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import apiFetch from "../api/client";
import type { User } from "../types";
import { connectWebSocket, disconnectWebSocket } from "../ws";
import { AuthProvider, useAuth } from "./AuthContext";

vi.mock("../ws", () => ({
  connectWebSocket: vi.fn(),
  disconnectWebSocket: vi.fn(),
}));

const ADMIN: User = { id: 1, email: "admin@test.local", fullName: "Admin", role: "ADMIN", active: true };

function jsonResponse(status: number, body: unknown = {}) {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}

function Probe() {
  const { currentUser, loading, loginError } = useAuth();
  if (loading) return <p>loading</p>;
  return (
    <>
      <p>user: {currentUser?.email ?? "none"}</p>
      <p>error: {loginError ?? "none"}</p>
    </>
  );
}

describe("AuthProvider", () => {
  beforeEach(() => {
    vi.mocked(connectWebSocket).mockClear();
    vi.mocked(disconnectWebSocket).mockClear();
  });

  it("restores an existing session on load and connects the WebSocket", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse(200, ADMIN)));

    render(<AuthProvider><Probe /></AuthProvider>);

    expect(await screen.findByText("user: admin@test.local")).toBeInTheDocument();
    expect(connectWebSocket).toHaveBeenCalledOnce();
  });

  it("does not show a session-expired message when there was no session", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse(401)));

    render(<AuthProvider><Probe /></AuthProvider>);

    expect(await screen.findByText("user: none")).toBeInTheDocument();
    expect(screen.getByText("error: none")).toBeInTheDocument();
  });

  it("logs the user out when any API call returns 401", async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(200, ADMIN));
    vi.stubGlobal("fetch", fetchMock);
    render(<AuthProvider><Probe /></AuthProvider>);
    await screen.findByText("user: admin@test.local");

    fetchMock.mockResolvedValue(jsonResponse(401));
    await act(async () => {
      await apiFetch("/api/complaints").catch(() => undefined);
    });

    await waitFor(() => expect(screen.getByText("user: none")).toBeInTheDocument());
    expect(screen.getByText("error: Your session has expired. Please log in again.")).toBeInTheDocument();
    expect(disconnectWebSocket).toHaveBeenCalled();
  });
});
