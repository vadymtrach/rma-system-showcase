export class ApiError extends Error {}
export class UnauthorizedError extends ApiError {}

async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };

  const res = await fetch(path, {
    ...options,
    headers,
    credentials: "same-origin",
  });

  if (res.status === 401) {
    throw new UnauthorizedError("Your session has expired or you don't have permission. Please log in again.");
  }
  if (!res.ok) {
    let message = `An error occurred (${res.status})`;
    try {
      const body = await res.json();
      message = body.message || message;
    } catch {
      // response body wasn't JSON, keep the generic message
    }
    throw new ApiError(message);
  }
  if (res.status === 204) return null as T;
  return res.json() as Promise<T>;
}

export async function login(email: string, password: string): Promise<void> {
  const formData = new URLSearchParams();
  formData.append("username", email);
  formData.append("password", password);

  const res = await fetch("/api/login", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: formData,
    credentials: "same-origin",
  });

  if (!res.ok) {
    throw new ApiError("Invalid email or password.");
  }
}

export async function logout(): Promise<void> {
  try {
    await fetch("/api/logout", { method: "POST", credentials: "same-origin" });
  } catch {
    // best-effort; the client-side session state is cleared regardless
  }
}

export default apiFetch;
