export type Role = "ADMIN" | "SERVICE" | "WAREHOUSE" | "EMPLOYEE";

export type ProductType =
  | "SERVER"
  | "NETWORK_ROUTER"
  | "SWITCH"
  | "LAPTOP"
  | "WORKSTATION"
  | "MONITOR"
  | "PRINTER"
  | "STORAGE_NAS"
  | "POWER_SUPPLY_UPS"
  | "ACCESSORY_CABLE"
  | "ACCESSORY_PERIPHERAL"
  | "OTHER";

export type ComplaintStatus =
  | "NEW"
  | "ASSIGNED"
  | "ACCEPTED"
  | "REPAIRED"
  | "RETURNED"
  | "SHIPPED";

export const PRODUCT_LABELS: Record<ProductType, string> = {
  SERVER: "Server",
  NETWORK_ROUTER: "Network router",
  SWITCH: "Switch",
  LAPTOP: "Laptop",
  WORKSTATION: "Workstation",
  MONITOR: "Monitor",
  PRINTER: "Printer / MFP",
  STORAGE_NAS: "Storage (NAS)",
  POWER_SUPPLY_UPS: "UPS",
  ACCESSORY_CABLE: "Cabling / Power supplies",
  ACCESSORY_PERIPHERAL: "Peripherals",
  OTHER: "Other",
};

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: "System Administrator",
  SERVICE: "Service Engineer",
  WAREHOUSE: "Logistics Department",
  EMPLOYEE: "Employee",
};

// The backend returns one of four role-specific DTOs (AdminComplaintResponse,
// ServiceComplaintResponse, WarehouseComplaintResponse, EmployeeComplaintResponse)
// with no discriminator field, and the field sets genuinely differ by role
// (e.g. EMPLOYEE never gets sentToClient/insuranceAmount). Model every
// role-specific field as optional and render "-" when absent.
export interface Complaint {
  id: number;
  rmaNumber: string;
  productType: ProductType;
  description: string;
  status: ComplaintStatus;

  assignedToId?: number | null;
  assignedToFullName?: string | null;
  assignedDate?: string | null;
  pickupConfirmed?: string | null;
  repairDescription?: string | null;
  repairConfirmed?: string | null;
  returnConfirmed?: string | null;
  sentToClient?: string | null;

  deliveryAddress: string;
  insuranceAmount?: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  active: boolean;
}

export interface TableColumn {
  key: keyof Complaint;
  label: string;
  render?: (value: unknown, row: Complaint) => string;
}

export const COLUMNS: TableColumn[] = [
  { key: "rmaNumber", label: "RMA ID" },
  { key: "productType", label: "Device", render: (v) => PRODUCT_LABELS[v as ProductType] ?? String(v) },
  { key: "description", label: "Fault description" },
  { key: "assignedToFullName", label: "Assigned technician" },
  { key: "pickupConfirmed", label: "Pickup date" },
  {
    key: "repairConfirmed",
    label: "Repair report",
    render: (val, row) => {
      const desc = row.repairDescription;
      if (!val && !desc) return "-";
      if (desc && val) return `${desc} (${val})`;
      return String(desc || val || "-");
    },
  },
  { key: "returnConfirmed", label: "Service completed" },
  { key: "sentToClient", label: "Sent to customer" },
  { key: "deliveryAddress", label: "Delivery address" },
  { key: "insuranceAmount", label: "Value (PLN)" },
];
