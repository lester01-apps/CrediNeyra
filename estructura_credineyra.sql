


SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


CREATE SCHEMA IF NOT EXISTS "public";


ALTER SCHEMA "public" OWNER TO "pg_database_owner";


COMMENT ON SCHEMA "public" IS 'standard public schema';



CREATE OR REPLACE FUNCTION "public"."redondear_a_sol"("monto" numeric) RETURNS numeric
    LANGUAGE "sql" IMMUTABLE
    AS $$
    SELECT ROUND(monto, 0);
$$;


ALTER FUNCTION "public"."redondear_a_sol"("monto" numeric) OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."trg_actualizar_suscripcion_push"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN

    NEW.fecha_actualizacion := NOW();

    RETURN NEW;
END;
$$;


ALTER FUNCTION "public"."trg_actualizar_suscripcion_push"() OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."trg_redondear_credito"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN

    -- ==========================
    -- CAPITAL
    -- ==========================

    NEW.capital_original :=
        redondear_a_sol(NEW.capital_original);

    NEW.capital_pagado :=
        redondear_a_sol(
            COALESCE(NEW.capital_pagado, 0)
        );

    NEW.capital_pendiente :=
        NEW.capital_original
        - NEW.capital_pagado;


    -- ==========================
    -- INTERES
    -- ==========================

    NEW.interes_generado :=
        redondear_a_sol(NEW.interes_generado);

    NEW.interes_pagado :=
        redondear_a_sol(
            COALESCE(NEW.interes_pagado, 0)
        );

    NEW.interes_pendiente :=
        NEW.interes_generado
        - NEW.interes_pagado;

    RETURN NEW;

END;
$$;


ALTER FUNCTION "public"."trg_redondear_credito"() OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."trg_redondear_cuota"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN

    NEW.capital_original :=
        redondear_a_sol(NEW.capital_original);

    NEW.interes_original :=
        redondear_a_sol(NEW.interes_original);

    NEW.capital_pagado :=
        redondear_a_sol(
            COALESCE(NEW.capital_pagado, 0)
        );

    NEW.interes_pagado :=
        redondear_a_sol(
            COALESCE(NEW.interes_pagado, 0)
        );

    NEW.capital_pendiente :=
        NEW.capital_original
        - NEW.capital_pagado;

    NEW.interes_pendiente :=
        NEW.interes_original
        - NEW.interes_pagado;

    NEW.capital_regularizado_asignado :=
        redondear_a_sol(
            COALESCE(
                NEW.capital_regularizado_asignado,
                0
            )
        );

    NEW.capital_regularizado_pagado :=
        redondear_a_sol(
            COALESCE(
                NEW.capital_regularizado_pagado,
                0
            )
        );

    NEW.capital_regularizado_pendiente :=
        NEW.capital_regularizado_asignado
        - NEW.capital_regularizado_pagado;

    RETURN NEW;
END;
$$;


ALTER FUNCTION "public"."trg_redondear_cuota"() OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."trg_redondear_garantia"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN

    NEW.valor_estimado :=
        redondear_a_sol(NEW.valor_estimado);

    RETURN NEW;
END;
$$;


ALTER FUNCTION "public"."trg_redondear_garantia"() OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."trg_redondear_pago"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN

    NEW.monto :=
        redondear_a_sol(NEW.monto);

    RETURN NEW;
END;
$$;


ALTER FUNCTION "public"."trg_redondear_pago"() OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."trg_redondear_pago_detalle"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN

    NEW.monto_capital :=
        redondear_a_sol(
            COALESCE(NEW.monto_capital, 0)
        );

    NEW.monto_interes :=
        redondear_a_sol(
            COALESCE(NEW.monto_interes, 0)
        );

    NEW.monto_capital_regularizado :=
        redondear_a_sol(
            COALESCE(
                NEW.monto_capital_regularizado,
                0
            )
        );

    NEW.monto_total :=
        NEW.monto_capital
        + NEW.monto_interes
        + NEW.monto_capital_regularizado;

    RETURN NEW;
END;
$$;


ALTER FUNCTION "public"."trg_redondear_pago_detalle"() OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."trg_redondear_plan_detalle"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN

    NEW.monto_capital_regularizado :=
        redondear_a_sol(
            NEW.monto_capital_regularizado
        );

    RETURN NEW;
END;
$$;


ALTER FUNCTION "public"."trg_redondear_plan_detalle"() OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."trg_redondear_plan_regularizacion"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN

    NEW.capital_pendiente_origen :=
        redondear_a_sol(
            NEW.capital_pendiente_origen
        );

    RETURN NEW;
END;
$$;


ALTER FUNCTION "public"."trg_redondear_plan_regularizacion"() OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."trg_redondear_traspaso_cliente"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS $$
BEGIN

    NEW.capital_pendiente_inicial :=
        redondear_a_sol(
            COALESCE(
                NEW.capital_pendiente_inicial,
                0
            )
        );

    NEW.interes_pendiente_inicial :=
        redondear_a_sol(
            COALESCE(
                NEW.interes_pendiente_inicial,
                0
            )
        );

    RETURN NEW;
END;
$$;


ALTER FUNCTION "public"."trg_redondear_traspaso_cliente"() OWNER TO "postgres";

SET default_tablespace = '';

SET default_table_access_method = "heap";


CREATE TABLE IF NOT EXISTS "public"."acuerdo" (
    "id_acuerdo" bigint NOT NULL,
    "id_credito" bigint NOT NULL,
    "id_cuota" bigint,
    "id_usuario_creador" bigint NOT NULL,
    "tipo_acuerdo" character varying(50) NOT NULL,
    "fecha_original" "date",
    "fecha_acordada" "date",
    "dias_aplazamiento" integer,
    "motivo" character varying(200) NOT NULL,
    "observaciones" character varying(200),
    "fecha_creacion" timestamp with time zone DEFAULT "now"() NOT NULL,
    "estado" character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    CONSTRAINT "acuerdo_dias_aplazamiento_check" CHECK ((("dias_aplazamiento" IS NULL) OR ("dias_aplazamiento" > 0))),
    CONSTRAINT "acuerdo_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['ACTIVO'::character varying, 'CUMPLIDO'::character varying, 'ANULADO'::character varying])::"text"[]))),
    CONSTRAINT "acuerdo_tipo_acuerdo_check" CHECK ((("tipo_acuerdo")::"text" = ANY ((ARRAY['APLAZAMIENTO'::character varying, 'RESTRICCION'::character varying, 'DESBLOQUEO'::character varying])::"text"[])))
);


ALTER TABLE "public"."acuerdo" OWNER TO "postgres";


ALTER TABLE "public"."acuerdo" ALTER COLUMN "id_acuerdo" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."acuerdo_id_acuerdo_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."auditoria" (
    "id_auditoria" bigint NOT NULL,
    "id_usuario" bigint,
    "accion" character varying(100) NOT NULL,
    "entidad" character varying(50) NOT NULL,
    "identificador" character varying(50),
    "motivo" character varying(500),
    "datos_anteriores" "jsonb",
    "datos_nuevos" "jsonb",
    "direccion_ip" character varying(50),
    "fecha_hora" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."auditoria" OWNER TO "postgres";


