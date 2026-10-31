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
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import careerService from '../../services/careerService';
import studentService from '../../services/studentService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import PageHeader from '../../components/common/PageHeader';
import { STUDENT_STATUS_LABELS, labelFor } from '../../ui/labels';
import { isValidRun } from '../../ui/validation';
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
  temporaryPassword: '',
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
        await studentService.update(editing.id, {
          firstName: form.firstName,
          lastName: form.lastName,
          email: form.email,
          careerId: Number(form.careerId),
        });
        setSuccess(`Estudiante ${editing.run} actualizado.`);
      } else {
        await studentService.create({
          ...form,
          run: form.run.trim(),
          careerId: Number(form.careerId),
        });
        setSuccess(`Estudiante ${form.run} creado. Debe cambiar la contraseña en su primer inicio de sesión.`);
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
      setSuccess(`El estudiante ${statusTarget.run} ahora está ${labelFor(STUDENT_STATUS_LABELS, newStatus)}.`);
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
      setSuccess(`Estudiante ${toDelete.run} eliminado.`);
      setToDelete(null);
      await loadData();
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
        title="Estudiantes"
        subtitle="Registra estudiantes, actualiza sus datos y administra su estado académico."
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
            Nuevo estudiante
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
                <TableCell>Carrera</TableCell>
                <TableCell>Plan de estudio</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
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
                      label={labelFor(STUDENT_STATUS_LABELS, student.status)}
                      color={STATUS_COLOR[student.status] ?? 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button size="small" onClick={() => openEdit(student)}>Editar</Button>
                    <Button size="small" onClick={() => openStatus(student)}>Estado</Button>
                    <Button size="small" color="error" onClick={() => setToDelete(student)}>
                      Eliminar
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {students.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center">Aún no hay estudiantes registrados.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editing ? 'Editar estudiante' : 'Nuevo estudiante'}</DialogTitle>
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
            select
            label="Carrera"
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
              helperText="El estudiante deberá cambiarla en su primer inicio de sesión."
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

      <Dialog
        open={Boolean(statusTarget)}
        onClose={statusSaving ? undefined : () => setStatusTarget(null)}
      >
        <DialogTitle>Cambiar estado del estudiante</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            select
            label="Estado"
            fullWidth
            margin="dense"
            value={newStatus}
            onChange={(event) => setNewStatus(event.target.value)}
          >
            {MANUAL_STATUSES.map((status) => (
              <MenuItem key={status} value={status}>
                {labelFor(STUDENT_STATUS_LABELS, status)}
              </MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStatusTarget(null)} disabled={statusSaving}>Cancelar</Button>
          <Button variant="contained" onClick={handleStatus} disabled={statusSaving}>
            {statusSaving ? 'Guardando...' : 'Guardar'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Eliminar estudiante"
        message={`¿Eliminar ${toDelete?.run}? Un estudiante con inscripciones o historial no se puede eliminar.`}
        confirmLabel="Eliminar"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setToDelete(null)}
      />
    </Box>
  );
}
