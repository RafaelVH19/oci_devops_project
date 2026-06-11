import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Bar, Line } from 'react-chartjs-2';
import {
  buildBurndownDatasets,
  buildMemberBarDataset,
  burndownChartOptions,
  memberBarChartOptions,
} from './dashboardCharts';
import { fetchJsonSafe } from './dashboardApi';
import ActivityListRow from './ActivityListRow';
import DashboardSection from './DashboardSection';
import { DashboardHomeSkeleton } from './DashboardSkeletons';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const SPRINT_SP = 192;

function fmtDay(date) {
  return `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}`;
}

function findCurrentSprint(sprints) {
  if (!sprints.length) return null;
  const now = new Date();
  const active = sprints.find(
    (s) => s.startDate && s.endDate && new Date(s.startDate) <= now && new Date(s.endDate) >= now
  );
  if (active) return active;
  return sprints.reduce((latest, s) => {
    if (!latest) return s;
    return new Date(s.startDate || 0) > new Date(latest.startDate || 0) ? s : latest;
  }, null);
}

function buildBurndown(tasks, sprint, sprintTaskIdSet) {
  if (!sprint?.startDate || !sprint?.endDate || !sprintTaskIdSet) return null;

  const sprintTasks = tasks.filter((t) => sprintTaskIdSet.has(t.id));

  const startDay = new Date(sprint.startDate);
  startDay.setHours(0, 0, 0, 0);
  const endDay = new Date(sprint.endDate);
  endDay.setHours(0, 0, 0, 0);
  const todayStart = new Date();
  todayStart.setHours(0, 0, 0, 0);

  const days = [];
  const cursor = new Date(startDay);
  while (cursor <= endDay) {
    days.push(new Date(cursor));
    cursor.setDate(cursor.getDate() + 1);
  }
  if (days.length === 0) return null;

  const totalDays = Math.max(days.length - 1, 1);
  const labels = days.map(fmtDay);
  const ideal = days.map((_, i) => Math.round(SPRINT_SP * (1 - i / totalDays)));

  let remaining = SPRINT_SP;
  const actual = days.map((day) => {
    if (day > todayStart) return null;
    const dayEnd = new Date(day);
    dayEnd.setHours(23, 59, 59, 999);

    const burned = sprintTasks
      .filter((t) => {
        if (t.status !== 'DONE') return false;
        const dateStr = t.completedDate || t.updatedAt;
        if (!dateStr) return false;
        const d = new Date(dateStr);
        return d >= day && d <= dayEnd;
      })
      .reduce((sum, t) => sum + (t.expectedHours || 0), 0);

    remaining -= burned;
    return remaining;
  });

  return { labels, actual, ideal };
}

function buildMemberChart(tasks, users, sprintTaskIdSet) {
  const usersMap = {};
  users.forEach((u) => {
    usersMap[u.id] = u.name || u.username || `User ${u.id}`;
  });

  const scoped = sprintTaskIdSet ? tasks.filter((t) => sprintTaskIdSet.has(t.id)) : tasks;

  const totals = {};
  scoped
    .filter((t) => t.status === 'DONE')
    .forEach((t) => {
      const name = usersMap[t.assignedTo] || 'Unassigned';
      const first = name.split(' ')[0];
      totals[first] = (totals[first] || 0) + (t.expectedHours || 1);
    });

  const entries = Object.entries(totals);
  if (entries.length === 0) return { labels: [], data: [] };
  return {
    labels: entries.map(([n]) => n),
    data: entries.map(([, pts]) => pts),
  };
}

function buildActivity(tasks, users) {
  const usersMap = new Map(
    users.map((u) => [String(u.id), u.name || u.username || u.email || `User ${u.id}`])
  );

  const sorted = [...tasks]
    .sort(
      (a, b) =>
        new Date(b.updatedAt || b.createdAt || 0) - new Date(a.updatedAt || a.createdAt || 0)
    )
    .slice(0, 6);

  if (sorted.length === 0) return [];

  return sorted.map((t, i) => {
    const actorId = t.assignedTo ?? t.createdBy;
    const actor =
      usersMap.get(String(actorId)) ||
      t.assignedToName ||
      t.createdByName ||
      t.assigneeName ||
      'Team member';
    return {
      title: t.title || `User Task ${i + 1}`,
      detail: t.description?.slice(0, 40) || `Assigned to ${actor}`,
      by: actor,
    };
  });
}

function ChartEmpty({ message }) {
  return (
    <div className="flex h-full min-h-[8rem] items-center justify-center px-4 text-center text-sm text-[#6B6560]">
      {message}
    </div>
  );
}

