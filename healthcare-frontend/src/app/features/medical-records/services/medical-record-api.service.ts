import {
    Injectable,
    inject
  } from '@angular/core';
  
  import {
    HttpClient
  } from '@angular/common/http';
  
  import {
    Observable
  } from 'rxjs';
  
  import {
    CreateMedicalRecordRequest
  } from '../models/create-medical-record-request';
  
  import {
    MedicalRecord
  } from '../models/medical-record';
  
  import {
    UpdateMedicalRecordRequest
  } from '../models/update-medical-record-request';
  
  @Injectable({
    providedIn: 'root'
  })
  export class MedicalRecordApiService {
  
    private readonly http =
      inject(HttpClient);
  
    private readonly baseUrl =
      '/api/medical-records';
  
    createMedicalRecord(
      request: CreateMedicalRecordRequest
    ): Observable<MedicalRecord> {
  
      return this.http.post<MedicalRecord>(
        this.baseUrl,
        request
      );
    }
  
    getProviderMedicalRecords():
      Observable<MedicalRecord[]> {
  
      return this.http.get<MedicalRecord[]>(
        `${this.baseUrl}/provider`
      );
    }
  
    getProviderMedicalRecordsByDoctor(
      doctorId: string
    ): Observable<MedicalRecord[]> {
  
      return this.http.get<MedicalRecord[]>(
        `${this.baseUrl}/provider/doctor/${doctorId}`
      );
    }
  
    getProviderMedicalRecordById(
      medicalRecordId: string
    ): Observable<MedicalRecord> {
  
      return this.http.get<MedicalRecord>(
        `${this.baseUrl}/provider/${medicalRecordId}`
      );
    }
  
    updateProviderMedicalRecord(
      medicalRecordId: string,
      request: UpdateMedicalRecordRequest
    ): Observable<MedicalRecord> {
  
      return this.http.put<MedicalRecord>(
        `${this.baseUrl}/provider/${medicalRecordId}`,
        request
      );
    }
  
    getMyMedicalRecords():
      Observable<MedicalRecord[]> {
  
      return this.http.get<MedicalRecord[]>(
        `${this.baseUrl}/me`
      );
    }
  
    getMyMedicalRecordById(
      medicalRecordId: string
    ): Observable<MedicalRecord> {
  
      return this.http.get<MedicalRecord>(
        `${this.baseUrl}/me/${medicalRecordId}`
      );
    }
  }