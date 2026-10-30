import httpClient from '../http-common';

const getAvailable = (academicPeriodId) => {
  return httpClient.get('/api/enrollments/available', {
    params: { academicPeriodId },
  });
};

const enroll = (sectionIds) => {
  return httpClient.post('/api/enrollments', { sectionIds });
};

const getMyEnrollments = (academicPeriodId) => {
  return httpClient.get('/api/enrollments/my', {
    params: { academicPeriodId },
  });
};

const cancel = (id) => {
  return httpClient.delete(`/api/enrollments/${id}`);
};

export default { getAvailable, enroll, getMyEnrollments, cancel };
