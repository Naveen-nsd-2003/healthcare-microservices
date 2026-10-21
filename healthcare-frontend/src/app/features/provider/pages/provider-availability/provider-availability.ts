// import {
//   Component,
//   OnInit,
//   computed,
//   inject,
//   signal
// } from '@angular/core';

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
//   MatFormFieldModule
// } from '@angular/material/form-field';

// import {
//   MatIconModule
// } from '@angular/material/icon';

// import {
//   MatInputModule
// } from '@angular/material/input';

// import {
//   MatProgressSpinnerModule
// } from '@angular/material/progress-spinner';

// import {
//   MatSelectModule
// } from '@angular/material/select';

// import {
//   NotificationMessageService
// } from '../../../../core/services/notification-message.service';

// import {
//   AvailabilityStatus
// } from '../../models/availability-status';

// import {
//   DayOfWeek
// } from '../../models/day-of-week';

// import {
//   DoctorAvailability
// } from '../../models/doctor-availability';

// import {
//   AvailabilityManagementStateService
// } from '../../services/availability-management-state.service';

// type AvailabilityStatusFilter =
//   | 'ALL'
//   | AvailabilityStatus;

// type DayFilter =
//   | 'ALL'
//   | DayOfWeek;

// @Component({
//   selector: 'app-provider-availability',
//   standalone: true,
//   imports: [
//     MatButtonModule,
//     MatCardModule,
//     MatFormFieldModule,
//     MatIconModule,
//     MatInputModule,
//     MatProgressSpinnerModule,
//     MatSelectModule
//   ],
//   templateUrl: './provider-availability.html',
//   styleUrl: './provider-availability.scss'
// })
// export class ProviderAvailability
//   implements OnInit {

//   private readonly activatedRoute =
//     inject(ActivatedRoute);

//   private readonly router =
//     inject(Router);

//   private readonly notificationService =
//     inject(NotificationMessageService);

//   readonly availabilityState =
//     inject(AvailabilityManagementStateService);

//   private doctorId: string | null =
//     null;

//   readonly searchText =
//     signal('');

//   readonly selectedStatus =
//     signal<AvailabilityStatusFilter>('ALL');

//   readonly selectedDay =
//     signal<DayFilter>('ALL');

//   readonly selectedDoctorId =
//     signal('ALL');

//   readonly dayOptions:
//     {
//       value: DayOfWeek;
//       label: string;
//     }[] = [
//       {
//         value: 'MONDAY',
//         label: 'Monday'
//       },
//       {
//         value: 'TUESDAY',
//         label: 'Tuesday'
//       },
//       {
//         value: 'WEDNESDAY',
//         label: 'Wednesday'
//       },
//       {
//         value: 'THURSDAY',
//         label: 'Thursday'
//       },
//       {
//         value: 'FRIDAY',
//         label: 'Friday'
//       },
//       {
//         value: 'SATURDAY',
//         label: 'Saturday'
//       },
//       {
//         value: 'SUNDAY',
//         label: 'Sunday'
//       }
//     ];

//   readonly doctorOptions =
//     computed(() => {

//       const doctorMap =
//         new Map<string, string>();

//       this.availabilityState
//         .availabilities()
//         .forEach(availability => {

//           doctorMap.set(
//             availability.doctorId,
//             availability.doctorName
//           );
//         });

//       return Array.from(
//         doctorMap.entries()
//       )
//         .map(([id, name]) => {
//           return {
//             id,
//             name
//           };
//         })
//         .sort(
//           (firstDoctor, secondDoctor) => {

//             return firstDoctor.name
//               .localeCompare(
//                 secondDoctor.name
//               );
//           }
//         );
//     });

//   readonly filteredAvailabilities =
//     computed(() => {

//       const search =
//         this.searchText()
//           .trim()
//           .toLowerCase();

//       const status =
//         this.selectedStatus();

//       const day =
//         this.selectedDay();

//       const doctorId =
//         this.selectedDoctorId();

//       return this.availabilityState
//         .availabilities()
//         .filter(availability => {

//           const matchesSearch =
//             search.length === 0
//             || availability.doctorName
//               .toLowerCase()
//               .includes(search)
//             || availability.specialization
//               .toLowerCase()
//               .includes(search)
//             || availability.departmentName
//               .toLowerCase()
//               .includes(search);

//           const matchesStatus =
//             status === 'ALL'
//             || availability.status === status;

//           const matchesDay =
//             day === 'ALL'
//             || availability.dayOfWeek === day;

