import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createComplaint } from "../api/complaints";
import { useData } from "../data/DataContext";
import { PRODUCT_LABELS, type ProductType } from "../types";

export function CreateComplaintPage() {
  const navigate = useNavigate();
  const { refreshComplaints } = useData();
  const [rmaNumber, setRmaNumber] = useState("");
  const [productType, setProductType] = useState<ProductType>("SERVER");
  const [description, setDescription] = useState("");
  const [address, setAddress] = useState("");
  const [insurance, setInsurance] = useState("");
  const [error, setError] = useState("");

  async function submit() {
    const insuranceAmount = Number(insurance);
    if (!rmaNumber.trim() || !description.trim() || !address.trim() || Number.isNaN(insuranceAmount)) {
      setError("Fill in all required fields.");
      return;
    }
    try {
      await createComplaint({
        rmaNumber: rmaNumber.trim(),
        productType,
        description: description.trim(),
        deliveryAddress: address.trim(),
        insuranceAmount,
      });
      await refreshComplaints();
      navigate("/");
    } catch (e) {
      setError(e instanceof Error ? e.message : "An error occurred.");
    }
  }

  return (
    <section id="view-create">
      <h2>Register a new complaint</h2>
      <div className="form-box">
        <label>RMA reference number</label>
        <input
          type="text"
          placeholder="e.g. RMA-2026-001"
          value={rmaNumber}
          onChange={(e) => setRmaNumber(e.target.value)}
        />

        <label>Equipment category</label>
        <select value={productType} onChange={(e) => setProductType(e.target.value as ProductType)}>
          {(Object.keys(PRODUCT_LABELS) as ProductType[]).map((key) => (
            <option key={key} value={key}>
              {PRODUCT_LABELS[key]}
            </option>
          ))}
        </select>

        <label>Detailed fault description</label>
        <textarea
          placeholder="Fault symptoms, error codes..."
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        <label>Customer return address</label>
        <textarea
          placeholder="Company / Recipient, Street, ZIP and City"
          value={address}
          onChange={(e) => setAddress(e.target.value)}
        />

        <label>Insurance value (PLN)</label>
        <input
          type="number"
          step="0.01"
          min="0"
          placeholder="0.00"
          value={insurance}
          onChange={(e) => setInsurance(e.target.value)}
        />

        <div className="form-actions">
          <button className="btn btn-green" onClick={submit}>
            Create complaint
          </button>
          <button className="btn btn-blue" onClick={() => navigate("/")}>
            Cancel
          </button>
        </div>
        <div className="error-msg">{error}</div>
      </div>
    </section>
  );
}
