import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <main class="dashboard-shell">
      <header><div><strong>invPlus</strong><span>{{ auth.contexto()?.empresaNome }}</span></div><button type="button" (click)="sair()">Sair</button></header>
      <section><p>Visão geral</p><h1>Operação pronta</h1><div class="summary"><span>Empresa ativa</span><strong>{{ auth.contexto()?.empresaNome }}</strong><small>{{ auth.contexto()?.filialNome || 'Todas as filiais permitidas' }}</small></div></section>
    </main>
  `,
  styles: [`
    :host{display:block;min-height:100vh;background:#f4f6f8;color:#17202a;font-family:Inter,Arial,sans-serif}.dashboard-shell{min-height:100vh}header{height:64px;padding:0 28px;background:#fff;border-bottom:1px solid #dde2e7;display:flex;align-items:center;justify-content:space-between}header div{display:flex;gap:18px;align-items:baseline}header span{color:#66717d;font-size:14px}button{border:1px solid #c9d0d7;background:#fff;padding:8px 14px;border-radius:6px;cursor:pointer}section{max-width:1100px;margin:0 auto;padding:42px 24px}section>p{color:#68737e;margin:0 0 8px}h1{font-size:30px;margin:0 0 28px;letter-spacing:0}.summary{width:min(420px,100%);background:#fff;border:1px solid #dde2e7;border-radius:8px;padding:20px;display:grid;gap:6px}.summary span,.summary small{color:#68737e}.summary strong{font-size:20px}`]
})
export class DashboardComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  sair(): void { this.auth.logout(); void this.router.navigate(['/login']); }
}
