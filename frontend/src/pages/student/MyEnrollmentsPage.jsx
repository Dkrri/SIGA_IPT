import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
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
  Typography,
} from '@mui/material';
import academicPeriodService from '../../services/academicPeriodService';
import enrollmentService from '../../services/enrollmentService';
import { getErrorMessage } from '../../http-common';

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
      <Typography variant="h4" gutterBottom>
        My Enrollments
      </Typography>

      <TextField
        select
        label="Academic period"
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
                <TableCell>Subject</TableCell>
                <TableCell>Code</TableCell>
                <TableCell>Period</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>Grade</TableCell>
                <TableCell>Result</TableCell>
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
                      ? new Date(enrollment.enrolledAt).toLocaleString()
                      : '-'}
                  </TableCell>
                  <TableCell>{enrollment.finalGrade ?? '-'}</TableCell>
                  <TableCell>{enrollment.result}</TableCell>
                  <TableCell>
                    <Button
                      color="error"
                      onClick={() => setToCancel(enrollment)}
                    >
                      Cancel
                    </Button>
                  </TableCell>
                </TableRow>
              ))}

              {enrollments.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    No enrollments for this period.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={Boolean(toCancel)} onClose={() => setToCancel(null)}>
        <DialogTitle>Cancel enrollment</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Cancel the enrollment in {toCancel?.subjectName}?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setToCancel(null)}>No</Button>
          <Button color="error" onClick={confirmCancel}>
            Yes, cancel
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