ALTER TABLE "public"."auditoria" ALTER COLUMN "id_auditoria" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."auditoria_id_auditoria_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."cliente" (
    "id_cliente" bigint NOT NULL,
    "id_persona" bigint NOT NULL,
    "codigo_cliente" character varying(20) NOT NULL,
    "tipo_origen" character varying(20) DEFAULT 'NUEVO'::character varying NOT NULL,
    "fecha_registro" timestamp with time zone DEFAULT "now"() NOT NULL,
    "estado" character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    "observaciones" character varying(200),
    CONSTRAINT "cliente_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying])::"text"[]))),
    CONSTRAINT "cliente_tipo_origen_check" CHECK ((("tipo_origen")::"text" = ANY ((ARRAY['NUEVO'::character varying, 'TRASPASO'::character varying])::"text"[])))
);


ALTER TABLE "public"."cliente" OWNER TO "postgres";


ALTER TABLE "public"."cliente" ALTER COLUMN "id_cliente" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."cliente_id_cliente_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."condicion_credito" (
    "id_condicion" bigint NOT NULL,
    "id_tasa_origen" bigint,
    "tipo_calculo" character varying(30) NOT NULL,
    "tasa_interes" numeric(5,2) NOT NULL,
    "periodicidad_tasa" character varying(30) NOT NULL,
    "frecuencia_pago" character varying(30) NOT NULL,
    "plazo_meses" integer NOT NULL,
    "numero_cuotas" integer NOT NULL,
    "fecha_inicio" "date" NOT NULL,
    "fecha_primer_pago" "date" NOT NULL,
    "permite_pago_solo_interes" boolean DEFAULT false NOT NULL,
    CONSTRAINT "chk_fecha_primer_pago" CHECK (("fecha_primer_pago" >= "fecha_inicio")),
    CONSTRAINT "chk_interes_economico" CHECK (((("tipo_calculo")::"text" <> 'INTERES_ECONOMICO'::"text") OR (("periodicidad_tasa")::"text" = 'MENSUAL'::"text"))),
    CONSTRAINT "chk_numero_cuotas_frecuencia" CHECK (("numero_cuotas" = ("plazo_meses" *
CASE "frecuencia_pago"
    WHEN 'MENSUAL'::"text" THEN 1
    WHEN 'QUINCENAL'::"text" THEN 2
    WHEN 'SEMANAL'::"text" THEN 4
    WHEN 'DIARIA'::"text" THEN 30
    ELSE NULL::integer
END))),
    CONSTRAINT "chk_periodicidad_interes_credito" CHECK ((("periodicidad_tasa")::"text" = ANY ((ARRAY['SEMANAL'::character varying, 'QUINCENAL'::character varying, 'MENSUAL'::character varying])::"text"[]))),
    CONSTRAINT "condicion_credito_frecuencia_pago_check" CHECK ((("frecuencia_pago")::"text" = ANY ((ARRAY['DIARIA'::character varying, 'SEMANAL'::character varying, 'QUINCENAL'::character varying, 'MENSUAL'::character varying])::"text"[]))),
    CONSTRAINT "condicion_credito_numero_cuotas_check" CHECK (("numero_cuotas" > 0)),
    CONSTRAINT "condicion_credito_periodicidad_tasa_check" CHECK ((("periodicidad_tasa")::"text" = ANY ((ARRAY['DIARIA'::character varying, 'SEMANAL'::character varying, 'QUINCENAL'::character varying, 'MENSUAL'::character varying])::"text"[]))),
    CONSTRAINT "condicion_credito_plazo_meses_check" CHECK (("plazo_meses" > 0)),
    CONSTRAINT "condicion_credito_tasa_interes_check" CHECK (("tasa_interes" >= (0)::numeric)),
    CONSTRAINT "condicion_credito_tipo_calculo_check" CHECK ((("tipo_calculo")::"text" = ANY ((ARRAY['NORMAL'::character varying, 'INTERES_ECONOMICO'::character varying])::"text"[])))
);


ALTER TABLE "public"."condicion_credito" OWNER TO "postgres";


ALTER TABLE "public"."condicion_credito" ALTER COLUMN "id_condicion" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."condicion_credito_id_condicion_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."credito" (
    "id_credito" bigint NOT NULL,
    "id_cliente" bigint NOT NULL,
    "id_condicion" bigint NOT NULL,
    "codigo_credito" character varying(20) NOT NULL,
    "fecha_solicitud" timestamp with time zone DEFAULT "now"() NOT NULL,
    "capital_original" numeric(12,2) NOT NULL,
    "capital_pagado" numeric(12,2) DEFAULT 0 NOT NULL,
    "capital_pendiente" numeric(12,2) NOT NULL,
    "interes_generado" numeric(12,2) NOT NULL,
    "interes_pagado" numeric(12,2) DEFAULT 0 NOT NULL,
    "interes_pendiente" numeric(12,2) NOT NULL,
    "estado" character varying(30) DEFAULT 'ACTIVO'::character varying NOT NULL,
    "observaciones" character varying(200),
    "version" integer DEFAULT 0 NOT NULL,
    CONSTRAINT "chk_credito_capital_cuadre" CHECK ((("capital_pagado" + "capital_pendiente") = "capital_original")),
    CONSTRAINT "chk_credito_interes_cuadre" CHECK ((("interes_pagado" + "interes_pendiente") = "interes_generado")),
    CONSTRAINT "credito_capital_original_check" CHECK (("capital_original" > (0)::numeric)),
    CONSTRAINT "credito_capital_pagado_check" CHECK (("capital_pagado" >= (0)::numeric)),
    CONSTRAINT "credito_capital_pendiente_check" CHECK (("capital_pendiente" >= (0)::numeric)),
    CONSTRAINT "credito_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['ACTIVO'::character varying, 'AL_DIA'::character varying, 'EN_MORA'::character varying, 'ATRASO_PROLONGADO'::character varying, 'EN_REGULARIZACION'::character varying, 'FINALIZADO'::character varying])::"text"[]))),
    CONSTRAINT "credito_interes_generado_check" CHECK (("interes_generado" >= (0)::numeric)),
    CONSTRAINT "credito_interes_pagado_check" CHECK (("interes_pagado" >= (0)::numeric)),
    CONSTRAINT "credito_interes_pendiente_check" CHECK (("interes_pendiente" >= (0)::numeric))
);


ALTER TABLE "public"."credito" OWNER TO "postgres";