//           const matchesDoctor =
//             doctorId === 'ALL'
//             || availability.doctorId === doctorId;

//           return matchesSearch
//             && matchesStatus
//             && matchesDay
//             && matchesDoctor;
//         });
//     });

//   readonly filtersApplied =
//     computed(() => {

//       return this.searchText()
//         .trim()
//         .length > 0
//         || this.selectedStatus() !== 'ALL'
//         || this.selectedDay() !== 'ALL'
//         || this.selectedDoctorId() !== 'ALL';
//     });

//   ngOnInit(): void {

//     this.doctorId =
//       this.activatedRoute
//         .snapshot
//         .queryParamMap
//         .get('doctorId');

//     if (this.doctorId) {

//       this.selectedDoctorId.set(
//         this.doctorId
//       );

//       this.availabilityState
//         .loadMyDoctorAvailabilities(
//           this.doctorId
//         );

//       return;
//     }

//     this.loadAvailabilities();
//   }

//   loadAvailabilities(): void {

//     this.doctorId = null;
//     this.selectedDoctorId.set('ALL');

//     this.router.navigate(
//       [],
//       {
//         relativeTo:
//           this.activatedRoute,

//         queryParams: {
//           doctorId: null
//         },

//         queryParamsHandling:
//           'merge',

//         replaceUrl: true
//       }
//     );

//     this.availabilityState
//       .loadMyAvailabilities();
//   }

//   createAvailability(): void {

//     const queryParams:
//       {
//         doctorId?: string;
//       } = {};

//     if (
//       this.selectedDoctorId() !== 'ALL'
//     ) {
//       queryParams.doctorId =
//         this.selectedDoctorId();
//     }

//     this.router.navigate(
//       [
//         '/provider/availability/new'
//       ],
//       {
//         queryParams
//       }
//     );
//   }

//   editAvailability(
//     availability: DoctorAvailability
//   ): void {

//     this.router.navigate([
//       '/provider/availability',
//       availability.id,
//       'edit'
//     ]);
//   }

//   updateSearch(
//     value: string
//   ): void {

//     this.searchText.set(value);
//   }

//   updateStatus(
//     status: AvailabilityStatusFilter
//   ): void {

//     this.selectedStatus.set(status);
//   }

//   updateDay(
//     day: DayFilter
//   ): void {

//     this.selectedDay.set(day);
//   }

//   updateDoctor(
//     doctorId: string
//   ): void {

//     this.selectedDoctorId.set(
//       doctorId
//     );
//   }

//   clearFilters(): void {

//     this.searchText.set('');
//     this.selectedStatus.set('ALL');
//     this.selectedDay.set('ALL');

//     if (this.doctorId) {
//       this.selectedDoctorId.set(
//         this.doctorId
//       );
//     } else {
//       this.selectedDoctorId.set('ALL');
//     }
//   }

//   getDayLabel(
//     dayOfWeek: DayOfWeek
//   ): string {

//     const dayOption =
//       this.dayOptions.find(option => {
//         return option.value === dayOfWeek;
//       });

//     return dayOption?.label
//       ?? dayOfWeek;
//   }

//   getTimeLabel(
//     timeValue: string
//   ): string {

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
//     ) {
//       return timeValue;
//     }

//     const period =
//       hours >= 12
//         ? 'PM'
//         : 'AM';

//     const displayHour =
//       hours % 12 === 0
//         ? 12
//         : hours % 12;

//     const displayMinutes =
//       String(minutes).padStart(
//         2,
//         '0'
//       );

//     return (
//       displayHour
//       + ':'
//       + displayMinutes
//       + ' '
//       + period
//     );
//   }

//   getTimeRange(
//     availability: DoctorAvailability
//   ): string {

//     return (
//       this.getTimeLabel(
//         availability.startTime
//       )
//       + ' - '
//       + this.getTimeLabel(
//         availability.endTime
//       )
//     );
//   }

//   activateAvailability(
//     availability: DoctorAvailability
//   ): void {

//     this.updateAvailabilityStatus(
//       availability,
//       'ACTIVE'
//     );
//   }

//   deactivateAvailability(
//     availability: DoctorAvailability
//   ): void {

//     this.updateAvailabilityStatus(
//       availability,
//       'INACTIVE'
//     );
//   }

//   deleteAvailability(
//     availability: DoctorAvailability
//   ): void {

//     if (
//       this.availabilityState.deleting()
//     ) {
//       return;
//     }

