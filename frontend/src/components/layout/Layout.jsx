import { Box, Toolbar } from '@mui/material';
import AppHeader from './AppHeader';
import Sidebar from './Sidebar';
import { getPrimaryRole } from '../../auth/keycloak';

/**
 * Application shell: top bar, sidebar and the page content.
 * The page rendered by React Router arrives through `children`.
 */
export default function Layout({ children }) {
  const role = getPrimaryRole();

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppHeader role={role} />
      <Sidebar role={role} />

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          p: { xs: 2, md: 3 },
        }}
      >
        <Toolbar />
        <Box sx={{ maxWidth: 1500, mx: 'auto' }}>{children}</Box>
      </Box>
    </Box>
  );
}