function DashboardHome() {
  const [tasks, setTasks] = useState([]);
  const [users, setUsers] = useState([]);
  const [sprints, setSprints] = useState([]);
  const [sprintLinks, setSprintLinks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      const [tasksResult, usersResult, sprintsResult, sprintLinksResult] = await Promise.all([
        fetchJsonSafe('/tasks'),
        fetchJsonSafe('/api/users'),
        fetchJsonSafe('/sprints'),
        fetchJsonSafe('/sprint-tasks'),
      ]);
      if (!cancelled) {
        setTasks(tasksResult.ok && Array.isArray(tasksResult.data) ? tasksResult.data : []);
        setUsers(usersResult.ok && Array.isArray(usersResult.data) ? usersResult.data : []);
        setSprints(sprintsResult.ok && Array.isArray(sprintsResult.data) ? sprintsResult.data : []);
        setSprintLinks(
          sprintLinksResult.ok && Array.isArray(sprintLinksResult.data) ? sprintLinksResult.data : []
        );
        setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const currentSprint = useMemo(() => findCurrentSprint(sprints), [sprints]);

  const sprintTaskIdSet = useMemo(() => {
    if (!currentSprint) return null;
    return new Set(
      sprintLinks
        .filter((st) => !st.removedAt && st.sprintId === currentSprint.id)
        .map((st) => st.taskId)
    );
  }, [sprintLinks, currentSprint]);

  const burndown = useMemo(
    () => buildBurndown(tasks, currentSprint, sprintTaskIdSet),
    [tasks, currentSprint, sprintTaskIdSet]
  );

  const memberChart = useMemo(
    () => buildMemberChart(tasks, users, sprintTaskIdSet),
    [tasks, users, sprintTaskIdSet]
  );

  const activityItems = useMemo(() => buildActivity(tasks, users), [tasks, users]);

  const burndownData = useMemo(() => {
    if (!burndown) return null;
    return {
      labels: burndown.labels,
      datasets: buildBurndownDatasets(burndown.actual, burndown.ideal),
    };
  }, [burndown]);

  const memberBarData = useMemo(
    () => buildMemberBarDataset(memberChart.labels, memberChart.data),
    [memberChart.labels, memberChart.data]
  );

  if (loading) {
    return <DashboardHomeSkeleton />;
  }

  const sprintSubtitle = currentSprint
    ? `${currentSprint.name ?? 'Current sprint'} · ${SPRINT_SP} SP standard`
    : 'No active sprint found';

  return (
    <div className="dashboard-page-enter flex h-full min-h-0 w-full flex-col">
      <div className="grid min-h-0 flex-1 gap-10 lg:grid-cols-[minmax(0,1fr)_minmax(260px,320px)] lg:gap-12">
        <div className="flex min-h-0 flex-col gap-10">
          <DashboardSection
            title="Story Points this Sprint"
            subtitle={sprintSubtitle}
          >
            <div className="h-52 sm:h-60 [&_canvas]:bg-transparent">
              {burndownData ? (
                <Line data={burndownData} options={burndownChartOptions} />
              ) : (
                <ChartEmpty message="No active sprint data available." />
              )}
            </div>
          </DashboardSection>

          <DashboardSection
            title="Story Points Completed per Member"
            subtitle={
              currentSprint
                ? `Completed work in ${currentSprint.name ?? 'current sprint'}`
                : 'Individual performance tracking'
            }
            className="flex min-h-0 flex-1 flex-col"
          >
            <div className="min-h-[10rem] flex-1 [&_canvas]:bg-transparent">
              {memberChart.labels.length > 0 ? (
                <Bar data={memberBarData} options={memberBarChartOptions} />
              ) : (
                <ChartEmpty message="No completed work to show yet." />
              )}
            </div>
          </DashboardSection>
        </div>

        <DashboardSection
          title="Recent Activity"
          className="flex min-h-0 flex-col lg:border-l lg:border-[#2A1814]/[0.08] lg:pl-10"
        >
          {activityItems.length === 0 ? (
            <ChartEmpty message="No recent activity." />
          ) : (
            <div className="min-h-0 flex-1">
              {activityItems.map((item, index) => (
                <ActivityListRow
                  key={item.title + item.detail}
                  item={item}
                  isLast={index === activityItems.length - 1}
                  showBy={false}
                  showTime={false}
                  rowIndex={index}
                />
              ))}
            </div>
          )}
          <div className="mt-4 flex shrink-0 justify-center border-t border-[#2A1814]/[0.06] pt-4">
            <Link
              to="/dashboard/activity"
              className="inline-flex min-w-[220px] items-center justify-center rounded-full bg-[#2A1814] px-10 py-2.5 text-sm font-medium text-white transition hover:bg-[#1d110e]"
            >
              View all
            </Link>
          </div>
        </DashboardSection>
      </div>
    </div>
  );
}

export default DashboardHome;
