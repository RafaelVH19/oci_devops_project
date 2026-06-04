/**
 * Pruebas de utilidades de fechas para el calendario y listas del tablero dev.
 * Archivo fuente: src/components/dev/devTaskDates.js
 */
import moment from 'moment';
import {
  getTaskTargetKey,
  groupTasksByTargetDate,
  isTaskDone,
  isTaskDueToday,
  isTaskOverdue,
} from '@/components/dev/devTaskDates';

describe('devTaskDates', () => {
  describe('isTaskDone', () => {
    /** status DONE (cualquier capitalización interna) cuenta como terminada. */
    it('returns true when status is DONE', () => {
      expect(isTaskDone({ status: 'DONE' })).toBe(true);
    });

    it('returns false for open tasks', () => {
      expect(isTaskDone({ status: 'IN_PROGRESS' })).toBe(false);
    });
  });

  describe('getTaskTargetKey', () => {
    /**
     * La fecha objetivo del calendario es el fin de sprint (YYYY-MM-DD)
     * cuando viene en task.sprint.endDate.
     */
    it('uses sprint end date when provided on the task', () => {
      const task = {
        sprint: { id: 1, endDate: '2026-06-15' },
      };
      expect(getTaskTargetKey(task)).toBe('2026-06-15');
    });

    /**
     * Si la tarea solo trae sprint.id, se busca endDate en el array de sprints del equipo.
     */
    it('resolves sprint end date from sprints list', () => {
      const task = { sprint: { id: 2 } };
      const sprints = [{ id: 2, endDate: '2026-07-01' }];
      expect(getTaskTargetKey(task, sprints)).toBe('2026-07-01');
    });
  });

  describe('isTaskOverdue', () => {
    /** Las tareas DONE nunca se marcan como vencidas. */
    it('returns false for completed tasks', () => {
      const task = {
        status: 'DONE',
        sprint: { endDate: '2020-01-01' },
      };
      expect(isTaskOverdue(task)).toBe(false);
    });

    /** Fecha objetivo anterior a hoy + tarea abierta → vencida. */
    it('returns true when target date is before today', () => {
      const task = {
        status: 'OPEN',
        sprint: { endDate: moment().subtract(2, 'days').format('YYYY-MM-DD') },
      };
      expect(isTaskOverdue(task)).toBe(true);
    });

    it('returns false when target date is in the future', () => {
      const task = {
        status: 'OPEN',
        sprint: { endDate: moment().add(5, 'days').format('YYYY-MM-DD') },
      };
      expect(isTaskOverdue(task)).toBe(false);
    });
  });

  describe('isTaskDueToday', () => {
    /** Sprint termina hoy y la tarea sigue abierta → "due today". */
    it('returns true when sprint ends today and task is open', () => {
      const task = {
        status: 'OPEN',
        sprint: { endDate: moment().format('YYYY-MM-DD') },
      };
      expect(isTaskDueToday(task)).toBe(true);
    });

    it('returns false for completed tasks', () => {
      const task = {
        status: 'DONE',
        sprint: { endDate: moment().format('YYYY-MM-DD') },
      };
      expect(isTaskDueToday(task)).toBe(false);
    });
  });

  describe('groupTasksByTargetDate', () => {
    /**
     * Devuelve un Map donde la clave es YYYY-MM-DD y el valor es el array de tareas
     * que caen en esa fecha en el calendario.
     */
    it('groups tasks by their target date key', () => {
      const tasks = [
        { id: 1, status: 'OPEN', sprint: { endDate: '2026-06-10' } },
        { id: 2, status: 'OPEN', sprint: { endDate: '2026-06-10' } },
        { id: 3, status: 'OPEN', sprint: { endDate: '2026-06-20' } },
      ];
      const grouped = groupTasksByTargetDate(tasks);
      expect(grouped.get('2026-06-10')).toHaveLength(2);
      expect(grouped.get('2026-06-20')).toHaveLength(1);
    });
  });
});
