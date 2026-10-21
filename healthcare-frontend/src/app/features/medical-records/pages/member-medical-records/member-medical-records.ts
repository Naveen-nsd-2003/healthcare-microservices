import {
  Component,
  OnDestroy,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';
import {
  Router
} from '@angular/router';
import {
  MatButtonModule
} from '@angular/material/button';
import {
  MatCardModule
} from '@angular/material/card';
import {
  MatFormFieldModule
} from '@angular/material/form-field';
import {
  MatIconModule
} from '@angular/material/icon';
import {
  MatInputModule
} from '@angular/material/input';
import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';
import {
  MatSelectModule
} from '@angular/material/select';
import {
  MedicalRecord
} from '../../models/medical-record';
import {
  MedicalRecordStatus
} from '../../models/medical-record-status';
import {
  MedicalRecordStateService
} from '../../services/medical-record-state.service';

type MedicalRecordStatusFilter =
  | 'ALL'
  | MedicalRecordStatus;

type MedicalRecordSortOption =
  | 'APPOINTMENT_DESCENDING'
  | 'APPOINTMENT_ASCENDING'
  | 'DOCTOR_ASCENDING'
  | 'UPDATED_DESCENDING';

interface MemberDoctorMedicalRecordGroup {
  doctorId: string;
  doctorName: string;
  doctorSpecialization: string;
  providerOrganizationName: string;
  departmentName: string;
  totalRecords: number;
  activeRecords: number;
  amendedRecords: number;
  voidedRecords: number;
  records: MedicalRecord[];
}

@Component({
  selector: 'app-member-medical-records',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './member-medical-records.html',
  styleUrl: './member-medical-records.scss'
})
export class MemberMedicalRecords
  implements OnInit, OnDestroy {

  private readonly router =
    inject(Router);

  readonly medicalRecordState =
    inject(MedicalRecordStateService);

  readonly searchText =
    signal('');

  readonly selectedStatus =
    signal<MedicalRecordStatusFilter>(
      'ALL'
    );

  readonly selectedDoctorId =
    signal('ALL');

  readonly selectedSort =
    signal<MedicalRecordSortOption>(
      'APPOINTMENT_DESCENDING'
    );

  readonly expandedDoctorIds =
    signal<Set<string>>(
      new Set<string>()
    );

  readonly totalRecordCount =
    computed(() => {
      return this.medicalRecordState
        .memberRecords()
        .length;
    });

  readonly activeRecordCount =
    computed(() => {
      return this.medicalRecordState
        .memberRecords()
        .filter(record => {
          return record.status === 'ACTIVE';
        })
        .length;
    });

  readonly followUpRecordCount =
    computed(() => {
      return this.medicalRecordState
        .memberRecords()
        .filter(record => {
          return Boolean(
            record.followUpDate
          );
        })
        .length;
    });

  readonly doctorCount =
    computed(() => {
      return new Set(
        this.medicalRecordState
          .memberRecords()
          .map(record => {
            return record.doctorId;
          })
      ).size;
    });

  readonly doctorOptions =
    computed(() => {
      const doctors =
        new Map<
          string,
          {
            doctorName: string;
            doctorSpecialization: string;
          }
        >();

      this.medicalRecordState
        .memberRecords()
        .forEach(record => {
          doctors.set(
            record.doctorId,
            {
              doctorName:
                record.doctorName,
              doctorSpecialization:
                record.doctorSpecialization
            }
          );
        });

      return Array.from(
        doctors.entries()
      )
        .map(([
          doctorId,
          doctor
        ]) => {
          return {
            doctorId,
            doctorName:
              doctor.doctorName,
            doctorSpecialization:
              doctor.doctorSpecialization
          };
        })
        .sort((
          firstDoctor,
          secondDoctor
        ) => {
          return firstDoctor.doctorName
            .localeCompare(
              secondDoctor.doctorName
            );
        });
    });

  readonly filteredMedicalRecords =
    computed(() => {
      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const selectedStatus =
        this.selectedStatus();

      const selectedDoctorId =
        this.selectedDoctorId();

      const selectedSort =
        this.selectedSort();

      const filteredRecords =
        this.medicalRecordState
          .memberRecords()
          .filter(record => {
            const clinicalObservations =
              record.clinicalObservations
              ?? '';

            const prescriptionNotes =
              record.prescriptionNotes
              ?? '';

            const matchesSearch =
              search.length === 0
              || record.doctorName
                .toLowerCase()
                .includes(search)
              || record.doctorSpecialization
                .toLowerCase()
                .includes(search)
              || record.providerOrganizationName
                .toLowerCase()
                .includes(search)
              || record.departmentName
                .toLowerCase()
                .includes(search)
              || record.chiefComplaint
                .toLowerCase()
                .includes(search)
              || record.diagnosis
                .toLowerCase()
                .includes(search)
              || clinicalObservations
                .toLowerCase()
                .includes(search)
              || record.treatmentPlan
                .toLowerCase()
                .includes(search)
              || prescriptionNotes
                .toLowerCase()
                .includes(search);

            const matchesStatus =
              selectedStatus === 'ALL'
              || record.status
                === selectedStatus;

            const matchesDoctor =
              selectedDoctorId === 'ALL'
              || record.doctorId
                === selectedDoctorId;

            return matchesSearch
              && matchesStatus
              && matchesDoctor;
          });

      return [
        ...filteredRecords
      ].sort((
        firstRecord,
        secondRecord
      ) => {
        if (
          selectedSort
          === 'APPOINTMENT_ASCENDING'
        ) {
          return this.getAppointmentDateTimeValue(
            firstRecord
          ) - this.getAppointmentDateTimeValue(
            secondRecord
          );
        }

        if (
          selectedSort
          === 'DOCTOR_ASCENDING'
        ) {
          return firstRecord.doctorName
            .localeCompare(
              secondRecord.doctorName
            );
        }

        if (
          selectedSort
          === 'UPDATED_DESCENDING'
        ) {
          return this.getTimestampValue(
            secondRecord.updatedAt
          ) - this.getTimestampValue(
            firstRecord.updatedAt
          );
        }

        return this.getAppointmentDateTimeValue(
          secondRecord
        ) - this.getAppointmentDateTimeValue(
          firstRecord
        );
      });
    });

  readonly doctorGroups =
    computed<
      MemberDoctorMedicalRecordGroup[]
    >(() => {
      const groups =
        new Map<
          string,
          MedicalRecord[]
        >();

      this.filteredMedicalRecords()
        .forEach(record => {
          const existingRecords =
            groups.get(
              record.doctorId
            ) ?? [];

          existingRecords.push(
            record
          );

          groups.set(
            record.doctorId,
            existingRecords
          );
        });

      return Array.from(
        groups.entries()
      )
        .map(([
          doctorId,
          records
        ]) => {
          const firstRecord =
            records[0];

          return {
            doctorId,
            doctorName:
              firstRecord.doctorName,
            doctorSpecialization:
              firstRecord.doctorSpecialization,
            providerOrganizationName:
              firstRecord.providerOrganizationName,
            departmentName:
              firstRecord.departmentName,
            totalRecords:
              records.length,
            activeRecords:
              this.countStatus(
                records,
                'ACTIVE'
              ),
            amendedRecords:
              this.countStatus(
                records,
                'AMENDED'
              ),
            voidedRecords:
              this.countStatus(
                records,
                'VOIDED'
              ),
            records
          };
        })
        .sort((
          firstGroup,
          secondGroup
        ) => {
          return firstGroup.doctorName
            .localeCompare(
              secondGroup.doctorName
            );
        });
    });

  readonly filtersApplied =
    computed(() => {
      return this.searchText()
        .trim()
        .length > 0
        || this.selectedStatus()
          !== 'ALL'
        || this.selectedDoctorId()
          !== 'ALL'
        || this.selectedSort()
          !== 'APPOINTMENT_DESCENDING';
    });

  ngOnInit(): void {
    this.loadMedicalRecords();
  }

  ngOnDestroy(): void {
    this.medicalRecordState
      .clearError();
  }

  loadMedicalRecords(): void {
    this.medicalRecordState
      .loadMyMedicalRecords();
  }

  updateSearch(
    value: string
  ): void {
    this.searchText.set(
      value
    );
  }

  updateStatus(
    status: MedicalRecordStatusFilter
  ): void {
    this.selectedStatus.set(
      status
    );
  }

  updateDoctor(
    doctorId: string
  ): void {
    this.selectedDoctorId.set(
      doctorId
    );

    if (doctorId === 'ALL') {
      return;
    }

    this.expandedDoctorIds.update(
      currentIds => {
        const updatedIds =
          new Set(currentIds);

        updatedIds.add(
          doctorId
        );

        return updatedIds;
      }
    );
  }

  updateSort(
    sort: MedicalRecordSortOption
  ): void {
    this.selectedSort.set(
      sort
    );
  }

  clearFilters(): void {
    this.searchText.set('');

    this.selectedStatus.set(
      'ALL'
    );

    this.selectedDoctorId.set(
      'ALL'
    );

    this.selectedSort.set(
      'APPOINTMENT_DESCENDING'
    );
  }

  toggleDoctorGroup(
    doctorId: string
  ): void {
    this.expandedDoctorIds.update(
      currentIds => {
        const updatedIds =
          new Set(currentIds);

        if (
          updatedIds.has(
            doctorId
          )
        ) {
          updatedIds.delete(
            doctorId
          );
        } else {
          updatedIds.add(
            doctorId
          );
        }

        return updatedIds;
      }
    );
  }

  isDoctorExpanded(
    doctorId: string
  ): boolean {
    return this.expandedDoctorIds()
      .has(
        doctorId
      );
  }

  expandAll(): void {
    this.expandedDoctorIds.set(
      new Set(
        this.doctorGroups()
          .map(group => {
            return group.doctorId;
          })
      )
    );
  }

  collapseAll(): void {
    this.expandedDoctorIds.set(
      new Set<string>()
    );
  }

  viewMedicalRecord(
    record: MedicalRecord
  ): void {
    this.router.navigate([
      '/member/medical-records',
      record.id
    ]);
  }

  getDoctorInitials(
    doctorName: string
  ): string {
    const nameParts =
      doctorName
        .trim()
        .split(/\s+/)
        .filter(namePart => {
          return namePart.length > 0;
        });

    if (nameParts.length === 0) {
      return 'DR';
    }

    if (nameParts.length === 1) {
      return nameParts[0]
        .substring(
          0,
          2
        )
        .toUpperCase();
    }

    return (
      nameParts[0].charAt(0)
      + nameParts[
        nameParts.length - 1
      ].charAt(0)
    ).toUpperCase();
  }

  getFormattedDate(
    dateValue: string
  ): string {
    const dateParts =
      dateValue.split('-');

    if (dateParts.length !== 3) {
      return dateValue;
    }

    const year =
      Number(dateParts[0]);

    const month =
      Number(dateParts[1]);

    const day =
      Number(dateParts[2]);

    const date =
      new Date(
        year,
        month - 1,
        day
      );

    if (
      Number.isNaN(
        date.getTime()
      )
    ) {
      return dateValue;
    }

    return new Intl.DateTimeFormat(
      'en-IN',
      {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
      }
    ).format(date);
  }

  getTimeRange(
    record: MedicalRecord
  ): string {
    return this.getTimeLabel(
      record.appointmentStartTime
    )
      + ' - '
      + this.getTimeLabel(
        record.appointmentEndTime
      );
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

  getDiagnosisPreview(
    diagnosis: string
  ): string {
    const normalizedDiagnosis =
      diagnosis.trim();

    if (
      normalizedDiagnosis.length
      <= 130
    ) {
      return normalizedDiagnosis;
    }

    return normalizedDiagnosis
      .substring(
        0,
        127
      )
      .trimEnd()
      + '...';
  }

  private getTimeLabel(
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

  private countStatus(
    records: MedicalRecord[],
    status: MedicalRecordStatus
  ): number {
    return records.filter(record => {
      return record.status === status;
    }).length;
  }

  private getAppointmentDateTimeValue(
    record: MedicalRecord
  ): number {
    const timestamp =
      new Date(
        record.appointmentDate
        + 'T'
        + record.appointmentStartTime
      ).getTime();

    return Number.isNaN(timestamp)
      ? 0
      : timestamp;
  }

  private getTimestampValue(
    timestamp: string
  ): number {
    const value =
      new Date(
        timestamp
      ).getTime();

    return Number.isNaN(value)
      ? 0
      : value;
  }
}