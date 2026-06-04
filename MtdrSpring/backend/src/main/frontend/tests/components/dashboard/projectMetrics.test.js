/**
 * Pruebas de las funciones que alimentan las gráficas del dashboard de manager.
 * Archivo fuente: src/components/dashboard/projectMetrics.js
 */
import {
  buildBugRatioChartData,
  buildCarryOverChartData,
  buildTimeConsistencyShares,
} from '@/components/dashboard/projectMetrics';

describe('projectMetrics', () => {
  /** Datos de sprints compartidos entre varios tests de gráficas. */
  const sprints = [
    { id: 1, name: 'Sprint A', startDate: '2026-01-01', endDate: '2026-01-14' },
    { id: 2, name: 'Sprint B', startDate: '2026-01-15', endDate: '2026-01-28' },
  ];

  describe('buildCarryOverChartData', () => {
    /**
     * Sin sprints en API, la UI usa una curva demo fija para que el gráfico no quede vacío.
     */
    it('returns demo fallback when there are no sprints', () => {
      const result = buildCarryOverChartData([], []);
      expect(result.labels).toHaveLength(5);
      expect(result.actual).toEqual([5, 7, 12, 7, 6]);
      expect(result.maximum).toBe(9);
    });

    /**
     * Carry-over ≈ % de tareas no DONE por sprint, escalado a máx. 12.
     * Sprint 1: 2/3 abiertas → 8; Sprint 2: 0 abiertas → 0.
     */
    it('computes carry-over from open tasks per sprint', () => {
      const tasks = [
        { sprint: { id: 1 }, status: 'DONE' },
        { sprint: { id: 1 }, status: 'IN_PROGRESS' },
        { sprint: { id: 1 }, status: 'PENDING' },
        { sprint: { id: 2 }, status: 'DONE' },
        { sprint: { id: 2 }, status: 'DONE' },
      ];
      const result = buildCarryOverChartData(tasks, sprints);
      expect(result.labels).toEqual(['Sprint A', 'Sprint B']);
      expect(result.actual[0]).toBe(8);
      expect(result.actual[1]).toBe(0);
      expect(result.maximum).toBeGreaterThanOrEqual(6);
    });
  });

  describe('buildBugRatioChartData', () => {
    /**
     * 2 bugs de 4 tareas en sprint 1 → ratio 0.5, escalado al eje Y (máx. 8) → 4.
     */
    it('scales bug ratio for tasks marked as bugs', () => {
      const tasks = [
        { sprint: { id: 1 }, isBug: true },
        { sprint: { id: 1 }, isBug: false },
        { sprint: { id: 1 }, isBug: true },
        { sprint: { id: 1 }, isBug: false },
      ];
      const result = buildBugRatioChartData(tasks, sprints);
      expect(result.actual[0]).toBe(4);
    });
  });

  describe('buildTimeConsistencyShares', () => {
    /**
     * Sin tareas DONE con expectedHours y hoursDone, devuelve porcentajes placeholder
     * y hasData: false para que la UI no muestre datos inventados como reales.
     */
    it('returns placeholder shares when no completed tasks have estimates', () => {
      const result = buildTimeConsistencyShares([
        { status: 'IN_PROGRESS', expectedHours: 8, hoursDone: 2 },
      ]);
      expect(result.hasData).toBe(false);
      expect(result.onTimePct).toBe(80);
    });

    /**
     * Cuatro tareas DONE: 2 a tiempo, 1 con horas de más, 1 con horas de menos
     * → 50% / 25% / 25% en el gráfico de consistencia.
     */
    it('classifies on-time, extra, and under delivery', () => {
      const result = buildTimeConsistencyShares([
        { status: 'DONE', expectedHours: 10, hoursDone: 10 },
        { status: 'DONE', expectedHours: 10, hoursDone: 10 },
        { status: 'DONE', expectedHours: 10, hoursDone: 14 },
        { status: 'DONE', expectedHours: 10, hoursDone: 5 },
      ]);
      expect(result.hasData).toBe(true);
      expect(result.onTime).toBe(50);
      expect(result.extra).toBe(25);
      expect(result.under).toBe(25);
      expect(result.onTimePct).toBe(50);
    });
  });
});
