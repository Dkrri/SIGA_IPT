import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';

export default function Unauthorized() {
  const navigate = useNavigate();

  return (
    <Box sx={{ p: 4 }}>
      <Typography variant="h4" gutterBottom>
        Unauthorized
      </Typography>
      <Typography variant="body1" gutterBottom>
        Your account does not have permission to access this section.
      </Typography>
      <Button variant="contained" onClick={() => navigate('/')}>
        Go home
      </Button>
    </Box>
  );
}
