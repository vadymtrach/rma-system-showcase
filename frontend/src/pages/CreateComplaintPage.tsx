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
      setError("Wypełnij wszystkie wymagane pola.");
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
      setError(e instanceof Error ? e.message : "Wystąpił błąd.");
    }
  }

  return (
    <section id="view-create">
      <h2>Rejestracja nowego zgłoszenia</h2>
      <div className="form-box">
        <label>Numer referencyjny RMA</label>
        <input
          type="text"
          placeholder="np. RMA-2026-001"
          value={rmaNumber}
          onChange={(e) => setRmaNumber(e.target.value)}
        />

        <label>Kategoria sprzętu</label>
        <select value={productType} onChange={(e) => setProductType(e.target.value as ProductType)}>
          {(Object.keys(PRODUCT_LABELS) as ProductType[]).map((key) => (
            <option key={key} value={key}>
              {PRODUCT_LABELS[key]}
            </option>
          ))}
        </select>

        <label>Szczegółowy opis awarii</label>
        <textarea
          placeholder="Objawy usterki, kody błędów..."
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        <label>Adres zwrotny klienta</label>
        <textarea
          placeholder="Firma / Odbiorca, Ulica, Kod i Miasto"
          value={address}
          onChange={(e) => setAddress(e.target.value)}
        />

        <label>Wartość ubezpieczenia (PLN)</label>
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
            Utwórz zgłoszenie
          </button>
          <button className="btn btn-blue" onClick={() => navigate("/")}>
            Anuluj
          </button>
        </div>
        <div className="error-msg">{error}</div>
      </div>
    </section>
  );
}