ALTER TABLE "public"."credito" ALTER COLUMN "id_credito" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."credito_id_credito_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."cuota" (
    "id_cuota" bigint NOT NULL,
    "id_credito" bigint NOT NULL,
    "numero_cuota" integer NOT NULL,
    "fecha_vencimiento" "date" NOT NULL,
    "capital_original" numeric(12,2) NOT NULL,
    "interes_original" numeric(12,2) NOT NULL,
    "capital_pagado" numeric(12,2) DEFAULT 0 NOT NULL,
    "interes_pagado" numeric(12,2) DEFAULT 0 NOT NULL,
    "capital_pendiente" numeric(12,2) NOT NULL,
    "interes_pendiente" numeric(12,2) NOT NULL,
    "capital_regularizado_asignado" numeric(12,2) DEFAULT 0 NOT NULL,
    "capital_regularizado_pagado" numeric(12,2) DEFAULT 0 NOT NULL,
    "capital_regularizado_pendiente" numeric(12,2) DEFAULT 0 NOT NULL,
    "estado" character varying(20) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    "version" integer DEFAULT 0 NOT NULL,
    CONSTRAINT "chk_cuota_capital_cuadre" CHECK ((("capital_pagado" + "capital_pendiente") = "capital_original")),
    CONSTRAINT "chk_cuota_interes_cuadre" CHECK ((("interes_pagado" + "interes_pendiente") = "interes_original")),
    CONSTRAINT "chk_cuota_regularizacion_cuadre" CHECK ((("capital_regularizado_pagado" + "capital_regularizado_pendiente") = "capital_regularizado_asignado")),
    CONSTRAINT "cuota_capital_original_check" CHECK (("capital_original" >= (0)::numeric)),
    CONSTRAINT "cuota_capital_pagado_check" CHECK (("capital_pagado" >= (0)::numeric)),
    CONSTRAINT "cuota_capital_pendiente_check" CHECK (("capital_pendiente" >= (0)::numeric)),
    CONSTRAINT "cuota_capital_regularizado_asignado_check" CHECK (("capital_regularizado_asignado" >= (0)::numeric)),
    CONSTRAINT "cuota_capital_regularizado_pagado_check" CHECK (("capital_regularizado_pagado" >= (0)::numeric)),
    CONSTRAINT "cuota_capital_regularizado_pendiente_check" CHECK (("capital_regularizado_pendiente" >= (0)::numeric)),
    CONSTRAINT "cuota_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['PENDIENTE'::character varying, 'PARCIAL'::character varying, 'PAGADA'::character varying, 'VENCIDA'::character varying])::"text"[]))),
    CONSTRAINT "cuota_interes_original_check" CHECK (("interes_original" >= (0)::numeric)),
    CONSTRAINT "cuota_interes_pagado_check" CHECK (("interes_pagado" >= (0)::numeric)),
    CONSTRAINT "cuota_interes_pendiente_check" CHECK (("interes_pendiente" >= (0)::numeric)),
    CONSTRAINT "cuota_numero_cuota_check" CHECK (("numero_cuota" > 0))
);


ALTER TABLE "public"."cuota" OWNER TO "postgres";


ALTER TABLE "public"."cuota" ALTER COLUMN "id_cuota" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."cuota_id_cuota_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."documento" (
    "id_documento" bigint NOT NULL,
    "id_cliente" bigint NOT NULL,
    "id_credito" bigint,
    "id_pago" bigint,
    "id_garantia" bigint,
    "tipo_documento" character varying(40) NOT NULL,
    "codigo_documento" character varying(50),
    "nombre_archivo" character varying(200) NOT NULL,
    "ruta_archivo" character varying(500) NOT NULL,
    "visible_cliente" boolean DEFAULT false NOT NULL,
    "estado" character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    "fecha_subida" timestamp with time zone DEFAULT "now"() NOT NULL,
    "observaciones" character varying(200),
    CONSTRAINT "chk_constancia_datos" CHECK (((("tipo_documento")::"text" <> 'CONSTANCIA_PAGO'::"text") OR (("id_pago" IS NOT NULL) AND ("codigo_documento" IS NOT NULL)))),
    CONSTRAINT "chk_doc_garantia_credito" CHECK ((("id_garantia" IS NULL) OR ("id_credito" IS NOT NULL))),
    CONSTRAINT "chk_doc_pago_credito" CHECK ((("id_pago" IS NULL) OR ("id_credito" IS NOT NULL))),
    CONSTRAINT "documento_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['ACTIVO'::character varying, 'ANULADO'::character varying])::"text"[]))),
    CONSTRAINT "documento_tipo_documento_check" CHECK ((("tipo_documento")::"text" = ANY ((ARRAY['CONTRATO_GENERADO'::character varying, 'CONTRATO_FIRMADO'::character varying, 'CONSTANCIA_PAGO'::character varying, 'COMPROBANTE_PAGO'::character varying, 'DNI'::character varying, 'FOTO_PERFIL'::character varying, 'FOTO_GARANTIA'::character varying, 'DOCUMENTO_GARANTIA'::character varying])::"text"[])))
);


ALTER TABLE "public"."documento" OWNER TO "postgres";


ALTER TABLE "public"."documento" ALTER COLUMN "id_documento" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."documento_id_documento_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."garantia" (
    "id_garantia" bigint NOT NULL,
    "id_credito" bigint NOT NULL,
    "tipo_garantia" character varying(50) NOT NULL,
    "descripcion" character varying(200),
    "valor_estimado" numeric(12,2) NOT NULL,
    "estado" character varying(20) DEFAULT 'ACTIVA'::character varying NOT NULL,
    "fecha_registro" timestamp with time zone DEFAULT "now"() NOT NULL,
    "observaciones" character varying(200),
    CONSTRAINT "garantia_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['ACTIVA'::character varying, 'EN_REVISION'::character varying, 'LIBERADA'::character varying, 'ANULADA'::character varying])::"text"[]))),
    CONSTRAINT "garantia_valor_estimado_check" CHECK (("valor_estimado" >= (0)::numeric))
);


ALTER TABLE "public"."garantia" OWNER TO "postgres";


ALTER TABLE "public"."garantia" ALTER COLUMN "id_garantia" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."garantia_id_garantia_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."notificacion" (
    "id_notificacion" bigint NOT NULL,
    "id_cliente" bigint NOT NULL,
    "id_credito" bigint,
    "id_cuota" bigint,
    "creado_por" bigint,
    "tipo" character varying(40) NOT NULL,
    "canal" character varying(20) NOT NULL,
    "titulo" character varying(100) NOT NULL,
    "mensaje" character varying(500) NOT NULL,
    "estado" character varying(20) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    "leida" boolean DEFAULT false NOT NULL,
    "fecha_programada" timestamp with time zone DEFAULT "now"() NOT NULL,
    "fecha_enviada" timestamp with time zone,
    "fecha_leida" timestamp with time zone,
    "clave_evento" character varying(120),
    CONSTRAINT "chk_notificacion_cuota_credito" CHECK ((("id_cuota" IS NULL) OR ("id_credito" IS NOT NULL))),
    CONSTRAINT "chk_notificacion_fecha_leida" CHECK ((("fecha_leida" IS NULL) OR ("leida" = true))),
    CONSTRAINT "notificacion_canal_check" CHECK ((("canal")::"text" = ANY ((ARRAY['WEB'::character varying, 'PUSH'::character varying, 'WHATSAPP'::character varying])::"text"[]))),
    CONSTRAINT "notificacion_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['PENDIENTE'::character varying, 'ENVIADA'::character varying, 'FALLIDA'::character varying, 'CANCELADA'::character varying])::"text"[]))),
    CONSTRAINT "notificacion_tipo_check" CHECK ((("tipo")::"text" = ANY ((ARRAY['VENCIMIENTO_PROXIMO'::character varying, 'VENCE_HOY'::character varying, 'CUOTA_VENCIDA'::character varying, 'RECORDATORIO_PAGO'::character varying, 'MENSAJE_ADMIN'::character varying])::"text"[])))
);


