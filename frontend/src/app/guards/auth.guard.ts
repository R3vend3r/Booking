import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  const requiredRole = route.data?.['role'];
  const currentUser = authService.currentUser;

  if (requiredRole && currentUser?.role !== requiredRole) {
    router.navigate(['/']);
    return false;
  }

  return true;
};
