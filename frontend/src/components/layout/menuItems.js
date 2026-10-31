import HomeIcon from '@mui/icons-material/Home';
import SchoolIcon from '@mui/icons-material/School';
import AccountTreeIcon from '@mui/icons-material/AccountTree';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import PersonIcon from '@mui/icons-material/Person';
import GroupsIcon from '@mui/icons-material/Groups';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import ViewWeekIcon from '@mui/icons-material/ViewWeek';
import AssignmentIcon from '@mui/icons-material/Assignment';
import ClassIcon from '@mui/icons-material/Class';
import ChecklistIcon from '@mui/icons-material/Checklist';
import LibraryBooksIcon from '@mui/icons-material/LibraryBooks';

/** Width of the permanent sidebar, shared by the layout and the main content. */
export const DRAWER_WIDTH = 260;

/** Menu options for each application role. */
export const MENU_BY_ROLE = {
  ADMIN: [
    { label: 'Inicio', path: '/admin', icon: HomeIcon },
    { label: 'Carreras', path: '/admin/careers', icon: SchoolIcon },
    { label: 'Planes de estudio', path: '/admin/study-plans', icon: AccountTreeIcon },
    { label: 'Asignaturas', path: '/admin/subjects', icon: MenuBookIcon },
    { label: 'Docentes', path: '/admin/teachers', icon: PersonIcon },
    { label: 'Estudiantes', path: '/admin/students', icon: GroupsIcon },
    { label: 'Períodos académicos', path: '/admin/academic-periods', icon: CalendarMonthIcon },
    { label: 'Secciones', path: '/admin/sections', icon: ViewWeekIcon },
    { label: 'Matrículas', path: '/admin/matriculations', icon: AssignmentIcon },
  ],
  TEACHER: [
    { label: 'Inicio', path: '/teacher', icon: HomeIcon },
    { label: 'Mis secciones', path: '/teacher/sections', icon: ClassIcon },
  ],
  STUDENT: [
    { label: 'Inicio', path: '/student', icon: HomeIcon },
    { label: 'Asignaturas disponibles', path: '/student/available', icon: LibraryBooksIcon },
    { label: 'Mis inscripciones', path: '/student/enrollments', icon: ChecklistIcon },
  ],
};
