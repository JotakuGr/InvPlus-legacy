import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest } from '../../shared/models/login-request.model';
import {
  AuthResponse,
  ContextoAtual,
  EmpresaDisponivel,
  TokenContextoResponse,
  UsuarioAutenticado,
} from '../../shared/models/auth-response.model';
import { SessionStorageService } from './session-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly storage = inject(SessionStorageService);
  private readonly api = `${environment.apiUrl}/auth`;
  private persistir = false;

  readonly contexto = signal<ContextoAtual | null>(this.readJson('contexto'));
  readonly usuario = signal<UsuarioAutenticado | null>(this.readJson('usuario'));
  readonly autenticado = computed(() => !!this.accessToken() && !!this.contexto());

  login(request: LoginRequest, lembrar: boolean): Observable<AuthResponse> {
    this.persistir = lembrar;
    return this.http.post<AuthResponse>(`${this.api}/login`, request).pipe(
      tap((response) => {
        if (response.status === 'AUTENTICADO' && response.accessToken && response.refreshToken) {
          this.saveSession(response.accessToken, response.refreshToken, response.contexto, response.usuario);
        }
      }),
    );
  }

  selecionarContexto(selectionToken: string, empresaId: string, filialId: string | null): Observable<TokenContextoResponse> {
    const headers = new HttpHeaders({ Authorization: `Bearer ${selectionToken}` });
    return this.http.post<TokenContextoResponse>(`${this.api}/contexto/selecionar`,
      { empresaId, filialId }, { headers }).pipe(tap((response) => this.saveContextResponse(response)));
  }

  trocarContexto(empresaId: string, filialId: string | null): Observable<TokenContextoResponse> {
    return this.http.post<TokenContextoResponse>(`${this.api}/contexto/trocar`, { empresaId, filialId })
      .pipe(tap((response) => this.saveContextResponse(response)));
  }

  empresas(): Observable<EmpresaDisponivel[]> {
    return this.http.get<EmpresaDisponivel[]>(`${this.api}/contexto/empresas`);
  }

  refresh(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/refresh`, { refreshToken: this.refreshToken() }).pipe(
      tap((response) => {
        if (response.accessToken && response.refreshToken) {
          this.saveSession(response.accessToken, response.refreshToken, response.contexto, response.usuario);
        }
      }),
    );
  }

  logout(): void {
    const refreshToken = this.refreshToken();
    if (refreshToken) this.http.post(`${this.api}/logout`, { refreshToken }).subscribe({ error: () => undefined });
    this.storage.clearAuth();
    this.contexto.set(null);
    this.usuario.set(null);
  }

  accessToken(): string | null { return this.storage.get('accessToken'); }
  refreshToken(): string | null { return this.storage.get('refreshToken'); }

  private saveContextResponse(response: TokenContextoResponse): void {
    this.saveSession(response.accessToken, response.refreshToken, response.contexto, this.usuario());
  }

  private saveSession(accessToken: string, refreshToken: string, contexto: ContextoAtual | null,
      usuario: UsuarioAutenticado | null): void {
    this.storage.set('accessToken', accessToken, this.persistir);
    this.storage.set('refreshToken', refreshToken, this.persistir);
    if (contexto) this.storage.set('contexto', JSON.stringify(contexto), this.persistir);
    if (usuario) this.storage.set('usuario', JSON.stringify(usuario), this.persistir);
    this.contexto.set(contexto);
    this.usuario.set(usuario);
  }

  private readJson<T>(key: string): T | null {
    const value = this.storage.get(key);
    if (!value) return null;
    try { return JSON.parse(value) as T; } catch { return null; }
  }
}
