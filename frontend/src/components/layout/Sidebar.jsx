import { useLocation, useNavigate } from 'react-router-dom';
import {
  Box,
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material';
import { DRAWER_WIDTH, MENU_BY_ROLE } from './menuItems';
import { ROLE_LABELS } from '../../ui/labels';

const CURRENT_YEAR = new Date().getFullYear();

/**
 * Permanent sidebar with the menu of the current role.
 */
export default function Sidebar({ role }) {
  const navigate = useNavigate();
  const location = useLocation();
  const items = MENU_BY_ROLE[role] ?? [];
  const homePath = `/${role?.toLowerCase()}`;

  const isSelected = (path) =>
    path === homePath
      ? location.pathname === path
      : location.pathname.startsWith(path);

  const content = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Toolbar />
      <Divider />
      <Typography
        variant="overline"
        sx={{ px: 3, pt: 2, color: 'text.secondary', letterSpacing: '0.12em' }}
      >
        {ROLE_LABELS[role] ?? 'Menú'}
      </Typography>
      <List sx={{ px: 1, py: 1 }}>
        {items.map((item) => {
          const Icon = item.icon;
          return (
            <ListItemButton
              key={item.path}
              selected={isSelected(item.path)}
              onClick={() => navigate(item.path)}
            >
              <ListItemIcon sx={{ minWidth: 38, color: 'inherit' }}>
                <Icon fontSize="small" />
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                slotProps={{
                  primary: {
                    sx: {
                      fontSize: 14,
                      fontWeight: 600,
                      whiteSpace: 'nowrap',
                    },
                  },
                }}
              />
            </ListItemButton>
          );
        })}
      </List>
      <Box sx={{ flexGrow: 1 }} />
      <Divider />
      <Typography variant="caption" color="text.secondary" sx={{ p: 2, textAlign: 'center' }}>
        SIGA IPT · {CURRENT_YEAR}
      </Typography>
    </Box>
  );

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: DRAWER_WIDTH,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: DRAWER_WIDTH,
          boxSizing: 'border-box',
          borderRight: '1px solid',
          borderColor: 'divider',
          backgroundColor: 'background.paper',
        },
      }}
    >
      {content}
    </Drawer>
  );
}
