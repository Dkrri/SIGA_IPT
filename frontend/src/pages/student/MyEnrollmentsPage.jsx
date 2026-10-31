import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  MenuItem,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
} from '@mui/material';
import academicPeriodService from '../../services/academicPeriodService';
import enrollmentService from '../../services/enrollmentService';
import PageHeader from '../../components/common/PageHeader';
import { ENROLLMENT_RESULT_LABELS, labelFor } from '../../ui/labels';
import { getErrorMessage } from '../../http-common';

const RESULT_COLOR = {
  APPROVED: 'success',
  FAILED: 'error',
  PENDING: 'default',
};

/**
 * Enrollments of the authenticated student in a selected period.
 * The student can cancel an enrollment while the period is open.
 */
export default function MyEnrollmentsPage() {
  const [periods, setPeriods] = useState([]);
  const [periodId, setPeriodId] = useState('');
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [toCancel, setToCancel] = useState(null);

  // Load periods, newest first, and preselect the newest one.
  useEffect(() => {
    async function loadPeriods() {
      try {
        const response = await academicPeriodService.getAll();
        const sorted = [...response.data].sort(
          (a, b) => b.year - a.year || b.semester - a.semester
        );
        setPeriods(sorted);
        if (sorted.length > 0) {
          setPeriodId(String(sorted[0].id));
        }
      } catch (requestError) {
        setError(getErrorMessage(requestError));
      }
    }

    loadPeriods();
  }, []);

  const loadEnrollments = async (id) => {
    setLoading(true);
    setError('');
    try {
      const response = await enrollmentService.getMyEnrollments(id);
      setEnrollments(response.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (periodId) {
      loadEnrollments(periodId);
    }
  }, [periodId]);

  const confirmCancel = async () => {
    try {
      await enrollmentService.cancel(toCancel.id);
      setToCancel(null);
      await loadEnrollments(periodId);
    } catch (requestError) {
      setToCancel(null);
      setError(getErrorMessage(requestError));
    }
  };

  return (
    <Box>
      <PageHeader
        title="Mis inscripciones"
        subtitle="Consulta las asignaturas en las que te inscribiste y anula las que necesites."
      />

      <TextField
        select
        label="Período académico"
        value={periodId}
        onChange={(event) => setPeriodId(event.target.value)}
        sx={{ minWidth: 240, mb: 2 }}
      >
        {periods.map((period) => (
          <MenuItem key={period.id} value={String(period.id)}>
            {period.code}
          </MenuItem>
        ))}
      </TextField>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {loading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Asignatura</TableCell>
                <TableCell>Código</TableCell>
                <TableCell>Período</TableCell>
                <TableCell>Fecha</TableCell>
                <TableCell>Nota</TableCell>
                <TableCell>Resultado</TableCell>
                <TableCell />
              </TableRow>
            </TableHead>
            <TableBody>
              {enrollments.map((enrollment) => (
                <TableRow key={enrollment.id}>
                  <TableCell>{enrollment.subjectName}</TableCell>
                  <TableCell>{enrollment.subjectCode}</TableCell>
                  <TableCell>{enrollment.academicPeriodCode}</TableCell>
                  <TableCell>
                    {enrollment.enrolledAt
                      ? new Date(enrollment.enrolledAt).toLocaleDateString('es-CL')
                      : '-'}
                  </TableCell>
                  <TableCell>{enrollment.finalGrade ?? '-'}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={labelFor(ENROLLMENT_RESULT_LABELS, enrollment.result)}
                      color={RESULT_COLOR[enrollment.result] ?? 'default'}
                    />
                  </TableCell>
                  <TableCell>
                    <Button
                      color="error"
                      onClick={() => setToCancel(enrollment)}
                    >
                      Anular
                    </Button>
                  </TableCell>
                </TableRow>
              ))}

              {enrollments.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    No tienes inscripciones en este período.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={Boolean(toCancel)} onClose={() => setToCancel(null)}>
        <DialogTitle>Anular inscripción</DialogTitle>
        <DialogContent>
          <DialogContentText>
            ¿Anular la inscripción en {toCancel?.subjectName}?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setToCancel(null)}>No</Button>
          <Button color="error" onClick={confirmCancel}>
            Sí, anular
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
