import { MOCK_SPRINTS, MOCK_TASKS, MOCK_TEAMS } from '../components/dashboard/dashboardMocks';

function clone(value) {
  return structuredClone(value);
}

function normalizeTask(task) {
  const status = task.status === 'TODO' ? 'PENDING' : String(task.status || 'PENDING').toUpperCase();
  return {
    ...task,
    status,
    description: task.description ?? '',
    priority: task.priority ?? 'MEDIUM',
    hoursDone: task.hoursDone ?? 0,
    expectedHours: task.expectedHours ?? 0,
    isBug: Boolean(task.isBug),
    assignedTo: task.assignedTo ?? 1,
    createdBy: task.createdBy ?? 1,
    vector: task.vector ?? 'web',
  };
}

let teams = clone(MOCK_TEAMS);
let tasks = clone(MOCK_TASKS).map(normalizeTask);
let sprints = clone(MOCK_SPRINTS);
let nextTaskId = 1000;

function allUsers() {
  const byId = new Map();
  for (const team of teams) {
    for (const user of team.users ?? []) {
      byId.set(user.id, { ...user });
    }
  }
  return [...byId.values()];
}

function findTask(id) {
  return tasks.find((task) => task.id === Number(id));
}

export function getTeams() {
  return clone(teams);
}

export function getTasks() {
  return clone(tasks);
}

export function getSprints() {
  return clone(sprints);
}

export function getUsers() {
  return allUsers();
}

export function getTaskById(id) {
  const task = findTask(id);
  return task ? clone(task) : null;
}

export function createTask(body, sprintId) {
  const id = nextTaskId++;
  const sprint = sprintId ? sprints.find((s) => s.id === Number(sprintId)) : null;
  const created = normalizeTask({
    id,
    title: body.title,
    description: body.description ?? '',
    status: 'PENDING',
    priority: body.priority ?? 'MEDIUM',
    expectedHours: Number(body.expectedHours ?? 0),
    hoursDone: 0,
    isBug: Boolean(body.isBug),
    assignedTo: body.assignedTo ?? 1,
    createdBy: body.createdBy ?? 1,
    vector: body.vector ?? 'web',
    sprint: sprint ? { id: sprint.id, name: sprint.name } : null,
  });
  tasks = [created, ...tasks];
  return clone(created);
}

export function updateTaskById(id, patch) {
  const index = tasks.findIndex((task) => task.id === Number(id));
  if (index < 0) return null;
  const current = tasks[index];
  const sprintId = patch.sprintId;
  let sprint = current.sprint;
  if (sprintId !== undefined) {
    if (sprintId) {
      const match = sprints.find((s) => s.id === Number(sprintId));
      sprint = match ? { id: match.id, name: match.name } : null;
    } else {
      sprint = null;
    }
  }
  const updated = normalizeTask({
    ...current,
    ...patch,
    sprint,
  });
  tasks = tasks.map((task, i) => (i === index ? updated : task));
  return clone(updated);
}

export function deleteTaskById(id) {
  const before = tasks.length;
  tasks = tasks.filter((task) => task.id !== Number(id));
  return tasks.length < before;
}

export function linkTaskToSprint(sprintId, taskId) {
  const sprint = sprints.find((s) => s.id === Number(sprintId));
  if (!sprint) return null;
  return updateTaskById(taskId, { sprintId: sprint.id });
}

export function unlinkTaskFromSprint(_sprintId, taskId) {
  return updateTaskById(taskId, { sprintId: null });
}