ALTER TABLE "public"."notificacion" OWNER TO "postgres";


ALTER TABLE "public"."notificacion" ALTER COLUMN "id_notificacion" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."notificacion_id_notificacion_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."pago" (
    "id_pago" bigint NOT NULL,
    "id_credito" bigint NOT NULL,
    "registrado_por" bigint NOT NULL,
    "validado_por" bigint,
    "fecha_pago" timestamp with time zone DEFAULT "now"() NOT NULL,
    "fecha_validacion" timestamp with time zone,
    "monto" numeric(12,2) NOT NULL,
    "metodo_pago" character varying(30) NOT NULL,
    "referencia" character varying(100),
    "idempotency_key" character varying(100) NOT NULL,
    "observaciones" character varying(200),
    "estado" character varying(30) DEFAULT 'PENDIENTE_VALIDACION'::character varying NOT NULL,
    "version" integer DEFAULT 0 NOT NULL,
    CONSTRAINT "pago_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['PENDIENTE_VALIDACION'::character varying, 'APROBADO'::character varying, 'RECHAZADO'::character varying])::"text"[]))),
    CONSTRAINT "pago_metodo_pago_check" CHECK ((("metodo_pago")::"text" = ANY ((ARRAY['EFECTIVO'::character varying, 'TRANSFERENCIA'::character varying, 'YAPE'::character varying])::"text"[]))),
    CONSTRAINT "pago_monto_check" CHECK (("monto" > (0)::numeric))
);


ALTER TABLE "public"."pago" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."pago_detalle" (
    "id_detalle" bigint NOT NULL,
    "id_pago" bigint NOT NULL,
    "id_cuota" bigint NOT NULL,
    "id_credito" bigint NOT NULL,
    "monto_capital" numeric(12,2) DEFAULT 0 NOT NULL,
    "monto_interes" numeric(12,2) DEFAULT 0 NOT NULL,
    "monto_capital_regularizado" numeric(12,2) DEFAULT 0 NOT NULL,
    "monto_total" numeric(12,2) NOT NULL,
    CONSTRAINT "chk_suma_montos_detalle" CHECK (("monto_total" = (("monto_capital" + "monto_interes") + "monto_capital_regularizado"))),
    CONSTRAINT "pago_detalle_monto_capital_check" CHECK (("monto_capital" >= (0)::numeric)),
    CONSTRAINT "pago_detalle_monto_capital_regularizado_check" CHECK (("monto_capital_regularizado" >= (0)::numeric)),
    CONSTRAINT "pago_detalle_monto_interes_check" CHECK (("monto_interes" >= (0)::numeric)),
    CONSTRAINT "pago_detalle_monto_total_check" CHECK (("monto_total" > (0)::numeric))
);


ALTER TABLE "public"."pago_detalle" OWNER TO "postgres";


ALTER TABLE "public"."pago_detalle" ALTER COLUMN "id_detalle" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."pago_detalle_id_detalle_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



ALTER TABLE "public"."pago" ALTER COLUMN "id_pago" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."pago_id_pago_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."permiso" (
    "id_permiso" bigint NOT NULL,
    "nombre" character varying(50) NOT NULL,
    "modulo" character varying(50),
    "descripcion" character varying(100)
);


ALTER TABLE "public"."permiso" OWNER TO "postgres";


ALTER TABLE "public"."permiso" ALTER COLUMN "id_permiso" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."permiso_id_permiso_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."persona" (
    "id_persona" bigint NOT NULL,
    "dni" character varying(8) NOT NULL,
    "nombres" character varying(100) NOT NULL,
    "apellidos" character varying(100) NOT NULL,
    "telefono" character varying(20),
    "email" character varying(100),
    "direccion" character varying(200),
    "fecha_registro" timestamp with time zone DEFAULT "now"() NOT NULL,
    "estado" character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    CONSTRAINT "chk_persona_apellidos" CHECK ((("btrim"(("apellidos")::"text") <> ''::"text") AND (("apellidos")::"text" ~ '^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ''-]+$'::"text"))),
    CONSTRAINT "chk_persona_nombres" CHECK ((("btrim"(("nombres")::"text") <> ''::"text") AND (("nombres")::"text" ~ '^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ''-]+$'::"text"))),
    CONSTRAINT "persona_dni_check" CHECK ((("dni")::"text" ~ '^[0-9]{8}$'::"text")),
    CONSTRAINT "persona_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying])::"text"[])))
);


ALTER TABLE "public"."persona" OWNER TO "postgres";


ALTER TABLE "public"."persona" ALTER COLUMN "id_persona" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."persona_id_persona_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."plan_regularizacion" (
    "id_plan" bigint NOT NULL,
    "id_credito" bigint NOT NULL,
    "id_usuario_creador" bigint NOT NULL,
    "capital_pendiente_origen" numeric(12,2) NOT NULL,
    "cantidad_abonos" integer NOT NULL,
    "motivo" character varying(200) NOT NULL,
    "fecha_creacion" timestamp with time zone DEFAULT "now"() NOT NULL,
    "estado" character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    CONSTRAINT "plan_regularizacion_cantidad_abonos_check" CHECK (("cantidad_abonos" > 0)),
    CONSTRAINT "plan_regularizacion_capital_pendiente_origen_check" CHECK (("capital_pendiente_origen" >= (0)::numeric)),
    CONSTRAINT "plan_regularizacion_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['ACTIVO'::character varying, 'COMPLETADO'::character varying, 'MODIFICADO'::character varying, 'CANCELADO'::character varying])::"text"[])))
);


ALTER TABLE "public"."plan_regularizacion" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."plan_regularizacion_detalle" (
    "id_detalle_plan" bigint NOT NULL,
    "id_plan" bigint NOT NULL,
    "id_cuota" bigint NOT NULL,
    "id_credito" bigint NOT NULL,
    "monto_capital_regularizado" numeric(12,2) NOT NULL,
    "estado" character varying(20) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    CONSTRAINT "plan_regularizacion_detalle_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['PENDIENTE'::character varying, 'PAGADO'::character varying])::"text"[]))),
    CONSTRAINT "plan_regularizacion_detalle_monto_capital_regularizado_check" CHECK (("monto_capital_regularizado" > (0)::numeric))
);


ALTER TABLE "public"."plan_regularizacion_detalle" OWNER TO "postgres";


ALTER TABLE "public"."plan_regularizacion_detalle" ALTER COLUMN "id_detalle_plan" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."plan_regularizacion_detalle_id_detalle_plan_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