//     const confirmed =
//       window.confirm(
//         'Delete the '
//         + this.getDayLabel(
//           availability.dayOfWeek
//         )
//         + ' schedule for '
//         + availability.doctorName
//         + '?'
//       );

//     if (!confirmed) {
//       return;
//     }

//     this.availabilityState
//       .deleteMyAvailability(
//         availability.id,
//         () => {

//           this.notificationService.showSuccess(
//             'Availability deleted successfully.'
//           );
//         },
//         message => {

//           this.notificationService.showError(
//             message
//           );
//         }
//       );
//   }

//   private updateAvailabilityStatus(
//     availability: DoctorAvailability,
//     status: AvailabilityStatus
//   ): void {

//     if (
//       availability.status === status
//       || this.availabilityState
//         .statusUpdating()
//     ) {
//       return;
//     }

//     this.availabilityState
//       .updateMyAvailabilityStatus(
//         availability.id,
//         status,
//         updatedAvailability => {

//           if (
//             updatedAvailability.status
//             === 'ACTIVE'
//           ) {

//             this.notificationService.showSuccess(
//               'Availability activated successfully.'
//             );

//             return;
//           }

//           this.notificationService.showSuccess(
//             'Availability made inactive successfully.'
//           );
//         },
//         message => {

//           this.notificationService.showError(
//             message
//           );
//         }
//       );
//   }
// }


import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  ActivatedRoute,
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
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  AvailabilityStatus
} from '../../models/availability-status';

import {
  DayOfWeek
} from '../../models/day-of-week';

import {
  DoctorAvailability
} from '../../models/doctor-availability';

import {
  AvailabilityManagementStateService
} from '../../services/availability-management-state.service';

type AvailabilityStatusFilter =
  | 'ALL'
  | AvailabilityStatus;

type DayFilter =
  | 'ALL'
  | DayOfWeek;

interface DoctorAvailabilityGroup {
  doctorId: string;
  doctorName: string;
  specialization: string;
  departmentName: string;
  totalSchedules: number;
  activeSchedules: number;
  inactiveSchedules: number;
  availabilities: DoctorAvailability[];
}

