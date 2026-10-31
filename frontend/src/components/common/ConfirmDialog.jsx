import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';

/**
 * Reusable confirmation dialog for destructive or irreversible actions.
 * Keeps all pages consistent and prevents duplicated dialog markup.
 */
export default function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Confirmar',
  color = 'error',
  loading = false,
  onConfirm,
  onClose,
}) {
  return (
    <Dialog open={open} onClose={loading ? undefined : onClose}>
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <WarningAmberIcon color="warning" />
        {title}
      </DialogTitle>
      <DialogContent>
        <DialogContentText>{message}</DialogContentText>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={loading}>
          Cancelar
        </Button>
        <Button
          onClick={onConfirm}
          color={color}
          variant="contained"
          disabled={loading}
        >
          {loading ? 'Procesando...' : confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