ALTER TABLE "public"."plan_regularizacion" ALTER COLUMN "id_plan" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."plan_regularizacion_id_plan_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."recuperacion_password" (
    "id_recuperacion" bigint NOT NULL,
    "id_usuario" bigint NOT NULL,
    "token_hash" character varying(255) NOT NULL,
    "expira_en" timestamp with time zone NOT NULL,
    "usado_en" timestamp with time zone,
    "creado_en" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."recuperacion_password" OWNER TO "postgres";


ALTER TABLE "public"."recuperacion_password" ALTER COLUMN "id_recuperacion" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."recuperacion_password_id_recuperacion_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."rol" (
    "id_rol" bigint NOT NULL,
    "nombre" character varying(50) NOT NULL,
    "descripcion" character varying(100),
    "estado" character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    CONSTRAINT "rol_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying])::"text"[])))
);


ALTER TABLE "public"."rol" OWNER TO "postgres";


ALTER TABLE "public"."rol" ALTER COLUMN "id_rol" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."rol_id_rol_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."rol_permiso" (
    "id_rol" bigint NOT NULL,
    "id_permiso" bigint NOT NULL
);


ALTER TABLE "public"."rol_permiso" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."suscripcion_push" (
    "id_suscripcion" bigint NOT NULL,
    "id_usuario" bigint NOT NULL,
    "token_fcm" character varying(500) NOT NULL,
    "dispositivo" character varying(100),
    "navegador" character varying(100),
    "sistema_operativo" character varying(100),
    "activo" boolean DEFAULT true NOT NULL,
    "fecha_registro" timestamp with time zone DEFAULT "now"() NOT NULL,
    "fecha_actualizacion" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."suscripcion_push" OWNER TO "postgres";


ALTER TABLE "public"."suscripcion_push" ALTER COLUMN "id_suscripcion" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."suscripcion_push_id_suscripcion_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."tasa" (
    "id_tasa" bigint NOT NULL,
    "codigo" character varying(30) NOT NULL,
    "version" integer NOT NULL,
    "nombre" character varying(100) NOT NULL,
    "tipo" character varying(20) NOT NULL,
    "valor" numeric(5,2) NOT NULL,
    "periodicidad" character varying(30) NOT NULL,
    "estado" character varying(20) DEFAULT 'ACTIVA'::character varying NOT NULL,
    "vigente_desde" timestamp with time zone DEFAULT "now"() NOT NULL,
    "vigente_hasta" timestamp with time zone,
    "id_usuario_creador" bigint NOT NULL,
    "motivo" character varying(200) NOT NULL,
    CONSTRAINT "chk_tasa_vigencia" CHECK ((("vigente_hasta" IS NULL) OR ("vigente_hasta" >= "vigente_desde"))),
    CONSTRAINT "tasa_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['ACTIVA'::character varying, 'INACTIVA'::character varying])::"text"[]))),
    CONSTRAINT "tasa_periodicidad_check" CHECK ((("periodicidad")::"text" = ANY ((ARRAY['DIARIA'::character varying, 'SEMANAL'::character varying, 'QUINCENAL'::character varying, 'MENSUAL'::character varying])::"text"[]))),
    CONSTRAINT "tasa_tipo_check" CHECK ((("tipo")::"text" = ANY ((ARRAY['ESTANDAR'::character varying, 'PREFERENCIAL'::character varying, 'PERSONALIZADA'::character varying])::"text"[]))),
    CONSTRAINT "tasa_valor_check" CHECK (("valor" >= (0)::numeric)),
    CONSTRAINT "tasa_version_check" CHECK (("version" > 0))
);


ALTER TABLE "public"."tasa" OWNER TO "postgres";


ALTER TABLE "public"."tasa" ALTER COLUMN "id_tasa" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."tasa_id_tasa_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."traspaso_cliente" (
    "id_traspaso" bigint NOT NULL,
    "id_cliente" bigint NOT NULL,
    "registrado_por" bigint NOT NULL,
    "capital_pendiente_inicial" numeric(12,2) DEFAULT 0 NOT NULL,
    "interes_pendiente_inicial" numeric(12,2) DEFAULT 0 NOT NULL,
    "fecha_corte_origen" "date",
    "fecha_hora" timestamp with time zone DEFAULT "now"() NOT NULL,
    "observacion_motivo" character varying(200),
    "datos_iniciales" "jsonb",
    "obligaciones_incorporadas" "jsonb",
    CONSTRAINT "traspaso_cliente_capital_pendiente_inicial_check" CHECK (("capital_pendiente_inicial" >= (0)::numeric)),
    CONSTRAINT "traspaso_cliente_interes_pendiente_inicial_check" CHECK (("interes_pendiente_inicial" >= (0)::numeric))
);


ALTER TABLE "public"."traspaso_cliente" OWNER TO "postgres";


ALTER TABLE "public"."traspaso_cliente" ALTER COLUMN "id_traspaso" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."traspaso_cliente_id_traspaso_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."usuario" (
    "id_usuario" bigint NOT NULL,
    "id_persona" bigint NOT NULL,
    "username" character varying(50) NOT NULL,
    "password_hash" character varying(255) NOT NULL,
    "estado" character varying(20) DEFAULT 'ACTIVA'::character varying NOT NULL,
    "cambio_password_obligatorio" boolean DEFAULT true NOT NULL,
    "ultimo_acceso" timestamp with time zone,
    "fecha_creacion" timestamp with time zone DEFAULT "now"() NOT NULL,
    "version" integer DEFAULT 0 NOT NULL,
    "foto_perfil_ruta" character varying(500),
    CONSTRAINT "usuario_estado_check" CHECK ((("estado")::"text" = ANY ((ARRAY['ACTIVA'::character varying, 'RESTRINGIDA'::character varying, 'BLOQUEADA'::character varying])::"text"[])))
);


ALTER TABLE "public"."usuario" OWNER TO "postgres";


ALTER TABLE "public"."usuario" ALTER COLUMN "id_usuario" ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME "public"."usuario_id_usuario_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE IF NOT EXISTS "public"."usuario_rol" (
    "id_usuario" bigint NOT NULL,
    "id_rol" bigint NOT NULL,
    "fecha_asignacion" timestamp with time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."usuario_rol" OWNER TO "postgres";


ALTER TABLE ONLY "public"."acuerdo"
    ADD CONSTRAINT "acuerdo_pkey" PRIMARY KEY ("id_acuerdo");



ALTER TABLE ONLY "public"."auditoria"
    ADD CONSTRAINT "auditoria_pkey" PRIMARY KEY ("id_auditoria");



ALTER TABLE ONLY "public"."cliente"
    ADD CONSTRAINT "cliente_codigo_cliente_key" UNIQUE ("codigo_cliente");



ALTER TABLE ONLY "public"."cliente"
    ADD CONSTRAINT "cliente_id_persona_key" UNIQUE ("id_persona");



ALTER TABLE ONLY "public"."cliente"
    ADD CONSTRAINT "cliente_pkey" PRIMARY KEY ("id_cliente");



