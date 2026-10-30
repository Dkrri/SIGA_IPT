import { useLocation, useNavigate } from 'react-router-dom';
import {
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import { DRAWER_WIDTH, MENU_BY_ROLE } from './menuItems';

/** Permanent sidebar with the menu of the current role. */
export default function Sidebar({ role }) {
  const navigate = useNavigate();
  const location = useLocation();
  const items = MENU_BY_ROLE[role] ?? [];
  const homePath = `/${role?.toLowerCase()}`;

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: DRAWER_WIDTH,
        flexShrink: 0,
        '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
      }}
    >
      <Toolbar>
        <Typography variant="h6" noWrap>
          SIGA IPT
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {items.map((item) => (
          <ListItemButton
            key={item.path}
            selected={location.pathname === item.path}
            onClick={() => navigate(item.path)}
          >
            {item.path === homePath && (
              <ListItemIcon>
                <HomeIcon />
              </ListItemIcon>
            )}
            <ListItemText primary={item.label} />
          </ListItemButton>
        ))}
      </List>
    </Drawer>
  );
}
