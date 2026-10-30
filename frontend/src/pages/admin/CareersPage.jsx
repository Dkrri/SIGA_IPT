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
import careerService from '../../services/careerService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { getErrorMessage } from '../../http-common';

const emptyForm = { code: '', name: '', description: '' };

/** Careers: create, edit, change status and delete. */
export default function CareersPage() {
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

  const loadCareers = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await careerService.getAll();
      setCareers(response.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCareers();
  }, []);

  const openCreate = () => {
    setEditing(null);
    setForm(emptyForm);
    setFormOpen(true);
  };

  const openEdit = (career) => {
    setEditing(career);
    setForm({
      code: career.code,
      name: career.name,
      description: career.description ?? '',
    });
    setFormOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      if (editing) {
        await careerService.update(editing.id, form);
        setSuccess(`Career ${form.code} updated.`);
      } else {
        await careerService.create(form);
        setSuccess(`Career ${form.code} created.`);
      }
      setFormOpen(false);
      await loadCareers();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  const handleStatus = async (career) => {
    setError('');
    setSuccess('');
    try {
      const status = career.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
      await careerService.changeStatus(career.id, status);
      setSuccess(`Career ${career.code} is now ${status}.`);
      await loadCareers();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    }
  };

  const confirmDelete = async () => {
    setDeleting(true);
    setError('');
    setSuccess('');
    try {
      await careerService.remove(toDelete.id);
      setSuccess(`Career ${toDelete.code} deleted.`);
      setToDelete(null);
      await loadCareers();
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
        <Typography variant="h4" sx={{ flexGrow: 1 }}>Careers</Typography>
        <Button variant="contained" onClick={openCreate}>New career</Button>
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
                <TableCell>Code</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Semesters</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {careers.map((career) => (
                <TableRow key={career.id} hover>
                  <TableCell>{career.code}</TableCell>
                  <TableCell>{career.name}</TableCell>
                  <TableCell>{career.semesters}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={career.status}
                      color={career.status === 'ACTIVE' ? 'success' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button size="small" onClick={() => openEdit(career)}>Edit</Button>
                    <Button size="small" onClick={() => handleStatus(career)}>
                      {career.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                    </Button>
                    <Button size="small" color="error" onClick={() => setToDelete(career)}>
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {careers.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center">No careers yet.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editing ? 'Edit career' : 'New career'}</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            label="Code"
            fullWidth
            margin="dense"
            value={form.code}
            onChange={(event) => setForm({ ...form, code: event.target.value })}
          />
          <TextField
            label="Name"
            fullWidth
            margin="dense"
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
          />
          <TextField
            label="Description"
            fullWidth
            multiline
            minRows={2}
            margin="dense"
            value={form.description}
            onChange={(event) => setForm({ ...form, description: event.target.value })}
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
        title="Delete career"
        message={`Delete ${toDelete?.code}? It cannot have study plans or students.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setToDelete(null)}
      />
    </Box>
  );
}
