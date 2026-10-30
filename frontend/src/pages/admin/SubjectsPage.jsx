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
import careerService from '../../services/careerService';
import studyPlanService from '../../services/studyPlanService';
import subjectService from '../../services/subjectService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
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
      setError('A subject can have at most 3 prerequisites.');
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
        setSuccess(`Subject ${form.code} updated.`);
      } else {
        await subjectService.create(payload);
        setSuccess(`Subject ${form.code} created.`);
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
      setSuccess(`Subject ${toDelete.code} deleted.`);
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
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, gap: 2 }}>
        <Typography variant="h4" sx={{ flexGrow: 1 }}>Subjects</Typography>
        <Button variant="contained" onClick={openCreate} disabled={!planId}>
          New subject
        </Button>
      </Box>

      <TextField
        select
        label="Study plan"
        value={planId}
        onChange={(event) => setPlanId(event.target.value)}
        sx={{ minWidth: 320, mb: 2 }}
      >
        {plans.map((plan) => (
          <MenuItem key={plan.id} value={String(plan.id)}>
            {plan.careerName} - {plan.code} ({plan.year})
            {plan.status === 'ACTIVE' ? ' - current' : ''}
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
                <TableCell>Code</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Semester</TableCell>
                <TableCell>TEL</TableCell>
                <TableCell>SCT</TableCell>
                <TableCell>Prerequisites</TableCell>
                <TableCell align="right">Actions</TableCell>
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
                    <Button size="small" onClick={() => openEdit(subject)}>Edit</Button>
                    <Button size="small" color="error" onClick={() => setToDelete(subject)}>
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {subjects.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    This study plan has no subjects.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editing ? 'Edit subject' : 'New subject'}</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            label="Code"
            fullWidth
            margin="dense"
            value={form.code}
            onChange={(event) => updateField('code', event.target.value)}
          />
          <TextField
            label="Name"
            fullWidth
            margin="dense"
            value={form.name}
            onChange={(event) => updateField('name', event.target.value)}
          />
          <TextField
            select
            label="Semester"
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
              label="Theory hours"
              type="number"
              margin="dense"
              value={form.theoryHours}
              onChange={(event) => updateField('theoryHours', event.target.value)}
              slotProps={{ htmlInput: { min: 0 } }}
            />
            <TextField
              label="Exercise hours"
              type="number"
              margin="dense"
              value={form.exerciseHours}
              onChange={(event) => updateField('exerciseHours', event.target.value)}
              slotProps={{ htmlInput: { min: 0 } }}
            />
            <TextField
              label="Lab hours"
              type="number"
              margin="dense"
              value={form.labHours}
              onChange={(event) => updateField('labHours', event.target.value)}
              slotProps={{ htmlInput: { min: 0 } }}
            />
          </Box>
          <Typography variant="caption" color={telTotal >= 8 ? 'error' : 'text.secondary'}>
            TEL total: {telTotal}. It must be less than 8; sections need an even TEL
            (blocks = TEL / 2).
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
            <InputLabel>Prerequisites</InputLabel>
            <Select
              multiple
              label="Prerequisites"
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
                    secondary={`Semester ${subject.semester}`}
                  />
                </MenuItem>
              ))}
            </Select>
            <FormHelperText>{form.prerequisiteIds.length}/3 selected</FormHelperText>
          </FormControl>
          <Typography variant="caption" color="text.secondary">
            Only subjects from previous semesters of the same plan (maximum 3).
          </Typography>
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
        title="Delete subject"
        message={`Delete ${toDelete?.code}? It cannot have sections, enrollments or academic history.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setToDelete(null)}
      />
    </Box>
  );
}
