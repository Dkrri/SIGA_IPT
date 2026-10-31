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
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import careerService from '../../services/careerService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import PageHeader from '../../components/common/PageHeader';
import { CAREER_STATUS_LABELS, labelFor } from '../../ui/labels';
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
        setSuccess(`Carrera ${form.code} actualizada.`);
      } else {
        await careerService.create(form);
        setSuccess(`Carrera ${form.code} creada.`);
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
      setSuccess(`La carrera ${career.code} ahora está ${labelFor(CAREER_STATUS_LABELS, status)}.`);
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
      setSuccess(`Carrera ${toDelete.code} eliminada.`);
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
      <PageHeader
        title="Carreras"
        subtitle="Administra las carreras disponibles y su estado."
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
            Nueva carrera
          </Button>
        }
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

      {loading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Código</TableCell>
                <TableCell>Nombre</TableCell>
                <TableCell>Semestres</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
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
                      label={labelFor(CAREER_STATUS_LABELS, career.status)}
                      color={career.status === 'ACTIVE' ? 'success' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button size="small" onClick={() => openEdit(career)}>Editar</Button>
                    <Button size="small" onClick={() => handleStatus(career)}>
                      {career.status === 'ACTIVE' ? 'Desactivar' : 'Activar'}
                    </Button>
                    <Button size="small" color="error" onClick={() => setToDelete(career)}>
                      Eliminar
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {careers.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center">Aún no hay carreras registradas.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editing ? 'Editar carrera' : 'Nueva carrera'}</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            label="Código"
            fullWidth
            margin="dense"
            value={form.code}
            onChange={(event) => setForm({ ...form, code: event.target.value })}
          />
          <TextField
            label="Nombre"
            fullWidth
            margin="dense"
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
          />
          <TextField
            label="Descripción"
            fullWidth
            multiline
            minRows={2}
            margin="dense"
            value={form.description}
            onChange={(event) => setForm({ ...form, description: event.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFormOpen(false)} disabled={saving}>Cancelar</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>
            {saving ? 'Guardando...' : 'Guardar'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Eliminar carrera"
        message={`¿Eliminar ${toDelete?.code}? No debe tener planes de estudio ni estudiantes asociados.`}
        confirmLabel="Eliminar"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setToDelete(null)}
      />
    </Box>
  );
}
