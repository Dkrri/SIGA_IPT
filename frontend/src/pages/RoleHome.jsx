import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import keycloak, { getDisplayName, getPrimaryRole } from '../auth/keycloak';
import { getErrorMessage } from '../http-common';
import studentService from '../services/studentService';
import teacherService from '../services/teacherService';
import {
  ROLE_LABELS,
  STUDENT_STATUS_LABELS,
  TEACHER_STATUS_LABELS,
  labelFor,
} from '../ui/labels';

const ROLE_DESCRIPTIONS = {
  ADMIN: 'Administra carreras, planes de estudio, asignaturas, docentes, secciones y el proceso académico.',
  TEACHER: 'Revisa tus secciones asignadas y registra las notas finales de tus estudiantes.',
  STUDENT: 'Inscríbete en asignaturas, revisa tu avance y administra tus inscripciones del período.',
};

/**
 * Landing page for every role. It shows the session and the profile
 * (for students and teachers); navigation lives in the sidebar.
 */
export default function RoleHome() {
  const role = getPrimaryRole();
  const displayName = getDisplayName();
  const email = keycloak.tokenParsed?.email ?? '';

  const [profile, setProfile] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function loadProfile() {
      if (role !== 'STUDENT' && role !== 'TEACHER') return;

      setLoading(true);
      try {
        const response =
          role === 'STUDENT'
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

  const statusLabels =
    role === 'STUDENT' ? STUDENT_STATUS_LABELS : TEACHER_STATUS_LABELS;

  return (
    <Box>
      <Paper
        sx={{
          p: { xs: 3, md: 4 },
          mb: 3,
          border: 0,
          color: '#ffffff',
          background: 'linear-gradient(135deg, #0f4c81 0%, #17679f 55%, #2f74ad 100%)',
        }}
      >
        <Chip
          size="small"
          label={ROLE_LABELS[role] ?? role}
          sx={{ mb: 1.5, color: '#ffffff', backgroundColor: 'rgba(255,255,255,0.18)' }}
        />
        <Typography variant="h4" sx={{ color: '#ffffff' }} gutterBottom>
          Hola, {displayName}
        </Typography>
        <Typography sx={{ maxWidth: 720, opacity: 0.92 }}>
          {ROLE_DESCRIPTIONS[role]}
        </Typography>
      </Paper>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading && <CircularProgress sx={{ mb: 2 }} />}

      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr' },
          gap: 2,
        }}
      >
        {(role === 'STUDENT' || role === 'TEACHER') && profile && (
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Tu perfil
              </Typography>
              <Stack spacing={1}>
                <Typography variant="body2">
                  <strong>Nombre:</strong> {profile.firstName} {profile.lastName}
                </Typography>
                <Typography variant="body2">
                  <strong>RUN:</strong> {profile.run}
                </Typography>
                <Typography variant="body2">
                  <strong>Correo:</strong> {profile.email}
                </Typography>
                <Typography variant="body2">
                  <strong>Estado:</strong>{' '}
                  {labelFor(statusLabels, profile.status)}
                </Typography>
                {profile.careerName && (
                  <Typography variant="body2">
                    <strong>Carrera:</strong> {profile.careerName}
                  </Typography>
                )}
                {profile.studyPlanCode && (
                  <Typography variant="body2">
                    <strong>Plan de estudio:</strong> {profile.studyPlanCode}
                  </Typography>
                )}
              </Stack>
            </CardContent>
          </Card>
        )}

        <Card
          sx={
            role !== 'STUDENT' && role !== 'TEACHER'
              ? { gridColumn: { lg: '1 / -1' } }
              : undefined
          }
        >
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Sesión
            </Typography>
            <Stack spacing={1}>
              <Typography variant="body2">
                <strong>Correo:</strong> {email}
              </Typography>
              <Typography variant="body2">
                <strong>Rol:</strong> {ROLE_LABELS[role] ?? role}
              </Typography>
            </Stack>
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
}
