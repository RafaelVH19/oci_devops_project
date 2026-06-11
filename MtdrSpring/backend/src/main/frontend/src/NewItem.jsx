import React, { useEffect, useRef, useState } from 'react';
import { Bug, ListTodo, Plus, X } from 'lucide-react';
import DevTaskForm, { buildDependencyOptions, emptyTaskForm } from './components/dev/DevTaskForm';

function AddTaskModal({ open, onClose, addItem, isInserting, sprints, assigneeOptions, tasks }) {
  const [openMenu, setOpenMenu] = useState(null);
  const [formData, setFormData] = useState(emptyTaskForm);
  const panelRef = useRef(null);

  useEffect(() => {
    if (!open) {
      setFormData(emptyTaskForm);
      setOpenMenu(null);
    }
  }, [open]);

  useEffect(() => {
    if (!open) return undefined;
    function handleKeyDown(event) {
      if (event.key !== 'Escape' || isInserting) return;
      if (openMenu) {
        setOpenMenu(null);
        return;
      }
      onClose();
    }
    document.addEventListener('keydown', handleKeyDown);
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = prevOverflow;
    };
  }, [open, isInserting, onClose, openMenu]);

  if (!open) return null;

  const sprintOptions = [
    { value: '', label: 'No sprint' },
    ...sprints.map((s) => ({ value: String(s.id), label: s.name })),
  ];
  const dependencyOptions = buildDependencyOptions(tasks ?? []);

  function handleSubmit(event) {
    event.preventDefault();
    if (!formData.title.trim()) return;
    Promise.resolve(
      addItem({
        title: formData.title.trim(),
        description: formData.description.trim(),
        expectedHours: Number(formData.expectedHours),
        priority: formData.priority,
        isBug: formData.isBug,
        sprintId: formData.sprintId ? Number(formData.sprintId) : null,
        dependsOnId: formData.dependsOnId ? Number(formData.dependsOnId) : null,
        ...(formData.assignedTo ? { assignedTo: Number(formData.assignedTo) } : {}),
      })
    )
      .then(() => onClose())
      .catch(() => {});
  }

  return (
    <div
      className="dashboard-modal-overlay-enter fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4 py-8"
      role="presentation"
      onMouseDown={(event) => {
        if (!isInserting && event.target === event.currentTarget) onClose();
      }}
    >
      <div
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="add-task-modal-title"
        className="dashboard-modal-panel-enter flex max-h-[min(92vh,56rem)] w-full max-w-3xl flex-col rounded-2xl border border-[#2A1814]/10 bg-white shadow-xl sm:max-w-[44rem]"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="flex shrink-0 items-start justify-between gap-4 border-b border-[#2A1814]/[0.08] px-8 py-6">
          <div className="flex items-start gap-3">
            <span
              className={`mt-0.5 flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${
                formData.isBug ? 'bg-[#c74634]/10 text-[#c74634]' : 'bg-[#2A1814]/[0.06] text-[#2A1814]'
              }`}
            >
              {formData.isBug ? <Bug className="h-5 w-5" aria-hidden /> : <ListTodo className="h-5 w-5" aria-hidden />}
            </span>
            <div>
              <h2 id="add-task-modal-title" className="text-lg font-semibold text-[#2A1814]">
                {formData.isBug ? 'New bug' : 'New task'}
              </h2>
              <p className="mt-1 text-sm text-[#6B6560]">
                Add work to your backlog and optionally assign it to a sprint.
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            disabled={isInserting}
            className="rounded-full p-2 text-[#6B6560] transition hover:bg-[#faf9f6] hover:text-[#2A1814] disabled:opacity-50"
            aria-label="Close"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain px-8 py-6">
          <DevTaskForm
            formId="add-task-form"
            formData={formData}
            setFormData={setFormData}
            openMenu={openMenu}
            setOpenMenu={setOpenMenu}
            sprintOptions={sprintOptions}
            assigneeOptions={assigneeOptions}
            dependencyOptions={dependencyOptions}
            onSubmit={handleSubmit}
          />
        </div>
        <div className="flex shrink-0 justify-end border-t border-[#2A1814]/[0.08] px-8 py-6">
          <button
            type="submit"
            form="add-task-form"
            disabled={isInserting}
            className="inline-flex items-center gap-2 rounded-full bg-[#2A1814] px-6 py-3 text-sm font-medium text-white transition hover:bg-[#1d110e] disabled:cursor-not-allowed disabled:opacity-60"
          >
            <Plus className="h-4 w-4" />
            {isInserting ? 'Adding...' : formData.isBug ? 'Add bug' : 'Add task'}
          </button>
        </div>
      </div>
    </div>
  );
}

function NewItem({ addItem, isInserting, sprints, assigneeOptions, tasks }) {
  const [modalOpen, setModalOpen] = useState(false);

  return (
    <>
      <div className="dashboard-section-enter flex flex-wrap items-center justify-between gap-4 border-b border-[#2A1814]/[0.08] pb-6">
        <p className="text-sm text-[#6B6560]">Add tasks or bugs to your sprint backlog.</p>
        <button
          type="button"
          onClick={() => setModalOpen(true)}
          className="inline-flex items-center gap-2 rounded-full bg-[#2A1814] px-5 py-2.5 text-sm font-medium text-white transition hover:bg-[#1d110e]"
        >
          <Plus className="h-4 w-4" />
          Add task
        </button>
      </div>
      <AddTaskModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        addItem={addItem}
        isInserting={isInserting}
        sprints={sprints}
        assigneeOptions={assigneeOptions}
        tasks={tasks}
      />
    </>
  );
}

export default NewItem;
