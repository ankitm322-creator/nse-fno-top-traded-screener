* {
  box-sizing: border-box;
}

:root {
  --bg: #0a1220;
  --panel: #101b2e;
  --panel-alt: #13233d;
  --border: rgba(148, 163, 184, 0.18);
  --text: #e5eefb;
  --muted: #a3b3cb;
  --accent: #4fd1c5;
  --accent-strong: #0ea5e9;
  --good: #22c55e;
  --bad: #ef4444;
  --warning: #f59e0b;
}

body {
  margin: 0;
  font-family: Inter, 'Segoe UI', sans-serif;
  background: linear-gradient(180deg, #0a1220 0%, #111827 100%);
  color: var(--text);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px 40px;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 26px;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--accent);
}

h1 {
  margin: 0;
  font-size: clamp(28px, 3vw, 44px);
}

.status-box {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 10px 16px;
  color: var(--muted);
  font-size: 14px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--warning);
  box-shadow: 0 0 12px rgba(245, 158, 11, 0.9);
}

.controls {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 22px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 160px;
}

label {
  font-size: 12px;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

select,
input,
button {
  border-radius: 12px;
  border: 1px solid var(--border);
  background: rgba(15, 23, 42, 0.7);
  color: var(--text);
  padding: 12px 14px;
  font-size: 14px;
}

.search-box {
  flex: 1;
  min-width: 220px;
}

.primary-btn {
  cursor: pointer;
  background: linear-gradient(120deg, var(--accent) 0%, var(--accent-strong) 100%);
  color: #031622;
  border: none;
  font-weight: 700;
  min-width: 120px;
}

.meta-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.meta-card {
  background: rgba(15, 23, 42, 0.75);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 16px 18px;
}

.meta-label {
  display: block;
  color: var(--muted);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.table-wrapper {
  overflow-x: auto;
  border: 1px solid var(--border);
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.76);
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 900px;
}

thead th {
  background: rgba(19, 35, 61, 0.9);
  color: var(--muted);
  text-align: left;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  padding: 16px 18px;
}

tbody td {
  padding: 16px 18px;
  border-top: 1px solid var(--border);
  font-size: 15px;
}

tbody tr:hover {
  background: rgba(148, 163, 184, 0.04);
}

.rank {
  color: var(--muted);
  font-weight: 600;
}

.symbol-cell {
  font-weight: 700;
}

.positive {
  color: var(--good);
}

.negative {
  color: var(--bad);
}

.empty-state {
  text-align: center;
  color: var(--muted);
  padding: 28px;
}

@media (max-width: 640px) {
  .topbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .controls {
    align-items: stretch;
  }

  .field-group,
  .primary-btn {
    width: 100%;
  }
}
