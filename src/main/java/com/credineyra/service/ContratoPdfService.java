package com.credineyra.service;

import com.credineyra.dto.ContratoCreditoDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ContratoPdfService {

    /*
     * =========================================================
     * COLORES
     * =========================================================
     */

    private static final Color AZUL =
            new Color(
                    45,
                    112,
                    170
            );

    private static final Color AZUL_OSCURO =
            new Color(
                    40,
                    67,
                    94
            );

    private static final Color CELESTE_CLARO =
            new Color(
                    235,
                    243,
                    249
            );

    private static final Color GRIS_FILA =
            new Color(
                    245,
                    247,
                    249
            );

    private static final Color GRIS_BORDE =
            new Color(
                    205,
                    214,
                    222
            );

    private static final Color BLANCO =
            Color.WHITE;

    private static final Color NEGRO =
            new Color(
                    25,
                    35,
                    45
            );


    /*
     * =========================================================
     * FUENTES
     * =========================================================
     */

    private final PDFont regular =
            new PDType1Font(
                    Standard14Fonts
                            .FontName
                            .HELVETICA
            );

    private final PDFont bold =
            new PDType1Font(
                    Standard14Fonts
                            .FontName
                            .HELVETICA_BOLD
            );

    private final PDFont italic =
            new PDType1Font(
                    Standard14Fonts
                            .FontName
                            .HELVETICA_OBLIQUE
            );


    /*
     * =========================================================
     * GENERAR PDF
     * =========================================================
     */

    public byte[] generar(
            ContratoCreditoDto dto
    ) {

        if (dto == null) {

            throw new IllegalArgumentException(
                    "Los datos del contrato son obligatorios."
            );
        }


        try (
                PDDocument document =
                        new PDDocument();

                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            PDPage page =
                    new PDPage(
                            PDRectangle.A4
                    );


            document.addPage(
                    page
            );


            try (
                    PDPageContentStream canvas =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {

                dibujarContrato(
                        canvas,
                        dto,
                        page
                );
            }


            document.save(
                    output
            );


            return output
                    .toByteArray();


        } catch (IOException ex) {

            throw new IllegalStateException(
                    "No se pudo generar el contrato PDF.",
                    ex
            );
        }
    }


    /*
     * =========================================================
     * DIBUJAR CONTRATO
     * =========================================================
     */

    private void dibujarContrato(
            PDPageContentStream canvas,
            ContratoCreditoDto dto,
            PDPage page
    ) throws IOException {

        float ancho =
                page
                        .getMediaBox()
                        .getWidth();


        float margen =
                36f;


        float anchoUtil =
                ancho
                        -
                        margen * 2;


        float y =
                810f;


        /*
         * =====================================================
         * LÍNEA SUPERIOR
         * =====================================================
         */

        canvas.setStrokingColor(
                AZUL_OSCURO
        );

        canvas.setLineWidth(
                1.2f
        );

        canvas.moveTo(
                180,
                y
        );

        canvas.lineTo(
                ancho - 180,
                y
        );

        canvas.stroke();


        y -= 17f;


        textoCentrado(
                canvas,
                "CREDINEYRA · FINANZAS Y SERVICIOS FINANCIEROS",
                bold,
                10,
                y,
                ancho,
                AZUL
        );


        y -= 19f;


        textoCentrado(
                canvas,
                "CARTA Y PLAN DE PAGO DE PRÉSTAMO",
                bold,
                13,
                y,
                ancho,
                AZUL_OSCURO
        );


        y -= 27f;


        /*
         * =====================================================
         * CÓDIGO / FECHA
         * =====================================================
         */

        texto(
                canvas,
                "Código:",
                bold,
                8,
                margen,
                y,
                NEGRO
        );


        texto(
                canvas,
                seguro(
                        dto.getCodigoCredito()
                ),
                regular,
                8,
                margen + 42,
                y,
                NEGRO
        );


        texto(
                canvas,
                "Fecha de entrega:",
                bold,
                8,
                350,
                y,
                NEGRO
        );


        texto(
                canvas,
                fecha(
                        dto.getFechaDesembolso()
                ),
                regular,
                8,
                430,
                y,
                NEGRO
        );


        y -= 22f;


        /*
         * =====================================================
         * PÁRRAFO
         * =====================================================
         */

        String introduccion =
                "Por medio del presente documento se deja constancia de que "
                        + "EL/LA PRESTATARIO(A) recibe de EL/LA PRESTAMISTA "
                        + "la cantidad de "
                        + moneda(dto.getCapitalOriginal())
                        + " en calidad de préstamo, comprometiéndose a devolver "
                        + "el monto conforme a las condiciones y cronograma "
                        + "detallados en el presente documento.";


        y =
                parrafo(
                        canvas,
                        introduccion,
                        bold,
                        7.2f,
                        margen,
                        y,
                        anchoUtil,
                        9.5f,
                        NEGRO
                );


        y -= 8f;


        /*
         * =====================================================
         * DATOS DE LAS PARTES
         * =====================================================
         */

        textoCentrado(
                canvas,
                "DATOS DE LAS PARTES INTERVINIENTES",
                bold,
                8.5f,
                y,
                ancho,
                AZUL_OSCURO
        );


        y -= 12f;


        float altoPartes =
                60f;


        rectanguloRelleno(
                canvas,
                margen,
                y - altoPartes,
                anchoUtil,
                altoPartes,
                CELESTE_CLARO
        );


        lineaVertical(
                canvas,
                margen + anchoUtil / 2,
                y - altoPartes,
                y,
                GRIS_BORDE
        );


        float izquierda =
                margen + 8f;


        float derecha =
                margen
                        +
                        anchoUtil / 2
                        +
                        8f;


        float yp =
                y - 13f;


        texto(
                canvas,
                "PRESTATARIO(A) (DEUDOR)",
                bold,
                7.4f,
                izquierda,
                yp,
                AZUL_OSCURO
        );


        texto(
                canvas,
                "PRESTAMISTA (ACREEDOR)",
                bold,
                7.4f,
                derecha,
                yp,
                AZUL_OSCURO
        );


        yp -= 14f;


        texto(
                canvas,
                "Nombre: "
                        + seguro(
                        dto.getPrestatarioNombre()
                ),
                regular,
                6.8f,
                izquierda,
                yp,
                NEGRO
        );


        texto(
                canvas,
                "Nombre: "
                        + seguro(
                        dto.getPrestamistaNombre()
                ),
                regular,
                6.8f,
                derecha,
                yp,
                NEGRO
        );


        yp -= 12f;


        texto(
                canvas,
                "DNI/CE: "
                        + seguro(
                        dto.getPrestatarioDocumento()
                ),
                regular,
                6.8f,
                izquierda,
                yp,
                NEGRO
        );


        texto(
                canvas,
                "DNI/RUC: "
                        + seguro(
                        dto.getPrestamistaDocumento()
                ),
                regular,
                6.8f,
                derecha,
                yp,
                NEGRO
        );


        yp -= 12f;


        textoAjustado(
                canvas,
                "Domicilio: "
                        + seguro(
                        dto.getPrestatarioDomicilio()
                ),
                regular,
                6.3f,
                izquierda,
                yp,
                anchoUtil / 2 - 14,
                NEGRO
        );


        textoAjustado(
                canvas,
                "Domicilio: "
                        + seguro(
                        dto.getPrestamistaDomicilio()
                ),
                regular,
                6.3f,
                derecha,
                yp,
                anchoUtil / 2 - 14,
                NEGRO
        );


        y -= altoPartes + 15f;


        /*
         * =====================================================
         * CONDICIONES FINANCIERAS
         * =====================================================
         */

        textoCentrado(
                canvas,
                "CONDICIONES FINANCIERAS DEL CRÉDITO",
                bold,
                8.5f,
                y,
                ancho,
                AZUL_OSCURO
        );


        y -= 12f;


        float altoCondicion =
                42f;


        rectanguloRelleno(
                canvas,
                margen,
                y - altoCondicion,
                anchoUtil,
                altoCondicion,
                GRIS_FILA
        );


        float col1 =
                margen + 8;

        float col2 =
                margen + 185;

        float col3 =
                margen + 355;


        float yc =
                y - 12;


        texto(
                canvas,
                "Capital: "
                        + moneda(
                        dto.getCapitalOriginal()
                ),
                bold,
                6.8f,
                col1,
                yc,
                NEGRO
        );


        texto(
                canvas,
                "Tipo: "
                        + tipoCalculo(
                        dto.getTipoCalculo()
                ),
                bold,
                6.8f,
                col2,
                yc,
                NEGRO
        );


        /*
         * ESTE ES EL PUNTO IMPORTANTE:
         *
         * NO SE RECALCULA LA TASA.
         *
         * Se imprime exactamente la tasa congelada
         * en la condición del crédito.
         */
        texto(
                canvas,
                "Tasa: "
                        + tasaContrato(
                        dto
                ),
                bold,
                6.8f,
                col3,
                yc,
                NEGRO
        );


        yc -= 16;


        texto(
                canvas,
                "Plazo: "
                        + seguroNumero(
                        dto.getPlazoMeses()
                )
                        + " meses",
                regular,
                6.5f,
                col1,
                yc,
                NEGRO
        );


        texto(
                canvas,
                "Periodicidad interés: "
                        + nombrePeriodicidad(
                        dto.getPeriodicidadInteres()
                ),
                regular,
                6.5f,
                col2,
                yc,
                NEGRO
        );


        texto(
                canvas,
                "Frecuencia pago: "
                        + nombrePeriodicidad(
                        dto.getFrecuenciaPago()
                ),
                regular,
                6.5f,
                col3,
                yc,
                NEGRO
        );


        y -= altoCondicion + 16;


        /*
         * =====================================================
         * CRONOGRAMA
         * =====================================================
         */

        textoCentrado(
                canvas,
                "CRONOGRAMA DEL PLAN DE PAGOS",
                bold,
                8.5f,
                y,
                ancho,
                AZUL_OSCURO
        );


        y -= 12;


        List<ContratoCreditoDto.CuotaContratoDto> cuotas =
                dto.getCronograma() == null
                        ? new ArrayList<>()
                        : dto.getCronograma();


        float altoDisponibleTabla =
                170f;


        int cantidad =
                Math.max(
                        cuotas.size(),
                        1
                );


        float altoFila =
                Math.min(
                        15f,
                        altoDisponibleTabla
                                /
                                (
                                        cantidad + 2f
                                )
                );


        altoFila =
                Math.max(
                        altoFila,
                        6f
                );


        float fuenteTabla =
                Math.min(
                        6.4f,
                        altoFila * .48f
                );


        fuenteTabla =
                Math.max(
                        fuenteTabla,
                        4.2f
                );


        float[] anchos = {
                24f,
                61f,
                93f,
                74f,
                67f,
                79f,
                95f
        };


        String[] titulos = {
                "#",
                "Fecha",
                "Capital inicial",
                "Capital",
                "Interés",
                "Pago total",
                "Saldo final"
        };


        dibujarFilaTabla(
                canvas,
                margen,
                y,
                altoFila,
                anchos,
                titulos,
                bold,
                fuenteTabla,
                AZUL,
                BLANCO
        );


        y -= altoFila;


        int indice =
                0;


        for (
                ContratoCreditoDto.CuotaContratoDto cuota :
                cuotas
        ) {

            Color fondo =
                    indice % 2 == 0
                            ? BLANCO
                            : GRIS_FILA;


            String[] valores = {
                    String.valueOf(
                            cuota.getNumero()
                    ),

                    fecha(
                            cuota.getFecha()
                    ),

                    moneda(
                            cuota.getSaldoInicial()
                    ),

                    moneda(
                            cuota.getCapital()
                    ),

                    moneda(
                            cuota.getInteres()
                    ),

                    moneda(
                            cuota.getTotal()
                    ),

                    moneda(
                            cuota.getSaldoFinal()
                    )
            };


            dibujarFilaTabla(
                    canvas,
                    margen,
                    y,
                    altoFila,
                    anchos,
                    valores,
                    regular,
                    fuenteTabla,
                    fondo,
                    NEGRO
            );


            y -= altoFila;

            indice++;
        }


        /*
         * TOTAL
         */

        String[] total = {
                "",
                "TOTAL",
                "",
                moneda(
                        dto.getCapitalOriginal()
                ),
                moneda(
                        dto.getInteresTotal()
                ),
                moneda(
                        dto.getTotalPagar()
                ),
                ""
        };


        dibujarFilaTabla(
                canvas,
                margen,
                y,
                altoFila,
                anchos,
                total,
                bold,
                fuenteTabla,
                CELESTE_CLARO,
                AZUL_OSCURO
        );


        y -= altoFila + 12f;


        /*
         * =====================================================
         * GARANTÍA
         * =====================================================
         */

        texto(
                canvas,
                "Garantía:",
                bold,
                6.7f,
                margen,
                y,
                AZUL_OSCURO
        );


        textoAjustado(
                canvas,
                seguroGarantia(
                        dto.getGarantiaDescripcion()
                ),
                regular,
                6.5f,
                margen + 48,
                y,
                anchoUtil - 48,
                NEGRO
        );


        y -= 16;


        /*
         * =====================================================
         * CONDICIONES
         * =====================================================
         */

        textoCentrado(
                canvas,
                "CONDICIONES DE PAGO, GARANTÍA Y REGULARIZACIÓN",
                bold,
                7.8f,
                y,
                ancho,
                AZUL_OSCURO
        );


        y -= 12;


        List<String> condiciones =
                List.of(

                        "1. EL/LA PRESTATARIO(A) se compromete a cumplir los pagos conforme a las fechas y montos establecidos en el cronograma.",

                        "2. La tasa contractual es "
                                + tasaContrato(dto)
                                + " y la frecuencia de pago es "
                                + nombrePeriodicidad(
                                dto.getFrecuenciaPago()
                        )
                                + ". Estas condiciones quedan congeladas para este crédito.",

                        "3. Si al finalizar una fecha de vencimiento existe saldo pendiente, inicia un período de regularización de tres (3) días calendario consecutivos.",

                        "4. Los tres días de regularización incluyen sábados, domingos, feriados y días no laborables.",

                        "5. La existencia de una garantía no implica transferencia automática de propiedad; cualquier gestión sobre ella requiere revisión administrativa.",

                        "6. Todo pago registrado deberá conservar fecha, monto aplicado, capital, interés y saldo pendiente."
                );


        for (
                String condicion :
                condiciones
        ) {

            y =
                    parrafo(
                            canvas,
                            condicion,
                            regular,
                            5.7f,
                            margen,
                            y,
                            anchoUtil,
                            7.2f,
                            NEGRO
                    );


            y -= 2f;
        }


        y -= 3f;


        texto(
                canvas,
                "En señal de conformidad, ambas partes declaran haber leído y aceptado las condiciones del presente documento.",
                italic,
                5.8f,
                margen,
                y,
                NEGRO
        );


        y -= 16;


        texto(
                canvas,
                "Fecha de firma: ____ / ____ / ______",
                bold,
                6.3f,
                margen,
                y,
                NEGRO
        );


        y -= 32;


        /*
         * =====================================================
         * FIRMAS
         * =====================================================
         */

        float mitad =
                anchoUtil / 2;


        lineaHorizontal(
                canvas,
                margen + 20,
                margen + mitad - 20,
                y,
                NEGRO
        );


        lineaHorizontal(
                canvas,
                margen + mitad + 20,
                margen + anchoUtil - 20,
                y,
                NEGRO
        );


        y -= 10;


        textoCentradoZona(
                canvas,
                "Firma y huella",
                regular,
                5.7f,
                margen,
                mitad,
                y,
                NEGRO
        );


        textoCentradoZona(
                canvas,
                "Firma y sello",
                regular,
                5.7f,
                margen + mitad,
                mitad,
                y,
                NEGRO
        );


        y -= 10;


        textoCentradoZona(
                canvas,
                "EL/LA PRESTATARIO(A)",
                bold,
                6.3f,
                margen,
                mitad,
                y,
                AZUL_OSCURO
        );


        textoCentradoZona(
                canvas,
                "EL/LA PRESTAMISTA",
                bold,
                6.3f,
                margen + mitad,
                mitad,
                y,
                AZUL_OSCURO
        );


        y -= 11;


        textoCentradoZona(
                canvas,
                seguro(
                        dto.getPrestatarioNombre()
                ),
                regular,
                5.5f,
                margen,
                mitad,
                y,
                NEGRO
        );


        textoCentradoZona(
                canvas,
                seguro(
                        dto.getPrestamistaNombre()
                ),
                regular,
                5.5f,
                margen + mitad,
                mitad,
                y,
                NEGRO
        );


        y -= 9;


        textoCentradoZona(
                canvas,
                "DNI/CE: "
                        + seguro(
                        dto.getPrestatarioDocumento()
                ),
                regular,
                5.3f,
                margen,
                mitad,
                y,
                NEGRO
        );


        textoCentradoZona(
                canvas,
                "DNI/RUC: "
                        + seguro(
                        dto.getPrestamistaDocumento()
                ),
                regular,
                5.3f,
                margen + mitad,
                mitad,
                y,
                NEGRO
        );
    }


    /*
     * =========================================================
     * TASA CONTRACTUAL
     * =========================================================
     *
     * IMPORTANTE:
     *
     * NO SE CALCULA:
     *
     * interesTotal / capital
     *
     * NO SE CREA UNA TASA EFECTIVA.
     *
     * Se imprime exactamente:
     *
     * condicion_credito.tasa_interes
     *
     * +
     *
     * condicion_credito.periodicidad_tasa
     *
     * =========================================================
     */

    private String tasaContrato(
            ContratoCreditoDto dto
    ) {

        if (
                dto.getTasaInteres() == null
        ) {

            return "No especificada";
        }


        String porcentaje =
                dto.getTasaInteres()
                        .stripTrailingZeros()
                        .toPlainString();


        return porcentaje
                + "% "
                + nombrePeriodicidad(
                dto.getPeriodicidadInteres()
        );
    }


    /*
     * =========================================================
     * TABLA
     * =========================================================
     */

    private void dibujarFilaTabla(
            PDPageContentStream canvas,
            float x,
            float y,
            float alto,
            float[] anchos,
            String[] valores,
            PDFont fuente,
            float tamano,
            Color fondo,
            Color texto
    ) throws IOException {

        float anchoTotal =
                0;


        for (
                float ancho :
                anchos
        ) {

            anchoTotal +=
                    ancho;
        }


        rectanguloRelleno(
                canvas,
                x,
                y - alto,
                anchoTotal,
                alto,
                fondo
        );


        float posicionX =
                x;


        for (
                int i = 0;
                i < anchos.length;
                i++
        ) {

            canvas.setStrokingColor(
                    GRIS_BORDE
            );

            canvas.setLineWidth(
                    .4f
            );

            canvas.addRect(
                    posicionX,
                    y - alto,
                    anchos[i],
                    alto
            );

            canvas.stroke();


            String valor =
                    i < valores.length
                            ? seguro(
                            valores[i]
                    )
                            : "";


            textoCentradoZona(
                    canvas,
                    ajustarTexto(
                            valor,
                            fuente,
                            tamano,
                            anchos[i] - 4
                    ),
                    fuente,
                    tamano,
                    posicionX,
                    anchos[i],
                    y - alto + alto / 2 - tamano / 3,
                    texto
            );


            posicionX +=
                    anchos[i];
        }
    }


    /*
     * =========================================================
     * TEXTO
     * =========================================================
     */

    private void texto(
            PDPageContentStream canvas,
            String valor,
            PDFont fuente,
            float tamano,
            float x,
            float y,
            Color color
    ) throws IOException {

        canvas.beginText();

        canvas.setFont(
                fuente,
                tamano
        );

        canvas.setNonStrokingColor(
                color
        );

        canvas.newLineAtOffset(
                x,
                y
        );

        canvas.showText(
                limpiarPdf(
                        valor
                )
        );

        canvas.endText();
    }


    private void textoCentrado(
            PDPageContentStream canvas,
            String valor,
            PDFont fuente,
            float tamano,
            float y,
            float anchoPagina,
            Color color
    ) throws IOException {

        String texto =
                limpiarPdf(
                        valor
                );


        float ancho =
                fuente
                        .getStringWidth(
                                texto
                        )
                        /
                        1000f
                        *
                        tamano;


        this.texto(
                canvas,
                texto,
                fuente,
                tamano,
                (
                        anchoPagina
                                -
                                ancho
                )
                        /
                        2f,
                y,
                color
        );
    }


    private void textoCentradoZona(
            PDPageContentStream canvas,
            String valor,
            PDFont fuente,
            float tamano,
            float x,
            float anchoZona,
            float y,
            Color color
    ) throws IOException {

        String contenido =
                limpiarPdf(
                        valor
                );


        float ancho =
                fuente
                        .getStringWidth(
                                contenido
                        )
                        /
                        1000f
                        *
                        tamano;


        float posicion =
                x
                        +
                        Math.max(
                                2,
                                (
                                        anchoZona
                                                -
                                                ancho
                                )
                                        /
                                        2f
                        );


        texto(
                canvas,
                contenido,
                fuente,
                tamano,
                posicion,
                y,
                color
        );
    }


    private void textoAjustado(
            PDPageContentStream canvas,
            String valor,
            PDFont fuente,
            float tamano,
            float x,
            float y,
            float maxAncho,
            Color color
    ) throws IOException {

        texto(
                canvas,
                ajustarTexto(
                        seguro(valor),
                        fuente,
                        tamano,
                        maxAncho
                ),
                fuente,
                tamano,
                x,
                y,
                color
        );
    }


    private float parrafo(
            PDPageContentStream canvas,
            String valor,
            PDFont fuente,
            float tamano,
            float x,
            float y,
            float ancho,
            float interlineado,
            Color color
    ) throws IOException {

        List<String> lineas =
                dividirLineas(
                        valor,
                        fuente,
                        tamano,
                        ancho
                );


        float actual =
                y;


        for (
                String linea :
                lineas
        ) {

            texto(
                    canvas,
                    linea,
                    fuente,
                    tamano,
                    x,
                    actual,
                    color
            );


            actual -=
                    interlineado;
        }


        return actual;
    }


    private List<String> dividirLineas(
            String texto,
            PDFont fuente,
            float tamano,
            float ancho
    ) throws IOException {

        List<String> resultado =
                new ArrayList<>();


        String limpio =
                limpiarPdf(
                        texto
                );


        String[] palabras =
                limpio.split("\\s+");


        StringBuilder linea =
                new StringBuilder();


        for (
                String palabra :
                palabras
        ) {

            String candidata =
                    linea.isEmpty()
                            ? palabra
                            : linea
                            + " "
                            + palabra;


            float anchoCandidata =
                    fuente
                            .getStringWidth(
                                    candidata
                            )
                            /
                            1000f
                            *
                            tamano;


            if (
                    anchoCandidata
                            <= ancho
            ) {

                linea =
                        new StringBuilder(
                                candidata
                        );

            } else {

                if (
                        !linea.isEmpty()
                ) {

                    resultado.add(
                            linea.toString()
                    );
                }


                linea =
                        new StringBuilder(
                                palabra
                        );
            }
        }


        if (
                !linea.isEmpty()
        ) {

            resultado.add(
                    linea.toString()
            );
        }


        return resultado;
    }


    private String ajustarTexto(
            String texto,
            PDFont fuente,
            float tamano,
            float ancho
    ) throws IOException {

        String valor =
                limpiarPdf(
                        seguro(texto)
                );


        if (
                fuente
                        .getStringWidth(
                                valor
                        )
                        /
                        1000f
                        *
                        tamano
                        <= ancho
        ) {

            return valor;
        }


        String puntos =
                "...";


        while (
                !valor.isEmpty()
                        &&
                        fuente
                                .getStringWidth(
                                        valor
                                                + puntos
                                )
                                /
                                1000f
                                *
                                tamano
                                > ancho
        ) {

            valor =
                    valor.substring(
                            0,
                            valor.length() - 1
                    );
        }


        return valor
                + puntos;
    }


    /*
     * =========================================================
     * FORMAS
     * =========================================================
     */

    private void rectanguloRelleno(
            PDPageContentStream canvas,
            float x,
            float y,
            float ancho,
            float alto,
            Color color
    ) throws IOException {

        canvas.setNonStrokingColor(
                color
        );

        canvas.addRect(
                x,
                y,
                ancho,
                alto
        );

        canvas.fill();
    }


    private void lineaHorizontal(
            PDPageContentStream canvas,
            float x1,
            float x2,
            float y,
            Color color
    ) throws IOException {

        canvas.setStrokingColor(
                color
        );

        canvas.setLineWidth(
                .8f
        );

        canvas.moveTo(
                x1,
                y
        );

        canvas.lineTo(
                x2,
                y
        );

        canvas.stroke();
    }


    private void lineaVertical(
            PDPageContentStream canvas,
            float x,
            float y1,
            float y2,
            Color color
    ) throws IOException {

        canvas.setStrokingColor(
                color
        );

        canvas.setLineWidth(
                .5f
        );

        canvas.moveTo(
                x,
                y1
        );

        canvas.lineTo(
                x,
                y2
        );

        canvas.stroke();
    }


    /*
     * =========================================================
     * FORMATOS
     * =========================================================
     */

    private String moneda(
            BigDecimal valor
    ) {

        BigDecimal numero =
                valor == null
                        ? BigDecimal.ZERO
                        : valor;


        return "S/ "
                + numero
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }


    private String fecha(
            LocalDate fecha
    ) {

        if (
                fecha == null
        ) {

            return "-";
        }


        return fecha.format(
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy",
                        new Locale(
                                "es",
                                "PE"
                        )
                )
        );
    }


    private String tipoCalculo(
            Object valor
    ) {

        if (
                valor == null
        ) {

            return "-";
        }


        return switch (
                valor
                        .toString()
                        .toUpperCase()
                ) {

            case "NORMAL" ->
                    "NORMAL";

            case "INTERES_ECONOMICO" ->
                    "INTERÉS ECONÓMICO";

            default ->
                    valor.toString();
        };
    }


    private String nombrePeriodicidad(
            Object valor
    ) {

        if (
                valor == null
        ) {

            return "-";
        }


        return switch (
                valor
                        .toString()
                        .toUpperCase()
                ) {

            case "DIARIA" ->
                    "diaria";

            case "SEMANAL" ->
                    "semanal";

            case "QUINCENAL" ->
                    "quincenal";

            case "MENSUAL" ->
                    "mensual";

            default ->
                    valor
                            .toString()
                            .toLowerCase();
        };
    }


    private String seguro(
            Object valor
    ) {

        if (
                valor == null
        ) {

            return "-";
        }


        String texto =
                valor
                        .toString()
                        .trim();


        return texto.isBlank()
                ? "-"
                : texto;
    }


    private String seguroNumero(
            Object valor
    ) {

        return valor == null
                ? "-"
                : valor.toString();
    }


    private String seguroGarantia(
            String valor
    ) {

        if (
                valor == null
                        ||
                        valor.isBlank()
        ) {

            return "Sin garantía registrada.";
        }


        return valor.trim();
    }


    /*
     * Helvetica estándar trabaja con WinAnsi.
     * Limpiamos caracteres que podrían provocar
     * error de codificación.
     */
    private String limpiarPdf(
            String valor
    ) {

        if (
                valor == null
        ) {

            return "";
        }


        return valor
                .replace("–", "-")
                .replace("—", "-")
                .replace("•", "-")
                .replace("→", "->")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}