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
import AddIcon from '@mui/icons-material/Add';
import academicPeriodService from '../../services/academicPeriodService';
import sectionService from '../../services/sectionService';
import subjectService from '../../services/subjectService';
import teacherService from '../../services/teacherService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import PageHeader from '../../components/common/PageHeader';
import {
  ACADEMIC_PERIOD_STATUS_LABELS,
  TEACHER_STATUS_LABELS,
  WEEKDAY_LABELS,
  labelFor,
} from '../../ui/labels';
import { getErrorMessage } from '../../http-common';

const WEEKDAYS = [
  { value: 'MONDAY', label: 'Monday' },
  { value: 'TUESDAY', label: 'Tuesday' },
  { value: 'WEDNESDAY', label: 'Wednesday' },
  { value: 'THURSDAY', label: 'Thursday' },
  { value: 'FRIDAY', label: 'Friday' },
];

const emptyForm = {
  subjectId: '',
  academicPeriodId: '',
  teacherId: '',
  capacity: 30,
  scheduleBlocks: [{ weekday: 'MONDAY', module: '1' }],
};

/** Sections of a period: subject, teacher, capacity and schedule blocks. */
export default function SectionsPage() {
  const [periods, setPeriods] = useState([]);
  const [periodId, setPeriodId] = useState('');
  const [sections, setSections] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sectionsLoading, setSectionsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    async function loadReferenceData() {
      setLoading(true);
      setError('');
      try {
        const [periodsResponse, subjectsResponse, teachersResponse] = await Promise.all([
          academicPeriodService.getAll(),
          subjectService.getAll(),
          teacherService.getAll(),
        ]);
        setPeriods(periodsResponse.data);
        setSubjects(subjectsResponse.data);
        setTeachers(teachersResponse.data);

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

  const loadSections = async (id) => {
    setSectionsLoading(true);
    setError('');
    try {
      const response = await sectionService.getAll(id);
      setSections(response.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSectionsLoading(false);
    }
  };

  useEffect(() => {
    if (periodId) {
      loadSections(periodId);
    }
  }, [periodId]);

  const selectedSubject = subjects.find(
    (subject) => subject.id === Number(form.subjectId)
  );
  const requiredBlocks = selectedSubject ? selectedSubject.telTotal / 2 : null;

  const openCreate = () => {
    setEditing(null);
    setForm({ ...emptyForm, academicPeriodId: periodId });
    setFormOpen(true);
  };

  const openEdit = (section) => {
    setEditing(section);
    setForm({
      subjectId: String(section.subjectId),
      academicPeriodId: String(section.academicPeriodId),
      teacherId: String(section.teacherId),
      capacity: section.capacity,
      scheduleBlocks: section.scheduleBlocks.map((block) => ({
        weekday: block.weekday,
        module: String(block.module),
      })),
    });
    setFormOpen(true);
  };

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const updateBlock = (index, field, value) => {
    setForm((current) => ({
      ...current,
      scheduleBlocks: current.scheduleBlocks.map((block, position) =>
        position === index ? { ...block, [field]: value } : block
      ),
    }));
  };

  const addBlock = () => {
    setForm((current) => ({
      ...current,
      scheduleBlocks: [...current.scheduleBlocks, { weekday: 'MONDAY', module: '1' }],
    }));
  };

  const removeBlock = (index) => {
    setForm((current) => ({
      ...current,
      scheduleBlocks: current.scheduleBlocks.filter((_, position) => position !== index),
    }));
  };

  const handleSave = async () => {
    setError('');
    setSuccess('');

    if (!form.subjectId || !form.academicPeriodId || !form.teacherId) {
      setError('Asignatura, período y docente son obligatorios.');
      return;
    }
    if (requiredBlocks !== null && form.scheduleBlocks.length !== requiredBlocks) {
      setError(`Esta asignatura requiere exactamente ${requiredBlocks} bloques de horario.`);
      return;
    }

    const keys = form.scheduleBlocks.map(
      (block) => `${block.weekday}-${block.module}`
    );
    if (new Set(keys).size !== keys.length) {
      setError('Los bloques de horario no se pueden repetir.');
      return;
    }

    setSaving(true);
    try {
      const payload = {
        subjectId: Number(form.subjectId),
        academicPeriodId: Number(form.academicPeriodId),
        teacherId: Number(form.teacherId),
        capacity: Number(form.capacity),
        scheduleBlocks: form.scheduleBlocks.map((block) => ({
          weekday: block.weekday,
          module: Number(block.module),
        })),
      };

      if (editing) {
        await sectionService.update(editing.id, payload);
        setSuccess(`Sección ${editing.subjectCode} actualizada.`);
      } else {
        await sectionService.create(payload);
        setSuccess('Sección creada.');
      }
      setFormOpen(false);
      await loadSections(periodId);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  const confirmDelete = async () => {
    setDeleting(true);
    setError('');
    setSuccess('');
    try {
      await sectionService.remove(toDelete.id);
      setSuccess(`Sección ${toDelete.subjectCode} eliminada.`);
      setToDelete(null);
      await loadSections(periodId);
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
        title="Secciones"
        subtitle="Crea secciones por período, asigna docentes y define cupos y horarios."
        action={
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openCreate}
            disabled={!periodId}
          >
            Nueva sección
          </Button>
        }
      />

      <TextField
        select
        label="Período académico"
        value={periodId}
        onChange={(event) => setPeriodId(event.target.value)}
        sx={{ minWidth: 260, mb: 2 }}
      >
        {periods.map((period) => (
          <MenuItem key={period.id} value={String(period.id)}>
            {period.code} ({labelFor(ACADEMIC_PERIOD_STATUS_LABELS, period.status)})
          </MenuItem>
        ))}
      </TextField>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

      {loading || sectionsLoading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Asignatura</TableCell>
                <TableCell>Docente</TableCell>
                <TableCell>Cupos</TableCell>
                <TableCell>Horario</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {sections.map((section) => (
                <TableRow key={section.id} hover>
                  <TableCell>
                    {section.subjectCode} - {section.subjectName}
                  </TableCell>
                  <TableCell>{section.teacherName}</TableCell>
                  <TableCell>{section.capacity}</TableCell>
                  <TableCell>
                    {section.scheduleBlocks.map((block) => block.code).join(', ')}
                  </TableCell>
                  <TableCell align="right">
                    <Button size="small" onClick={() => openEdit(section)}>Editar</Button>
                    <Button size="small" color="error" onClick={() => setToDelete(section)}>
                      Eliminar
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {sections.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    Este período no tiene secciones.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="md">
        <DialogTitle>{editing ? 'Editar sección' : 'Nueva sección'}</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            select
            label="Asignatura"
            fullWidth
            margin="dense"
            value={form.subjectId}
            onChange={(event) => updateField('subjectId', event.target.value)}
          >
            {subjects.map((subject) => (
              <MenuItem key={subject.id} value={String(subject.id)}>
                {subject.code} - {subject.name} (TEL {subject.telTotal})
              </MenuItem>
            ))}
          </TextField>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField
              select
              label="Período académico"
              fullWidth
              margin="dense"
              value={form.academicPeriodId}
              onChange={(event) => updateField('academicPeriodId', event.target.value)}
            >
              {periods.map((period) => (
                <MenuItem key={period.id} value={String(period.id)}>
                  {period.code} ({labelFor(ACADEMIC_PERIOD_STATUS_LABELS, period.status)})
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Docente"
              fullWidth
              margin="dense"
              value={form.teacherId}
              onChange={(event) => updateField('teacherId', event.target.value)}
            >
              {teachers.map((teacher) => (
                <MenuItem key={teacher.id} value={String(teacher.id)}>
                  {teacher.firstName} {teacher.lastName} ({labelFor(
                    TEACHER_STATUS_LABELS,
                    teacher.status
                  )})
                </MenuItem>
              ))}
            </TextField>
          </Box>
          <TextField
            label="Cupos"
            type="number"
            fullWidth
            margin="dense"
            value={form.capacity}
            onChange={(event) => updateField('capacity', event.target.value)}
            slotProps={{ htmlInput: { min: 1, max: 60 } }}
          />

          <Typography variant="subtitle2" sx={{ mt: 1 }}>
            Bloques de horario
            {requiredBlocks !== null && ` (requeridos: ${requiredBlocks})`}
          </Typography>
          {form.scheduleBlocks.map((block, index) => (
            <Box key={index} sx={{ display: 'flex', gap: 1, alignItems: 'center', mt: 1 }}>
              <TextField
                select
                label="Día"
                value={block.weekday}
                onChange={(event) => updateBlock(index, 'weekday', event.target.value)}
                sx={{ minWidth: 150 }}
              >
                {WEEKDAYS.map((day) => (
                  <MenuItem key={day.value} value={day.value}>
                    {labelFor(WEEKDAY_LABELS, day.value)}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                select
                label="Módulo"
                value={block.module}
                onChange={(event) => updateBlock(index, 'module', event.target.value)}
                sx={{ minWidth: 110 }}
              >
                {[1, 2, 3, 4, 5, 6].map((module) => (
                  <MenuItem key={module} value={String(module)}>{module}</MenuItem>
                ))}
              </TextField>
              <Button color="error" onClick={() => removeBlock(index)} disabled={form.scheduleBlocks.length === 1}>
                Quitar
              </Button>
            </Box>
          ))}
          <Button onClick={addBlock} sx={{ mt: 1 }}>Agregar bloque</Button>
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
        title="Eliminar sección"
        message={`¿Eliminar la sección ${toDelete?.subjectCode}? No debe tener estudiantes inscritos.`}
        confirmLabel="Eliminar"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setToDelete(null)}
      />
    </Box>
  );
}
