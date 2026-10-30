import httpClient from '../http-common';

const getByCareer = (careerId) => {
  return httpClient.get('/api/study-plans', { params: { careerId } });
};

const create = (data) => {
  return httpClient.post('/api/study-plans', data);
};

const setCurrent = (id) => {
  return httpClient.patch(`/api/study-plans/${id}/current`);
};

const remove = (id) => {
  return httpClient.delete(`/api/study-plans/${id}`);
};

export default { getByCareer, create, setCurrent, remove };
