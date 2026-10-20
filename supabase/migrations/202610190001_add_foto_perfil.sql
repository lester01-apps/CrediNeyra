-- Permite guardar una foto privada por cada cliente en la tabla documento.
ALTER TABLE public.documento
    DROP CONSTRAINT IF EXISTS documento_tipo_documento_check;

ALTER TABLE public.documento
    ADD CONSTRAINT documento_tipo_documento_check
    CHECK (tipo_documento IN (
        'CONTRATO_GENERADO', 'CONTRATO_FIRMADO', 'CONSTANCIA_PAGO',
        'COMPROBANTE_PAGO', 'DNI', 'FOTO_PERFIL',
        'FOTO_GARANTIA', 'DOCUMENTO_GARANTIA'
    ));
