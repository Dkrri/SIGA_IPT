import httpClient from '../http-common';

/** Enrollments and current grades of a section (only its responsible teacher). */
const getBySection = (sectionId) => {
  return httpClient.get(`/api/sections/${sectionId}/grades`);
};

/** Creates or updates the grade of one enrollment. */
const saveGrade = (enrollmentId, finalGrade) => {
  return httpClient.put(`/api/enrollments/${enrollmentId}/grade`, { finalGrade });
};

export default { getBySection, saveGrade };
