import { Box, Button, Card, CardContent, Typography } from '@mui/material';
import LockIcon from '@mui/icons-material/Lock';
import { useNavigate } from 'react-router-dom';

export default function Unauthorized() {
  const navigate = useNavigate();

  return (
    <Box
      sx={{
        minHeight: '60vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <Card sx={{ maxWidth: 460, textAlign: 'center' }}>
        <CardContent sx={{ p: 4 }}>
          <Box
            sx={{
              width: 64,
              height: 64,
              borderRadius: '50%',
              backgroundColor: 'rgba(198, 40, 40, 0.10)',
              color: 'error.main',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              mx: 'auto',
              mb: 2,
            }}
          >
            <LockIcon fontSize="large" />
          </Box>
          <Typography variant="h5" gutterBottom>
            Acceso denegado
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Tu cuenta no tiene permisos para acceder a esta sección.
          </Typography>
          <Button variant="contained" onClick={() => navigate('/')}>
            Volver al inicio
          </Button>
        </CardContent>
      </Card>
    </Box>
  );
}
