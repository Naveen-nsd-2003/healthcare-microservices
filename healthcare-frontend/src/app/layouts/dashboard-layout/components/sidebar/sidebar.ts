import {
  Component,
  EventEmitter,
  Input,
  Output,
  computed,
  inject
} from '@angular/core';

import {
  RouterLink,
  RouterLinkActive
} from '@angular/router';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  AuthService
} from '../../../../core/auth/auth.service';

import {
  NavigationService
} from '../../../../core/services/navigation.service';

@Component({
  selector: 'app-sidebar',
  imports: [
    RouterLink,
    RouterLinkActive,
    MatIconModule
  ],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class Sidebar {

  private readonly authService =
    inject(AuthService);

  private readonly navigationService =
    inject(NavigationService);

  @Input()
  collapsed = false;

  @Output()
  navigationSelected =
    new EventEmitter<void>();

  readonly navigationItems =
    computed(
      () =>
        this.navigationService
          .getNavigationItemsForRole(
            this.authService.currentRole()
          )
    );

  selectNavigation(): void {

    this.navigationSelected.emit();
  }
}