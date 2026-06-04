import {
  buildTaskPutBody,
  nextStatus,
  updateTask,
} from '@/components/dev/devTaskApi';

describe('devTaskApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    global.fetch = jest.fn();
  });

  describe('buildTaskPutBody', () => {
    it('merges patch values over original task', () => {
      const task = {
        title: 'Old',
        description: 'Desc',
        status: 'PENDING',
        priority: 'LOW',
        expectedHours: 2,
      };

      const result = buildTaskPutBody(task, {
        title: 'New',
        priority: 'HIGH',
      });

      expect(result.title).toBe('New');
      expect(result.priority).toBe('HIGH');
      expect(result.description).toBe('Desc');
    });

    it('converts expectedHours to number', () => {
      const task = {
        expectedHours: '5',
      };

      const result = buildTaskPutBody(task);

      expect(result.expectedHours).toBe(5);
    });
  });

  describe('nextStatus', () => {
    it('moves PENDING to IN_PROGRESS', () => {
      expect(nextStatus('PENDING'))
        .toBe('IN_PROGRESS');
    });

    it('moves IN_PROGRESS to DONE', () => {
      expect(nextStatus('IN_PROGRESS'))
        .toBe('DONE');
    });

    it('keeps DONE as DONE', () => {
      expect(nextStatus('DONE'))
        .toBe('DONE');
    });
  });

  describe('updateTask', () => {
    it('sends PUT request', async () => {
      global.fetch.mockResolvedValue({
        ok: true,
        json: async () => ({
          id: 1,
          title: 'Task'
        }),
      });

      const task = {
        id: 1,
        title: 'Task',
        status: 'PENDING',
      };

      await updateTask(task, {
        status: 'DONE',
      });

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/1'),
        expect.objectContaining({
          method: 'PUT',
        })
      );
    });

    it('throws when update fails', async () => {
      global.fetch.mockResolvedValue({
        ok: false,
      });

      await expect(
        updateTask(
          { id: 1 },
          {}
        )
      ).rejects.toThrow(
        'Could not update task'
      );
    });
  });
});