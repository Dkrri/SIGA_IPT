import httpClient from '../http-common';

const getByPeriod = (academicPeriodId) => {
  return httpClient.get(`/api/matriculations/period/${academicPeriodId}`);
};

const create = (data) => {
  return httpClient.post('/api/matriculations', data);
};

export default { getByPeriod, create };
