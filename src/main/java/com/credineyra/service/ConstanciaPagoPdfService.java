package com.credineyra.service;

import com.credineyra.dto.PagoDetalleVistaDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class ConstanciaPagoPdfService {

    private final PDType1Font normal =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private final PDType1Font bold =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    public byte[] generar(
            PagoDetalleVistaDto dto
    ) {

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

            document.addPage(page);

            try (
                    PDPageContentStream cs =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {

                float y = 790;

                texto(
                        cs,
                        bold,
                        21,
                        55,
                        y,
                        "crediNeyra"
                );

                y -= 25;

                texto(
                        cs,
                        bold,
                        15,
                        55,
                        y,
                        "CONSTANCIA INTERNA DE PAGO"
                );

                y -= 18;

                texto(
                        cs,
                        normal,
                        9,
                        55,
                        y,
                        "Documento interno - No constituye comprobante tributario"
                );

                y -= 30;

                linea(
                        cs,
                        55,
                        y,
                        540,
                        y
                );

                y -= 28;

                y = campo(
                        cs,
                        y,
                        "Codigo de pago:",
                        dto.codigoPago()
                );

                y = campo(
                        cs,
                        y,
                        "Credito:",
                        dto.codigoCredito()
                );

                y = campo(
                        cs,
                        y,
                        "Cliente:",
                        dto.clienteNombre()
                );

                y = campo(
                        cs,
                        y,
                        "DNI:",
                        dto.clienteDni()
                );

                y = campo(
                        cs,
                        y,
                        "Metodo:",
                        dto.metodoPago()
                );

                y = campo(
                        cs,
                        y,
                        "Referencia:",
                        dto.referencia()
                );

                y = campo(
                        cs,
                        y,
                        "Monto confirmado:",
                        moneda(dto.monto())
                );

                String fecha =
                        dto.fechaValidacion() == null
                                ? "-"
                                : dto.fechaValidacion()
                                .format(
                                        DateTimeFormatter
                                                .ofPattern(
                                                        "dd/MM/yyyy HH:mm"
                                                )
                                );

                y = campo(
                        cs,
                        y,
                        "Fecha de validacion:",
                        fecha
                );

                y -= 12;

                texto(
                        cs,
                        bold,
                        11,
                        55,
                        y,
                        "Aplicacion del pago"
                );

                y -= 18;

                for (
                        PagoDetalleVistaDto.AplicacionPagoVistaDto aplicacion :
                        dto.aplicaciones()
                ) {

                    String fila =
                            "Cuota "
                                    + aplicacion.numeroCuota()
                                    + "  | Capital "
                                    + moneda(
                                    aplicacion.montoCapital()
                            )
                                    + " | Interes "
                                    + moneda(
                                    aplicacion.montoInteres()
                            )
                                    + " | Regularizado "
                                    + moneda(
                                    aplicacion
                                            .montoCapitalRegularizado()
                            )
                                    + " | Total "
                                    + moneda(
                                    aplicacion.montoTotal()
                            );

                    texto(
                            cs,
                            normal,
                            8,
                            65,
                            y,
                            fila
                    );

                    y -= 15;
                }

                y -= 15;

                linea(
                        cs,
                        55,
                        y,
                        540,
                        y
                );

                y -= 25;

                texto(
                        cs,
                        bold,
                        10,
                        55,
                        y,
                        "Saldo despues del pago"
                );

                y -= 18;

                texto(
                        cs,
                        normal,
                        9,
                        65,
                        y,
                        "Capital pendiente: "
                                + moneda(
                                dto.capitalPendienteCredito()
                        )
                );

                y -= 15;

                texto(
                        cs,
                        normal,
                        9,
                        65,
                        y,
                        "Interes pendiente: "
                                + moneda(
                                dto.interesPendienteCredito()
                        )
                );

                y -= 15;

                texto(
                        cs,
                        normal,
                        9,
                        65,
                        y,
                        "Total pendiente: "
                                + moneda(
                                dto.totalPendienteCredito()
                        )
                );

                y -= 45;

                texto(
                        cs,
                        normal,
                        8,
                        55,
                        y,
                        "Esta constancia fue generada automaticamente por crediNeyra."
                );
            }

            document.save(output);

            return output.toByteArray();

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "No se pudo generar la constancia PDF",
                    ex
            );
        }
    }

    private float campo(
            PDPageContentStream cs,
            float y,
            String etiqueta,
            String valor
    ) throws IOException {

        texto(
                cs,
                bold,
                9,
                55,
                y,
                etiqueta
        );

        texto(
                cs,
                normal,
                9,
                180,
                y,
                valor == null
                        ? "-"
                        : limpiar(valor)
        );

        return y - 18;
    }

    private void texto(
            PDPageContentStream cs,
            PDType1Font fuente,
            float tamano,
            float x,
            float y,
            String texto
    ) throws IOException {

        cs.beginText();

        cs.setFont(
                fuente,
                tamano
        );

        cs.newLineAtOffset(
                x,
                y
        );

        cs.showText(
                limpiar(texto)
        );

        cs.endText();
    }

    private void linea(
            PDPageContentStream cs,
            float x1,
            float y1,
            float x2,
            float y2
    ) throws IOException {

        cs.moveTo(
                x1,
                y1
        );

        cs.lineTo(
                x2,
                y2
        );

        cs.stroke();
    }

    private String moneda(
            BigDecimal monto
    ) {

        if (monto == null) {
            return "S/ 0.00";
        }

        return "S/ "
                + monto.setScale(2)
                .toPlainString();
    }

    private String limpiar(
            String valor
    ) {

        if (valor == null) {
            return "";
        }

        return valor
                .replace("–", "-")
                .replace("—", "-");
    }
}