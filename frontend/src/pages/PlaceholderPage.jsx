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
            This section is part of the skeleton. Implement its UI and connect it to the API next.
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
