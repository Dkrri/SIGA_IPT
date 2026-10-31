import {
  AppBar,
  Avatar,
  Box,
  Button,
  Chip,
  IconButton,
  Toolbar,
  Typography,
} from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import SchoolIcon from '@mui/icons-material/School';
import keycloak, { getDisplayName } from '../../auth/keycloak';
import { ROLE_LABELS } from '../../ui/labels';

/** Top bar: application brand, current user, role and logout. */
export default function AppHeader({ role }) {
  const displayName = getDisplayName();
  const initial = displayName.charAt(0).toUpperCase();

  return (
    <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
      <Toolbar sx={{ gap: 1 }}>
        <SchoolIcon sx={{ display: { xs: 'none', sm: 'block' }, opacity: 0.9 }} />
        <Typography variant="h6" sx={{ fontWeight: 800, letterSpacing: '0.02em' }}>
          SIGA IPT
        </Typography>

        <Chip
          size="small"
          label={ROLE_LABELS[role] ?? role}
          sx={{
            ml: 1,
            display: { xs: 'none', sm: 'inline-flex' },
            color: '#ffffff',
            backgroundColor: 'rgba(255, 255, 255, 0.18)',
          }}
        />

        <Box sx={{ flexGrow: 1 }} />

        <Typography variant="body2" sx={{ display: { xs: 'none', md: 'block' }, mr: 1 }}>
          {displayName}
        </Typography>
        <Avatar
          sx={{
            width: 34,
            height: 34,
            bgcolor: 'secondary.main',
            color: 'secondary.contrastText',
            fontSize: 14,
            fontWeight: 700,
          }}
        >
          {initial}
        </Avatar>

        <Button
          color="inherit"
          startIcon={<LogoutIcon />}
          onClick={() => keycloak.logout({ redirectUri: window.location.origin })}
          sx={{ ml: 1, display: { xs: 'none', sm: 'inline-flex' } }}
        >
          Cerrar sesión
        </Button>
        <IconButton
          color="inherit"
          onClick={() => keycloak.logout({ redirectUri: window.location.origin })}
          aria-label="Cerrar sesión"
          sx={{ display: { xs: 'inline-flex', sm: 'none' } }}
        >
          <LogoutIcon />
        </IconButton>
      </Toolbar>
    </AppBar>
  );
}
