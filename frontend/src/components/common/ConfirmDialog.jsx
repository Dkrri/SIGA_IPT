import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material';

/**
 * Reusable confirmation dialog for destructive or irreversible actions.
 * Keeps all pages consistent and prevents duplicated dialog markup.
 */
export default function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Confirm',
  color = 'error',
  loading = false,
  onConfirm,
  onClose,
}) {
  return (
    <Dialog open={open} onClose={loading ? undefined : onClose}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{message}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button
          onClick={onConfirm}
          color={color}
          variant="contained"
          disabled={loading}
        >
          {loading ? 'Working...' : confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
