import { useMemo, useState } from "react";
import { useOutletContext } from "react-router-dom";
import * as complaintsApi from "../api/complaints";
import { useAuth } from "../auth/AuthContext";
import type { ComplaintFilter } from "../components/AppLayout";
import { ComplaintsTable } from "../components/ComplaintsTable";
import { Modal } from "../components/Modal";
import { useData } from "../data/DataContext";
import { PRODUCT_LABELS, type Complaint, type ProductType } from "../types";

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

type ModalKind = "assign" | "pickup" | "repair" | "return" | "shipment" | "edit" | null;

export function ComplaintsPage() {
  const { searchTerm, productTypeFilter } = useOutletContext<ComplaintFilter>();
  const { currentUser } = useAuth();
  const { complaints, users, refreshComplaints } = useData();
  const [modal, setModal] = useState<ModalKind>(null);
  const [activeComplaint, setActiveComplaint] = useState<Complaint | null>(null);
  const [modalError, setModalError] = useState("");

  const role = currentUser?.role;
  const canManage = role === "ADMIN" || role === "SERVICE";
  const canWarehouse = role === "ADMIN" || role === "WAREHOUSE";

  const activeComplaints = useMemo(() => complaints.filter((c) => c.status !== "SHIPPED"), [complaints]);

  const visibleRows = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();
    return activeComplaints.filter((c) => {
      const matchesTerm =
        !term ||
        Object.values(c).some((v) => v !== null && v !== undefined && String(v).toLowerCase().includes(term));
      const matchesType = !productTypeFilter || c.productType === productTypeFilter;
      return matchesTerm && matchesType;
    });
  }, [activeComplaints, searchTerm, productTypeFilter]);

  function openModal(kind: ModalKind, complaint: Complaint) {
    setModalError("");
    setActiveComplaint(complaint);
    setModal(kind);
  }
  function closeModal() {
    setModal(null);
    setActiveComplaint(null);
  }

  async function runAction(action: () => Promise<unknown>) {
    try {
      await action();
      closeModal();
      await refreshComplaints();
    } catch (e) {
      setModalError(e instanceof Error ? e.message : "An error occurred.");
    }
  }

  async function handleDelete(complaint: Complaint) {
    if (!confirm(`Are you sure you want to permanently delete complaint ${complaint.rmaNumber}?`)) return;
    try {
      await complaintsApi.deleteComplaint(complaint.id);
      await refreshComplaints();
    } catch (e) {
      alert(e instanceof Error ? e.message : "An error occurred.");
    }
  }

  function renderActions(complaint: Complaint) {
    return (
      <div>
        {canManage && complaint.status === "NEW" && (
          <button className="btn btn-small btn-blue" onClick={() => openModal("assign", complaint)}>
            Assign
          </button>
        )}
        {canManage && complaint.status === "ASSIGNED" && (
          <button className="btn btn-small btn-blue" onClick={() => openModal("pickup", complaint)}>
            Accept
          </button>
        )}
        {canManage && complaint.status === "ACCEPTED" && (
          <button className="btn btn-small btn-blue" onClick={() => openModal("repair", complaint)}>
            Repair
          </button>
        )}
        {canManage && complaint.status === "REPAIRED" && (
          <button className="btn btn-small btn-blue" onClick={() => openModal("return", complaint)}>
            Return to warehouse
          </button>
        )}
        {canWarehouse && complaint.status === "RETURNED" && (
          <button className="btn btn-small btn-green" onClick={() => openModal("shipment", complaint)}>
            Ship to customer
          </button>
        )}
        {canManage && (
          <>
            <button className="btn btn-small" onClick={() => openModal("edit", complaint)}>
              Edit
            </button>
            <button className="btn btn-small btn-red" onClick={() => handleDelete(complaint)}>
              Delete
            </button>
          </>
        )}
      </div>
    );
  }

  return (
    <section>
      <h2>Service complaints in progress</h2>
      <div className="userbar">
        <span>{visibleRows.length} active cases</span>
        <button className="btn" onClick={() => refreshComplaints()}>
          Refresh
        </button>
      </div>
      <ComplaintsTable allComplaints={complaints} rows={visibleRows} renderActions={canManage || canWarehouse ? renderActions : undefined} />

      {modal === "assign" && activeComplaint && (
        <Modal onClose={closeModal}>
          <AssignForm
            complaint={activeComplaint}
            users={users}
            error={modalError}
            onCancel={closeModal}
            onSubmit={(assignedToId, assignedDate) =>
              runAction(() => complaintsApi.assignComplaint(activeComplaint.id, assignedToId, assignedDate))
            }
          />
        </Modal>
      )}

      {modal === "pickup" && activeComplaint && (
        <Modal onClose={closeModal}>
          <DateForm
            title={`Confirm service pickup for ${activeComplaint.rmaNumber}`}
            label="Equipment pickup date"
            error={modalError}
            onCancel={closeModal}
            onSubmit={(date) => runAction(() => complaintsApi.confirmPickup(activeComplaint.id, date))}
          />
        </Modal>
      )}

      {modal === "repair" && activeComplaint && (
        <Modal onClose={closeModal}>
          <RepairForm
            complaint={activeComplaint}
            error={modalError}
            onCancel={closeModal}
            onSubmit={(desc, date) => runAction(() => complaintsApi.confirmRepair(activeComplaint.id, desc, date))}
          />
        </Modal>
      )}

      {modal === "return" && activeComplaint && (
        <Modal onClose={closeModal}>
          <DateForm
            title={`Return repaired equipment ${activeComplaint.rmaNumber}`}
            label="Warehouse handover date"
            error={modalError}
            onCancel={closeModal}
            onSubmit={(date) => runAction(() => complaintsApi.confirmReturn(activeComplaint.id, date))}
          />
        </Modal>
      )}

      {modal === "shipment" && activeComplaint && (
        <Modal onClose={closeModal}>
          <DateForm
            title={`Return shipment to customer ${activeComplaint.rmaNumber}`}
            label="Shipment dispatch date"
            submitLabel="Confirm shipment"
            error={modalError}
            onCancel={closeModal}
            onSubmit={(date) => runAction(() => complaintsApi.confirmShipment(activeComplaint.id, date))}
          />
        </Modal>
      )}

      {modal === "edit" && activeComplaint && (
        <Modal onClose={closeModal}>
          <EditForm
            complaint={activeComplaint}
            error={modalError}
            onCancel={closeModal}
            onSubmit={(input) => runAction(() => complaintsApi.updateComplaint(activeComplaint.id, input))}
          />
        </Modal>
      )}
    </section>
  );
}

