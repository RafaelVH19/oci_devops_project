import { useEffect, useMemo, useState } from 'react';
import { Link, useOutletContext, useParams } from 'react-router-dom';
import moment from 'moment';
import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Tooltip,
} from 'chart.js';
import { Bar, Doughnut } from 'react-chartjs-2';

ChartJS.register(ArcElement, CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const priorityPalette = ['#c74634', '#e86b58', '#d8c995', '#efe7d4'];
const statusPalette = ['#c74634', '#e86b58', '#d8c995', '#efe7d4'];

const chartFont = 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';

const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: '66%',
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        boxWidth: 8,
        boxHeight: 8,
        usePointStyle: true,
        pointStyle: 'circle',
        color: '#6B6560',
        font: { family: chartFont, size: 11 },
        padding: 12,
      },
    },
    tooltip: {
      backgroundColor: '#2A1814',
      titleColor: '#faf9f6',
      bodyColor: '#faf9f6',
      callbacks: {
        label: (ctx) => `${ctx.label}: ${ctx.raw}%`,
      },
    },
  },
};

const velocityOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top',
      align: 'end',
      labels: {
        boxWidth: 8,
        boxHeight: 8,
        usePointStyle: true,
        pointStyle: 'circle',
        color: '#6B6560',
        font: { family: chartFont, size: 12 },
      },
    },
    tooltip: {
      backgroundColor: '#2A1814',
      titleColor: '#faf9f6',
      bodyColor: '#faf9f6',
      callbacks: {
        label: (ctx) => `Tasks Done: ${ctx.raw}`,
      },
    },
  },
  scales: {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: { color: '#8a8580', font: { family: chartFont, size: 11 } },
    },
    y: {
      beginAtZero: true,
      ticks: { color: '#8a8580', font: { family: chartFont, size: 11 }, stepSize: 1 },
      border: { display: false },
      grid: { color: 'rgba(42,24,20,0.06)', drawTicks: false },
    },
  },
};

function percentageBreakdown(total, counts) {
  if (total === 0) return counts.map(() => 0);
  return counts.map((value) => Math.round((value / total) * 100));
}

function buildPriorityData(rows) {
  const counts = {
    CRITICAL: rows.filter((t) => t.priority === 'CRITICAL').length,
    HIGH: rows.filter((t) => t.priority === 'HIGH').length,
    MEDIUM: rows.filter((t) => t.priority === 'MEDIUM').length,
    LOW: rows.filter((t) => t.priority === 'LOW').length,
  };
  const total = rows.length;
  const data = percentageBreakdown(total, [
    counts.CRITICAL,
    counts.HIGH,
    counts.MEDIUM,
    counts.LOW,
  ]);
  return {
    labels: ['Critical', 'High', 'Medium', 'Low'],
    datasets: [{ data, backgroundColor: priorityPalette, borderWidth: 0, hoverOffset: 4 }],
  };
}

function buildStatusData(rows) {
  const counts = {
    BACKLOG: rows.filter((t) => t.status === 'PENDING').length,
    TODO: rows.filter((t) => t.status === 'TODO').length,
    IN_PROGRESS: rows.filter((t) => t.status === 'IN_PROGRESS').length,
    DONE: rows.filter((t) => t.status === 'DONE').length,
  };
  const total = rows.length;
  const data = percentageBreakdown(total, [
    counts.BACKLOG,
    counts.TODO,
    counts.IN_PROGRESS,
    counts.DONE,
  ]);
  return {
    labels: ['Backlog', 'To Do', 'In Progress', 'Done'],
    datasets: [{ data, backgroundColor: statusPalette, borderWidth: 0, hoverOffset: 4 }],
  };
}

