import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Bar } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

function Manager() {
  const navigate = useNavigate();
  const [tasks, setTasks] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      try {
        const [tasksRes, usersRes] = await Promise.all([fetch('/tasks'), fetch('/users')]);
        const tasksJson = tasksRes.ok ? await tasksRes.json() : [];
        const usersJson = usersRes.ok ? await usersRes.json() : [];
        if (!cancelled) {
          setTasks(tasksJson);
          setUsers(usersJson);
        }
      } catch (e) {
        console.error(e);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => (cancelled = true);
  }, []);

  function buildCharts() {
    // Only consider DONE tasks
    const doneTasks = tasks.filter((t) => t.status === 'DONE');

    // sprints list (use sprint.name or 'No sprint')
    const sprintNames = Array.from(
      new Set(doneTasks.map((t) => (t.sprint && t.sprint.name) ? t.sprint.name : 'No sprint'))
    ).sort((left, right) => {
      if (left === 'No sprint') return 1;
      if (right === 'No sprint') return -1;
      return left.localeCompare(right, undefined, { numeric: true, sensitivity: 'base' });
    });

    // users involved
    const userIds = Array.from(new Set(doneTasks.map((t) => t.assignedTo)));

    const usersMap = {};
    users.forEach((u) => {
      usersMap[u.id] = u.name || u.username || `User ${u.id}`;
    });

    // palette
    const colors = [
      '#2563eb', '#f97316', '#16a34a', '#dc2626', '#7c3aed', '#0ea5e9', '#f43f5e'
    ];

    const taskDatasets = userIds.map((uid, idx) => ({
      label: usersMap[uid] || `User ${uid}`,
      backgroundColor: colors[idx % colors.length],
      data: sprintNames.map((sprintName) =>
        doneTasks.filter((t) => ((t.sprint && t.sprint.name) ? t.sprint.name : 'No sprint') === sprintName && t.assignedTo === uid).length
      )
    }));

    const hoursDatasets = userIds.map((uid, idx) => ({
      label: usersMap[uid] || `User ${uid}`,
      backgroundColor: colors[idx % colors.length],
      data: sprintNames.map((sprintName) =>
        doneTasks.reduce((acc, t) => {
          const name = (t.sprint && t.sprint.name) ? t.sprint.name : 'No sprint';
          if (name === sprintName && t.assignedTo === uid) {
            return acc + (t.hoursDone || 0);
          }
          return acc;
        }, 0)
      )
    }));

    const tasksChartData = {
      labels: sprintNames,
      datasets: taskDatasets
    };

    const hoursChartData = {
      labels: sprintNames,
      datasets: hoursDatasets
    };

    return { tasksChartData, hoursChartData };
  }

  const { tasksChartData, hoursChartData } = buildCharts();

  return (
    <section className="text-gray-600 body-font min-h-screen">
      <div className="container mx-auto px-5 py-12 md:py-20">
        <div className="mx-auto max-w-5xl rounded-2xl bg-white shadow-xl ring-1 ring-gray-100">
          <div className="border-b border-gray-100 px-6 py-6 sm:px-8 flex items-center justify-between">
            <div>
              <h1 className="title-font text-3xl font-semibold text-gray-900 sm:text-4xl">Manager Dashboard</h1>
              <p className="mt-2 text-sm leading-relaxed text-gray-500">Overview of completed tasks and hours per sprint</p>
            </div>
            <div>
              <button onClick={() => navigate('/login')} className="inline-flex items-center rounded-lg bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700">Return</button>
            </div>
          </div>

          <div className="px-6 py-6 sm:px-8">
            {loading && <p>Loading...</p>}

            {!loading && (
              <div className="space-y-8">
                <div>
                  <h2 className="mb-4 text-lg font-semibold text-gray-900">Tasks DONE per sprint (by person)</h2>
                  <div className="overflow-x-auto rounded-xl border border-gray-200 p-4">
                    <Bar
                      data={tasksChartData}
                      options={{
                        responsive: true,
                        plugins: {
                          legend: { position: 'top' },
                          title: { display: false }
                        }
                      }}
                    />
                  </div>
                </div>

                <div>
                  <h2 className="mb-4 text-lg font-semibold text-gray-900">Hours worked per sprint (by person)</h2>
                  <div className="overflow-x-auto rounded-xl border border-gray-200 p-4">
                    <Bar
                      data={hoursChartData}
                      options={{
                        responsive: true,
                        plugins: {
                          legend: { position: 'top' },
                          title: { display: false }
                        }
                      }}
                    />
                  </div>
                </div>
              </div>
            )}

            {!loading && tasks.length === 0 && (
              <p className="mt-4 text-sm text-gray-500">No tasks available to generate charts.</p>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}

export default Manager;
