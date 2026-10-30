import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
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
import studyPlanService from '../../services/studyPlanService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { getErrorMessage } from '../../http-common';

const emptyForm = { code: '', year: new Date().getFullYear(), current: true };

/** Study plans of a career. One plan can be marked as the current one. */
export default function StudyPlansPage() {
  const [careers, setCareers] = useState([]);
  const [careerId, setCareerId] = useState('');
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  // Careers are needed to choose which plans to list.
  useEffect(() => {
    async function loadCareers() {
      try {
        const response = await careerService.getAll();
        setCareers(response.data);
        if (response.data.length > 0) {
          setCareerId(String(response.data[0].id));
        }
      } catch (requestError) {
        setError(getErrorMessage(requestError));
      }
    }

    loadCareers();
  }, []);

  const loadPlans = async (id) => {
    setLoading(true);
    setError('');
    try {
      const response = await studyPlanService.getByCareer(id);
      setPlans(response.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (careerId) {
      loadPlans(careerId);
    }
  }, [careerId]);

  const openCreate = () => {
    setForm(emptyForm);
    setFormOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      await studyPlanService.create({
        code: form.code,
        year: Number(form.year),
        careerId: Number(careerId),
        current: form.current,
      });
      setSuccess(`Study plan ${form.code} created.`);
      setFormOpen(false);
      await loadPlans(careerId);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  const handleSetCurrent = async (plan) => {
    setError('');
    setSuccess('');
    try {
      await studyPlanService.setCurrent(plan.id);
      setSuccess(`Study plan ${plan.code} is now current.`);
      await loadPlans(careerId);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    }
  };

  const confirmDelete = async () => {
    setDeleting(true);
    setError('');
    setSuccess('');
    try {
      await studyPlanService.remove(toDelete.id);
      setSuccess(`Study plan ${toDelete.code} deleted.`);
      setToDelete(null);
      await loadPlans(careerId);
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
        <Typography variant="h4" sx={{ flexGrow: 1 }}>Study Plans</Typography>
        <Button variant="contained" onClick={openCreate} disabled={!careerId}>
          New plan
        </Button>
      </Box>

      <TextField
        select
        label="Career"
        value={careerId}
        onChange={(event) => setCareerId(event.target.value)}
        sx={{ minWidth: 260, mb: 2 }}
      >
        {careers.map((career) => (
          <MenuItem key={career.id} value={String(career.id)}>
            {career.code} - {career.name}
          </MenuItem>
        ))}
      </TextField>

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
                <TableCell>Year</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {plans.map((plan) => (
                <TableRow key={plan.id} hover>
                  <TableCell>{plan.code}</TableCell>
                  <TableCell>{plan.year}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={plan.status === 'ACTIVE' ? 'CURRENT' : plan.status}
                      color={plan.status === 'ACTIVE' ? 'success' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    {plan.status !== 'ACTIVE' && (
                      <Button size="small" onClick={() => handleSetCurrent(plan)}>
                        Set current
                      </Button>
                    )}
                    <Button size="small" color="error" onClick={() => setToDelete(plan)}>
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {plans.length === 0 && (
                <TableRow>
                  <TableCell colSpan={4} align="center">
                    This career has no study plans.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>New study plan</DialogTitle>
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
            label="Year"
            type="number"
            fullWidth
            margin="dense"
            value={form.year}
            onChange={(event) => setForm({ ...form, year: event.target.value })}
            slotProps={{ htmlInput: { min: 1900, max: 2100 } }}
          />
          <FormControlLabel
            control={
              <Checkbox
                checked={form.current}
                onChange={(event) => setForm({ ...form, current: event.target.checked })}
              />
            }
            label="Set as the current plan of the career"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFormOpen(false)} disabled={saving}>Cancel</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>
            {saving ? 'Creating...' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Delete study plan"
        message={`Delete ${toDelete?.code}? It cannot have associated students.`}
        confirmLabel="Delete"
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setToDelete(null)}
      />
    </Box>
  );
}
