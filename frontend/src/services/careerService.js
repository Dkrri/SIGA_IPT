import httpClient from '../http-common';

const getAll = () => {
  return httpClient.get('/api/careers');
};

const get = (id) => {
  return httpClient.get(`/api/careers/${id}`);
};

const create = (data) => {
  return httpClient.post('/api/careers', data);
};

const update = (id, data) => {
  return httpClient.put(`/api/careers/${id}`, data);
};

const changeStatus = (id, status) => {
  // The backend exposes this operation with PATCH and a query parameter.
  return httpClient.patch(`/api/careers/${id}/status`, null, {
    params: { status },
  });
};

const remove = (id) => {
  return httpClient.delete(`/api/careers/${id}`);
};

export default { getAll, get, create, update, changeStatus, remove };