ALTER TABLE ONLY "public"."condicion_credito"
    ADD CONSTRAINT "condicion_credito_pkey" PRIMARY KEY ("id_condicion");



ALTER TABLE ONLY "public"."credito"
    ADD CONSTRAINT "credito_codigo_credito_key" UNIQUE ("codigo_credito");



ALTER TABLE ONLY "public"."credito"
    ADD CONSTRAINT "credito_id_condicion_key" UNIQUE ("id_condicion");



ALTER TABLE ONLY "public"."credito"
    ADD CONSTRAINT "credito_pkey" PRIMARY KEY ("id_credito");



ALTER TABLE ONLY "public"."cuota"
    ADD CONSTRAINT "cuota_pkey" PRIMARY KEY ("id_cuota");



ALTER TABLE ONLY "public"."documento"
    ADD CONSTRAINT "documento_codigo_documento_key" UNIQUE ("codigo_documento");



ALTER TABLE ONLY "public"."documento"
    ADD CONSTRAINT "documento_pkey" PRIMARY KEY ("id_documento");



ALTER TABLE ONLY "public"."garantia"
    ADD CONSTRAINT "garantia_pkey" PRIMARY KEY ("id_garantia");



ALTER TABLE ONLY "public"."notificacion"
    ADD CONSTRAINT "notificacion_clave_evento_key" UNIQUE ("clave_evento");



ALTER TABLE ONLY "public"."notificacion"
    ADD CONSTRAINT "notificacion_pkey" PRIMARY KEY ("id_notificacion");



ALTER TABLE ONLY "public"."pago_detalle"
    ADD CONSTRAINT "pago_detalle_pkey" PRIMARY KEY ("id_detalle");



ALTER TABLE ONLY "public"."pago"
    ADD CONSTRAINT "pago_idempotency_key_key" UNIQUE ("idempotency_key");



ALTER TABLE ONLY "public"."pago"
    ADD CONSTRAINT "pago_pkey" PRIMARY KEY ("id_pago");



ALTER TABLE ONLY "public"."permiso"
    ADD CONSTRAINT "permiso_pkey" PRIMARY KEY ("id_permiso");



ALTER TABLE ONLY "public"."persona"
    ADD CONSTRAINT "persona_dni_key" UNIQUE ("dni");



ALTER TABLE ONLY "public"."persona"
    ADD CONSTRAINT "persona_email_key" UNIQUE ("email");



ALTER TABLE ONLY "public"."persona"
    ADD CONSTRAINT "persona_pkey" PRIMARY KEY ("id_persona");



ALTER TABLE ONLY "public"."plan_regularizacion_detalle"
    ADD CONSTRAINT "plan_regularizacion_detalle_pkey" PRIMARY KEY ("id_detalle_plan");



ALTER TABLE ONLY "public"."plan_regularizacion"
    ADD CONSTRAINT "plan_regularizacion_pkey" PRIMARY KEY ("id_plan");



ALTER TABLE ONLY "public"."recuperacion_password"
    ADD CONSTRAINT "recuperacion_password_pkey" PRIMARY KEY ("id_recuperacion");



ALTER TABLE ONLY "public"."recuperacion_password"
    ADD CONSTRAINT "recuperacion_password_token_hash_key" UNIQUE ("token_hash");



ALTER TABLE ONLY "public"."rol"
    ADD CONSTRAINT "rol_nombre_key" UNIQUE ("nombre");



ALTER TABLE ONLY "public"."rol_permiso"
    ADD CONSTRAINT "rol_permiso_pkey" PRIMARY KEY ("id_rol", "id_permiso");



ALTER TABLE ONLY "public"."rol"
    ADD CONSTRAINT "rol_pkey" PRIMARY KEY ("id_rol");



ALTER TABLE ONLY "public"."suscripcion_push"
    ADD CONSTRAINT "suscripcion_push_pkey" PRIMARY KEY ("id_suscripcion");



ALTER TABLE ONLY "public"."suscripcion_push"
    ADD CONSTRAINT "suscripcion_push_token_fcm_key" UNIQUE ("token_fcm");



ALTER TABLE ONLY "public"."tasa"
    ADD CONSTRAINT "tasa_pkey" PRIMARY KEY ("id_tasa");



ALTER TABLE ONLY "public"."traspaso_cliente"
    ADD CONSTRAINT "traspaso_cliente_pkey" PRIMARY KEY ("id_traspaso");



ALTER TABLE ONLY "public"."credito"
    ADD CONSTRAINT "uk_credito_cliente" UNIQUE ("id_credito", "id_cliente");



ALTER TABLE ONLY "public"."cuota"
    ADD CONSTRAINT "uk_credito_numero_cuota" UNIQUE ("id_credito", "numero_cuota");



ALTER TABLE ONLY "public"."cuota"
    ADD CONSTRAINT "uk_cuota_credito" UNIQUE ("id_cuota", "id_credito");



ALTER TABLE ONLY "public"."garantia"
    ADD CONSTRAINT "uk_garantia_credito" UNIQUE ("id_garantia", "id_credito");



ALTER TABLE ONLY "public"."pago"
    ADD CONSTRAINT "uk_pago_credito" UNIQUE ("id_pago", "id_credito");



ALTER TABLE ONLY "public"."pago_detalle"
    ADD CONSTRAINT "uk_pago_cuota" UNIQUE ("id_pago", "id_cuota");



ALTER TABLE ONLY "public"."permiso"
    ADD CONSTRAINT "uk_permiso_modulo_nombre" UNIQUE ("modulo", "nombre");



ALTER TABLE ONLY "public"."plan_regularizacion"
    ADD CONSTRAINT "uk_plan_credito" UNIQUE ("id_plan", "id_credito");



ALTER TABLE ONLY "public"."plan_regularizacion_detalle"
    ADD CONSTRAINT "uk_plan_cuota" UNIQUE ("id_plan", "id_cuota");



ALTER TABLE ONLY "public"."tasa"
    ADD CONSTRAINT "uk_tasa_codigo_version" UNIQUE ("codigo", "version");



ALTER TABLE ONLY "public"."traspaso_cliente"
    ADD CONSTRAINT "uk_traspaso_cliente" UNIQUE ("id_cliente");



ALTER TABLE ONLY "public"."usuario"
    ADD CONSTRAINT "usuario_id_persona_key" UNIQUE ("id_persona");



ALTER TABLE ONLY "public"."usuario"
    ADD CONSTRAINT "usuario_pkey" PRIMARY KEY ("id_usuario");



ALTER TABLE ONLY "public"."usuario_rol"
    ADD CONSTRAINT "usuario_rol_pkey" PRIMARY KEY ("id_usuario", "id_rol");



ALTER TABLE ONLY "public"."usuario"
    ADD CONSTRAINT "usuario_username_key" UNIQUE ("username");



CREATE INDEX "idx_auditoria_entidad" ON "public"."auditoria" USING "btree" ("entidad", "identificador");



CREATE INDEX "idx_auditoria_usuario_fecha" ON "public"."auditoria" USING "btree" ("id_usuario", "fecha_hora");