function buildVelocityData(rows, sprint) {
  const start = sprint?.startDate ? moment(sprint.startDate) : moment().subtract(6, 'day');
  const end = sprint?.endDate ? moment(sprint.endDate) : moment();
  const dayCount = Math.max(end.diff(start, 'days') + 1, 1);
  const labels = Array.from({ length: dayCount }, (_, i) =>
    start.clone().add(i, 'day').format('DD/MM')
  );
  const donePerDay = labels.map((_, idx) => {
    const dayStart = start.clone().add(idx, 'day').startOf('day');
    const dayEnd = dayStart.clone().endOf('day');
    return rows.filter((task) => {
      if (task.status !== 'DONE') return false;
      const date = task.completedDate || task.updatedAt || task.createdAt;
      if (!date) return false;
      const m = moment(date);
      return m.isBetween(dayStart, dayEnd, null, '[]');
    }).length;
  });

  return {
    labels,
    datasets: [
      {
        label: 'Tasks Done',
        data: donePerDay,
        backgroundColor: '#c74634',
        borderRadius: 6,
        barThickness: 28,
        maxBarThickness: 34,
      },
    ],
  };
}

function memberInitial(name) {
  return (name || '?')[0].toUpperCase();
}

function MemberFilterBar({ members, selectedId, onSelect }) {
  if (members.length === 0) return null;
  return (
    <div className="flex flex-wrap items-center gap-2">
      <button
        type="button"
        onClick={() => onSelect(null)}
        className={`rounded-full px-3 py-1.5 text-xs font-medium transition ${
          selectedId == null
            ? 'bg-[#2A1814] text-white'
            : 'bg-[#f5f2ec] text-[#6B6560] hover:text-[#2A1814]'
        }`}
      >
        All members
      </button>
      {members.map((m) => (
        <button
          key={m.id}
          type="button"
          onClick={() => onSelect(m.id)}
          className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-medium transition ${
            selectedId === m.id
              ? 'bg-[#2A1814] text-white'
              : 'bg-[#f5f2ec] text-[#6B6560] hover:text-[#2A1814]'
          }`}
        >
          <span
            className={`flex h-4 w-4 shrink-0 items-center justify-center rounded-full text-[9px] font-bold ${
              selectedId === m.id
                ? 'bg-white/20 text-white'
                : 'bg-[#c74634]/15 text-[#c74634]'
            }`}
          >
            {memberInitial(m.name)}
          </span>
          {m.name.split(' ')[0]}
        </button>
      ))}
    </div>
  );
}

function DashboardProjectSprint() {
  const { sprintId } = useParams();
  const sid = Number(sprintId);
  const { project, teamTasks, orderedSprints, users } = useOutletContext();

  const [selectedMemberId, setSelectedMemberId] = useState(null);

  const sprint = useMemo(
    () => orderedSprints.find((s) => s.id === sid) || null,
    [orderedSprints, sid]
  );

  const rows = useMemo(
    () => teamTasks.filter((t) => t.sprint?.id === sid),
    [teamTasks, sid]
  );

  // Build the member list from whoever actually has tasks in this sprint
  const sprintMembers = useMemo(() => {
    const usersById = new Map(users.map((u) => [u.id, u.name || u.email || `User ${u.id}`]));
    const seen = new Map();
    rows.forEach((t) => {
      if (t.assignedTo != null && !seen.has(t.assignedTo)) {
        seen.set(t.assignedTo, usersById.get(t.assignedTo) || `User ${t.assignedTo}`);
      }
    });
    return [...seen.entries()].map(([id, name]) => ({ id, name }));
  }, [rows, users]);

  // Reset selection when sprint changes
  useEffect(() => {
    setSelectedMemberId(null);
  }, [sid]);

  const filteredRows = useMemo(() => {
    if (selectedMemberId == null) return rows;
    return rows.filter((t) => t.assignedTo === selectedMemberId);
  }, [rows, selectedMemberId]);

  const priorityData = useMemo(() => buildPriorityData(filteredRows), [filteredRows]);
  const statusData = useMemo(() => buildStatusData(filteredRows), [filteredRows]);
  const velocityData = useMemo(() => buildVelocityData(filteredRows, sprint), [filteredRows, sprint]);

  const selectedMemberName = useMemo(() => {
    if (selectedMemberId == null) return null;
    return sprintMembers.find((m) => m.id === selectedMemberId)?.name ?? null;
  }, [selectedMemberId, sprintMembers]);

  if (!sprint) {
    return (
      <div className="rounded-2xl border border-[#2A1814]/[0.06] bg-[#faf9f6] p-8 text-center">
        <p className="text-sm text-[#6B6560]">This sprint is not part of this project.</p>
        <Link
          to={`/dashboard/projects/${project.id}`}
          className="mt-4 inline-block text-sm font-medium text-[#c74634] hover:underline"
        >
          Back to overview
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Sprint header */}
      <div className="rounded-2xl border border-[#2A1814]/[0.06] bg-white p-6 shadow-sm">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h2 className="text-lg font-semibold text-[#2A1814]">{sprint.name || `Sprint ${sprint.id}`}</h2>
            <p className="mt-1 text-sm text-[#6B6560]">
              {sprint.startDate && `Start ${moment(sprint.startDate).format('MMM D, YYYY')}`}
              {sprint.endDate && ` · End ${moment(sprint.endDate).format('MMM D, YYYY')}`}
            </p>
          </div>
          <span className="rounded-full bg-[#faf9f6] px-3 py-1 text-sm text-[#6B6560] ring-1 ring-[#2A1814]/8">
            {rows.length} tasks in sprint
          </span>
        </div>

        {/* Member filter */}
        {sprintMembers.length > 0 && (
          <div className="mt-4 border-t border-[#2A1814]/[0.06] pt-4">
            <p className="mb-2.5 text-xs font-medium uppercase tracking-wide text-[#6B6560]">
              View by member
            </p>
            <MemberFilterBar
              members={sprintMembers}
              selectedId={selectedMemberId}
              onSelect={setSelectedMemberId}
            />
          </div>
        )}
      </div>

      {/* Scope label when a member is selected */}
      {selectedMemberName && (
        <p className="text-sm text-[#6B6560]">
          Showing data for{' '}
          <span className="font-medium text-[#2A1814]">{selectedMemberName}</span>
          {' '}·{' '}
          <button
            type="button"
            onClick={() => setSelectedMemberId(null)}
            className="text-[#c74634] hover:underline"
          >
            Clear filter
          </button>
        </p>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="rounded-2xl border border-[#2A1814]/[0.06] bg-white p-5 shadow-sm sm:p-6">
          <h3 className="text-sm font-semibold text-[#2A1814]">Sprint Task Priority</h3>
          <div className="mx-auto mt-4 h-52 w-full max-w-[260px] [&_canvas]:bg-transparent">
            <Doughnut data={priorityData} options={doughnutOptions} />
          </div>
        </div>
        <div className="rounded-2xl border border-[#2A1814]/[0.06] bg-white p-5 shadow-sm sm:p-6">
          <h3 className="text-sm font-semibold text-[#2A1814]">Sprint Tasks</h3>
          <div className="mx-auto mt-4 h-52 w-full max-w-[260px] [&_canvas]:bg-transparent">
            <Doughnut data={statusData} options={doughnutOptions} />
          </div>
        </div>
      </div>

      <div className="rounded-2xl border border-[#2A1814]/[0.06] bg-white p-5 shadow-sm sm:p-6">
        <div className="mb-4">
          <h3 className="text-sm font-semibold text-[#2A1814]">Sprint Velocity</h3>
          <p className="mt-1 text-xs text-[#6B6560]">Tasks completed per day</p>
        </div>
        <div className="h-72 [&_canvas]:bg-transparent">
          <Bar data={velocityData} options={velocityOptions} />
        </div>
        {filteredRows.length === 0 && (
          <p className="mt-3 text-center text-sm text-[#6B6560]">
            {selectedMemberName
              ? `No tasks for ${selectedMemberName} in this sprint.`
              : 'No tasks assigned to this sprint yet.'}
          </p>
        )}
      </div>
    </div>
  );
}

export default DashboardProjectSprint;
