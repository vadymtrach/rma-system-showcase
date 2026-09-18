import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import * as complaintsApi from "../api/complaints";
import type { Complaint, User } from "../types";
import { ComplaintsPage } from "./ComplaintsPage";

vi.mock("react-router-dom", async (importOriginal) => ({
  ...(await importOriginal<typeof import("react-router-dom")>()),
  useOutletContext: () => ({ searchTerm: "", productTypeFilter: "" }),
}));
vi.mock("../auth/AuthContext", () => ({
  useAuth: () => ({
    currentUser: { id: 1, email: "admin@test.local", fullName: "Admin", role: "ADMIN", active: true },
  }),
}));
vi.mock("../api/complaints");

const USERS: User[] = [
  { id: 1, email: "admin@test.local", fullName: "Ada Admin", role: "ADMIN", active: true },
  { id: 2, email: "emp@test.local", fullName: "Eve Employee", role: "EMPLOYEE", active: true },
  { id: 3, email: "svc@test.local", fullName: "Sam Service", role: "SERVICE", active: true },
  { id: 4, email: "wh@test.local", fullName: "Walt Warehouse", role: "WAREHOUSE", active: true },
  { id: 5, email: "gone@test.local", fullName: "Gina Gone", role: "EMPLOYEE", active: false },
];

const COMPLAINT: Complaint = {
  id: 10,
  rmaNumber: "RMA-10",
  productType: "LAPTOP",
  description: "Does not boot",
  status: "NEW",
  deliveryAddress: "Main St 1",
  insuranceAmount: 100,
  createdAt: "2026-03-01T10:00:00",
  updatedAt: "2026-03-01T10:00:00",
  version: 3,
};

vi.mock("../data/DataContext", () => ({
  useData: () => ({ complaints: [COMPLAINT], users: USERS, refreshComplaints: vi.fn(), refreshUsers: vi.fn() }),
}));

describe("ComplaintsPage", () => {
  beforeEach(() => {
    vi.mocked(complaintsApi.updateComplaint).mockResolvedValue(COMPLAINT);
    vi.mocked(complaintsApi.assignComplaint).mockResolvedValue(COMPLAINT);
  });

  it("offers only active employees and service engineers as assignees", async () => {
    render(<ComplaintsPage />);

    await userEvent.click(screen.getByRole("button", { name: "Assign" }));

    const options = within(screen.getByRole("combobox")).getAllByRole("option").map((o) => o.textContent);
    expect(options).toEqual(["Eve Employee (EMPLOYEE)", "Sam Service (SERVICE)"]);
  });

  it("defaults dates to the local day, not the UTC day", async () => {
    // 23:30 UTC is already the next day in Warsaw (the test time zone).
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(new Date("2026-03-10T23:30:00Z"));
    render(<ComplaintsPage />);

    await userEvent.click(screen.getByRole("button", { name: "Assign" }));

    expect(screen.getByDisplayValue("2026-03-11")).toBeInTheDocument();
  });

  it("sends the version the edit was based on", async () => {
    render(<ComplaintsPage />);

    await userEvent.click(screen.getByRole("button", { name: "Edit" }));
    await userEvent.click(screen.getByRole("button", { name: "Save changes" }));

    expect(complaintsApi.updateComplaint).toHaveBeenCalledWith(10, expect.objectContaining({ version: 3 }));
  });
});
