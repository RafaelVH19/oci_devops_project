/*
## MyToDoReact version 1.0.
##
## Copyright (c) 2022 Oracle, Inc.
## Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/
import React, { useState, useEffect } from 'react';
import NewItem from './NewItem';
import API_LIST from './API';
import DeleteIcon from '@mui/icons-material/Delete';
import { CircularProgress } from '@mui/material';
import moment from 'moment';

function App() {
  const [isLoading, setLoading] = useState(true);
  const [isInserting, setInserting] = useState(false);
  const [items, setItems] = useState([]);
  const [sprints, setSprints] = useState([]);
  const [error, setError] = useState();

  function deleteItem(deleteId) {
    fetch(API_LIST + '/' + deleteId, {
      method: 'DELETE'
    })
      .then((response) => {
        if (response.ok) return response;
        throw new Error('Something went wrong ...');
      })
      .then(
        () => {
          setItems((prev) => prev.filter((item) => item.id !== deleteId));
        },
        (err) => setError(err)
      );
  }

  useEffect(() => {
    let cancelled = false;

    (async () => {
      try {
        const [tasksResponse, sprintsResponse] = await Promise.all([
          fetch(API_LIST),
          fetch('/sprints')
        ]);

        if (!tasksResponse.ok) {
          throw new Error('Could not load tasks');
        }

        const tasks = await tasksResponse.json();
        const sprintResult = sprintsResponse.ok ? await sprintsResponse.json() : [];

        if (!cancelled) {
          setItems(tasks);
          setSprints(sprintResult);
        }
      } catch (err) {
        if (!cancelled) setError(err);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  function addItem(taskData) {
    setInserting(true);
    const data = {
      title: taskData.title,
      description: taskData.description,
      status: taskData.status,
      priority: taskData.priority,
      assignedTo: 1,
      createdBy: 1,
      vector: 'web'
    };

    fetch(API_LIST, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    })
      .then((response) => {
        if (response.ok) return response;
        throw new Error('Something went wrong ...');
      })
      .then((result) => {
        const id = Number(result.headers.get('location'));
        if (!id) {
          throw new Error('Task created but no location header was returned');
        }

        if (!taskData.sprintId) {
          return fetch(API_LIST + '/' + id).then((taskResponse) => {
            if (!taskResponse.ok) {
              throw new Error('Task created but it could not be reloaded');
            }
            return taskResponse.json();
          });
        }

        return fetch('/sprint-tasks', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            id: {
              sprintId: taskData.sprintId,
              taskId: id
            },
            addedAt: moment().format('YYYY-MM-DDTHH:mm:ss')
          })
        }).then((linkResponse) => {
          if (!linkResponse.ok) {
            throw new Error('Task created, but sprint assignment failed');
          }
          return fetch(API_LIST + '/' + id).then((taskResponse) => {
            if (!taskResponse.ok) {
              throw new Error('Task created but it could not be reloaded');
            }
            return taskResponse.json();
          });
        });
      })
      .then((createdTask) => {
        setItems((prev) => [createdTask, ...prev]);
        setInserting(false);
      })
      .catch((err) => {
        setInserting(false);
        setError(err);
      });
  }

  return (
    <section className="text-gray-600 body-font min-h-screen">
      <div className="container mx-auto px-5 py-12 md:py-20">
        <div className="mx-auto max-w-4xl rounded-2xl bg-white shadow-xl ring-1 ring-gray-100">
          <div className="border-b border-gray-100 px-6 py-6 sm:px-8">
            <h1 className="title-font text-3xl font-semibold text-gray-900 sm:text-4xl">Sprint 2</h1>
            <p className="mt-2 text-sm leading-relaxed text-gray-500">Maneja tus tareas cada dia</p>
          </div>

          <div className="px-6 py-6 sm:px-8">
            <NewItem addItem={addItem} isInserting={isInserting} sprints={sprints} />

            {error && (
              <p className="mt-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                Error: {error.message}
              </p>
            )}

            {isLoading && (
              <div className="mt-8 flex justify-center">
                <CircularProgress />
              </div>
            )}

            {!isLoading && (
              <div className="mt-8">
                <h2 className="mb-4 text-lg font-semibold text-gray-900">Tasks</h2>
                <div className="overflow-x-auto rounded-xl border border-gray-200">
                  <table className="min-w-full divide-y divide-gray-200 bg-white">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Title</th>
                        <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Description</th>
                        <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Status</th>
                        <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Priority</th>
                        <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Sprint</th>
                        <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-600">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                      {items.map((item) => (
                        <tr key={item.id}>
                          <td className="px-4 py-3 text-sm font-medium text-gray-900">{item.title}</td>
                          <td className="max-w-xs px-4 py-3 text-sm text-gray-700">{item.description || '-'}</td>
                          <td className="px-4 py-3 text-sm text-gray-700">{item.status}</td>
                          <td className="px-4 py-3 text-sm text-gray-700">{item.priority}</td>
                          <td className="px-4 py-3 text-sm text-gray-700">{item.sprint?.name || 'No sprint'}</td>
                          <td className="px-4 py-3 text-right">
                            <button
                              type="button"
                              onClick={() => deleteItem(item.id)}
                              className="inline-flex items-center justify-center rounded-lg bg-red-600 px-3 py-2 text-xs font-medium text-white transition hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500"
                            >
                              <DeleteIcon fontSize="small" />
                              <span className="ml-1">Delete</span>
                            </button>
                          </td>
                        </tr>
                      ))}
                      {items.length === 0 && (
                        <tr>
                          <td className="px-4 py-4 text-sm text-gray-500" colSpan={6}>
                            No tasks found.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}

export default App;