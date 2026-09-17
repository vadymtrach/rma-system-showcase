import { useMemo } from "react";
import { ComplaintsTable } from "../components/ComplaintsTable";
import { useData } from "../data/DataContext";

export function ArchivePage() {
  const { complaints } = useData();
  const archived = useMemo(() => complaints.filter((c) => c.status === "SHIPPED"), [complaints]);

  return (
    <section>
      <h2>Archiwum zrealizowanych zgłoszeń</h2>
      <ComplaintsTable allComplaints={complaints} rows={archived} />
    </section>
  );
}
