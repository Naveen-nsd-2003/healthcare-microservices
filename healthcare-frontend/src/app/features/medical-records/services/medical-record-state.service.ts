import {
    Injectable,
    computed,
    inject,
    signal
  } from '@angular/core';
  
  import {
    HttpErrorResponse
  } from '@angular/common/http';
  
  import {
    finalize
  } from 'rxjs/operators';
  
  import {
    CreateMedicalRecordRequest
  } from '../models/create-medical-record-request';
  
  import {
    MedicalRecord
  } from '../models/medical-record';
  
  import {
    UpdateMedicalRecordRequest
  } from '../models/update-medical-record-request';
  
  import {
    MedicalRecordApiService
  } from './medical-record-api.service';
  
  type SuccessCallback =
    (medicalRecord: MedicalRecord) => void;
  
  type ErrorCallback =
    (message: string) => void;
  
  @Injectable({
    providedIn: 'root'
  })
  export class MedicalRecordStateService {
  
    private readonly medicalRecordApi =
      inject(MedicalRecordApiService);
  
    private readonly providerRecordsSignal =
      signal<MedicalRecord[]>([]);
  
    private readonly memberRecordsSignal =
      signal<MedicalRecord[]>([]);
  
    private readonly selectedRecordSignal =
      signal<MedicalRecord | null>(null);
  
    private readonly listLoadingSignal =
      signal(false);
  
    private readonly detailsLoadingSignal =
      signal(false);
  
    private readonly creatingSignal =
      signal(false);
  
    private readonly updatingSignal =
      signal(false);
  
    private readonly errorMessageSignal =
      signal<string | null>(null);
  
    readonly providerRecords =
      this.providerRecordsSignal.asReadonly();
  
    readonly memberRecords =
      this.memberRecordsSignal.asReadonly();
  
    readonly selectedRecord =
      this.selectedRecordSignal.asReadonly();
  
    readonly listLoading =
      this.listLoadingSignal.asReadonly();
  
    readonly detailsLoading =
      this.detailsLoadingSignal.asReadonly();
  
    readonly creating =
      this.creatingSignal.asReadonly();
  
    readonly updating =
      this.updatingSignal.asReadonly();
  
    readonly errorMessage =
      this.errorMessageSignal.asReadonly();
  
    readonly actionInProgress =
      computed(
        () =>
          this.creatingSignal()
          || this.updatingSignal()
      );
  
    loadProviderMedicalRecords(): void {
  
      if (this.listLoadingSignal()) {
        return;
      }
  
      this.listLoadingSignal.set(true);
      this.errorMessageSignal.set(null);
  
      this.medicalRecordApi
        .getProviderMedicalRecords()
        .pipe(
          finalize(
            () => {
              this.listLoadingSignal.set(false);
            }
          )
        )
        .subscribe({
          next: (
            medicalRecords: MedicalRecord[]
          ) => {
            this.providerRecordsSignal.set(
              medicalRecords
            );
          },
          error: (
            error: unknown
          ) => {
            this.providerRecordsSignal.set([]);
  
            this.errorMessageSignal.set(
              this.extractErrorMessage(
                error,
                'Unable to load medical records.'
              )
            );
          }
        });
    }
  
    loadProviderMedicalRecordsByDoctor(
      doctorId: string
    ): void {
  
      if (
        !doctorId
        || this.listLoadingSignal()
      ) {
        return;
      }
  
      this.listLoadingSignal.set(true);
      this.errorMessageSignal.set(null);
  
      this.medicalRecordApi
        .getProviderMedicalRecordsByDoctor(
          doctorId
        )
        .pipe(
          finalize(
            () => {
              this.listLoadingSignal.set(false);
            }
          )
        )
        .subscribe({
          next: (
            medicalRecords: MedicalRecord[]
          ) => {
            this.providerRecordsSignal.set(
              medicalRecords
            );
          },
          error: (
            error: unknown
          ) => {
            this.providerRecordsSignal.set([]);
  
            this.errorMessageSignal.set(
              this.extractErrorMessage(
                error,
                'Unable to load medical records '
                + 'for the selected doctor.'
              )
            );
          }
        });
    }
  
    loadProviderMedicalRecordById(
      medicalRecordId: string
    ): void {
  
      if (
        !medicalRecordId
        || this.detailsLoadingSignal()
      ) {
        return;
      }
  
      this.detailsLoadingSignal.set(true);
      this.errorMessageSignal.set(null);
      this.selectedRecordSignal.set(null);
  
      this.medicalRecordApi
        .getProviderMedicalRecordById(
          medicalRecordId
        )
        .pipe(
          finalize(
            () => {
              this.detailsLoadingSignal.set(false);
            }
          )
        )
        .subscribe({
          next: (
            medicalRecord: MedicalRecord
          ) => {
            this.selectedRecordSignal.set(
              medicalRecord
            );
  
            this.upsertProviderRecord(
              medicalRecord
            );
          },
          error: (
            error: unknown
          ) => {
            this.errorMessageSignal.set(
              this.extractErrorMessage(
                error,
                'Unable to load the medical record.'
              )
            );
          }
        });
    }
  
    createMedicalRecord(
      request: CreateMedicalRecordRequest,
      onSuccess?: SuccessCallback,
      onError?: ErrorCallback
    ): void {
  
      if (this.creatingSignal()) {
        return;
      }
  
      this.creatingSignal.set(true);
      this.errorMessageSignal.set(null);
  
      this.medicalRecordApi
        .createMedicalRecord(
          request
        )
        .pipe(
          finalize(
            () => {
              this.creatingSignal.set(false);
            }
          )
        )
        .subscribe({
          next: (
            medicalRecord: MedicalRecord
          ) => {
            this.selectedRecordSignal.set(
              medicalRecord
            );
  
            this.upsertProviderRecord(
              medicalRecord
            );
  
            if (onSuccess) {
              onSuccess(
                medicalRecord
              );
            }
          },
          error: (
            error: unknown
          ) => {
            const message =
              this.extractErrorMessage(
                error,
                'Unable to create the '
                + 'medical record.'
              );
  
            this.errorMessageSignal.set(
              message
            );
  
            if (onError) {
              onError(
                message
              );
            }
          }
        });
    }
  
    updateProviderMedicalRecord(
      medicalRecordId: string,
      request: UpdateMedicalRecordRequest,
      onSuccess?: SuccessCallback,
      onError?: ErrorCallback
    ): void {
  
      if (
        !medicalRecordId
        || this.updatingSignal()
      ) {
        return;
      }
  
      this.updatingSignal.set(true);
      this.errorMessageSignal.set(null);
  
      this.medicalRecordApi
        .updateProviderMedicalRecord(
          medicalRecordId,
          request
        )
        .pipe(
          finalize(
            () => {
              this.updatingSignal.set(false);
            }
          )
        )
        .subscribe({
          next: (
            medicalRecord: MedicalRecord
          ) => {
            this.selectedRecordSignal.set(
              medicalRecord
            );
  
            this.upsertProviderRecord(
              medicalRecord
            );
  
            if (onSuccess) {
              onSuccess(
                medicalRecord
              );
            }
          },
          error: (
            error: unknown
          ) => {
            const message =
              this.extractErrorMessage(
                error,
                'Unable to update the '
                + 'medical record.'
              );
  
            this.errorMessageSignal.set(
              message
            );
  
            if (onError) {
              onError(
                message
              );
            }
          }
        });
    }
  
    loadMyMedicalRecords(): void {
  
      if (this.listLoadingSignal()) {
        return;
      }
  
      this.listLoadingSignal.set(true);
      this.errorMessageSignal.set(null);
  
      this.medicalRecordApi
        .getMyMedicalRecords()
        .pipe(
          finalize(
            () => {
              this.listLoadingSignal.set(false);
            }
          )
        )
        .subscribe({
          next: (
            medicalRecords: MedicalRecord[]
          ) => {
            this.memberRecordsSignal.set(
              medicalRecords
            );
          },
          error: (
            error: unknown
          ) => {
            this.memberRecordsSignal.set([]);
  
            this.errorMessageSignal.set(
              this.extractErrorMessage(
                error,
                'Unable to load your '
                + 'medical records.'
              )
            );
          }
        });
    }
  
    loadMyMedicalRecordById(
      medicalRecordId: string
    ): void {
  
      if (
        !medicalRecordId
        || this.detailsLoadingSignal()
      ) {
        return;
      }
  
      this.detailsLoadingSignal.set(true);
      this.errorMessageSignal.set(null);
      this.selectedRecordSignal.set(null);
  
      this.medicalRecordApi
        .getMyMedicalRecordById(
          medicalRecordId
        )
        .pipe(
          finalize(
            () => {
              this.detailsLoadingSignal.set(false);
            }
          )
        )
        .subscribe({
          next: (
            medicalRecord: MedicalRecord
          ) => {
            this.selectedRecordSignal.set(
              medicalRecord
            );
  
            this.upsertMemberRecord(
              medicalRecord
            );
          },
          error: (
            error: unknown
          ) => {
            this.errorMessageSignal.set(
              this.extractErrorMessage(
                error,
                'Unable to load the medical record.'
              )
            );
          }
        });
    }
  
    selectProviderRecord(
      medicalRecordId: string
    ): void {
  
      const medicalRecord =
        this.providerRecordsSignal()
          .find(
            record =>
              record.id === medicalRecordId
          );
  
      this.selectedRecordSignal.set(
        medicalRecord ?? null
      );
  
      this.errorMessageSignal.set(null);
    }
  
    selectMemberRecord(
      medicalRecordId: string
    ): void {
  
      const medicalRecord =
        this.memberRecordsSignal()
          .find(
            record =>
              record.id === medicalRecordId
          );
  
      this.selectedRecordSignal.set(
        medicalRecord ?? null
      );
  
      this.errorMessageSignal.set(null);
    }
  
    clearProviderRecords(): void {
  
      this.providerRecordsSignal.set([]);
      this.errorMessageSignal.set(null);
    }
  
    clearMemberRecords(): void {
  
      this.memberRecordsSignal.set([]);
      this.errorMessageSignal.set(null);
    }
  
    clearSelectedRecord(): void {
  
      this.selectedRecordSignal.set(null);
      this.errorMessageSignal.set(null);
    }
  
    clearError(): void {
  
      this.errorMessageSignal.set(null);
    }
  
    clearState(): void {
  
      this.providerRecordsSignal.set([]);
      this.memberRecordsSignal.set([]);
      this.selectedRecordSignal.set(null);
  
      this.listLoadingSignal.set(false);
      this.detailsLoadingSignal.set(false);
      this.creatingSignal.set(false);
      this.updatingSignal.set(false);
  
      this.errorMessageSignal.set(null);
    }
  
    private upsertProviderRecord(
      medicalRecord: MedicalRecord
    ): void {
  
      const currentRecords =
        this.providerRecordsSignal();
  
      const existingRecordIndex =
        currentRecords.findIndex(
          record =>
            record.id === medicalRecord.id
        );
  
      if (existingRecordIndex === -1) {
        this.providerRecordsSignal.set([
          medicalRecord,
          ...currentRecords
        ]);
  
        return;
      }
  
      const updatedRecords =
        currentRecords.map(
          record =>
            record.id === medicalRecord.id
              ? medicalRecord
              : record
        );
  
      this.providerRecordsSignal.set(
        updatedRecords
      );
    }
  
    private upsertMemberRecord(
      medicalRecord: MedicalRecord
    ): void {
  
      const currentRecords =
        this.memberRecordsSignal();
  
      const existingRecordIndex =
        currentRecords.findIndex(
          record =>
            record.id === medicalRecord.id
        );
  
      if (existingRecordIndex === -1) {
        this.memberRecordsSignal.set([
          medicalRecord,
          ...currentRecords
        ]);
  
        return;
      }
  
      const updatedRecords =
        currentRecords.map(
          record =>
            record.id === medicalRecord.id
              ? medicalRecord
              : record
        );
  
      this.memberRecordsSignal.set(
        updatedRecords
      );
    }
  
    private extractErrorMessage(
      error: unknown,
      fallbackMessage: string
    ): string {
  
      if (!(error instanceof HttpErrorResponse)) {
        return fallbackMessage;
      }
  
      if (
        typeof error.error === 'string'
        && error.error.trim().length > 0
      ) {
        return error.error.trim();
      }
  
      const backendMessage =
        this.readBackendMessage(
          error.error
        );
  
      if (backendMessage) {
        return backendMessage;
      }
  
      if (error.status === 0) {
        return 'Unable to connect to the server. '
          + 'Check your connection and try again.';
      }
  
      if (error.status === 400) {
        return 'The medical record information '
          + 'is invalid. Review the form '
          + 'and try again.';
      }
  
      if (error.status === 401) {
        return 'Your session has expired. '
          + 'Please sign in again.';
      }
  
      if (error.status === 403) {
        return 'You are not authorized to perform '
          + 'this medical record operation.';
      }
  
      if (error.status === 404) {
        return 'The requested medical record '
          + 'could not be found.';
      }
  
      if (error.status === 409) {
        return 'A medical record already exists '
          + 'for this appointment.';
      }
  
      return fallbackMessage;
    }
  
    private readBackendMessage(
      responseBody: unknown
    ): string | null {
  
      if (
        responseBody === null
        || typeof responseBody !== 'object'
      ) {
        return null;
      }
  
      const response =
        responseBody as {
          message?: unknown;
          detail?: unknown;
          error?: unknown;
        };
  
      if (
        typeof response.message === 'string'
        && response.message.trim().length > 0
      ) {
        return response.message.trim();
      }
  
      if (
        typeof response.detail === 'string'
        && response.detail.trim().length > 0
      ) {
        return response.detail.trim();
      }
  
      if (
        typeof response.error === 'string'
        && response.error.trim().length > 0
      ) {
        return response.error.trim();
      }
  
      return null;
    }
  }