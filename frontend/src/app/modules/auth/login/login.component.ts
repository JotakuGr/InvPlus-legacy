import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/auth/auth.service';
import { EmpresaDisponivel } from '../../../shared/models/auth-response.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, NgIf, NgFor],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  username = '';
  password = '';
  lembrar = false;
  mostrarSenha = false;
  carregando = false;
  mensagemErro = '';
  empresas: EmpresaDisponivel[] = [];
  empresaId = '';
  filialId = '';
  private selectionToken = '';

  get selecionandoContexto(): boolean { return this.empresas.length > 0; }
  get filiais() { return this.empresas.find((empresa) => empresa.empresaId === this.empresaId)?.filiais ?? []; }

  entrar(): void {
    if (!this.username || !this.password) {
      this.mensagemErro = 'Informe o e-mail e a senha.';
      return;
    }
    this.mensagemErro = '';
    this.carregando = true;
    this.auth.login({ email: this.username.trim(), senha: this.password }, this.lembrar).subscribe({
      next: (response) => {
        this.carregando = false;
        if (response.status === 'SELECAO_EMPRESA_NECESSARIA' && response.selectionToken) {
          this.selectionToken = response.selectionToken;
          this.empresas = response.empresas;
          const principal = this.empresas.find((empresa) => empresa.principal) ?? this.empresas[0];
          this.empresaId = principal?.empresaId ?? '';
          this.atualizarFilialPadrao();
          return;
        }
        void this.router.navigate(['/dashboard']);
      },
      error: (error: HttpErrorResponse) => {
        this.carregando = false;
        this.mensagemErro = error.status === 0
          ? 'Não foi possível conectar ao servidor.'
          : 'E-mail ou senha inválidos.';
      }
    });
  }

  selecionarContexto(): void {
    if (!this.empresaId) return;
    this.carregando = true;
    this.mensagemErro = '';
    this.auth.selecionarContexto(this.selectionToken, this.empresaId, this.filialId || null).subscribe({
      next: () => void this.router.navigate(['/dashboard']),
      error: () => {
        this.carregando = false;
        this.mensagemErro = 'Não foi possível ativar a empresa selecionada.';
      }
    });
  }

  alterarEmpresa(): void { this.atualizarFilialPadrao(); }

  private atualizarFilialPadrao(): void {
    const principal = this.filiais.find((filial) => filial.principal);
    this.filialId = principal?.filialId ?? '';
  }
}
