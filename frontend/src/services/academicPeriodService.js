import httpClient from '../http-common';

const getAll = () => {
  return httpClient.get('/api/academic-periods');
};

const create = (data) => {
  return httpClient.post('/api/academic-periods', data);
};

const close = (id) => {
  return httpClient.post(`/api/academic-periods/${id}/close`);
};

export default { getAll, create, close };
