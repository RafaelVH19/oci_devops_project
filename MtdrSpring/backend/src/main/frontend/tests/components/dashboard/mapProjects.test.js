/**
 * Pruebas del mapeo API (teams, tasks, sprints) → tarjetas de proyecto del dashboard.
 * Archivo fuente: src/components/dashboard/mapProjects.js
 */
import {
  mapTeamsToProjects,
  partitionProjects,
  sprintsForTeam,
  summarizeProjects,
} from '@/components/dashboard/mapProjects';

describe('mapProjects', () => {
  const teams = [
    {
      id: 10,
      name: 'Alpha',
      users: [
        { id: 1, name: 'Alex Rivera', email: 'alex@test.com' },
        { id: 2, name: 'Bob Smith', email: 'bob@test.com' },
      ],
    },
    {
      id: 20,
      name: 'Beta',
      users: [{ id: 3, name: 'Casey Lee', email: 'casey@test.com' }],
    },
  ];

  const sprints = [
    { id: 1, name: 'S1', startDate: '2026-01-01', endDate: '2026-01-14' },
    { id: 2, name: 'S2', startDate: '2026-02-01', endDate: '2026-02-14' },
  ];

  /** Tareas asignadas a miembros de Alpha (1,2) y Beta (3). */
  const tasks = [
    { id: 1, assignedTo: 1, status: 'DONE', sprint: { id: 1, name: 'S1' } },
    { id: 2, assignedTo: 1, status: 'IN_PROGRESS', sprint: { id: 1, name: 'S1' } },
    { id: 3, assignedTo: 3, status: 'PENDING', sprint: { id: 2, name: 'S2' } },
  ];

  describe('mapTeamsToProjects', () => {
    /**
     * Cuenta tareas por equipo, estado Active, iniciales del primer miembro (AR)
     * y nombre del sprint activo en las tareas del equipo.
     */
    it('maps team tasks, status, and member initials', () => {
      const projects = mapTeamsToProjects(teams, tasks, sprints);
      const alpha = projects.find((p) => p.id === 10);
      const beta = projects.find((p) => p.id === 20);

      expect(alpha.taskCount).toBe(2);
      expect(alpha.openTasks).toBe(1);
      expect(alpha.doneTasks).toBe(1);
      expect(alpha.status).toBe('Active');
      expect(alpha.members[0].initials).toBe('AR');

      expect(beta.taskCount).toBe(1);
      expect(beta.status).toBe('Active');
      expect(beta.activeSprintName).toBe('S2');
    });

    /**
     * Si todas las tareas del equipo están DONE y hay al menos una, status = Completed.
     */
    it('marks team as completed when all tasks are done', () => {
      const doneOnly = [
        { id: 9, assignedTo: 1, status: 'DONE', sprint: { id: 1, name: 'S1' } },
      ];
      const [project] = mapTeamsToProjects([teams[0]], doneOnly, sprints);
      expect(project.status).toBe('Completed');
      expect(project.openTasks).toBe(0);
    });
  });

  describe('sprintsForTeam', () => {
    /**
     * Extrae ids de sprint de las tareas del equipo y los ordena por startDate.
     */
    it('returns sprints referenced by tasks, sorted by start date', () => {
      const ordered = sprintsForTeam(tasks, sprints);
      expect(ordered.map((s) => s.id)).toEqual([1, 2]);
    });

    /** Tareas sin sprint.id no aportan sprints al equipo. */
    it('returns empty array when tasks have no sprints', () => {
      expect(sprintsForTeam([{ assignedTo: 1, status: 'PENDING' }], sprints)).toEqual([]);
    });
  });

  describe('summarizeProjects and partitionProjects', () => {
    /**
     * summarizeProjects agrega totales de proyectos, miembros, tareas abiertas y % completado.
     */
    it('aggregates KPIs across project cards', () => {
      const projects = mapTeamsToProjects(teams, tasks, sprints);
      const summary = summarizeProjects(projects);

      expect(summary.totalProjects).toBe(2);
      expect(summary.totalMembers).toBe(3);
      expect(summary.openTasks).toBe(2);
      expect(summary.completionRate).toBe(33);
    });

    /**
     * partitionProjects separa listas para pestañas "activos" vs "completados".
     */
    it('splits active and completed project lists', () => {
      const projects = mapTeamsToProjects(teams, tasks, sprints);
      const completedTeam = mapTeamsToProjects(
        [teams[0]],
        [{ id: 1, assignedTo: 1, status: 'DONE', sprint: { id: 1 } }],
        sprints
      );
      const mixed = [...projects.filter((p) => p.id === 20), ...completedTeam];
      const { active, completed } = partitionProjects(mixed);

      expect(active).toHaveLength(1);
      expect(completed).toHaveLength(1);
      expect(completed[0].status).toBe('Completed');
    });
  });
});
