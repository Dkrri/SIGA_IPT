import httpClient from '../http-common';

const getMe = () => {
  return httpClient.get('/api/students/me');
};

const getAll = () => {
  return httpClient.get('/api/students');
};

const create = (data) => {
  return httpClient.post('/api/students', data);
};

const update = (id, data) => {
  return httpClient.put(`/api/students/${id}`, data);
};

const changeStatus = (id, status) => {
  return httpClient.patch(`/api/students/${id}/status`, null, {
    params: { status },
  });
};

const remove = (id) => {
  return httpClient.delete(`/api/students/${id}`);
};

export default { getMe, getAll, create, update, changeStatus, remove };