function AssignForm({
  complaint,
  users,
  error,
  onCancel,
  onSubmit,
}: {
  complaint: Complaint;
  users: { id: number; fullName: string; role: string; active: boolean }[];
  error: string;
  onCancel: () => void;
  onSubmit: (assignedToId: number, assignedDate: string) => void;
}) {
  const [userId, setUserId] = useState<string>(users.find((u) => u.active)?.id.toString() ?? "");
  const [date, setDate] = useState(todayIso());

  return (
    <>
      <h3>Assign task {complaint.rmaNumber}</h3>
      <label>Responsible technician</label>
      <select value={userId} onChange={(e) => setUserId(e.target.value)}>
        {users
          .filter((u) => u.active)
          .map((u) => (
            <option key={u.id} value={u.id}>
              {u.fullName} ({u.role})
            </option>
          ))}
      </select>
      <label>Assignment date</label>
      <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
      <div className="form-actions">
        <button className="btn btn-green" onClick={() => onSubmit(Number(userId), date)}>
          Confirm
        </button>
        <button className="btn" onClick={onCancel}>
          Cancel
        </button>
      </div>
      <div className="error-msg">{error}</div>
    </>
  );
}

function DateForm({
  title,
  label,
  submitLabel = "Confirm",
  error,
  onCancel,
  onSubmit,
}: {
  title: string;
  label: string;
  submitLabel?: string;
  error: string;
  onCancel: () => void;
  onSubmit: (date: string) => void;
}) {
  const [date, setDate] = useState(todayIso());
  return (
    <>
      <h3>{title}</h3>
      <label>{label}</label>
      <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
      <div className="form-actions">
        <button className="btn btn-green" onClick={() => onSubmit(date)}>
          {submitLabel}
        </button>
        <button className="btn" onClick={onCancel}>
          Cancel
        </button>
      </div>
      <div className="error-msg">{error}</div>
    </>
  );
}

