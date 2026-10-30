import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
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
import matriculationService from '../../services/matriculationService';
import studentService from '../../services/studentService';
import { getErrorMessage } from '../../http-common';

/** Minimal matriculation record: student + academic period. */
export default function MatriculationsPage() {
  const [periods, setPeriods] = useState([]);
  const [periodId, setPeriodId] = useState('');
  const [students, setStudents] = useState([]);
  const [matriculations, setMatriculations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [listLoading, setListLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState({ studentId: '', academicPeriodId: '' });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    async function loadReferenceData() {
      setLoading(true);
      setError('');
      try {
        const [periodsResponse, studentsResponse] = await Promise.all([
          academicPeriodService.getAll(),
          studentService.getAll(),
        ]);
        setPeriods(periodsResponse.data);
        setStudents(studentsResponse.data);

        const initial =
          periodsResponse.data.find((period) => period.status === 'OPEN') ??
          periodsResponse.data[0];
        if (initial) {
          setPeriodId(String(initial.id));
        }
      } catch (requestError) {
        setError(getErrorMessage(requestError));
      } finally {
        setLoading(false);
      }
    }

    loadReferenceData();
  }, []);

  const loadMatriculations = async (id) => {
    setListLoading(true);
    setError('');
    try {
      const response = await matriculationService.getByPeriod(id);
      setMatriculations(response.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setListLoading(false);
    }
  };

  useEffect(() => {
    if (periodId) {
      loadMatriculations(periodId);
    }
  }, [periodId]);

  const openCreate = () => {
    setForm({ studentId: '', academicPeriodId: periodId });
    setFormOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      await matriculationService.create({
        studentId: Number(form.studentId),
        academicPeriodId: Number(form.academicPeriodId),
      });
      setSuccess('Student matriculated.');
      setFormOpen(false);
      if (String(form.academicPeriodId) === periodId) {
        await loadMatriculations(periodId);
      } else {
        setPeriodId(String(form.academicPeriodId));
      }
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, gap: 2 }}>
        <Typography variant="h4" sx={{ flexGrow: 1 }}>Matriculations</Typography>
        <Button variant="contained" onClick={openCreate} disabled={students.length === 0}>
          New matriculation
        </Button>
      </Box>

      <TextField
        select
        label="Academic period"
        value={periodId}
        onChange={(event) => setPeriodId(event.target.value)}
        sx={{ minWidth: 240, mb: 2 }}
      >
        {periods.map((period) => (
          <MenuItem key={period.id} value={String(period.id)}>
            {period.code} ({period.status})
          </MenuItem>
        ))}
      </TextField>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

      {loading || listLoading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Student RUN</TableCell>
                <TableCell>Period</TableCell>
                <TableCell>Registered at</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {matriculations.map((matriculation) => (
                <TableRow key={matriculation.id} hover>
                  <TableCell>{matriculation.studentRun}</TableCell>
                  <TableCell>{matriculation.academicPeriodCode}</TableCell>
                  <TableCell>
                    {matriculation.registeredAt
                      ? new Date(matriculation.registeredAt).toLocaleDateString()
                      : '-'}
                  </TableCell>
                </TableRow>
              ))}
              {matriculations.length === 0 && (
                <TableRow>
                  <TableCell colSpan={3} align="center">
                    No matriculations in this period.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>New matriculation</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            select
            label="Student"
            fullWidth
            margin="dense"
            value={form.studentId}
            onChange={(event) => setForm({ ...form, studentId: event.target.value })}
          >
            {students.map((student) => (
              <MenuItem key={student.id} value={String(student.id)}>
                {student.run} - {student.firstName} {student.lastName}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Academic period"
            fullWidth
            margin="dense"
            value={form.academicPeriodId}
            onChange={(event) => setForm({ ...form, academicPeriodId: event.target.value })}
          >
            {periods.map((period) => (
              <MenuItem key={period.id} value={String(period.id)}>
                {period.code} ({period.status})
              </MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFormOpen(false)} disabled={saving}>Cancel</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
