-- V1: inicialização do schema (tabelas, constraints, índices)

-- Tabela users
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela subscriptions
CREATE TABLE IF NOT EXISTS public.subscriptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.users(id),
    plano VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    data_inicio DATE NOT NULL,
    data_expiracao DATE NOT NULL
);

-- Índices subscriptions
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_status ON public.subscriptions (user_id, status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_expiracao ON public.subscriptions (user_id, data_expiracao DESC);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status_expiracao ON public.subscriptions (status, data_expiracao);

-- Tabela payments
CREATE TABLE IF NOT EXISTS public.payments (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL UNIQUE REFERENCES public.subscriptions(id),
    amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    attempt_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL
);

-- Constraints e checks
ALTER TABLE public.subscriptions
    DROP CONSTRAINT IF EXISTS subscriptions_status_check;

ALTER TABLE public.subscriptions
    ADD CONSTRAINT subscriptions_status_check CHECK (
        status IN (
            'ATIVA',
            'CANCELADA_PENDENTE',
            'CANCELADA',
            'EXPIRADA',
            'SUSPENSA',
            'TRIAL',
            'PRE_PROCESSADA'
        )
    );

-- Constraints únicas condicionais (parciais)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE schemaname = 'public' AND indexname = 'uk_user_active_subscription'
    ) THEN
        EXECUTE 'CREATE UNIQUE INDEX uk_user_active_subscription ON public.subscriptions (user_id) WHERE status = ''ATIVA''';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE schemaname = 'public' AND indexname = 'uk_user_preprocessed_subscription'
    ) THEN
        EXECUTE 'CREATE UNIQUE INDEX uk_user_preprocessed_subscription ON public.subscriptions (user_id) WHERE status = ''PRE_PROCESSADA''';
    END IF;
END $$;
