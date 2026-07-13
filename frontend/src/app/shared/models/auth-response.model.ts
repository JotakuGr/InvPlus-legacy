export interface UsuarioAutenticado {
  id: string;
  nome: string;
  email: string;
  perfis: string[];
  permissoes: string[];
}

export interface FilialDisponivel {
  filialId: string;
  nome: string;
  principal: boolean;
}

export interface EmpresaDisponivel {
  empresaId: string;
  nomeFantasia: string;
  razaoSocial: string;
  principal: boolean;
  acessoTodasFiliais: boolean;
  filiais: FilialDisponivel[];
}

export interface ContextoAtual {
  usuarioId: string;
  empresaId: string;
  empresaNome: string;
  filialId: string | null;
  filialNome: string | null;
  permissoes: string[];
}

export interface AuthResponse {
  tokenType: string;
  accessToken: string | null;
  refreshToken: string | null;
  expiresAt: string | null;
  usuario: UsuarioAutenticado;
  status: 'AUTENTICADO' | 'SELECAO_EMPRESA_NECESSARIA';
  selectionToken: string | null;
  contexto: ContextoAtual | null;
  empresas: EmpresaDisponivel[];
}

export interface TokenContextoResponse {
  accessToken: string;
  refreshToken: string;
  contexto: ContextoAtual;
}
