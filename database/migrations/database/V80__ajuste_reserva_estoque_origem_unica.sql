-- Ajusta a chave de duplicidade das reservas ativas.
-- Um pedido de venda pode gerar mais de uma reserva quando os itens usam produtos,
-- depositos ou localizacoes diferentes; a duplicidade real deve ser por estoque.
DROP INDEX IF EXISTS uk_reserva_estoque_origem_ativa;

CREATE UNIQUE INDEX IF NOT EXISTS uk_reserva_estoque_estoque_origem_ativa
    ON reserva_estoque (estoque_id, origem, origem_id)
    WHERE origem_id IS NOT NULL
      AND status = 'ATIVA'
      AND ativo = TRUE
      AND deleted_at IS NULL;

COMMENT ON INDEX uk_reserva_estoque_estoque_origem_ativa IS
    'Impede reserva ativa duplicada para o mesmo estoque e mesma origem/origem_id';
