import { Box, Toolbar } from '@mui/material';
import AppHeader from './AppHeader';
import Sidebar from './Sidebar';
import { DRAWER_WIDTH } from './menuItems';
import { getPrimaryRole } from '../../auth/keycloak';

/**
 * Application shell: top bar, sidebar and the page content.
 * The page rendered by React Router arrives through `children`.
 */
export default function Layout({ children }) {
  const role = getPrimaryRole();

  return (
    <Box sx={{ display: 'flex' }}>
      <AppHeader role={role} />
      <Sidebar role={role} />

      <Box
        component="main"
        sx={{ flexGrow: 1, p: 3, width: `calc(100% - ${DRAWER_WIDTH}px)` }}
      >
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
}
