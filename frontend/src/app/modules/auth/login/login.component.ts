import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, NgIf],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  username = '';
  password = '';
  lembrar = false;
  mostrarSenha = false;

  carregando = false;
  erroLogin = false;

  constructor(private router: Router) {}

  entrar(): void {
    this.erroLogin = false;
    this.carregando = true;

    setTimeout(() => {
      this.carregando = false;

      if (this.username && this.password) {
        this.router.navigate(['/dashboard']);
        return;
      }

      this.erroLogin = true;
    }, 800);
  }
}