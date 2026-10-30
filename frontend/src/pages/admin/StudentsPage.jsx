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
import careerService from '../../services/careerService';
import studentService from '../../services/studentService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { getErrorMessage } from '../../http-common';

const MANUAL_STATUSES = ['REGULAR', 'POSTPONEMENT', 'TEMPORARY_WITHDRAWAL'];

const STATUS_COLOR = {
  REGULAR: 'success',
  POSTPONEMENT: 'warning',
  TEMPORARY_WITHDRAWAL: 'warning',
  GRADUATED: 'info',
  ELIMINATED: 'error',
};

const emptyForm = {
  run: '',
  firstName: '',
  lastName: '',
  email: '',
  careerId: '',
};

/** Students: register, edit, change the manual status and delete. */
export default function StudentsPage() {
  const [students, setStudents] = useState([]);
  const [careers, setCareers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const [statusTarget, setStatusTarget] = useState(null);
  const [newStatus, setNewStatus] = useState('REGULAR');
  const [statusSaving, setStatusSaving] = useState(false);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [studentsResponse, careersResponse] = await Promise.all([
        studentService.getAll(),
        careerService.getAll(),
      ]);
      setStudents(studentsResponse.data);
      setCareers(careersResponse.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const openCreate = () => {
    setEditing(null);
    setForm(emptyForm);
    setFormOpen(true);
  };

  const openEdit = (student) => {
    setEditing(student);
    setForm({
      run: student.run,
      firstName: student.firstName,
      lastName: student.lastName,
      email: student.email,
      careerId: String(student.careerId),
    });
    setFormOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      if (editing) {
        // RUN is immutable: the update DTO does not include it.
        await studentService.update(editing.id, {
          firstName: form.firstName,
          lastName: form.lastName,
          email: form.email,
          careerId: Number(form.careerId),
        });
        setSuccess(`Student ${editing.run} updated.`);
      } else {
        await studentService.create({
          ...form,
          careerId: Number(form.careerId),
        });
        setSuccess(`Student ${form.run} created.`);
      }
      setFormOpen(false);
      await loadData();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  const openStatus = (student) => {
    setStatusTarget(student);
    setNewStatus(
      MANUAL_STATUSES.includes(student.status) ? student.status : 'REGULAR'
    );
  };

  const handleStatus = async () => {
    setStatusSaving(true);
    setError('');
    setSuccess('');
    try {
      await studentService.changeStatus(statusTarget.id, newStatus);
      setSuccess(`Student ${statusTarget.run} is now ${newStatus}.`);
      setStatusTarget(null);
      await loadData();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setStatusSaving(false);
    }
  };

  const confirmDelete = async () => {
    setDeleting(true);
    setError('');
    setSuccess('');
    try {
      await studentService.remove(toDelete.id);
      setSuccess(`Student ${toDelete.run} deleted.`);
      setToDelete(null);
      await loadData();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
      setToDelete(null);
    } finally {
      setDeleting(false);
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, gap: 2 }}>
        <Typography variant="h4" sx={{ flexGrow: 1 }}>Students</Typography>
        <Button variant="contained" onClick={openCreate}>New student</Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

      {loading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>RUN</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Career</TableCell>
                <TableCell>Study plan</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {students.map((student) => (
                <TableRow key={student.id} hover>
                  <TableCell>{student.run}</TableCell>
                  <TableCell>{student.firstName} {student.lastName}</TableCell>
                  <TableCell>{student.email}</TableCell>
                  <TableCell>{student.careerName}</TableCell>
                  <TableCell>{student.studyPlanCode}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={student.status}
                      color={STATUS_COLOR[student.status] ?? 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button size="small" onClick={() => openEdit(student)}>Edit</Button>
                    <Button size="small" onClick={() => openStatus(student)}>Status</Button>
                    <Button size="small" color="error" onClick={() => setToDelete(student)}>
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {students.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center">No students yet.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editing ? 'Edit student' : 'New student'}</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            label="RUN"
            fullWidth
            margin="dense"
            value={form.run}
            onChange={(event) => setForm({ ...form, run: event.target.value })}
            disabled={Boolean(editing)}
            helperText={editing ? 'RUN cannot be changed.' : ''}
          />
          <TextField
            label="First name"
            fullWidth
            margin="dense"
            value={form.firstName}
            onChange={(event) => setForm({ ...form, firstName: event.target.value })}
          />
          <TextField
            label="Last name"
            fullWidth
            margin="dense"
            value={form.lastName}
            onChange={(event) => setForm({ ...form, lastName: event.target.value })}
          />
          <TextField
            label="Email"
            type="email"
            fullWidth
            margin="dense"
            value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })}
          />
          <TextField
            select
            label="Career"
            fullWidth
            margin="dense"
            value={form.careerId}
            onChange={(event) => setForm({ ...form, careerId: event.target.value })}
          >
            {careers.map((career) => (
              <MenuItem key={career.id} value={String(career.id)}>
                {career.code} - {career.name}
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

      <Dialog
        open={Boolean(statusTarget)}
        onClose={statusSaving ? undefined : () => setStatusTarget(null)}
      >
        <DialogTitle>Change student status</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            select
            label="Status"
            fullWidth
            margin="dense"
            value={newStatus}
            onChange={(event) => setNewStatus(event.target.value)}
          >
            {MANUAL_STATUSES.map((status) => (
              <MenuItem key={status} value={status}>{status}</MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStatusTarget(null)} disabled={statusSaving}>Cancel</Button>
          <Button variant="contained" onClick={handleStatus} disabled={statusSaving}>
            {statusSaving ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Delete student"
        message={`Delete ${toDelete?.run}? A student with enrollments or history cannot be deleted.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setToDelete(null)}
      />
    </Box>
  );
}
