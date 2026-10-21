// import {
//   Component,
//   OnDestroy,
//   OnInit,
//   inject
// } from '@angular/core';
// import {
//   DatePipe
// } from '@angular/common';
// import {
//   ActivatedRoute,
//   Router
// } from '@angular/router';
// import {
//   MatButtonModule
// } from '@angular/material/button';
// import {
//   MatCardModule
// } from '@angular/material/card';
// import {
//   MatIconModule
// } from '@angular/material/icon';
// import {
//   MatProgressSpinnerModule
// } from '@angular/material/progress-spinner';
// import {
//   NotificationMessageService
// } from '../../../../core/services/notification-message.service';
// import {
//   MedicalRecordStatus
// } from '../../models/medical-record-status';
// import {
//   MedicalRecordStateService
// } from '../../services/medical-record-state.service';

// @Component({
//   selector: 'app-provider-medical-record-details',
//   standalone: true,
//   imports: [
//     DatePipe,
//     MatButtonModule,
//     MatCardModule,
//     MatIconModule,
//     MatProgressSpinnerModule
//   ],
//   templateUrl: './provider-medical-record-details.html',
//   styleUrl: './provider-medical-record-details.scss'
// })
// export class ProviderMedicalRecordDetails
//   implements OnInit, OnDestroy {

//   private readonly activatedRoute =
//     inject(ActivatedRoute);

//   private readonly router =
//     inject(Router);

//   private readonly notificationService =
//     inject(NotificationMessageService);

//   readonly medicalRecordState =
//     inject(MedicalRecordStateService);

//   private medicalRecordId: string | null =
//     null;

//   ngOnInit(): void {
//     this.medicalRecordId =
//       this.activatedRoute
//         .snapshot
//         .paramMap
//         .get('medicalRecordId');

//     if (!this.medicalRecordId) {
//       this.notificationService.showError(
//         'Medical record identifier is missing.'
//       );

//       this.goBack();

//       return;
//     }

//     this.loadMedicalRecord();
//   }

//   ngOnDestroy(): void {
//     this.medicalRecordState
//       .clearSelectedRecord();
//   }

//   loadMedicalRecord(): void {
//     if (!this.medicalRecordId) {
//       return;
//     }

//     this.medicalRecordState
//       .loadProviderMedicalRecordById(
//         this.medicalRecordId
//       );
//   }

//   goBack(): void {
//     this.router.navigate([
//       '/provider/medical-records'
//     ]);
//   }

//   viewAppointment(): void {
//     const medicalRecord =
//       this.medicalRecordState
//         .selectedRecord();

//     if (!medicalRecord) {
//       return;
//     }

//     this.router.navigate([
//       '/provider/appointments',
//       medicalRecord.appointmentId
//     ]);
//   }

//   getTimeRange(
//     startTime: string,
//     endTime: string
//   ): string {
//     return this.getTimeLabel(startTime)
//       + ' - '
//       + this.getTimeLabel(endTime);
//   }

//   getTimeLabel(
//     timeValue: string
//   ): string {
//     if (!timeValue) {
//       return 'Not available';
//     }

//     const parts =
//       timeValue.split(':');

//     if (parts.length < 2) {
//       return timeValue;
//     }

//     const hours =
//       Number(parts[0]);

//     const minutes =
//       Number(parts[1]);

//     if (
//       Number.isNaN(hours)
//       || Number.isNaN(minutes)
//       || hours < 0
//       || hours > 23
//       || minutes < 0
//       || minutes > 59
//     ) {
//       return timeValue;
//     }

//     const period =
//       hours >= 12
//         ? 'PM'
//         : 'AM';

//     const displayHours =
//       hours % 12 === 0
//         ? 12
//         : hours % 12;

//     const displayMinutes =
//       String(minutes).padStart(
//         2,
//         '0'
//       );

//     return displayHours
//       + ':'
//       + displayMinutes
//       + ' '
//       + period;
//   }

//   getStatusLabel(
//     status: MedicalRecordStatus
//   ): string {
//     if (status === 'AMENDED') {
//       return 'Amended';
//     }

//     if (status === 'VOIDED') {
//       return 'Voided';
//     }

//     return 'Active';
//   }

