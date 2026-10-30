import httpClient from '../http-common';

/** Sections of the authenticated teacher. The backend resolves the teacher from the JWT. */
const getMySections = () => {
  return httpClient.get('/api/sections/my');
};

/** All sections, optionally filtered by academic period. */
const getAll = (academicPeriodId) => {
  return httpClient.get('/api/sections', {
    params: academicPeriodId ? { academicPeriodId } : {},
  });
};

/** One section by id. */
const getById = (id) => {
  return httpClient.get(`/api/sections/${id}`);
};

const create = (data) => {
  return httpClient.post('/api/sections', data);
};

const update = (id, data) => {
  return httpClient.put(`/api/sections/${id}`, data);
};

const remove = (id) => {
  return httpClient.delete(`/api/sections/${id}`);
};

export default { getMySections, getAll, getById, create, update, remove };
