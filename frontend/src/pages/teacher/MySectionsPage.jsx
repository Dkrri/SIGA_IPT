import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import sectionService from '../../services/sectionService';
import { getErrorMessage } from '../../http-common';

/**
 * Sections assigned to the authenticated teacher.
 * Each row opens the grade registration page for that section.
 */
export default function MySectionsPage() {
  const navigate = useNavigate();
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Load the teacher's sections once, when the page mounts.
  useEffect(() => {
    async function loadSections() {
      try {
        const response = await sectionService.getMySections();
        setSections(response.data);
      } catch (requestError) {
        setError(getErrorMessage(requestError));
      } finally {
        setLoading(false);
      }
    }

    loadSections();
  }, []);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        My Sections
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Sections assigned to you. Open one to register final grades.
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {loading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Code</TableCell>
                <TableCell>Subject</TableCell>
                <TableCell>Period</TableCell>
                <TableCell>Schedule</TableCell>
                <TableCell>Capacity</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {sections.map((section) => (
                <TableRow key={section.id} hover>
                  <TableCell>{section.subjectCode}</TableCell>
                  <TableCell>{section.subjectName}</TableCell>
                  <TableCell>{section.academicPeriodCode}</TableCell>
                  <TableCell>
                    {section.scheduleBlocks.map((block) => block.code).join(', ')}
                  </TableCell>
                  <TableCell>{section.capacity}</TableCell>
                  <TableCell align="right">
                    <Button
                      size="small"
                      variant="outlined"
                      onClick={() => navigate(`/teacher/sections/${section.id}/grades`)}
                    >
                      Grades
                    </Button>
                  </TableCell>
                </TableRow>
              ))}

              {sections.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    You have no assigned sections.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}