//   getStatusIcon(
//     status: MedicalRecordStatus
//   ): string {
//     if (status === 'AMENDED') {
//       return 'edit_note';
//     }

//     if (status === 'VOIDED') {
//       return 'block';
//     }

//     return 'verified';
//   }
// }

import {
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';
import {
  DatePipe
} from '@angular/common';
import {
  ActivatedRoute,
  Router
} from '@angular/router';
import {
  MatButtonModule
} from '@angular/material/button';
import {
  MatIconModule
} from '@angular/material/icon';
import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';
import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';
import {
  MedicalRecordStatus
} from '../../models/medical-record-status';
import {
  MedicalRecordStateService
} from '../../services/medical-record-state.service';

@Component({
  selector: 'app-provider-medical-record-details',
  standalone: true,
  imports: [
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './provider-medical-record-details.html',
  styleUrl: './provider-medical-record-details.scss'
})
export class ProviderMedicalRecordDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly medicalRecordState =
    inject(MedicalRecordStateService);

  private medicalRecordId: string | null =
    null;

  ngOnInit(): void {
    this.medicalRecordId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('medicalRecordId');

    if (!this.medicalRecordId) {
      this.notificationService.showError(
        'Medical record identifier is missing.'
      );

      this.goBack();

      return;
    }

    this.loadMedicalRecord();
  }

  ngOnDestroy(): void {
    this.medicalRecordState
      .clearSelectedRecord();
  }

  loadMedicalRecord(): void {
    if (!this.medicalRecordId) {
      return;
    }

    this.medicalRecordState
      .loadProviderMedicalRecordById(
        this.medicalRecordId
      );
  }

  goBack(): void {
    this.router.navigate([
      '/provider/medical-records'
    ]);
  }

  viewAppointment(): void {
    const medicalRecord =
      this.medicalRecordState
        .selectedRecord();

    if (!medicalRecord) {
      return;
    }

    this.router.navigate([
      '/provider/appointments',
      medicalRecord.appointmentId
    ]);
  }

  editMedicalRecord(): void {
    const medicalRecord =
      this.medicalRecordState
        .selectedRecord();

    if (!medicalRecord) {
      return;
    }

    this.router.navigate([
      '/provider/medical-records',
      medicalRecord.id,
      'edit'
    ]);
  }

  getTimeRange(
    startTime: string,
    endTime: string
  ): string {
    return this.getTimeLabel(startTime)
      + ' - '
      + this.getTimeLabel(endTime);
  }

  getTimeLabel(
    timeValue: string
  ): string {
    if (!timeValue) {
      return 'Not available';
    }

    const parts =
      timeValue.split(':');

    if (parts.length < 2) {
      return timeValue;
    }

    const hours =
      Number(parts[0]);

    const minutes =
      Number(parts[1]);

    if (
      Number.isNaN(hours)
      || Number.isNaN(minutes)
      || hours < 0
      || hours > 23
      || minutes < 0
      || minutes > 59
    ) {
      return timeValue;
    }

    const period =
      hours >= 12
        ? 'PM'
        : 'AM';

    const displayHours =
      hours % 12 === 0
        ? 12
        : hours % 12;

    const displayMinutes =
      String(minutes).padStart(
        2,
        '0'
      );

    return displayHours
      + ':'
      + displayMinutes
      + ' '
      + period;
  }

  getStatusLabel(
    status: MedicalRecordStatus
  ): string {
    if (status === 'AMENDED') {
      return 'Amended';
    }

    if (status === 'VOIDED') {
      return 'Voided';
    }

    return 'Active';
  }

  getStatusIcon(
    status: MedicalRecordStatus
  ): string {
    if (status === 'AMENDED') {
      return 'edit_note';
    }

    if (status === 'VOIDED') {
      return 'block';
    }

    return 'verified';
  }

  downloadMedicalRecord(): void {
    window.print();
  }


  submitInsuranceClaim(): void {
    const medicalRecord =
      this.medicalRecordState
        .selectedRecord();
  
    if (!medicalRecord) {
      return;
    }
  
    this.router.navigate(
      [
        '/provider/claims/new'
      ],
      {
        queryParams: {
          appointmentId:
            medicalRecord.appointmentId,
          medicalRecordId:
            medicalRecord.id
        }
      }
    );
  }
}