function RepairForm({
  complaint,
  error,
  onCancel,
  onSubmit,
}: {
  complaint: Complaint;
  error: string;
  onCancel: () => void;
  onSubmit: (description: string, date: string) => void;
}) {
  const [description, setDescription] = useState("");
  const [date, setDate] = useState(todayIso());
  const [localError, setLocalError] = useState("");

  function submit() {
    if (!description.trim()) {
      setLocalError("Enter a repair description.");
      return;
    }
    onSubmit(description.trim(), date);
  }

  return (
    <>
      <h3>Technical repair report {complaint.rmaNumber}</h3>
      <label>Service work performed</label>
      <textarea value={description} onChange={(e) => setDescription(e.target.value)} />
      <label>Completion date</label>
      <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
      <div className="form-actions">
        <button className="btn btn-green" onClick={submit}>
          Save report
        </button>
        <button className="btn" onClick={onCancel}>
          Cancel
        </button>
      </div>
      <div className="error-msg">{localError || error}</div>
    </>
  );
}

function EditForm({
  complaint,
  error,
  onCancel,
  onSubmit,
}: {
  complaint: Complaint;
  error: string;
  onCancel: () => void;
  onSubmit: (input: complaintsApi.ComplaintCreateInput) => void;
}) {
  const [rmaNumber, setRmaNumber] = useState(complaint.rmaNumber);
  const [productType, setProductType] = useState<ProductType>(complaint.productType);
  const [description, setDescription] = useState(complaint.description);
  const [address, setAddress] = useState(complaint.deliveryAddress);
  const [insurance, setInsurance] = useState(String(complaint.insuranceAmount ?? 0));
  const [localError, setLocalError] = useState("");

  function submit() {
    const insuranceAmount = Number(insurance);
    if (!rmaNumber.trim() || !description.trim() || !address.trim() || Number.isNaN(insuranceAmount)) {
      setLocalError("Fill in all required fields.");
      return;
    }
    onSubmit({
      rmaNumber: rmaNumber.trim(),
      productType,
      description: description.trim(),
      deliveryAddress: address.trim(),
      insuranceAmount,
    });
  }

  return (
    <>
      <h3>Edit complaint {complaint.rmaNumber}</h3>
      <label>RMA reference number</label>
      <input type="text" value={rmaNumber} onChange={(e) => setRmaNumber(e.target.value)} />
      <label>Equipment</label>
      <select value={productType} onChange={(e) => setProductType(e.target.value as ProductType)}>
        {(Object.keys(PRODUCT_LABELS) as ProductType[]).map((key) => (
          <option key={key} value={key}>
            {PRODUCT_LABELS[key]}
          </option>
        ))}
      </select>
      <label>Fault description</label>
      <textarea value={description} onChange={(e) => setDescription(e.target.value)} />
      <label>Delivery address</label>
      <textarea value={address} onChange={(e) => setAddress(e.target.value)} />
      <label>Insurance amount</label>
      <input type="number" step="0.01" min="0" value={insurance} onChange={(e) => setInsurance(e.target.value)} />
      <div className="form-actions">
        <button className="btn btn-green" onClick={submit}>
          Save changes
        </button>
        <button className="btn" onClick={onCancel}>
          Cancel
        </button>
      </div>
      <div className="error-msg">{localError || error}</div>
    </>
  );
}
