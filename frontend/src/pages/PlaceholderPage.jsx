import { Box, Card, CardContent, Typography } from '@mui/material';

/** Temporary page used until the real UI for a section is implemented. */
export default function PlaceholderPage({ title }) {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {title}
      </Typography>
      <Card>
        <CardContent>
          <Typography variant="body1">
            Esta sección forma parte del esqueleto inicial. Su interfaz se implementará
            y conectará con la API próximamente.
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
