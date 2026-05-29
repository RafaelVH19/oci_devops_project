import { useMemo, useState } from 'react';
import moment from 'moment';
import { AlertCircle, CalendarDays, ChevronLeft, ChevronRight, Clock } from 'lucide-react';
import {
  getTaskTargetDate,
  getTaskTargetKey,
  getUpcomingTasksAfter,
  groupTasksByTargetDate,
  isTaskDone,
  isTaskOverdue,
  sortTasksByTargetDate,
} from './devTaskDates';

const WEEKDAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

function formatStatusLabel(status) {
  const normalized = String(status || '').toLowerCase().replace(/_/g, ' ');
  return normalized.charAt(0).toUpperCase() + normalized.slice(1);
}

function formatTargetLabel(target, today) {
  if (target.isSame(today, 'day')) return 'Today';
  if (target.isSame(today.clone().add(1, 'day'), 'day')) return 'Tomorrow';
  if (target.isBefore(today, 'day')) return target.format('MMM D');
  if (target.diff(today, 'days') <= 6) return target.format('dddd');
  return target.format('MMM D');
}

function DayTaskItem({ task, sprints, onSelect, formatStatusLabel: formatLabel, showDueDate, today }) {
  const overdue = isTaskOverdue(task, sprints);
  const done = isTaskDone(task);
  const target = getTaskTargetDate(task, sprints);

  return (
    <button
      type="button"
      onClick={() => onSelect(task)}
      className={`w-full px-0 py-2.5 text-left transition hover:bg-[#2A1814]/[0.02] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#c74634] ${
        overdue ? 'text-[#c74634]' : ''
      }`}
    >
      <p className={`truncate text-sm font-medium ${done ? 'text-[#6B6560] line-through' : 'text-[#2A1814]'}`}>
        {task.title}
      </p>
      <p className="mt-0.5 flex flex-wrap items-center gap-x-2 gap-y-0.5 text-xs text-[#6B6560]">
        {showDueDate && (
          <span className="font-medium text-[#2A1814]">{formatTargetLabel(target, today)}</span>
        )}
        <span>{formatLabel(task.status)}</span>
        {task.sprint?.name && <span>· {task.sprint.name}</span>}
        {overdue && (
          <span className="inline-flex items-center gap-0.5 font-medium text-[#c74634]">
            <AlertCircle className="h-3 w-3" aria-hidden />
            Overdue
          </span>
        )}
      </p>
    </button>
  );
}

