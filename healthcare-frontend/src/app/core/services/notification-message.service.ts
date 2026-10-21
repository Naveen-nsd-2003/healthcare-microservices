import {
  Injectable,
  inject
} from '@angular/core';

import {
  MatSnackBar
} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class NotificationMessageService {

  private readonly snackBar =
    inject(MatSnackBar);

  showSuccess(
    message: string
  ): void {

    this.snackBar.open(
      message,
      'Close',
      {
        duration: 3500,
        horizontalPosition: 'end',
        verticalPosition: 'top',
        panelClass: [
          'success-snackbar'
        ]
      }
    );
  }

  showError(
    message: string
  ): void {

    this.snackBar.open(
      message,
      'Close',
      {
        duration: 5000,
        horizontalPosition: 'end',
        verticalPosition: 'top',
        panelClass: [
          'error-snackbar'
        ]
      }
    );
  }

  showInformation(
    message: string
  ): void {

    this.snackBar.open(
      message,
      'Close',
      {
        duration: 3500,
        horizontalPosition: 'end',
        verticalPosition: 'top',
        panelClass: [
          'information-snackbar'
        ]
      }
    );
  }
}