@Component({
  selector: 'app-provider-availability',
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
  templateUrl: './provider-availability.html',
  styleUrl: './provider-availability.scss'
})
export class ProviderAvailability
  implements OnInit {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly availabilityState =
    inject(AvailabilityManagementStateService);

  private routeDoctorId: string | null =
    null;

  readonly searchText =
    signal('');

  readonly selectedStatus =
    signal<AvailabilityStatusFilter>('ALL');

  readonly selectedDay =
    signal<DayFilter>('ALL');

  readonly selectedDoctorId =
    signal('ALL');

  readonly expandedDoctorIds =
    signal<Set<string>>(new Set<string>());

  readonly dayOptions:
    {
      value: DayOfWeek;
      label: string;
    }[] = [
      {
        value: 'MONDAY',
        label: 'Monday'
      },
      {
        value: 'TUESDAY',
        label: 'Tuesday'
      },
      {
        value: 'WEDNESDAY',
        label: 'Wednesday'
      },
      {
        value: 'THURSDAY',
        label: 'Thursday'
      },
      {
        value: 'FRIDAY',
        label: 'Friday'
      },
      {
        value: 'SATURDAY',
        label: 'Saturday'
      },
      {
        value: 'SUNDAY',
        label: 'Sunday'
      }
    ];

  private readonly dayOrder:
    Record<DayOfWeek, number> = {
      MONDAY: 1,
      TUESDAY: 2,
      WEDNESDAY: 3,
      THURSDAY: 4,
      FRIDAY: 5,
      SATURDAY: 6,
      SUNDAY: 7
    };

  readonly doctorOptions =
    computed(() => {

      const doctors =
        new Map<string, string>();

      this.availabilityState
        .availabilities()
        .forEach(availability => {

          doctors.set(
            availability.doctorId,
            availability.doctorName
          );
        });

      return Array.from(
        doctors.entries()
      )
        .map(([id, name]) => {
          return {
            id,
            name
          };
        })
        .sort(
          (firstDoctor, secondDoctor) => {

            return firstDoctor.name
              .localeCompare(
                secondDoctor.name
              );
          }
        );
    });

  readonly filteredAvailabilities =
    computed(() => {

      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const status =
        this.selectedStatus();

      const day =
        this.selectedDay();

      const doctorId =
        this.selectedDoctorId();

      return this.availabilityState
        .availabilities()
        .filter(availability => {

          const matchesSearch =
            search.length === 0
            || availability.doctorName
              .toLowerCase()
              .includes(search)
            || availability.specialization
              .toLowerCase()
              .includes(search)
            || availability.departmentName
              .toLowerCase()
              .includes(search);

          const matchesStatus =
            status === 'ALL'
            || availability.status === status;

          const matchesDay =
            day === 'ALL'
            || availability.dayOfWeek === day;

          const matchesDoctor =
            doctorId === 'ALL'
            || availability.doctorId === doctorId;

          return matchesSearch
            && matchesStatus
            && matchesDay
            && matchesDoctor;
        });
    });

  readonly doctorGroups =
    computed<DoctorAvailabilityGroup[]>(() => {

      const groups =
        new Map<string, DoctorAvailability[]>();

      this.filteredAvailabilities()
        .forEach(availability => {

          const currentGroup =
            groups.get(
              availability.doctorId
            ) ?? [];

          currentGroup.push(
            availability
          );

          groups.set(
            availability.doctorId,
            currentGroup
          );
        });

      return Array.from(
        groups.entries()
      )
        .map(([doctorId, availabilities]) => {

          const sortedAvailabilities =
            [...availabilities].sort(
              (
                firstAvailability,
                secondAvailability
              ) => {

                const dayDifference =
                  this.dayOrder[
                    firstAvailability.dayOfWeek
                  ]
                  - this.dayOrder[
                    secondAvailability.dayOfWeek
                  ];

                if (dayDifference !== 0) {
                  return dayDifference;
                }

                return firstAvailability.startTime
                  .localeCompare(
                    secondAvailability.startTime
                  );
              }
            );

          const firstAvailability =
            sortedAvailabilities[0];

          return {
            doctorId,

            doctorName:
              firstAvailability.doctorName,

            specialization:
              firstAvailability.specialization,

            departmentName:
              firstAvailability.departmentName,

            totalSchedules:
              sortedAvailabilities.length,

            activeSchedules:
              sortedAvailabilities.filter(
                availability => {
                  return availability.status
                    === 'ACTIVE';
                }
              ).length,

            inactiveSchedules:
              sortedAvailabilities.filter(
                availability => {
                  return availability.status
                    === 'INACTIVE';
                }
              ).length,

            availabilities:
              sortedAvailabilities
          };
        })
        .sort(
          (firstGroup, secondGroup) => {

            return firstGroup.doctorName
              .localeCompare(
                secondGroup.doctorName
              );
          }
        );
    });

  readonly filtersApplied =
    computed(() => {

      return this.searchText()
        .trim()
        .length > 0
        || this.selectedStatus() !== 'ALL'
        || this.selectedDay() !== 'ALL'
        || this.selectedDoctorId() !== 'ALL';
    });

  ngOnInit(): void {

    this.routeDoctorId =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('doctorId');

    if (this.routeDoctorId) {

      this.selectedDoctorId.set(
        this.routeDoctorId
      );

      this.expandedDoctorIds.set(
        new Set([
          this.routeDoctorId
        ])
      );

      this.availabilityState
        .loadMyDoctorAvailabilities(
          this.routeDoctorId
        );

      return;
    }

    this.loadAvailabilities();
  }

  loadAvailabilities(): void {

    this.routeDoctorId = null;

    this.selectedDoctorId.set('ALL');

    this.expandedDoctorIds.set(
      new Set<string>()
    );

    this.router.navigate(
      [],
      {
        relativeTo:
          this.activatedRoute,

        queryParams: {
          doctorId: null
        },

        queryParamsHandling:
          'merge',

        replaceUrl: true
      }
    );

    this.availabilityState
      .loadMyAvailabilities();
  }

  createAvailability(): void {

    const doctorId =
      this.selectedDoctorId();

    if (doctorId === 'ALL') {

      this.router.navigate([
        '/provider/availability/new'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/provider/availability/new'
      ],
      {
        queryParams: {
          doctorId
        }
      }
    );
  }

  createAvailabilityForDoctor(
    doctorId: string
  ): void {

    this.router.navigate(
      [
        '/provider/availability/new'
      ],
      {
        queryParams: {
          doctorId
        }
      }
    );
  }

  editAvailability(
    availability: DoctorAvailability
  ): void {

    this.router.navigate([
      '/provider/availability',
      availability.id,
      'edit'
    ]);
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(value);
  }

  updateStatus(
    status: AvailabilityStatusFilter
  ): void {

    this.selectedStatus.set(status);
  }

  updateDay(
    day: DayFilter
  ): void {

    this.selectedDay.set(day);
  }

  updateDoctor(
    doctorId: string
  ): void {

    this.selectedDoctorId.set(
      doctorId
    );

    if (doctorId !== 'ALL') {

      this.expandedDoctorIds.update(
        currentIds => {

          const updatedIds =
            new Set(currentIds);

          updatedIds.add(doctorId);

          return updatedIds;
        }
      );
    }
  }

  clearFilters(): void {

    this.searchText.set('');
    this.selectedStatus.set('ALL');
    this.selectedDay.set('ALL');

    if (this.routeDoctorId) {

      this.selectedDoctorId.set(
        this.routeDoctorId
      );

      return;
    }

    this.selectedDoctorId.set('ALL');
  }

  toggleDoctorGroup(
    doctorId: string
  ): void {

    this.expandedDoctorIds.update(
      currentIds => {

        const updatedIds =
          new Set(currentIds);

        if (updatedIds.has(doctorId)) {
          updatedIds.delete(doctorId);
        } else {
          updatedIds.add(doctorId);
        }

        return updatedIds;
      }
    );
  }

  isDoctorExpanded(
    doctorId: string
  ): boolean {

    return this.expandedDoctorIds()
      .has(doctorId);
  }

  expandAll(): void {

    const doctorIds =
      this.doctorGroups()
        .map(group => {
          return group.doctorId;
        });

    this.expandedDoctorIds.set(
      new Set(doctorIds)
    );
  }

  collapseAll(): void {

    this.expandedDoctorIds.set(
      new Set<string>()
    );
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
        .substring(0, 2)
        .toUpperCase();
    }

    return (
      nameParts[0].charAt(0)
      + nameParts[
        nameParts.length - 1
      ].charAt(0)
    ).toUpperCase();
  }

  getDayLabel(
    dayOfWeek: DayOfWeek
  ): string {

    const dayOption =
      this.dayOptions.find(option => {
        return option.value === dayOfWeek;
      });

    return dayOption?.label
      ?? dayOfWeek;
  }

  getTimeLabel(
    timeValue: string
  ): string {

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
    ) {
      return timeValue;
    }

    const period =
      hours >= 12
        ? 'PM'
        : 'AM';

    const displayHour =
      hours % 12 === 0
        ? 12
        : hours % 12;

    const displayMinutes =
      String(minutes).padStart(
        2,
        '0'
      );

    return (
      displayHour
      + ':'
      + displayMinutes
      + ' '
      + period
    );
  }

  getTimeRange(
    availability: DoctorAvailability
  ): string {

    return (
      this.getTimeLabel(
        availability.startTime
      )
      + ' - '
      + this.getTimeLabel(
        availability.endTime
      )
    );
  }

  activateAvailability(
    availability: DoctorAvailability
  ): void {

    this.updateAvailabilityStatus(
      availability,
      'ACTIVE'
    );
  }

  deactivateAvailability(
    availability: DoctorAvailability
  ): void {

    this.updateAvailabilityStatus(
      availability,
      'INACTIVE'
    );
  }

  deleteAvailability(
    availability: DoctorAvailability
  ): void {

    if (
      this.availabilityState.deleting()
    ) {
      return;
    }

    const confirmed =
      window.confirm(
        'Delete the '
        + this.getDayLabel(
          availability.dayOfWeek
        )
        + ' schedule for '
        + availability.doctorName
        + '?'
      );

    if (!confirmed) {
      return;
    }

    this.availabilityState
      .deleteMyAvailability(
        availability.id,
        () => {

          this.notificationService.showSuccess(
            'Availability deleted successfully.'
          );
        },
        message => {

          this.notificationService.showError(
            message
          );
        }
      );
  }

  private updateAvailabilityStatus(
    availability: DoctorAvailability,
    status: AvailabilityStatus
  ): void {

    if (
      availability.status === status
      || this.availabilityState
        .statusUpdating()
    ) {
      return;
    }

    this.availabilityState
      .updateMyAvailabilityStatus(
        availability.id,
        status,
        updatedAvailability => {

          if (
            updatedAvailability.status
            === 'ACTIVE'
          ) {

            this.notificationService.showSuccess(
              'Availability activated successfully.'
            );

            return;
          }

          this.notificationService.showSuccess(
            'Availability made inactive successfully.'
          );
        },
        message => {

          this.notificationService.showError(
            message
          );
        }
      );
  }
}
