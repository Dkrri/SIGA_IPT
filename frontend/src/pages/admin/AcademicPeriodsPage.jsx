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
import academicPeriodService from '../../services/academicPeriodService';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import PageHeader from '../../components/common/PageHeader';
import { ACADEMIC_PERIOD_STATUS_LABELS, labelFor } from '../../ui/labels';
import { getErrorMessage } from '../../http-common';

const emptyForm = { year: new Date().getFullYear(), semester: '1' };

/**
 * Academic periods: the ADMIN creates them and closes them.
 * Closing consolidates grades and updates student statuses.
 */
export default function AcademicPeriodsPage() {
  const [periods, setPeriods] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [closingResult, setClosingResult] = useState(null);

  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const [toClose, setToClose] = useState(null);
  const [closing, setClosing] = useState(false);

  const loadPeriods = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await academicPeriodService.getAll();
      setPeriods(response.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPeriods();
  }, []);

  const openCreate = () => {
    setForm(emptyForm);
    setFormOpen(true);
  };

  const handleCreate = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const response = await academicPeriodService.create({
        year: Number(form.year),
        semester: Number(form.semester),
      });
      setSuccess(`Período académico ${response.data.code} creado.`);
      setFormOpen(false);
      await loadPeriods();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  const confirmClose = async () => {
    setClosing(true);
    setError('');
    setSuccess('');
    try {
      const response = await academicPeriodService.close(toClose.id);
      setClosingResult(response.data);
      setToClose(null);
      await loadPeriods();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
      setToClose(null);
    } finally {
      setClosing(false);
    }
  };

  return (
    <Box>
      <PageHeader
        title="Períodos académicos"
        subtitle="Crea períodos y ciérralos cuando todas las notas estén registradas."
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
            Nuevo período
          </Button>
        }
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      {closingResult && (
        <Alert
          severity="success"
          sx={{ mb: 2 }}
          onClose={() => setClosingResult(null)}
        >
          Período {closingResult.academicPeriodCode} cerrado. Inscripciones consolidadas:{' '}
          {closingResult.consolidatedEnrollments}, graduados: {closingResult.graduatedStudents},
          eliminados: {closingResult.eliminatedStudents}.
        </Alert>
      )}

      {loading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Código</TableCell>
                <TableCell>Año</TableCell>
                <TableCell>Semestre</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {periods.map((period) => (
                <TableRow key={period.id} hover>
                  <TableCell>{period.code}</TableCell>
                  <TableCell>{period.year}</TableCell>
                  <TableCell>{period.semester}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={labelFor(ACADEMIC_PERIOD_STATUS_LABELS, period.status)}
                      color={period.status === 'OPEN' ? 'success' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    {period.status === 'OPEN' && (
                      <Button
                        size="small"
                        color="warning"
                        variant="outlined"
                        onClick={() => setToClose(period)}
                      >
                        Cerrar
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
              {periods.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    Aún no hay períodos académicos.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={formOpen} onClose={saving ? undefined : () => setFormOpen(false)}>
        <DialogTitle>Nuevo período académico</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 1 }}>{error}</Alert>}
          <TextField
            label="Año"
            type="number"
            fullWidth
            margin="dense"
            value={form.year}
            onChange={(event) => setForm({ ...form, year: event.target.value })}
            slotProps={{ htmlInput: { min: 1900, max: 2100 } }}
          />
          <TextField
            select
            label="Semestre"
            fullWidth
            margin="dense"
            value={form.semester}
            onChange={(event) => setForm({ ...form, semester: event.target.value })}
          >
            <MenuItem value="1">1</MenuItem>
            <MenuItem value="2">2</MenuItem>
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFormOpen(false)} disabled={saving}>Cancelar</Button>
          <Button variant="contained" onClick={handleCreate} disabled={saving}>
            {saving ? 'Creando...' : 'Crear'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(toClose)}
        title="Cerrar período académico"
        message={`¿Cerrar ${toClose?.code}? Todas las inscripciones deben tener nota. Esta acción no se puede deshacer.`}
        confirmLabel="Cerrar período"
        loading={closing}
        onConfirm={confirmClose}
        onClose={() => setToClose(null)}
      />
    </Box>
  );
}
