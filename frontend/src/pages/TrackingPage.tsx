import { useState } from "react";

export function TrackingPage() {
  const [number, setNumber] = useState("");

  function openDpdTracking() {
    const trimmed = number.trim();
    const url = trimmed
      ? `https://tracktrace.dpd.com.pl/parcelDetails?p1=${encodeURIComponent(trimmed)}`
      : "https://tracktrace.dpd.com.pl/";
    window.open(url, "_blank");
  }

  return (
    <section>
      <h2>Shipment tracking</h2>
      <div className="form-box" style={{ maxWidth: 440 }}>
        <label>DPD courier tracking number</label>
        <input
          type="text"
          placeholder="e.g. 01234567890123"
          value={number}
          onChange={(e) => setNumber(e.target.value)}
        />
        <div className="form-actions">
          <button className="btn btn-green" onClick={openDpdTracking}>
            Check status
          </button>
        </div>
        <div className="info-msg">Opens the official courier tracking portal in a new window.</div>
      </div>
    </section>
  );
}
