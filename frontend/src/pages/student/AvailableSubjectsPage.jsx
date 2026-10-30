import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  CircularProgress,
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

const MAX_SUBJECTS = 6;

/**
 * Offer available for the authenticated student in an open academic period.
 * The student selects sections and submits them in one enrollment request.
 */
export default function AvailableSubjectsPage() {
  const [periods, setPeriods] = useState([]);
  const [periodId, setPeriodId] = useState('');
  const [subjects, setSubjects] = useState([]);
  const [selected, setSelected] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Load open periods and preselect the first one.
  useEffect(() => {
    async function loadPeriods() {
      try {
        const response = await academicPeriodService.getAll();
        const open = response.data.filter((period) => period.status === 'OPEN');
        setPeriods(open);
        if (open.length > 0) {
          setPeriodId(String(open[0].id));
        }
      } catch (requestError) {
        setError(getErrorMessage(requestError));
      }
    }

    loadPeriods();
  }, []);

  // Load the available subjects whenever the period changes.
  useEffect(() => {
    if (!periodId) return;

    async function loadSubjects() {
      setLoading(true);
      setError('');
      setSuccess('');
      setSelected([]);
      try {
        const response = await enrollmentService.getAvailable(periodId);
        setSubjects(response.data);
      } catch (requestError) {
        setError(getErrorMessage(requestError));
      } finally {
        setLoading(false);
      }
    }

    loadSubjects();
  }, [periodId]);

  const toggle = (sectionId) => {
    setSelected((current) => {
      if (current.includes(sectionId)) {
        return current.filter((id) => id !== sectionId);
      }
      if (current.length >= MAX_SUBJECTS) {
        return current;
      }
      return [...current, sectionId];
    });
  };

  const handleEnroll = async () => {
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await enrollmentService.enroll(selected);
      setSuccess('Enrollment completed successfully.');

      // Refresh the offer (seats change and already-enrolled subjects disappear).
      const response = await enrollmentService.getAvailable(periodId);
      setSubjects(response.data);
      setSelected([]);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Available Subjects
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {periods.length === 0 ? (
        // Do not show the "no periods" hint when the real problem was a failed request.
        !error && <Alert severity="info">There are no open academic periods.</Alert>
      ) : (
        <>
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

          {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

          {loading ? (
            <CircularProgress />
          ) : (
            <>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell padding="checkbox" />
                      <TableCell>Code</TableCell>
                      <TableCell>Subject</TableCell>
                      <TableCell>Semester</TableCell>
                      <TableCell>Teacher</TableCell>
                      <TableCell>Seats</TableCell>
                      <TableCell>Schedule</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {subjects.map((subject) => {
                      const isFull = subject.enrolledCount >= subject.capacity;
                      return (
                        <TableRow key={subject.sectionId} hover>
                          <TableCell padding="checkbox">
                            <Checkbox
                              checked={selected.includes(subject.sectionId)}
                              onChange={() => toggle(subject.sectionId)}
                              disabled={isFull}
                            />
                          </TableCell>
                          <TableCell>{subject.subjectCode}</TableCell>
                          <TableCell>{subject.subjectName}</TableCell>
                          <TableCell>{subject.semester}</TableCell>
                          <TableCell>{subject.teacherName}</TableCell>
                          <TableCell>
                            {subject.enrolledCount} / {subject.capacity}
                          </TableCell>
                          <TableCell>
                            {subject.scheduleBlocks
                              .map((block) => block.code)
                              .join(', ')}
                          </TableCell>
                        </TableRow>
                      );
                    })}

                    {subjects.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={7} align="center">
                          No available subjects for this period.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>

              <Box sx={{ mt: 2, display: 'flex', alignItems: 'center', gap: 2 }}>
                <Typography variant="body2">
                  Selected: {selected.length} (minimum 3, maximum 6)
                </Typography>
                <Button
                  variant="contained"
                  disabled={selected.length === 0 || submitting}
                  onClick={handleEnroll}
                >
                  {submitting ? 'Enrolling...' : 'Enroll'}
                </Button>
              </Box>
            </>
          )}
        </>
      )}
    </Box>
  );
}
