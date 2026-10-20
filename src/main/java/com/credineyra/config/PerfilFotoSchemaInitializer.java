package com.credineyra.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Mantiene compatible la base existente con la foto de perfil. El proyecto no
 * usa un ejecutor de migraciones, por eso la corrección se aplica al arrancar.
 */
@Component
public class PerfilFotoSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public PerfilFotoSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE public.usuario "
                + "ADD COLUMN IF NOT EXISTS foto_perfil_ruta VARCHAR(500)");
        jdbcTemplate.execute("ALTER TABLE public.documento "
                + "DROP CONSTRAINT IF EXISTS documento_tipo_documento_check");
        jdbcTemplate.execute("ALTER TABLE public.documento "
                + "ADD CONSTRAINT documento_tipo_documento_check "
                + "CHECK (tipo_documento IN ("
                + "'CONTRATO_GENERADO','CONTRATO_FIRMADO','CONSTANCIA_PAGO',"
                + "'COMPROBANTE_PAGO','DNI','FOTO_PERFIL',"
                + "'FOTO_GARANTIA','DOCUMENTO_GARANTIA'))");
    }
}
