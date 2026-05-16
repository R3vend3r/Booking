import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const requiredRole = route.data?.['role'];

  if (requiredRole && !authService.hasRole(requiredRole)) {
    router.navigate(['/']);
    return false;
  }

  return true;
};
