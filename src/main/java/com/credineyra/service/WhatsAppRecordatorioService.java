package com.credineyra.service;

import com.credineyra.entity.Cuota;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Envía recordatorios por la API oficial de WhatsApp Business cuando las
 * credenciales están configuradas. Si no lo están, nunca simula un envío.
 */
@Service
public class WhatsAppRecordatorioService {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final boolean habilitado;
    private final String accessToken;
    private final String phoneNumberId;
    private final String apiVersion;
    private final String templateName;
    private final String templateLanguage;
    private final String countryCode;
    private final RestClient restClient;

    public WhatsAppRecordatorioService(
            @Value("${credineyra.whatsapp.enabled:false}") boolean habilitado,
            @Value("${credineyra.whatsapp.access-token:}") String accessToken,
            @Value("${credineyra.whatsapp.phone-number-id:}") String phoneNumberId,
            @Value("${credineyra.whatsapp.api-version:v23.0}") String apiVersion,
            @Value("${credineyra.whatsapp.reminder-template:recordatorio_cuota}") String templateName,
            @Value("${credineyra.whatsapp.template-language:es_PE}") String templateLanguage,
            @Value("${credineyra.whatsapp.country-code:51}") String countryCode
    ) {
        this.habilitado = habilitado;
        this.accessToken = accessToken;
        this.phoneNumberId = phoneNumberId;
        this.apiVersion = apiVersion;
        this.templateName = templateName;
        this.templateLanguage = templateLanguage;
        this.countryCode = countryCode;
        this.restClient = RestClient.create();
    }

    public ResultadoEnvio enviarRecordatorio(Cuota cuota) {
        if (!estaConfigurado()) {
            return ResultadoEnvio.configuracionPendiente(
                    "WhatsApp Business no está configurado"
            );
        }

        String telefono = normalizarTelefono(
                cuota.getCredito().getCliente().getPersona().getTelefono()
        );

        if (telefono == null) {
            return ResultadoEnvio.configuracionPendiente(
                    "El cliente no tiene un teléfono válido para WhatsApp"
            );
        }

        String nombre = cuota.getCredito().getCliente().getPersona().getNombres();
        String codigoCredito = cuota.getCredito().getCodigoCredito();
        String monto = "S/ " + total(cuota).setScale(2);
        String fecha = cuota.getFechaVencimiento().format(FECHA);

        Map<String, Object> contenido = Map.of(
                "messaging_product", "whatsapp",
                "to", telefono,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", templateLanguage),
                        "components", List.of(Map.of(
                                "type", "body",
                                "parameters", List.of(
                                        parametro(nombre),
                                        parametro("#" + cuota.getNumeroCuota()),
                                        parametro(codigoCredito),
                                        parametro(monto),
                                        parametro(fecha)
                                )
                        ))
                )
        );

        try {
            restClient.post()
                    .uri("https://graph.facebook.com/{version}/{phoneNumberId}/messages",
                            apiVersion, phoneNumberId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(contenido)
                    .retrieve()
                    .toBodilessEntity();

            return ResultadoEnvio.exito();
        } catch (RestClientException exception) {
            return ResultadoEnvio.error(
                    "Meta rechazó el envío: " + mensajeSeguro(exception)
            );
        }
    }

    private boolean estaConfigurado() {
        return habilitado
                && accessToken != null && !accessToken.isBlank()
                && phoneNumberId != null && !phoneNumberId.isBlank()
                && templateName != null && !templateName.isBlank();
    }

    private String normalizarTelefono(String telefono) {
        if (telefono == null) {
            return null;
        }
        String digitos = telefono.replaceAll("\\D", "");
        if (digitos.length() == 9) {
            return countryCode + digitos;
        }
        return digitos.length() >= 10 ? digitos : null;
    }

    private BigDecimal total(Cuota cuota) {
        return valor(cuota.getCapitalPendiente())
                .add(valor(cuota.getInteresPendiente()))
                .add(valor(cuota.getCapitalRegularizadoPendiente()));
    }

    private BigDecimal valor(BigDecimal monto) {
        return monto == null ? BigDecimal.ZERO : monto;
    }

    private Map<String, String> parametro(String texto) {
        return Map.of("type", "text", "text", texto == null ? "Cliente" : texto);
    }

    private String mensajeSeguro(Exception exception) {
        String mensaje = exception.getMessage();
        return mensaje == null || mensaje.isBlank()
                ? "error de comunicación"
                : mensaje.replaceAll("(?i)bearer\\s+[^\\s]+", "Bearer [oculto]");
    }

    public record ResultadoEnvio(boolean enviado, boolean reintentar, String detalle) {
        static ResultadoEnvio exito() {
            return new ResultadoEnvio(true, false, "Enviado a WhatsApp Business");
        }

        static ResultadoEnvio configuracionPendiente(String detalle) {
            return new ResultadoEnvio(false, true, detalle);
        }

        static ResultadoEnvio error(String detalle) {
            return new ResultadoEnvio(false, false, detalle);
        }
    }
}
