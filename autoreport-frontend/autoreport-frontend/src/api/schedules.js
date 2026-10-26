import apiClient from './client';

// GET /api/admin/schedules
export const listSchedules = () => apiClient.get('/api/admin/schedules').then((res) => res.data);

// POST /api/admin/schedules
export const createSchedule = (payload) =>
  apiClient.post('/api/admin/schedules', payload).then((res) => res.data);

// PUT /api/admin/schedules/{id}
export const updateSchedule = (id, payload) =>
  apiClient.put(`/api/admin/schedules/${id}`, payload).then((res) => res.data);

// DELETE /api/admin/schedules/{id}
export const deleteSchedule = (id) => apiClient.delete(`/api/admin/schedules/${id}`);
