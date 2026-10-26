import apiClient from './client';

const STORAGE_KEY = 'autoreport_users';

const readLocalUsers = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
};

const writeLocalUsers = (users) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(users));
};

const mergeWithLocalUsers = (remoteUsers = []) => {
  const localUsers = readLocalUsers();

  if (!Array.isArray(remoteUsers) || remoteUsers.length === 0) {
    return localUsers;
  }

  const merged = [...localUsers, ...remoteUsers.filter((user) => !localUsers.some((localUser) => localUser.id === user.id && localUser.email === user.email))];

  const deduped = merged.filter((user, index, array) => {
    const key = `${user.id ?? 'new'}:${user.email ?? ''}`;
    return array.findIndex((item) => `${item.id ?? 'new'}:${item.email ?? ''}` === key) === index;
  });

  writeLocalUsers(deduped);
  return deduped;
};

// GET /api/admin/users
export const listUsers = async () => {
  try {
    const response = await apiClient.get('/api/admin/users');
    const data = Array.isArray(response.data) ? response.data : [];
    return mergeWithLocalUsers(data);
  } catch (error) {
    return readLocalUsers();
  }
};

// POST /api/admin/users
export const createUser = async (payload) => {
  try {
    const response = await apiClient.post('/api/admin/users', payload);
    const createdUser = response.data || { ...payload, id: Date.now(), enabled: true };
    const localUsers = readLocalUsers();
    const nextUsers = [
      ...localUsers.filter((user) => !(user.email && payload.email && user.email.toLowerCase() === payload.email.toLowerCase())),
      {
        ...createdUser,
        fullName: createdUser.fullName || payload.fullName,
        email: createdUser.email || payload.email,
        phone: createdUser.phone || payload.phone || '',
        photoUrl: createdUser.photoUrl || payload.photoUrl || '',
        role: createdUser.role || payload.role,
        enabled: createdUser.enabled ?? true,
      },
    ];
    writeLocalUsers(nextUsers);
    return createdUser;
  } catch (error) {
    const fallbackUser = {
      id: Date.now(),
      fullName: payload.fullName,
      email: payload.email,
      phone: payload.phone || '',
      photoUrl: payload.photoUrl || '',
      role: payload.role || 'USER',
      enabled: true,
      password: payload.password,
    };
    const localUsers = readLocalUsers();
    const nextUsers = [
      ...localUsers.filter((user) => user.email.toLowerCase() !== fallbackUser.email.toLowerCase()),
      fallbackUser,
    ];
    writeLocalUsers(nextUsers);
    return fallbackUser;
  }
};

// PUT /api/admin/users/{id}/enable | disable
export const setUserEnabled = async (id, enabled) => {
  try {
    await apiClient.put(`/api/admin/users/${id}/${enabled ? 'enable' : 'disable'}`);
  } catch (error) {
    const localUsers = readLocalUsers();
    const nextUsers = localUsers.map((user) =>
      Number(user.id) === Number(id) ? { ...user, enabled } : user
    );
    writeLocalUsers(nextUsers);
  }
};

// PUT /api/admin/users/{id}/role?role=
export const changeUserRole = async (id, role) => {
  try {
    await apiClient.put(`/api/admin/users/${id}/role`, null, { params: { role } });
  } catch (error) {
    const localUsers = readLocalUsers();
    const nextUsers = localUsers.map((user) =>
      Number(user.id) === Number(id) ? { ...user, role } : user
    );
    writeLocalUsers(nextUsers);
  }
};

// GET /api/admin/users/permissions/available
export const getAvailablePermissions = async () => {
  const response = await apiClient.get('/api/admin/users/permissions/available');
  return Array.isArray(response.data) ? response.data : [];
};

// GET /api/admin/users/{id}/permissions
export const getUserPermissions = async (id) => {
  const response = await apiClient.get(`/api/admin/users/${id}/permissions`);
  return Array.isArray(response.data) ? response.data : [];
};

// PUT /api/admin/users/{id}/permissions
export const setUserPermissions = async (id, permissions) => {
  const response = await apiClient.put(`/api/admin/users/${id}/permissions`, { permissions });
  return response.data;
};

// DELETE /api/admin/users/{id}
export const deleteUser = async (id) => {
  try {
    await apiClient.delete(`/api/admin/users/${id}`);
  } catch (error) {
    const localUsers = readLocalUsers();
    const nextUsers = localUsers.filter((user) => Number(user.id) !== Number(id));
    writeLocalUsers(nextUsers);
  }
};
