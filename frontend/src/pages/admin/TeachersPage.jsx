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
import teacherService from '../../services/teacherService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import PageHeader from '../../components/common/PageHeader';
import { TEACHER_STATUS_LABELS, labelFor } from '../../ui/labels';
import { isValidRun } from '../../ui/validation';
import { getErrorMessage } from '../../http-common';

const emptyForm = {
  run: '',
  firstName: '',
  lastName: '',
  email: '',
  professionalTitle: '',
  academicDegree: '',
  temporaryPassword: '',
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
      temporaryPassword: '',
    });
    setFormOpen(true);
  };

  const handleSave = async () => {
    if (!editing && !isValidRun(form.run.trim())) {
      setError('El RUN debe tener el formato 1111111-1.');
      setSuccess('');
      return;
    }

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
        setSuccess(`Docente ${editing.run} actualizado.`);
      } else {
        await teacherService.create({ ...form, run: form.run.trim() });
        setSuccess(`Docente ${form.run} creado. Debe cambiar la contraseña en su primer inicio de sesión.`);
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
      setSuccess(`El docente ${teacher.run} ahora está ${labelFor(TEACHER_STATUS_LABELS, status)}.`);
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
      setSuccess(`Docente ${toDelete.run} eliminado.`);
      setToDelete(null);
      await loadTeachers();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
      setToDelete(null);
    } finally {
      setDeleting(false);
    }
  };

  const runError =
    !editing && form.run.length > 0 && !isValidRun(form.run.trim())
      ? 'Formato esperado: 1111111-1'
      : '';

  return (
    <Box>
      <PageHeader
        title="Docentes"
        subtitle="Gestiona los datos y el estado de los docentes."
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
            Nuevo docente
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
                <TableCell>RUN</TableCell>
                <TableCell>Nombre</TableCell>
                <TableCell>Correo</TableCell>
                <TableCell>Título profesional</TableCell>
                <TableCell>Grado académico</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
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
                      label={labelFor(TEACHER_STATUS_LABELS, teacher.status)}
                      color={teacher.status === 'ACTIVE' ? 'success' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button size="small" onClick={() => openEdit(teacher)}>Editar</Button>
                    <Button size="small" onClick={() => handleStatus(teacher)}>
                      {teacher.status === 'ACTIVE' ? 'Desactivar' : 'Activar'}
                    </Button>
                    <Button size="small" color="error" onClick={() => setToDelete(teacher)}>
                      Eliminar
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {teachers.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center">Aún no hay docentes registrados.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editing ? 'Editar docente' : 'Nuevo docente'}</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            label="RUN"
            fullWidth
            margin="dense"
            value={form.run}
            onChange={(event) => setForm({ ...form, run: event.target.value })}
            disabled={Boolean(editing)}
            error={Boolean(runError)}
            helperText={
              editing ? 'El RUN no se puede modificar.' : runError || 'Formato: 1111111-1'
            }
          />
          <TextField
            label="Nombre"
            fullWidth
            margin="dense"
            value={form.firstName}
            onChange={(event) => setForm({ ...form, firstName: event.target.value })}
          />
          <TextField
            label="Apellido"
            fullWidth
            margin="dense"
            value={form.lastName}
            onChange={(event) => setForm({ ...form, lastName: event.target.value })}
          />
          <TextField
            label="Correo"
            type="email"
            fullWidth
            margin="dense"
            value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })}
          />
          <TextField
            label="Título profesional"
            fullWidth
            margin="dense"
            value={form.professionalTitle}
            onChange={(event) => setForm({ ...form, professionalTitle: event.target.value })}
          />
          <TextField
            label="Grado académico"
            fullWidth
            margin="dense"
            value={form.academicDegree}
            onChange={(event) => setForm({ ...form, academicDegree: event.target.value })}
          />
          {!editing && (
            <TextField
              label="Contraseña temporal"
              type="password"
              fullWidth
              margin="dense"
              value={form.temporaryPassword}
              onChange={(event) =>
                setForm({ ...form, temporaryPassword: event.target.value })
              }
              helperText="El docente deberá cambiarla en su primer inicio de sesión."
              autoComplete="new-password"
            />
          )}
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
        title="Eliminar docente"
        message={`¿Eliminar ${toDelete?.run}? No debe tener secciones asociadas.`}
        confirmLabel="Eliminar"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setToDelete(null)}
      />
    </Box>
  );
}
