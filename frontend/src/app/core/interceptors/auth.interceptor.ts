import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.accessToken();
  const authenticatedRequest = token && !request.headers.has('Authorization')
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authenticatedRequest).pipe(catchError((error: HttpErrorResponse) => {
    const isAuthRoute = request.url.includes('/auth/login') || request.url.includes('/auth/refresh')
      || request.url.includes('/auth/contexto/selecionar');
    if (error.status !== 401 || isAuthRoute || !auth.refreshToken()) return throwError(() => error);
    return auth.refresh().pipe(switchMap(() => {
      const refreshed = auth.accessToken();
      return next(refreshed ? request.clone({ setHeaders: { Authorization: `Bearer ${refreshed}` } }) : request);
    }));
  }));
};
