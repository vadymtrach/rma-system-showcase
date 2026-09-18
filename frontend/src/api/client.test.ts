import { describe, expect, it, vi } from "vitest";
import apiFetch, { ApiError, login, setUnauthorizedHandler, UnauthorizedError } from "./client";

function stubFetch(status: number, body: unknown = {}, headers: Record<string, string> = {}) {
  const fetchMock = vi.fn().mockResolvedValue(
    new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json", ...headers } }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("apiFetch", () => {
  it("calls the unauthorized handler and throws on 401", async () => {
    stubFetch(401);
    const handler = vi.fn();
    setUnauthorizedHandler(handler);

    await expect(apiFetch("/api/complaints")).rejects.toBeInstanceOf(UnauthorizedError);
    expect(handler).toHaveBeenCalledOnce();

    setUnauthorizedHandler(null);
  });

  it("uses the server's error message for other failures", async () => {
    stubFetch(409, { message: "This record was changed by someone else." });

    await expect(apiFetch("/api/complaints/1")).rejects.toThrow("This record was changed by someone else.");
  });
});

describe("login", () => {
  it("reports wrong credentials", async () => {
    stubFetch(401);

    await expect(login("a@b.c", "wrong")).rejects.toThrow("Invalid email or password.");
  });

  it("reports rate limiting with the wait time from Retry-After", async () => {
    stubFetch(429, {}, { "Retry-After": "120" });

    const error = await login("a@b.c", "pw").catch((e: unknown) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect((error as Error).message).toBe("Too many failed login attempts. Try again in 2 minutes.");
  });

  it("does not trigger the unauthorized handler on a failed login", async () => {
    stubFetch(401);
    const handler = vi.fn();
    setUnauthorizedHandler(handler);

    await expect(login("a@b.c", "wrong")).rejects.toThrow();
    expect(handler).not.toHaveBeenCalled();

    setUnauthorizedHandler(null);
  });
});
