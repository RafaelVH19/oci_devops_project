/**
 * Pruebas del almacén en memoria usado en modo demo (sin backend Spring).
 * Archivo fuente: src/demo/demoStore.js
 */
import {
  createTask,
  getTaskById,
  getTeams,
  linkTaskToSprint,
  updateTaskById,
} from '@/demo/demoStore';

describe('demoStore', () => {
  describe('getTeams', () => {
    /**
     * getTeams devuelve una copia profunda: mutar el resultado no afecta la siguiente lectura.
     */
    it('returns cloned mock teams with users', () => {
      const teams = getTeams();
      expect(teams.length).toBeGreaterThan(0);
      expect(teams[0]).toHaveProperty('name');
      expect(teams[0].users?.length).toBeGreaterThan(0);

      teams[0].name = 'Mutated';
      expect(getTeams()[0].name).not.toBe('Mutated');
    });
  });

  describe('getTaskById', () => {
    /** Tarea id=1 del mock inicial (Auth middleware). */
    it('returns a known mock task by id', () => {
      const task = getTaskById(1);
      expect(task).not.toBeNull();
      expect(task.title).toBe('Auth middleware');
      expect(task.status).toBe('IN_PROGRESS');
    });

    it('returns null for unknown ids', () => {
      expect(getTaskById(999999)).toBeNull();
    });
  });

  describe('updateTaskById', () => {
    /**
     * normalizeTask convierte TODO a PENDING (convención del tablero dev).
     * Se restaura IN_PROGRESS al final para no dejar el mock sucio para otros tests.
     */
    it('normalizes TODO status to PENDING', () => {
      const updated = updateTaskById(1, { status: 'TODO' });
      expect(updated.status).toBe('PENDING');
      updateTaskById(1, { status: 'IN_PROGRESS' });
    });

    it('returns null when task does not exist', () => {
      expect(updateTaskById(999999, { title: 'Nope' })).toBeNull();
    });
  });

  describe('createTask and linkTaskToSprint', () => {
    /**
     * Flujo típico demo: crear tarea, asociarla a sprint 1 y luego moverla al sprint 2.
     */
    it('creates a normalized task and links it to a sprint', () => {
      const created = createTask(
        { title: 'Jest task', expectedHours: 4, assignedTo: 1, isBug: true },
        1
      );
      expect(created.title).toBe('Jest task');
      expect(created.status).toBe('PENDING');
      expect(created.isBug).toBe(true);
      expect(created.sprint?.id).toBe(1);

      const linked = linkTaskToSprint(2, created.id);
      expect(linked.sprint?.id).toBe(2);

      const fetched = getTaskById(created.id);
      expect(fetched.sprint?.id).toBe(2);
    });
  });
});
