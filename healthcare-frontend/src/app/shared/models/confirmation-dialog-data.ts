export interface ConfirmationDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  icon?: string;
  type?: 'default' | 'warning' | 'danger';
}