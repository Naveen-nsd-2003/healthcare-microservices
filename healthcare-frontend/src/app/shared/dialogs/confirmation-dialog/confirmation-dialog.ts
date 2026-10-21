import {
  Component,
  inject
} from '@angular/core';

import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  ConfirmationDialogData
} from '../../models/confirmation-dialog-data';

@Component({
  selector: 'app-confirmation-dialog',
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './confirmation-dialog.html',
  styleUrl: './confirmation-dialog.scss'
})
export class ConfirmationDialog {

  private readonly dialogRef =
    inject(
      MatDialogRef<ConfirmationDialog>
    );

  readonly data =
    inject<ConfirmationDialogData>(
      MAT_DIALOG_DATA
    );

  cancel(): void {

    this.dialogRef.close(false);
  }

  confirm(): void {

    this.dialogRef.close(true);
  }

  get dialogType(): string {

    return this.data.type ?? 'default';
  }

  get iconName(): string {

    return this.data.icon ?? 'help_outline';
  }

  get confirmButtonText(): string {

    return this.data.confirmText ?? 'Confirm';
  }

  get cancelButtonText(): string {

    return this.data.cancelText ?? 'Cancel';
  }
}