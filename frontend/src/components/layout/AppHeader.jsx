import { AppBar, Button, Toolbar, Typography } from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import keycloak from '../../auth/keycloak';

/** Top bar: application title, current user and logout. */
export default function AppHeader({ role }) {
  return (
    <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
      <Toolbar>
        <Typography variant="h6" sx={{ flexGrow: 1 }}>
          SIGA IPT — {role}
        </Typography>

        <Typography variant="body2" sx={{ mr: 2 }}>
          {keycloak.tokenParsed?.preferred_username}
        </Typography>

        <Button
          color="inherit"
          startIcon={<LogoutIcon />}
          onClick={() => keycloak.logout({ redirectUri: window.location.origin })}
        >
          Logout
        </Button>
      </Toolbar>
    </AppBar>
  );
}