CREATE INDEX "idx_credito_cliente_estado" ON "public"."credito" USING "btree" ("id_cliente", "estado");



CREATE INDEX "idx_cuota_credito_vencimiento" ON "public"."cuota" USING "btree" ("id_credito", "fecha_vencimiento");



CREATE INDEX "idx_cuota_estado_vencimiento" ON "public"."cuota" USING "btree" ("estado", "fecha_vencimiento");



CREATE INDEX "idx_documento_cliente" ON "public"."documento" USING "btree" ("id_cliente");



CREATE INDEX "idx_garantia_credito" ON "public"."garantia" USING "btree" ("id_credito");



CREATE INDEX "idx_notificacion_cliente_estado" ON "public"."notificacion" USING "btree" ("id_cliente", "estado");



CREATE INDEX "idx_notificacion_cliente_leida" ON "public"."notificacion" USING "btree" ("id_cliente", "leida");



CREATE INDEX "idx_notificacion_cuota" ON "public"."notificacion" USING "btree" ("id_cuota");



CREATE INDEX "idx_notificacion_programada" ON "public"."notificacion" USING "btree" ("estado", "fecha_programada");



CREATE INDEX "idx_pago_credito" ON "public"."pago" USING "btree" ("id_credito");



CREATE INDEX "idx_pago_estado_fecha" ON "public"."pago" USING "btree" ("estado", "fecha_pago");



CREATE INDEX "idx_plan_credito" ON "public"."plan_regularizacion" USING "btree" ("id_credito");



CREATE INDEX "idx_push_usuario_activo" ON "public"."suscripcion_push" USING "btree" ("id_usuario", "activo");



CREATE INDEX "idx_traspaso_cliente" ON "public"."traspaso_cliente" USING "btree" ("id_cliente");



CREATE UNIQUE INDEX "uk_constancia_pago_unica" ON "public"."documento" USING "btree" ("id_pago") WHERE (("tipo_documento")::"text" = 'CONSTANCIA_PAGO'::"text");



CREATE UNIQUE INDEX "uk_pago_metodo_referencia" ON "public"."pago" USING "btree" ("metodo_pago", "referencia") WHERE (("referencia" IS NOT NULL) AND (("referencia")::"text" <> ''::"text"));



CREATE OR REPLACE TRIGGER "before_credito_redondear" BEFORE INSERT OR UPDATE ON "public"."credito" FOR EACH ROW EXECUTE FUNCTION "public"."trg_redondear_credito"();



CREATE OR REPLACE TRIGGER "before_cuota_redondear" BEFORE INSERT OR UPDATE ON "public"."cuota" FOR EACH ROW EXECUTE FUNCTION "public"."trg_redondear_cuota"();



CREATE OR REPLACE TRIGGER "before_garantia_redondear" BEFORE INSERT OR UPDATE ON "public"."garantia" FOR EACH ROW EXECUTE FUNCTION "public"."trg_redondear_garantia"();



CREATE OR REPLACE TRIGGER "before_pago_detalle_redondear" BEFORE INSERT OR UPDATE ON "public"."pago_detalle" FOR EACH ROW EXECUTE FUNCTION "public"."trg_redondear_pago_detalle"();



CREATE OR REPLACE TRIGGER "before_pago_redondear" BEFORE INSERT OR UPDATE ON "public"."pago" FOR EACH ROW EXECUTE FUNCTION "public"."trg_redondear_pago"();



CREATE OR REPLACE TRIGGER "before_plan_detalle_redondear" BEFORE INSERT OR UPDATE ON "public"."plan_regularizacion_detalle" FOR EACH ROW EXECUTE FUNCTION "public"."trg_redondear_plan_detalle"();



CREATE OR REPLACE TRIGGER "before_plan_regularizacion_redondear" BEFORE INSERT OR UPDATE ON "public"."plan_regularizacion" FOR EACH ROW EXECUTE FUNCTION "public"."trg_redondear_plan_regularizacion"();



CREATE OR REPLACE TRIGGER "before_suscripcion_push_update" BEFORE UPDATE ON "public"."suscripcion_push" FOR EACH ROW EXECUTE FUNCTION "public"."trg_actualizar_suscripcion_push"();



CREATE OR REPLACE TRIGGER "before_traspaso_cliente_redondear" BEFORE INSERT OR UPDATE ON "public"."traspaso_cliente" FOR EACH ROW EXECUTE FUNCTION "public"."trg_redondear_traspaso_cliente"();



