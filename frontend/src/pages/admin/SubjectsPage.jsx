import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormHelperText,
  InputLabel,
  ListItemText,
  MenuItem,
  Paper,
  Select,
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
import careerService from '../../services/careerService';
import studyPlanService from '../../services/studyPlanService';
import subjectService from '../../services/subjectService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import PageHeader from '../../components/common/PageHeader';
import { getErrorMessage } from '../../http-common';

const emptyForm = {
  code: '',
  name: '',
  semester: '1',
  theoryHours: 0,
  exerciseHours: 0,
  labHours: 0,
  sct: 1,
  prerequisiteIds: [],
};

/** Subjects of a study plan, including prerequisites and TEL hours. */
export default function SubjectsPage() {
  const [plans, setPlans] = useState([]);
  const [planId, setPlanId] = useState('');
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [subjectsLoading, setSubjectsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  // There is no "list every plan" endpoint, so we load plans career by career.
  useEffect(() => {
    async function loadPlans() {
      setLoading(true);
      setError('');
      try {
        const careersResponse = await careerService.getAll();
        const planResponses = await Promise.all(
          careersResponse.data.map((career) => studyPlanService.getByCareer(career.id))
        );
        const allPlans = planResponses.flatMap((response) => response.data);
        setPlans(allPlans);

        const initial = allPlans.find((plan) => plan.status === 'ACTIVE') ?? allPlans[0];
        if (initial) {
          setPlanId(String(initial.id));
        }
      } catch (requestError) {
        setError(getErrorMessage(requestError));
      } finally {
        setLoading(false);
      }
    }

    loadPlans();
  }, []);

  const loadSubjects = async (id) => {
    setSubjectsLoading(true);
    setError('');
    try {
      const response = await subjectService.getAll(id);
      setSubjects(response.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSubjectsLoading(false);
    }
  };

  useEffect(() => {
    if (planId) {
      loadSubjects(planId);
    }
  }, [planId]);

  const openCreate = () => {
    setEditing(null);
    setForm(emptyForm);
    setFormOpen(true);
  };

  const openEdit = (subject) => {
    setEditing(subject);
    setForm({
      code: subject.code,
      name: subject.name,
      semester: String(subject.semester),
      theoryHours: subject.theoryHours,
      exerciseHours: subject.exerciseHours,
      labHours: subject.labHours,
      sct: subject.sct,
      prerequisiteIds: subject.prerequisites.map((item) => item.id),
    });
    setFormOpen(true);
  };

  const updateField = (field, value) => {
    setForm((current) => {
      const next = { ...current, [field]: value };
      if (field === 'semester') {
        // A prerequisite must always belong to a previous semester.
        next.prerequisiteIds = current.prerequisiteIds.filter((id) => {
          const subject = subjects.find((item) => item.id === id);
          return subject && subject.semester < Number(value);
        });
      }
      return next;
    });
  };

  const handlePrerequisites = (event) => {
    const value = event.target.value;
    const next = typeof value === 'string' ? value.split(',') : value;
    if (next.length > 3) {
      setError('Una asignatura puede tener como máximo 3 prerrequisitos.');
      return;
    }
    setForm({ ...form, prerequisiteIds: next });
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const payload = {
        code: form.code,
        name: form.name,
        semester: Number(form.semester),
        theoryHours: Number(form.theoryHours),
        exerciseHours: Number(form.exerciseHours),
        labHours: Number(form.labHours),
        sct: Number(form.sct),
        studyPlanId: editing ? editing.studyPlanId : Number(planId),
        prerequisiteIds: form.prerequisiteIds,
      };

      if (editing) {
        await subjectService.update(editing.id, payload);
        setSuccess(`Asignatura ${form.code} actualizada.`);
      } else {
        await subjectService.create(payload);
        setSuccess(`Asignatura ${form.code} creada.`);
      }
      setFormOpen(false);
      await loadSubjects(planId);
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
      await subjectService.remove(toDelete.id);
      setSuccess(`Asignatura ${toDelete.code} eliminada.`);
      setToDelete(null);
      await loadSubjects(planId);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
      setToDelete(null);
    } finally {
      setDeleting(false);
    }
  };

  const telTotal =
    Number(form.theoryHours || 0) +
    Number(form.exerciseHours || 0) +
    Number(form.labHours || 0);

  const candidatePrerequisites = subjects.filter(
    (subject) =>
      subject.id !== editing?.id && subject.semester < Number(form.semester)
  );

  return (
    <Box>
      <PageHeader
        title="Asignaturas"
        subtitle="Administra las asignaturas, sus horas TEL, créditos SCT y prerrequisitos."
        action={
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openCreate}
            disabled={!planId}
          >
            Nueva asignatura
          </Button>
        }
      />

      <TextField
        select
        label="Plan de estudio"
        value={planId}
        onChange={(event) => setPlanId(event.target.value)}
        sx={{ minWidth: 340, mb: 2 }}
      >
        {plans.map((plan) => (
          <MenuItem key={plan.id} value={String(plan.id)}>
            {plan.careerName} - {plan.code} ({plan.year})
            {plan.status === 'ACTIVE' ? ' - vigente' : ''}
          </MenuItem>
        ))}
      </TextField>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

      {loading || subjectsLoading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Código</TableCell>
                <TableCell>Nombre</TableCell>
                <TableCell>Semestre</TableCell>
                <TableCell>TEL</TableCell>
                <TableCell>SCT</TableCell>
                <TableCell>Prerrequisitos</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {subjects.map((subject) => (
                <TableRow key={subject.id} hover>
                  <TableCell>{subject.code}</TableCell>
                  <TableCell>{subject.name}</TableCell>
                  <TableCell>{subject.semester}</TableCell>
                  <TableCell>{subject.telTotal}</TableCell>
                  <TableCell>{subject.sct}</TableCell>
                  <TableCell>
                    {subject.prerequisites.map((item) => item.code).join(', ') || '-'}
                  </TableCell>
                  <TableCell align="right">
                    <Button size="small" onClick={() => openEdit(subject)}>Editar</Button>
                    <Button size="small" color="error" onClick={() => setToDelete(subject)}>
                      Eliminar
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {subjects.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    Este plan de estudio no tiene asignaturas.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editing ? 'Editar asignatura' : 'Nueva asignatura'}</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            label="Código"
            fullWidth
            margin="dense"
            value={form.code}
            onChange={(event) => updateField('code', event.target.value)}
          />
          <TextField
            label="Nombre"
            fullWidth
            margin="dense"
            value={form.name}
            onChange={(event) => updateField('name', event.target.value)}
          />
          <TextField
            select
            label="Semestre"
            fullWidth
            margin="dense"
            value={form.semester}
            onChange={(event) => updateField('semester', event.target.value)}
          >
            {[1, 2, 3, 4].map((semester) => (
              <MenuItem key={semester} value={String(semester)}>{semester}</MenuItem>
            ))}
          </TextField>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField
              label="Horas teoría"
              type="number"
              margin="dense"
              value={form.theoryHours}
              onChange={(event) => updateField('theoryHours', event.target.value)}
              slotProps={{ htmlInput: { min: 0 } }}
            />
            <TextField
              label="Horas ejercicios"
              type="number"
              margin="dense"
              value={form.exerciseHours}
              onChange={(event) => updateField('exerciseHours', event.target.value)}
              slotProps={{ htmlInput: { min: 0 } }}
            />
            <TextField
              label="Horas laboratorio"
              type="number"
              margin="dense"
              value={form.labHours}
              onChange={(event) => updateField('labHours', event.target.value)}
              slotProps={{ htmlInput: { min: 0 } }}
            />
          </Box>
          <Typography variant="caption" color={telTotal >= 8 ? 'error' : 'text.secondary'}>
            TEL total: {telTotal}. Debe ser menor que 8; las secciones necesitan un TEL
            par (bloques = TEL / 2).
          </Typography>
          <TextField
            label="SCT"
            type="number"
            fullWidth
            margin="dense"
            value={form.sct}
            onChange={(event) => updateField('sct', event.target.value)}
            slotProps={{ htmlInput: { min: 1, max: 7 } }}
          />
          <FormControl fullWidth margin="dense">
            <InputLabel>Prerrequisitos</InputLabel>
            <Select
              multiple
              label="Prerrequisitos"
              value={form.prerequisiteIds}
              onChange={handlePrerequisites}
              renderValue={(selected) =>
                selected
                  .map((id) => subjects.find((item) => item.id === id)?.code)
                  .filter(Boolean)
                  .join(', ')
              }
            >
              {candidatePrerequisites.map((subject) => (
                <MenuItem key={subject.id} value={subject.id}>
                  <Checkbox checked={form.prerequisiteIds.includes(subject.id)} />
                  <ListItemText
                    primary={`${subject.code} - ${subject.name}`}
                    secondary={`Semestre ${subject.semester}`}
                  />
                </MenuItem>
              ))}
            </Select>
            <FormHelperText>{form.prerequisiteIds.length}/3 seleccionados</FormHelperText>
          </FormControl>
          <Typography variant="caption" color="text.secondary">
            Solo asignaturas de semestres anteriores del mismo plan (máximo 3).
          </Typography>
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
        title="Eliminar asignatura"
        message={`¿Eliminar ${toDelete?.code}? No debe tener secciones, inscripciones ni historial académico.`}
        confirmLabel="Eliminar"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setToDelete(null)}
      />
    </Box>
  );
}
