import {
  createTask,
  deleteTaskById,
  getSprints,
  getTaskById,
  getTasks,
  getTeams,
  getUsers,
  linkTaskToSprint,
  unlinkTaskFromSprint,
  updateTaskById,
} from './demoStore';

function jsonResponse(data, status = 200, extraHeaders = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { 'Content-Type': 'application/json', ...extraHeaders },
  });
}

function emptyResponse(status = 204) {
  return new Response(null, { status });
}

function parseRequest(input, init = {}) {
  const url = typeof input === 'string' ? input : input?.url ?? '';
  const resolved = new URL(url, window.location.origin);
  const method = (init.method || (typeof input !== 'string' && input?.method) || 'GET').toUpperCase();
  return { pathname: resolved.pathname, method };
}

async function readJson(init) {
  if (!init.body) return {};
  if (typeof init.body === 'string') {
    try {
      return JSON.parse(init.body);
    } catch {
      return {};
    }
  }
  return {};
}

function handleDemoRequest(pathname, method, init) {
  const tasksMatch = pathname.match(/^\/tasks\/(\d+)$/);
  const sprintTasksMatch = pathname.match(/^\/sprint-tasks\/(\d+)\/(\d+)$/);

  if (pathname === '/tasks' && method === 'GET') {
    return jsonResponse(getTasks());
  }

  if (pathname === '/tasks' && method === 'POST') {
    return readJson(init).then((body) => {
      const created = createTask(body, null);
      return new Response(null, {
        status: 201,
        headers: { Location: String(created.id) },
      });
    });
  }

  if (tasksMatch) {
    const id = Number(tasksMatch[1]);
    if (method === 'GET') {
      const task = getTaskById(id);
      return task ? jsonResponse(task) : jsonResponse({ message: 'Not found' }, 404);
    }
    if (method === 'PUT') {
      return readJson(init).then((body) => {
        const updated = updateTaskById(id, body);
        return updated ? jsonResponse(updated) : jsonResponse({ message: 'Not found' }, 404);
      });
    }
    if (method === 'DELETE') {
      return deleteTaskById(id) ? emptyResponse() : jsonResponse({ message: 'Not found' }, 404);
    }
  }

  if (pathname === '/sprints' && method === 'GET') {
    return jsonResponse(getSprints());
  }

  if (pathname === '/sprints' && method === 'POST') {
    return jsonResponse({ id: 99, name: 'Demo sprint' }, 201);
  }

  if (pathname === '/users' && method === 'GET') {
    return jsonResponse(getUsers());
  }

  if (pathname === '/teams' && method === 'GET') {
    return jsonResponse(getTeams());
  }

  if (pathname === '/teams' && method === 'POST') {
    return jsonResponse({ id: 99, name: 'Demo team' }, 201);
  }

  if (pathname === '/team-members' && method === 'POST') {
    return emptyResponse(201);
  }

  if (pathname.startsWith('/team-members/') && method === 'DELETE') {
    return emptyResponse();
  }

  if (pathname === '/sprint-tasks' && method === 'POST') {
    return readJson(init).then((body) => {
      const sprintId = body?.id?.sprintId;
      const taskId = body?.id?.taskId;
      if (sprintId && taskId) linkTaskToSprint(sprintId, taskId);
      return emptyResponse(201);
    });
  }

  if (sprintTasksMatch && method === 'DELETE') {
    const sprintId = Number(sprintTasksMatch[1]);
    const taskId = Number(sprintTasksMatch[2]);
    unlinkTaskFromSprint(sprintId, taskId);
    return emptyResponse();
  }

  if (pathname === '/adduser' && method === 'POST') {
    return jsonResponse({ id: 99, name: 'Demo user' }, 201);
  }

  if (pathname === '/invite-user' && method === 'POST') {
    return jsonResponse(
      {
        user: { id: 99, name: 'Demo user', email: 'demo@lumen.dev', role: 'DEVELOPER' },
        temporaryPassword: 'DemoPass123',
        authAccountCreated: true,
        inviteEmailSent: true,
      },
      201
    );
  }

  if (pathname === '/users/by-email' && method === 'GET') {
    return jsonResponse({
      id: 1,
      name: 'Alex Rivera',
      email: 'alex@lumen.dev',
      role: 'DEVELOPER',
    });
  }

  if (pathname === '/api/genai/chat' && method === 'POST') {
    return jsonResponse({
      reply:
        'Demo mode: Lumi is showing sample data only. Deploy the backend for live AI and database features.',
    });
  }

  return null;
}

export function installDemoFetch() {
  const nativeFetch = window.fetch.bind(window);

  window.fetch = async (input, init = {}) => {
    const { pathname, method } = parseRequest(input, init);
    if (!pathname.startsWith('/') || pathname.startsWith('//')) {
      return nativeFetch(input, init);
    }

    const handled = handleDemoRequest(pathname, method, init);
    if (handled instanceof Promise) return handled;
    if (handled) return handled;

    return nativeFetch(input, init);
  };
}
