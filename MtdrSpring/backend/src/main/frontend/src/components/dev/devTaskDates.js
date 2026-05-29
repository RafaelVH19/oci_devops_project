import moment from 'moment';

function findSprintEndDate(task, sprints) {
  const fromTask = task.sprint?.endDate;
  if (fromTask) return moment(fromTask);
  const sprintId = task.sprint?.id;
  if (!sprintId) return null;
  const sprint = sprints.find((s) => s.id === sprintId);
  return sprint?.endDate ? moment(sprint.endDate) : null;
}

/** Target date used for calendar placement (sprint end, or estimated from creation). */
export function getTaskTargetDate(task, sprints = []) {
  const sprintEnd = findSprintEndDate(task, sprints);
  if (sprintEnd) return sprintEnd.clone().startOf('day');

  const base = task.createdAt ? moment(task.createdAt) : moment();
  const estimateDays = Math.max(1, Math.ceil((Number(task.expectedHours) || 4) / 4));
  return base.clone().add(estimateDays, 'days').startOf('day');
}

export function getTaskTargetKey(task, sprints = []) {
  return getTaskTargetDate(task, sprints).format('YYYY-MM-DD');
}

export function isTaskDone(task) {
  return String(task.status || '').toUpperCase() === 'DONE';
}

export function isTaskOverdue(task, sprints = []) {
  if (isTaskDone(task)) return false;
  return getTaskTargetDate(task, sprints).isBefore(moment(), 'day');
}

export function isTaskDueToday(task, sprints = []) {
  if (isTaskDone(task)) return false;
  return getTaskTargetDate(task, sprints).isSame(moment(), 'day');
}

export function groupTasksByTargetDate(tasks, sprints = []) {
  const map = new Map();
  for (const task of tasks) {
    const key = getTaskTargetKey(task, sprints);
    if (!map.has(key)) map.set(key, []);
    map.get(key).push(task);
  }
  return map;
}

export function sortTasksByTargetDate(tasks, sprints = []) {
  return [...tasks].sort(
    (a, b) => getTaskTargetDate(a, sprints).valueOf() - getTaskTargetDate(b, sprints).valueOf()
  );
}

/** Pending tasks with target date strictly after `fromDay`, soonest first. */
export function getUpcomingTasksAfter(tasks, sprints = [], fromDay = moment(), limit = 5) {
  const from = fromDay.clone().startOf('day');
  return sortTasksByTargetDate(
    tasks.filter(
      (task) => !isTaskDone(task) && getTaskTargetDate(task, sprints).isAfter(from, 'day')
    ),
    sprints
  ).slice(0, limit);
}
