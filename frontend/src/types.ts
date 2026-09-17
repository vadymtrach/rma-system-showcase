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
  SERVER: "Serwer",
  NETWORK_ROUTER: "Router sieciowy",
  SWITCH: "Przełącznik (Switch)",
  LAPTOP: "Laptop",
  WORKSTATION: "Stacja robocza",
  MONITOR: "Monitor",
  PRINTER: "Drukarka / Urządzenie wielofunkcyjne",
  STORAGE_NAS: "Pamięć masowa (NAS)",
  POWER_SUPPLY_UPS: "Zasilacz awaryjny (UPS)",
  ACCESSORY_CABLE: "Okablowanie / Zasilacze",
  ACCESSORY_PERIPHERAL: "Peryferia",
  OTHER: "Inne",
};

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: "Administrator Systemu",
  SERVICE: "Inżynier Serwisu",
  WAREHOUSE: "Dział Logistyki",
  EMPLOYEE: "Pracownik",
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
  { key: "productType", label: "Urządzenie", render: (v) => PRODUCT_LABELS[v as ProductType] ?? String(v) },
  { key: "description", label: "Opis awarii" },
  { key: "assignedToFullName", label: "Przypisany technik" },
  { key: "pickupConfirmed", label: "Data przyjęcia" },
  {
    key: "repairConfirmed",
    label: "Protokół naprawy",
    render: (val, row) => {
      const desc = row.repairDescription;
      if (!val && !desc) return "-";
      if (desc && val) return `${desc} (${val})`;
      return String(desc || val || "-");
    },
  },
  { key: "returnConfirmed", label: "Zakończenie serwisu" },
  { key: "sentToClient", label: "Wysłano do klienta" },
  { key: "deliveryAddress", label: "Adres dostawy" },
  { key: "insuranceAmount", label: "Wartość (PLN)" },
];
