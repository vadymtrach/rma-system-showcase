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
      <h2>Monitoring listu przewozowego</h2>
      <div className="form-box" style={{ maxWidth: 440 }}>
        <label>Numer przesyłki kurierskiej DPD</label>
        <input
          type="text"
          placeholder="np. 01234567890123"
          value={number}
          onChange={(e) => setNumber(e.target.value)}
        />
        <div className="form-actions">
          <button className="btn btn-green" onClick={openDpdTracking}>
            Sprawdź status
          </button>
        </div>
        <div className="info-msg">Przekierowanie do oficjalnego portalu kurierskiego w nowym oknie.</div>
      </div>
    </section>
  );
}
