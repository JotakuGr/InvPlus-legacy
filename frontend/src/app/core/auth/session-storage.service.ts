import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class SessionStorageService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly prefix = 'invplus.';

  get(key: string): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return localStorage.getItem(this.prefix + key) ?? sessionStorage.getItem(this.prefix + key);
  }

  set(key: string, value: string, persistent = false): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.remove(key);
    (persistent ? localStorage : sessionStorage).setItem(this.prefix + key, value);
  }

  remove(key: string): void {
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.removeItem(this.prefix + key);
    sessionStorage.removeItem(this.prefix + key);
  }

  clearAuth(): void {
    ['accessToken', 'refreshToken', 'contexto', 'usuario'].forEach((key) => this.remove(key));
  }
}
