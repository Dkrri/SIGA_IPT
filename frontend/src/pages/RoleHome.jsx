import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Card,
  CardContent,
  CircularProgress,
  Stack,
  Typography,
} from '@mui/material';
import keycloak, { getPrimaryRole } from '../auth/keycloak';
import { getErrorMessage } from '../http-common';
import studentService from '../services/studentService';
import teacherService from '../services/teacherService';

/**
 * Landing page for every role. For STUDENT and TEACHER it also calls the
 * backend /me endpoint, which proves that the JWT, Axios and the API work.
 */
export default function RoleHome() {
  const role = getPrimaryRole();
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function loadProfile() {
      if (role !== 'STUDENT' && role !== 'TEACHER') return;

      setLoading(true);
      try {
        const response = role === 'STUDENT'
          ? await studentService.getMe()
          : await teacherService.getMe();
        setProfile(response.data);
      } catch (requestError) {
        setError(getErrorMessage(requestError));
      } finally {
        setLoading(false);
      }
    }

    loadProfile();
  }, [role]);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Welcome to SIGA IPT
      </Typography>
      <Typography variant="body1" gutterBottom>
        Signed in as <strong>{keycloak.tokenParsed?.preferred_username}</strong> with role{' '}
        <strong>{role}</strong>.
      </Typography>

      <Card sx={{ mt: 3, maxWidth: 560 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Session
          </Typography>
          <Typography variant="body2">Email: {keycloak.tokenParsed?.email}</Typography>
          <Typography variant="body2">
            Roles: {(keycloak.realmAccess?.roles ?? []).join(', ')}
          </Typography>
        </CardContent>
      </Card>

      {loading && <CircularProgress sx={{ mt: 3 }} />}
      {error && <Alert severity="error" sx={{ mt: 3 }}>{error}</Alert>}

      {profile && (
        <Card sx={{ mt: 3, maxWidth: 560 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Profile
            </Typography>
            <Stack spacing={0.5}>
              <Typography variant="body2">
                Name: {profile.firstName} {profile.lastName}
              </Typography>
              <Typography variant="body2">RUN: {profile.run}</Typography>
              <Typography variant="body2">Email: {profile.email}</Typography>
              {profile.status && (
                <Typography variant="body2">Status: {profile.status}</Typography>
              )}
              {profile.careerName && (
                <Typography variant="body2">Career: {profile.careerName}</Typography>
              )}
              {profile.studyPlanCode && (
                <Typography variant="body2">Study plan: {profile.studyPlanCode}</Typography>
              )}
            </Stack>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}
