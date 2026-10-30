import httpClient from '../http-common';

const getMe = () => {
  return httpClient.get('/api/teachers/me');
};

const getAll = () => {
  return httpClient.get('/api/teachers');
};

const create = (data) => {
  return httpClient.post('/api/teachers', data);
};

const update = (id, data) => {
  return httpClient.put(`/api/teachers/${id}`, data);
};

const changeStatus = (id, status) => {
  return httpClient.patch(`/api/teachers/${id}/status`, null, {
    params: { status },
  });
};

const remove = (id) => {
  return httpClient.delete(`/api/teachers/${id}`);
};

export default { getMe, getAll, create, update, changeStatus, remove };
