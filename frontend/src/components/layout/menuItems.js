/** Width of the permanent sidebar, shared by the layout and the main content. */
export const DRAWER_WIDTH = 240;

/** Menu options for each application role. */
export const MENU_BY_ROLE = {
  ADMIN: [
    { label: 'Home', path: '/admin' },
    { label: 'Careers', path: '/admin/careers' },
    { label: 'Study Plans', path: '/admin/study-plans' },
    { label: 'Subjects', path: '/admin/subjects' },
    { label: 'Teachers', path: '/admin/teachers' },
    { label: 'Students', path: '/admin/students' },
    { label: 'Academic Periods', path: '/admin/academic-periods' },
    { label: 'Sections', path: '/admin/sections' },
    { label: 'Matriculations', path: '/admin/matriculations' },
  ],
  TEACHER: [
    { label: 'Home', path: '/teacher' },
    { label: 'My Sections', path: '/teacher/sections' },
  ],
  STUDENT: [
    { label: 'Home', path: '/student' },
    { label: 'Available Subjects', path: '/student/available' },
    { label: 'My Enrollments', path: '/student/enrollments' },
  ],
};
