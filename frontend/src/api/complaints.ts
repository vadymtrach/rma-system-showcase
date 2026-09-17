import apiFetch from "./client";
import type { Complaint, ProductType } from "../types";

export interface ComplaintCreateInput {
  rmaNumber: string;
  productType: ProductType;
  description: string;
  deliveryAddress: string;
  insuranceAmount: number;
}

export function getComplaints(): Promise<Complaint[]> {
  return apiFetch<Complaint[]>("/api/complaints");
}

export function createComplaint(input: ComplaintCreateInput): Promise<Complaint> {
  return apiFetch<Complaint>("/api/complaints", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateComplaint(id: number, input: ComplaintCreateInput): Promise<Complaint> {
  return apiFetch<Complaint>(`/api/complaints/${id}`, {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function deleteComplaint(id: number): Promise<void> {
  return apiFetch<void>(`/api/complaints/${id}`, { method: "DELETE" });
}

export function assignComplaint(id: number, assignedToId: number, assignedDate: string): Promise<Complaint> {
  return apiFetch<Complaint>(`/api/complaints/${id}/assign`, {
    method: "PATCH",
    body: JSON.stringify({ assignedToId, assignedDate }),
  });
}

export function confirmPickup(id: number, pickupConfirmed: string): Promise<Complaint> {
  return apiFetch<Complaint>(`/api/complaints/${id}/pickup`, {
    method: "PATCH",
    body: JSON.stringify({ pickupConfirmed }),
  });
}

export function confirmRepair(id: number, repairDescription: string, repairDate: string): Promise<Complaint> {
  return apiFetch<Complaint>(`/api/complaints/${id}/repair`, {
    method: "PATCH",
    body: JSON.stringify({ repairDescription, repairDate }),
  });
}

export function confirmReturn(id: number, returnConfirmed: string): Promise<Complaint> {
  return apiFetch<Complaint>(`/api/complaints/${id}/return`, {
    method: "PATCH",
    body: JSON.stringify({ returnConfirmed }),
  });
}

export function confirmShipment(id: number, sentToClient: string): Promise<Complaint> {
  return apiFetch<Complaint>(`/api/complaints/${id}/shipment`, {
    method: "PATCH",
    body: JSON.stringify({ sentToClient }),
  });
}
