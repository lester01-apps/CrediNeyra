-- Migración idempotente: requerida antes de que Hibernate valide Usuario.
ALTER TABLE public.usuario
    ADD COLUMN IF NOT EXISTS foto_perfil_ruta VARCHAR(500);