export default function DevTaskCalendar({
  tasks,
  sprints,
  onTaskSelect,
  formatStatusLabel: formatLabel = formatStatusLabel,
}) {
  const today = moment().startOf('day');
  const [viewMonth, setViewMonth] = useState(() => moment().startOf('month'));
  const [selectedKey, setSelectedKey] = useState(() => today.format('YYYY-MM-DD'));

  const tasksByDate = useMemo(() => groupTasksByTargetDate(tasks, sprints), [tasks, sprints]);

  const pendingTasks = useMemo(() => tasks.filter((t) => !isTaskDone(t)), [tasks]);

  const upcomingTasks = useMemo(
    () => sortTasksByTargetDate(pendingTasks, sprints).slice(0, 5),
    [pendingTasks, sprints]
  );

  const overdueCount = useMemo(
    () => pendingTasks.filter((t) => isTaskOverdue(t, sprints)).length,
    [pendingTasks, sprints]
  );

  const calendarDays = useMemo(() => {
    const start = viewMonth.clone().startOf('month').startOf('week');
    const end = viewMonth.clone().endOf('month').endOf('week');
    const days = [];
    const cursor = start.clone();
    while (cursor.isSameOrBefore(end, 'day')) {
      days.push(cursor.clone());
      cursor.add(1, 'day');
    }
    return days;
  }, [viewMonth]);

  const todayKey = today.format('YYYY-MM-DD');
  const selectedTasks = tasksByDate.get(selectedKey) ?? [];
  const isSelectedToday = selectedKey === todayKey;

  const upcomingFallback = useMemo(() => {
    if (!isSelectedToday || selectedTasks.length > 0) return [];
    return getUpcomingTasksAfter(pendingTasks, sprints, today, 5);
  }, [isSelectedToday, selectedTasks.length, pendingTasks, sprints, today]);

  const panelTasks = selectedTasks.length > 0 ? selectedTasks : upcomingFallback;
  const showingUpcomingFallback = selectedTasks.length === 0 && upcomingFallback.length > 0;

  const selectedLabel = moment(selectedKey, 'YYYY-MM-DD').format('dddd, MMMM D');
  const panelAriaLabel = showingUpcomingFallback
    ? `No tasks today; next ${upcomingFallback.length} upcoming`
    : `Tasks for ${selectedLabel}`;

  function handleDaySelect(day) {
    setSelectedKey(day.format('YYYY-MM-DD'));
  }

  function handleTaskClick(task) {
    onTaskSelect?.(task);
  }

  return (
    <aside
      className="dev-task-calendar space-y-6 lg:sticky lg:top-6 lg:self-start lg:pl-6 lg:border-l lg:border-[#2A1814]/[0.08]"
      aria-label="Task schedule"
    >
        <div className="flex items-start gap-3">
          <CalendarDays className="mt-0.5 h-5 w-5 shrink-0 text-[#c74634]" aria-hidden />
          <div className="min-w-0">
            <h2 className="text-base font-semibold text-[#2A1814]">Schedule</h2>
            <p className="mt-0.5 text-xs text-[#6B6560]">
              {overdueCount > 0
                ? `${overdueCount} overdue · click a day to see tasks`
                : 'Upcoming deadlines by sprint or estimate'}
            </p>
          </div>
        </div>

        {upcomingTasks.length > 0 && (
          <section aria-label="Closest deadlines">
            <div className="mb-2 flex items-center gap-1.5">
              <Clock className="h-3.5 w-3.5 text-[#6B6560]" aria-hidden />
              <h3 className="text-xs font-semibold uppercase tracking-wide text-[#6B6560]">Coming up</h3>
            </div>
            <ul className="space-y-2">
              {upcomingTasks.map((task) => {
                const target = getTaskTargetDate(task, sprints);
                const overdue = isTaskOverdue(task, sprints);
                const key = getTaskTargetKey(task, sprints);
                return (
                  <li key={task.id}>
                    <button
                      type="button"
                      onClick={() => {
                        setViewMonth(target.clone().startOf('month'));
                        setSelectedKey(key);
                        handleTaskClick(task);
                      }}
                      className="group flex w-full items-start gap-2 px-0 py-1 text-left transition hover:opacity-80 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#c74634]"
                    >
                      <span
                        className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${
                          overdue ? 'bg-[#c74634]' : 'bg-[#c74634]/55'
                        }`}
                        aria-hidden
                      />
                      <span className="min-w-0 flex-1">
                        <span className="block truncate text-sm font-medium text-[#2A1814] group-hover:text-[#c74634]">
                          {task.title}
                        </span>
                        <span className={`text-xs ${overdue ? 'font-medium text-[#c74634]' : 'text-[#6B6560]'}`}>
                          {overdue ? 'Overdue · ' : ''}
                          {formatTargetLabel(target, today)}
                        </span>
                      </span>
                    </button>
                  </li>
                );
              })}
            </ul>
          </section>
        )}

        <div>
          <div
            className="mb-3 flex items-center justify-between gap-2"
            role="group"
            aria-label="Calendar navigation"
          >
            <button
              type="button"
              onClick={() => setViewMonth((m) => m.clone().subtract(1, 'month'))}
              className="rounded-full p-2 text-[#6B6560] transition hover:bg-[#2A1814]/[0.04] hover:text-[#2A1814] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#c74634]"
              aria-label="Previous month"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <p className="text-sm font-semibold text-[#2A1814]" aria-live="polite">
              {viewMonth.format('MMMM YYYY')}
            </p>
            <button
              type="button"
              onClick={() => setViewMonth((m) => m.clone().add(1, 'month'))}
              className="rounded-full p-2 text-[#6B6560] transition hover:bg-[#2A1814]/[0.04] hover:text-[#2A1814] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#c74634]"
              aria-label="Next month"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>

          <div
            role="grid"
            aria-label={`Calendar for ${viewMonth.format('MMMM YYYY')}`}
            className="select-none"
          >
            <div role="row" className="mb-1 grid grid-cols-7 gap-0.5">
              {WEEKDAYS.map((day) => (
                <div
                  key={day}
                  role="columnheader"
                  className="py-1 text-center text-[10px] font-semibold uppercase tracking-wide text-[#6B6560]"
                >
                  <abbr title={day} className="no-underline">
                    {day}
                  </abbr>
                </div>
              ))}
            </div>

            <div className="grid grid-cols-7 gap-y-1">
              {calendarDays.map((day) => {
                const key = day.format('YYYY-MM-DD');
                const inMonth = day.month() === viewMonth.month();
                const isToday = day.isSame(today, 'day');
                const isSelected = key === selectedKey;
                const dayTasks = tasksByDate.get(key) ?? [];
                const pendingDayTasks = dayTasks.filter((t) => !isTaskDone(t));
                const hasOverdue = pendingDayTasks.some((t) => isTaskOverdue(t, sprints));
                const taskCount = dayTasks.length;
                const hasTasks = taskCount > 0;

                let ariaLabel = day.format('dddd, MMMM D');
                if (isToday) ariaLabel += ', today';
                if (taskCount > 0) {
                  ariaLabel += `, ${taskCount} task${taskCount === 1 ? '' : 's'}`;
                  if (hasOverdue) ariaLabel += ', includes overdue';
                }

                let dayCircleClass =
                  'text-[#6B6560] hover:bg-[#2A1814]/[0.04]';
                if (inMonth) {
                  if (isSelected) {
                    dayCircleClass =
                      'bg-[#2A1814] font-semibold text-white shadow-sm';
                  } else if (isToday) {
                    dayCircleClass =
                      'font-semibold text-[#c74634] ring-2 ring-[#c74634]/35';
                  } else if (hasOverdue) {
                    dayCircleClass =
                      'bg-[#fff6f4] font-medium text-[#c74634] ring-1 ring-[#c74634]/20 hover:bg-[#ffeeea]';
                  } else if (hasTasks) {
                    dayCircleClass =
                      'bg-[#2A1814]/[0.06] font-medium text-[#2A1814] hover:bg-[#2A1814]/[0.09]';
                  }
                }

                return (
                  <div key={key} className="flex justify-center py-0.5">
                    <button
                      type="button"
                      role="gridcell"
                      aria-label={ariaLabel}
                      aria-selected={isSelected}
                      aria-current={isToday ? 'date' : undefined}
                      disabled={!inMonth}
                      onClick={() => inMonth && handleDaySelect(day)}
                      className={`flex h-9 w-9 flex-col items-center justify-center gap-0.5 rounded-full text-sm transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#c74634] disabled:pointer-events-none disabled:opacity-0 ${dayCircleClass}`}
                    >
                      <span className="text-xs leading-none">{inMonth ? day.date() : ''}</span>
                      {inMonth && hasTasks && (
                        <span className="flex h-1 gap-0.5" aria-hidden>
                          {pendingDayTasks.length > 0 && (
                            <span
                              className={`h-1 w-1 rounded-full ${
                                isSelected ? 'bg-white' : 'bg-[#c74634]'
                              }`}
                            />
                          )}
                          {dayTasks.length - pendingDayTasks.length > 0 && (
                            <span
                              className={`h-1 w-1 rounded-full ${
                                isSelected ? 'bg-white/60' : 'bg-[#c74634]/45'
                              }`}
                            />
                          )}
                        </span>
                      )}
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        <section
          className="border-t border-[#2A1814]/[0.08] pt-5"
          aria-live="polite"
          aria-label={panelAriaLabel}
        >
          <h3 className="text-sm font-semibold text-[#2A1814]">{selectedLabel}</h3>
          {showingUpcomingFallback ? (
            <p className="mb-3 mt-1 text-xs text-[#6B6560]">Nothing due today — next up:</p>
          ) : (
            <div className="mb-3" aria-hidden="true" />
          )}
          {panelTasks.length === 0 ? (
            <p className="text-sm text-[#6B6560]">No tasks scheduled for this day.</p>
          ) : (
            <ul className="divide-y divide-[#2A1814]/[0.08]">
              {panelTasks.map((task) => (
                <li key={task.id}>
                  <DayTaskItem
                    task={task}
                    sprints={sprints}
                    onSelect={handleTaskClick}
                    formatStatusLabel={formatLabel}
                    showDueDate={showingUpcomingFallback}
                    today={today}
                  />
                </li>
              ))}
            </ul>
          )}
        </section>
    </aside>
  );
}