ALTER TABLE ONLY "public"."acuerdo"
    ADD CONSTRAINT "acuerdo_id_credito_fkey" FOREIGN KEY ("id_credito") REFERENCES "public"."credito"("id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."acuerdo"
    ADD CONSTRAINT "acuerdo_id_usuario_creador_fkey" FOREIGN KEY ("id_usuario_creador") REFERENCES "public"."usuario"("id_usuario") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."auditoria"
    ADD CONSTRAINT "auditoria_id_usuario_fkey" FOREIGN KEY ("id_usuario") REFERENCES "public"."usuario"("id_usuario") ON DELETE SET NULL;



ALTER TABLE ONLY "public"."cliente"
    ADD CONSTRAINT "cliente_id_persona_fkey" FOREIGN KEY ("id_persona") REFERENCES "public"."persona"("id_persona") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."condicion_credito"
    ADD CONSTRAINT "condicion_credito_id_tasa_origen_fkey" FOREIGN KEY ("id_tasa_origen") REFERENCES "public"."tasa"("id_tasa") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."credito"
    ADD CONSTRAINT "credito_id_cliente_fkey" FOREIGN KEY ("id_cliente") REFERENCES "public"."cliente"("id_cliente") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."credito"
    ADD CONSTRAINT "credito_id_condicion_fkey" FOREIGN KEY ("id_condicion") REFERENCES "public"."condicion_credito"("id_condicion") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."cuota"
    ADD CONSTRAINT "cuota_id_credito_fkey" FOREIGN KEY ("id_credito") REFERENCES "public"."credito"("id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."documento"
    ADD CONSTRAINT "documento_id_cliente_fkey" FOREIGN KEY ("id_cliente") REFERENCES "public"."cliente"("id_cliente") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."acuerdo"
    ADD CONSTRAINT "fk_acuerdo_cuota_credito" FOREIGN KEY ("id_cuota", "id_credito") REFERENCES "public"."cuota"("id_cuota", "id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."documento"
    ADD CONSTRAINT "fk_documento_credito_cliente" FOREIGN KEY ("id_credito", "id_cliente") REFERENCES "public"."credito"("id_credito", "id_cliente") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."documento"
    ADD CONSTRAINT "fk_documento_garantia_credito" FOREIGN KEY ("id_garantia", "id_credito") REFERENCES "public"."garantia"("id_garantia", "id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."documento"
    ADD CONSTRAINT "fk_documento_pago_credito" FOREIGN KEY ("id_pago", "id_credito") REFERENCES "public"."pago"("id_pago", "id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."plan_regularizacion_detalle"
    ADD CONSTRAINT "fk_dplan_cuota_credito" FOREIGN KEY ("id_cuota", "id_credito") REFERENCES "public"."cuota"("id_cuota", "id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."plan_regularizacion_detalle"
    ADD CONSTRAINT "fk_dplan_plan_credito" FOREIGN KEY ("id_plan", "id_credito") REFERENCES "public"."plan_regularizacion"("id_plan", "id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."notificacion"
    ADD CONSTRAINT "fk_notificacion_credito_cliente" FOREIGN KEY ("id_credito", "id_cliente") REFERENCES "public"."credito"("id_credito", "id_cliente") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."notificacion"
    ADD CONSTRAINT "fk_notificacion_cuota_credito" FOREIGN KEY ("id_cuota", "id_credito") REFERENCES "public"."cuota"("id_cuota", "id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."pago_detalle"
    ADD CONSTRAINT "fk_pago_det_cuota_credito" FOREIGN KEY ("id_cuota", "id_credito") REFERENCES "public"."cuota"("id_cuota", "id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."pago_detalle"
    ADD CONSTRAINT "fk_pago_det_pago_credito" FOREIGN KEY ("id_pago", "id_credito") REFERENCES "public"."pago"("id_pago", "id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."garantia"
    ADD CONSTRAINT "garantia_id_credito_fkey" FOREIGN KEY ("id_credito") REFERENCES "public"."credito"("id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."notificacion"
    ADD CONSTRAINT "notificacion_creado_por_fkey" FOREIGN KEY ("creado_por") REFERENCES "public"."usuario"("id_usuario") ON DELETE SET NULL;



ALTER TABLE ONLY "public"."notificacion"
    ADD CONSTRAINT "notificacion_id_cliente_fkey" FOREIGN KEY ("id_cliente") REFERENCES "public"."cliente"("id_cliente") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."pago"
    ADD CONSTRAINT "pago_id_credito_fkey" FOREIGN KEY ("id_credito") REFERENCES "public"."credito"("id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."pago"
    ADD CONSTRAINT "pago_registrado_por_fkey" FOREIGN KEY ("registrado_por") REFERENCES "public"."usuario"("id_usuario") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."pago"
    ADD CONSTRAINT "pago_validado_por_fkey" FOREIGN KEY ("validado_por") REFERENCES "public"."usuario"("id_usuario") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."plan_regularizacion"
    ADD CONSTRAINT "plan_regularizacion_id_credito_fkey" FOREIGN KEY ("id_credito") REFERENCES "public"."credito"("id_credito") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."plan_regularizacion"
    ADD CONSTRAINT "plan_regularizacion_id_usuario_creador_fkey" FOREIGN KEY ("id_usuario_creador") REFERENCES "public"."usuario"("id_usuario") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."recuperacion_password"
    ADD CONSTRAINT "recuperacion_password_id_usuario_fkey" FOREIGN KEY ("id_usuario") REFERENCES "public"."usuario"("id_usuario") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."rol_permiso"
    ADD CONSTRAINT "rol_permiso_id_permiso_fkey" FOREIGN KEY ("id_permiso") REFERENCES "public"."permiso"("id_permiso") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."rol_permiso"
    ADD CONSTRAINT "rol_permiso_id_rol_fkey" FOREIGN KEY ("id_rol") REFERENCES "public"."rol"("id_rol") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."suscripcion_push"
    ADD CONSTRAINT "suscripcion_push_id_usuario_fkey" FOREIGN KEY ("id_usuario") REFERENCES "public"."usuario"("id_usuario") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."tasa"
    ADD CONSTRAINT "tasa_id_usuario_creador_fkey" FOREIGN KEY ("id_usuario_creador") REFERENCES "public"."usuario"("id_usuario") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."traspaso_cliente"
    ADD CONSTRAINT "traspaso_cliente_id_cliente_fkey" FOREIGN KEY ("id_cliente") REFERENCES "public"."cliente"("id_cliente") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."traspaso_cliente"
    ADD CONSTRAINT "traspaso_cliente_registrado_por_fkey" FOREIGN KEY ("registrado_por") REFERENCES "public"."usuario"("id_usuario") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."usuario"
    ADD CONSTRAINT "usuario_id_persona_fkey" FOREIGN KEY ("id_persona") REFERENCES "public"."persona"("id_persona") ON DELETE RESTRICT;



ALTER TABLE ONLY "public"."usuario_rol"
    ADD CONSTRAINT "usuario_rol_id_rol_fkey" FOREIGN KEY ("id_rol") REFERENCES "public"."rol"("id_rol") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."usuario_rol"
    ADD CONSTRAINT "usuario_rol_id_usuario_fkey" FOREIGN KEY ("id_usuario") REFERENCES "public"."usuario"("id_usuario") ON DELETE CASCADE;



ALTER TABLE "public"."acuerdo" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."auditoria" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."cliente" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."condicion_credito" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."credito" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."cuota" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."documento" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."garantia" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."notificacion" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."pago" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."pago_detalle" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."permiso" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."persona" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."plan_regularizacion" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."plan_regularizacion_detalle" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."recuperacion_password" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."rol" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."rol_permiso" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."suscripcion_push" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."tasa" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."traspaso_cliente" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."usuario" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."usuario_rol" ENABLE ROW LEVEL SECURITY;


GRANT USAGE ON SCHEMA "public" TO "postgres";
GRANT USAGE ON SCHEMA "public" TO "anon";
GRANT USAGE ON SCHEMA "public" TO "authenticated";
GRANT USAGE ON SCHEMA "public" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."acuerdo" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."acuerdo" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."acuerdo" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."auditoria" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."auditoria" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."auditoria" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."cliente" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."cliente" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."cliente" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."condicion_credito" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."condicion_credito" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."condicion_credito" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."credito" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."credito" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."credito" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."cuota" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."cuota" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."cuota" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."documento" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."documento" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."documento" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."garantia" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."garantia" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."garantia" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."notificacion" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."notificacion" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."notificacion" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."pago" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."pago" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."pago" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."pago_detalle" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."pago_detalle" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."pago_detalle" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."permiso" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."permiso" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."permiso" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."persona" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."persona" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."persona" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."plan_regularizacion" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."plan_regularizacion" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."plan_regularizacion" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."plan_regularizacion_detalle" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."plan_regularizacion_detalle" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."plan_regularizacion_detalle" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."recuperacion_password" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."recuperacion_password" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."recuperacion_password" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."rol" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."rol" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."rol" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."rol_permiso" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."rol_permiso" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."rol_permiso" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."suscripcion_push" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."suscripcion_push" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."suscripcion_push" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."tasa" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."tasa" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."tasa" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."traspaso_cliente" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."traspaso_cliente" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."traspaso_cliente" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."usuario" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."usuario" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."usuario" TO "service_role";



GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."usuario_rol" TO "anon";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."usuario_rol" TO "authenticated";
GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLE "public"."usuario_rol" TO "service_role";



ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "postgres";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "postgres";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT REFERENCES,TRIGGER,TRUNCATE,MAINTAIN ON TABLES TO "service_role";







