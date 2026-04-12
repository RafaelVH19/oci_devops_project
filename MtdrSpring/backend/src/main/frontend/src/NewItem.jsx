/*
## MyToDoReact version 1.0.
##
## Copyright (c) 2022 Oracle, Inc.
## Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/
/*
 * Component that supports creating a new todo item.
 * @author  jean.de.lavarene@oracle.com
 */

import React, { useState } from "react";

function NewItem(props) {
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    status: "PENDING",
    priority: "MEDIUM",
    sprintId: ""
  });

  function handleSubmit(e) {
    e.preventDefault();
    if (!formData.title.trim()) return;
    props.addItem({
      title: formData.title.trim(),
      description: formData.description.trim(),
      status: formData.status,
      priority: formData.priority,
      sprintId: formData.sprintId ? Number(formData.sprintId) : null
    });
    setFormData({
      title: "",
      description: "",
      status: "PENDING",
      priority: "MEDIUM",
      sprintId: ""
    });
  }

  function handleChange(e) {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  }

  return (
    <div className="w-full">
      <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-6">
        <input
          placeholder="Task title"
          name="title"
          type="text"
          autoComplete="off"
          value={formData.title}
          onChange={handleChange}
          className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-gray-900 placeholder:text-gray-400 focus:border-red-500 focus:outline-none focus:ring-2 focus:ring-red-200 lg:col-span-2"
        />
        <input
          placeholder="Description"
          name="description"
          type="text"
          autoComplete="off"
          value={formData.description}
          onChange={handleChange}
          className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-gray-900 placeholder:text-gray-400 focus:border-red-500 focus:outline-none focus:ring-2 focus:ring-red-200 lg:col-span-2"
        />
        <select
          name="status"
          value={formData.status}
          onChange={handleChange}
          className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-gray-900 focus:border-red-500 focus:outline-none focus:ring-2 focus:ring-red-200"
        >
          <option value="PENDING">PENDING</option>
          <option value="IN_PROGRESS">IN_PROGRESS</option>
          <option value="DONE">DONE</option>
        </select>
        <select
          name="priority"
          value={formData.priority}
          onChange={handleChange}
          className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-gray-900 focus:border-red-500 focus:outline-none focus:ring-2 focus:ring-red-200"
        >
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
        </select>
        <select
          name="sprintId"
          value={formData.sprintId}
          onChange={handleChange}
          className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-gray-900 focus:border-red-500 focus:outline-none focus:ring-2 focus:ring-red-200"
        >
          <option value="">No sprint</option>
          {props.sprints.map((sprint) => (
            <option key={sprint.id} value={sprint.id}>
              {sprint.name}
            </option>
          ))}
        </select>
        <button
          type="submit"
          disabled={props.isInserting}
          className="inline-flex items-center justify-center rounded-lg bg-red-500 px-5 py-2 text-sm font-medium text-white transition hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-300 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {props.isInserting ? "Adding..." : "Add Task"}
        </button>
      </form>
    </div>
  );
}

export default NewItem;