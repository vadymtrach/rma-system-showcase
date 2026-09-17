import apiFetch from "./client";
import type { Role, User } from "../types";

export interface UserCreateInput {
  email: string;
  password: string;
  fullName: string;
  role: Role;
}

export interface UserUpdateInput {
  email: string;
  fullName: string;
  role: Role;
}

export function getUsers(): Promise<User[]> {
  return apiFetch<User[]>("/api/users");
}

export function getMyProfile(): Promise<User> {
  return apiFetch<User>("/api/users/me");
}

export function changeMyPassword(currentPassword: string, newPassword: string): Promise<void> {
  return apiFetch<void>("/api/users/me/password", {
    method: "PATCH",
    body: JSON.stringify({ currentPassword, newPassword }),
  });
}

export function createUser(input: UserCreateInput): Promise<User> {
  return apiFetch<User>("/api/users", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateUser(id: number, input: UserUpdateInput): Promise<User> {
  return apiFetch<User>(`/api/users/${id}`, {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function updateUserStatus(id: number, active: boolean): Promise<void> {
  return apiFetch<void>(`/api/users/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify({ active }),
  });
}
