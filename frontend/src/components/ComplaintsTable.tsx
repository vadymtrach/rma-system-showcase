import { COLUMNS, type Complaint } from "../types";

function formatCell(value: unknown): string {
  if (value === null || value === undefined || value === "") return "-";
  return String(value);
}

interface ComplaintsTableProps {
  allComplaints: Complaint[];
  rows: Complaint[];
  renderActions?: (complaint: Complaint) => React.ReactNode;
}

export function ComplaintsTable({ allComplaints, rows, renderActions }: ComplaintsTableProps) {
  const sample = allComplaints[0];
  const columns = sample ? COLUMNS.filter((col) => Object.prototype.hasOwnProperty.call(sample, col.key)) : COLUMNS;

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map((col) => (
              <th key={col.key}>{col.label}</th>
            ))}
            {renderActions && <th>Akcje</th>}
          </tr>
        </thead>
        <tbody>
          {rows.map((complaint) => (
            <tr key={complaint.id} className={`status-${complaint.status}`}>
              {columns.map((col) => {
                const raw = complaint[col.key];
                const value = col.render ? col.render(raw, complaint) : formatCell(raw);
                return <td key={col.key}>{formatCell(value)}</td>;
              })}
              {renderActions && <td className="actions-cell">{renderActions(complaint)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
