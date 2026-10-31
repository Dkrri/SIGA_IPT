import { createTheme } from '@mui/material/styles';

/**
 * Global visual identity of SIGA IPT.
 * Centralizing it here keeps every page consistent.
 */
const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#0f4c81', light: '#2f74ad', dark: '#08324f', contrastText: '#ffffff' },
    secondary: { main: '#e8a317', contrastText: '#1b2430' },
    success: { main: '#2e7d32' },
    warning: { main: '#ed6c02' },
    error: { main: '#c62828' },
    info: { main: '#0277bd' },
    background: { default: '#f4f7fb', paper: '#ffffff' },
    text: { primary: '#16243a', secondary: '#5b6b82' },
    divider: '#e6edf5',
  },
  shape: { borderRadius: 12 },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h4: { fontWeight: 750, letterSpacing: '-0.02em' },
    h5: { fontWeight: 700 },
    h6: { fontWeight: 700 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { backgroundColor: '#f4f7fb' },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundImage: 'linear-gradient(90deg, #0f4c81 0%, #17679f 60%, #2f74ad 100%)',
          boxShadow: '0 8px 24px rgba(15, 76, 129, 0.18)',
        },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { borderRadius: 9, minHeight: 36 },
      },
    },
    MuiTableContainer: {
      styleOverrides: {
        root: { borderRadius: 14, border: '1px solid #e6edf5' },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          fontWeight: 700,
          color: '#334155',
          backgroundColor: '#f8fafc',
          borderBottom: '1px solid #e2e8f0',
        },
        root: { borderColor: '#eef2f7' },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:last-child td': { borderBottom: 0 },
          '&:hover': { backgroundColor: '#f8fbff' },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600, borderRadius: 8 },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: { borderRadius: 18, padding: 6 },
      },
    },
    MuiTextField: { defaultProps: { size: 'small' } },
    MuiSelect: { defaultProps: { size: 'small' } },
    MuiCard: {
      styleOverrides: {
        root: { border: '1px solid #e6edf5', boxShadow: '0 4px 18px rgba(15, 76, 129, 0.06)' },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          margin: '2px 8px',
          '&.Mui-selected': {
            backgroundColor: 'rgba(15, 76, 129, 0.10)',
            color: '#0f4c81',
            '&:hover': { backgroundColor: 'rgba(15, 76, 129, 0.16)' },
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
      },
    },
  },
});

export default theme;
