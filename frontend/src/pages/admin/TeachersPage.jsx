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
import teacherService from '../../services/teacherService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { getErrorMessage } from '../../http-common';

const emptyForm = {
  run: '',
  firstName: '',
  lastName: '',
  email: '',
  professionalTitle: '',
  academicDegree: '',
};

/** Teachers: create, edit, activate/deactivate and delete. */
export default function TeachersPage() {
  const [teachers, setTeachers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const loadTeachers = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await teacherService.getAll();
      setTeachers(response.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTeachers();
  }, []);

  const openCreate = () => {
    setEditing(null);
    setForm(emptyForm);
    setFormOpen(true);
  };

  const openEdit = (teacher) => {
    setEditing(teacher);
    setForm({
      run: teacher.run,
      firstName: teacher.firstName,
      lastName: teacher.lastName,
      email: teacher.email,
      professionalTitle: teacher.professionalTitle,
      academicDegree: teacher.academicDegree,
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
        await teacherService.update(editing.id, {
          firstName: form.firstName,
          lastName: form.lastName,
          email: form.email,
          professionalTitle: form.professionalTitle,
          academicDegree: form.academicDegree,
        });
        setSuccess(`Teacher ${editing.run} updated.`);
      } else {
        await teacherService.create(form);
        setSuccess(`Teacher ${form.run} created.`);
      }
      setFormOpen(false);
      await loadTeachers();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  const handleStatus = async (teacher) => {
    setError('');
    setSuccess('');
    try {
      const status = teacher.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
      await teacherService.changeStatus(teacher.id, status);
      setSuccess(`Teacher ${teacher.run} is now ${status}.`);
      await loadTeachers();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    }
  };

  const confirmDelete = async () => {
    setDeleting(true);
    setError('');
    setSuccess('');
    try {
      await teacherService.remove(toDelete.id);
      setSuccess(`Teacher ${toDelete.run} deleted.`);
      setToDelete(null);
      await loadTeachers();
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
        <Typography variant="h4" sx={{ flexGrow: 1 }}>Teachers</Typography>
        <Button variant="contained" onClick={openCreate}>New teacher</Button>
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
                <TableCell>Title</TableCell>
                <TableCell>Degree</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {teachers.map((teacher) => (
                <TableRow key={teacher.id} hover>
                  <TableCell>{teacher.run}</TableCell>
                  <TableCell>{teacher.firstName} {teacher.lastName}</TableCell>
                  <TableCell>{teacher.email}</TableCell>
                  <TableCell>{teacher.professionalTitle}</TableCell>
                  <TableCell>{teacher.academicDegree}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={teacher.status}
                      color={teacher.status === 'ACTIVE' ? 'success' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button size="small" onClick={() => openEdit(teacher)}>Edit</Button>
                    <Button size="small" onClick={() => handleStatus(teacher)}>
                      {teacher.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                    </Button>
                    <Button size="small" color="error" onClick={() => setToDelete(teacher)}>
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {teachers.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center">No teachers yet.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editing ? 'Edit teacher' : 'New teacher'}</DialogTitle>
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
            label="Professional title"
            fullWidth
            margin="dense"
            value={form.professionalTitle}
            onChange={(event) => setForm({ ...form, professionalTitle: event.target.value })}
          />
          <TextField
            label="Academic degree"
            fullWidth
            margin="dense"
            value={form.academicDegree}
            onChange={(event) => setForm({ ...form, academicDegree: event.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFormOpen(false)} disabled={saving}>Cancel</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Delete teacher"
        message={`Delete ${toDelete?.run}? It cannot have associated sections.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setToDelete(null)}
      />
    </Box>
  );
}
