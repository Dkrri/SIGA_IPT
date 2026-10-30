import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import academicPeriodService from '../../services/academicPeriodService';
import gradeService from '../../services/gradeService';
import sectionService from '../../services/sectionService';
import { getErrorMessage } from '../../http-common';

const RESULT_COLOR = {
  APPROVED: 'success',
  FAILED: 'error',
  PENDING: 'default',
};

/** Parses a draft value; returns null for an empty field and NaN for invalid text. */
function parseGrade(raw) {
  if (raw === '' || raw === null || raw === undefined) {
    return null;
  }
  const value = Number(String(raw).replace(',', '.'));
  return Number.isFinite(value) ? value : NaN;
}

/**
 * Grades of one section. Only its responsible teacher can read or edit them,
 * and the period must be OPEN to accept changes.
 */
export default function SectionGradesPage() {
  const { sectionId } = useParams();
  const navigate = useNavigate();

  const [section, setSection] = useState(null);
  const [period, setPeriod] = useState(null);
  const [grades, setGrades] = useState([]);
  const [drafts, setDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [savingId, setSavingId] = useState(null);

  // Load the section, its grades and the periods in parallel.
  useEffect(() => {
    async function loadData() {
      setLoading(true);
      setError('');
      try {
        const [sectionResponse, gradesResponse, periodsResponse] = await Promise.all([
          sectionService.getById(sectionId),
          gradeService.getBySection(sectionId),
          academicPeriodService.getAll(),
        ]);

        const loadedSection = sectionResponse.data;
        const loadedGrades = gradesResponse.data;

        setSection(loadedSection);
        setPeriod(
          periodsResponse.data.find(
            (item) => item.id === loadedSection.academicPeriodId
          ) ?? null
        );
        setGrades(loadedGrades);
        setDrafts(
          Object.fromEntries(
            loadedGrades.map((grade) => [
              grade.enrollmentId,
              grade.finalGrade === null || grade.finalGrade === undefined
                ? ''
                : String(grade.finalGrade),
            ])
          )
        );
      } catch (requestError) {
        setError(getErrorMessage(requestError));
      } finally {
        setLoading(false);
      }
    }

    if (sectionId) {
      loadData();
    }
  }, [sectionId]);

  const isOpen = period?.status === 'OPEN';

  /** True when the draft differs from the value stored in the database. */
  const isDirty = (grade) => {
    const original =
      grade.finalGrade === null || grade.finalGrade === undefined
        ? ''
        : String(grade.finalGrade);
    return (drafts[grade.enrollmentId] ?? '') !== original;
  };

  const handleDraftChange = (enrollmentId, value) => {
    setDrafts((current) => ({ ...current, [enrollmentId]: value }));
    setSuccess('');
  };

  const handleSave = async (enrollmentId) => {
    const value = parseGrade(drafts[enrollmentId]);

    if (value === null || Number.isNaN(value) || value < 1 || value > 7) {
      setError('Grade must be a number between 1.0 and 7.0.');
      setSuccess('');
      return;
    }

    setSavingId(enrollmentId);
    setError('');
    setSuccess('');
    try {
      const response = await gradeService.saveGrade(enrollmentId, value);
      const saved = response.data;

      // Replace only the saved row, keeping the rest untouched.
      setGrades((current) =>
        current.map((grade) =>
          grade.enrollmentId === enrollmentId ? saved : grade
        )
      );
      setDrafts((current) => ({
        ...current,
        [enrollmentId]:
          saved.finalGrade === null || saved.finalGrade === undefined
            ? ''
            : String(saved.finalGrade),
      }));
      setSuccess(`Grade saved for ${saved.studentRun}.`);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSavingId(null);
    }
  };

  if (loading) {
    return (
      <Box>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Button onClick={() => navigate('/teacher/sections')} sx={{ mb: 1 }}>
        Back to my sections
      </Button>

      <Typography variant="h4" gutterBottom>
        Section Grades
      </Typography>

      {section && (
        <>
          <Typography variant="h6">
            {section.subjectCode} - {section.subjectName}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Period {section.academicPeriodCode} ·{' '}
            {section.scheduleBlocks.map((block) => block.code).join(', ')} ·{' '}
            {grades.length} enrolled
          </Typography>
        </>
      )}

      {error && <Alert severity="error" sx={{ my: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ my: 2 }}>{success}</Alert>}

      {period && !isOpen && (
        <Alert severity="warning" sx={{ my: 2 }}>
          The academic period is {period.status}. Grades are read-only.
        </Alert>
      )}

      {section && (
        <TableContainer component={Paper} sx={{ mt: 2 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>RUN</TableCell>
                <TableCell>Student</TableCell>
                <TableCell>Grade</TableCell>
                <TableCell>Result</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {grades.map((grade) => (
                <TableRow key={grade.enrollmentId} hover>
                  <TableCell>{grade.studentRun}</TableCell>
                  <TableCell>{grade.studentName}</TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      size="small"
                      value={drafts[grade.enrollmentId] ?? ''}
                      onChange={(event) =>
                        handleDraftChange(grade.enrollmentId, event.target.value)
                      }
                      disabled={!isOpen || savingId === grade.enrollmentId}
                      slotProps={{
                        htmlInput: {
                          min: 1,
                          max: 7,
                          step: 0.1,
                          inputMode: 'decimal',
                        },
                      }}
                      sx={{ width: 110 }}
                    />
                  </TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={grade.result}
                      color={RESULT_COLOR[grade.result] ?? 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button
                      size="small"
                      variant="contained"
                      disabled={
                        !isOpen || savingId !== null || !isDirty(grade)
                      }
                      onClick={() => handleSave(grade.enrollmentId)}
                    >
                      {savingId === grade.enrollmentId ? 'Saving...' : 'Save'}
                    </Button>
                  </TableCell>
                </TableRow>
              ))}

              {grades.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    No students are enrolled in this section.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Stack sx={{ mt: 2 }}>
        <Typography variant="caption" color="text.secondary">
          Grades range from 1.0 to 7.0. A grade of 4.0 or higher is APPROVED.
        </Typography>
      </Stack>
    </Box>
  );
}
