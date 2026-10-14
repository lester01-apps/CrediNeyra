package com.credineyra.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DocumentoStorageService {

    private final HttpClient httpClient;


    @Value("${credineyra.supabase.url}")
    private String supabaseUrl;


    /*
     * Se mantiene por compatibilidad con la
     * configuración actual del proyecto.
     */
    @Value("${credineyra.supabase.service-key:}")
    private String secretKey;


    /*
     * Legacy service_role JWT.
     * Es la credencial que estamos usando con
     * las llamadas REST directas de Storage.
     */
    @Value("${credineyra.supabase.service-role-key}")
    private String serviceRoleKey;


    @Value("${credineyra.supabase.storage-bucket}")
    private String bucket;


    public DocumentoStorageService() {

        this.httpClient =
                HttpClient
                        .newBuilder()
                        .connectTimeout(
                                Duration.ofSeconds(15)
                        )
                        .build();
    }


    /*
     * =========================================================
     * SUBIR ARCHIVO
     * =========================================================
     */

    public void subir(
            byte[] contenido,
            String ruta,
            String contentType,
            boolean sobrescribir
    ) {

        validarConfiguracion();


        if (
                contenido == null
                        ||
                        contenido.length == 0
        ) {

            throw new IllegalArgumentException(
                    "El archivo está vacío."
            );
        }


        if (
                ruta == null
                        ||
                        ruta.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La ruta del archivo es obligatoria."
            );
        }


        String endpoint =
                storageBaseUrl()
                        + "/object/"
                        + codificarSegmento(
                        bucket
                )
                        + "/"
                        + codificarRuta(
                        ruta
                );


        HttpRequest request =
                HttpRequest
                        .newBuilder()
                        .uri(
                                URI.create(
                                        endpoint
                                )
                        )
                        .timeout(
                                Duration.ofSeconds(60)
                        )
                        .header(
                                "apikey",
                                serviceRoleKey
                        )
                        .header(
                                "Authorization",
                                "Bearer "
                                        + serviceRoleKey
                        )
                        .header(
                                "Content-Type",
                                contentType
                        )
                        .header(
                                "x-upsert",
                                sobrescribir
                                        ? "true"
                                        : "false"
                        )
                        .POST(
                                HttpRequest
                                        .BodyPublishers
                                        .ofByteArray(
                                                contenido
                                        )
                        )
                        .build();


        HttpResponse<String> response =
                enviar(
                        request
                );


        validarRespuesta(
                response,
                "Supabase Storage rechazó la subida."
        );
    }


    /*
     * =========================================================
     * CREAR URL FIRMADA
     * =========================================================
     */

    public String crearUrlFirmada(
            String ruta,
            int segundos
    ) {

        validarConfiguracion();


        if (
                ruta == null
                        ||
                        ruta.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La ruta del documento es obligatoria."
            );
        }


        if (segundos <= 0) {

            segundos =
                    600;
        }


        /*
         * Endpoint utilizado PARA CREAR la URL firmada.
         */
        String endpoint =
                storageBaseUrl()
                        + "/object/sign/"
                        + codificarSegmento(
                        bucket
                )
                        + "/"
                        + codificarRuta(
                        ruta
                );


        String json =
                "{\"expiresIn\":"
                        + segundos
                        + "}";


        HttpRequest request =
                HttpRequest
                        .newBuilder()
                        .uri(
                                URI.create(
                                        endpoint
                                )
                        )
                        .timeout(
                                Duration.ofSeconds(30)
                        )
                        .header(
                                "apikey",
                                serviceRoleKey
                        )
                        .header(
                                "Authorization",
                                "Bearer "
                                        + serviceRoleKey
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest
                                        .BodyPublishers
                                        .ofString(
                                                json
                                        )
                        )
                        .build();


        HttpResponse<String> response =
                enviar(
                        request
                );


        validarRespuesta(
                response,
                "No se pudo crear la URL temporal."
        );


        String signedUrl =
                extraerSignedUrl(
                        response.body()
                );


        if (
                signedUrl == null
                        ||
                        signedUrl.isBlank()
        ) {

            throw new IllegalStateException(
                    "Supabase no devolvió una URL firmada válida."
            );
        }


        /*
         * Limpiar escapes JSON.
         */
        signedUrl =
                signedUrl
                        .replace(
                                "\\/",
                                "/"
                        )
                        .replace(
                                "\\u0026",
                                "&"
                        );


        return construirUrlFirmadaCompleta(
                signedUrl
        );
    }


    /*
     * =========================================================
     * CONSTRUIR URL FIRMADA COMPLETA
     * =========================================================
     *
     * Supabase normalmente devuelve:
     *
     * /object/sign/bucket/ruta?token=...
     *
     * Eso NO se debe pegar directamente al dominio.
     *
     * Debe convertirse en:
     *
     * https://proyecto.supabase.co
     * /storage/v1
     * /object/sign/bucket/ruta?token=...
     *
     * =========================================================
     */

    private String construirUrlFirmadaCompleta(
            String signedUrl
    ) {

        String url =
                signedUrl.trim();


        /*
         * Si Supabase alguna vez devuelve
         * una URL absoluta, la usamos tal cual.
         */
        if (
                url.startsWith("https://")
                        ||
                        url.startsWith("http://")
        ) {

            return url;
        }


        /*
         * Caso:
         *
         * /storage/v1/object/sign/...
         */
        if (
                url.startsWith(
                        "/storage/v1/"
                )
        ) {

            return baseProyecto()
                    + url;
        }


        /*
         * CASO NORMAL DE SUPABASE:
         *
         * /object/sign/...
         */
        if (
                url.startsWith(
                        "/object/"
                )
        ) {

            return storageBaseUrl()
                    + url;
        }


        /*
         * Por seguridad, normalizamos
         * cualquier otra ruta relativa.
         */
        if (
                url.startsWith("/")
        ) {

            return storageBaseUrl()
                    + url;
        }


        return storageBaseUrl()
                + "/"
                + url;
    }


    /*
     * =========================================================
     * BASE URL DEL PROYECTO
     * =========================================================
     */

    private String baseProyecto() {

        return limpiarUrl(
                supabaseUrl
        );
    }


    /*
     * =========================================================
     * BASE URL DE STORAGE
     * =========================================================
     */

    private String storageBaseUrl() {

        return baseProyecto()
                + "/storage/v1";
    }


    /*
     * =========================================================
     * HTTP
     * =========================================================
     */

    private HttpResponse<String> enviar(
            HttpRequest request
    ) {

        try {

            return httpClient.send(
                    request,
                    HttpResponse
                            .BodyHandlers
                            .ofString()
            );

        } catch (InterruptedException ex) {

            Thread.currentThread()
                    .interrupt();


            throw new IllegalStateException(
                    "La operación con Supabase Storage fue interrumpida.",
                    ex
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "No se pudo conectar con Supabase Storage.",
                    ex
            );
        }
    }


    private void validarRespuesta(
            HttpResponse<String> response,
            String mensaje
    ) {

        if (
                response.statusCode() >= 200
                        &&
                        response.statusCode() < 300
        ) {

            return;
        }


        throw new IllegalStateException(
                mensaje
                        + " HTTP "
                        + response.statusCode()
                        + ". "
                        + limpiarError(
                        response.body()
                )
        );
    }


    /*
     * =========================================================
     * EXTRAER SIGNED URL
     * =========================================================
     */

    private String extraerSignedUrl(
            String json
    ) {

        if (
                json == null
                        ||
                        json.isBlank()
        ) {

            return null;
        }


        /*
         * Algunas versiones utilizan signedURL.
         */
        Pattern patronMayuscula =
                Pattern.compile(
                        "\"signedURL\"\\s*:\\s*\"([^\"]+)\""
                );


        Matcher matcher =
                patronMayuscula.matcher(
                        json
                );


        if (matcher.find()) {

            return matcher.group(1);
        }


        /*
         * Otras respuestas / SDK pueden utilizar signedUrl.
         */
        Pattern patronMinuscula =
                Pattern.compile(
                        "\"signedUrl\"\\s*:\\s*\"([^\"]+)\""
                );


        matcher =
                patronMinuscula.matcher(
                        json
                );


        if (matcher.find()) {

            return matcher.group(1);
        }


        return null;
    }


    /*
     * =========================================================
     * CONFIGURACIÓN
     * =========================================================
     */

    private void validarConfiguracion() {

        if (
                supabaseUrl == null
                        ||
                        supabaseUrl.isBlank()
        ) {

            throw new IllegalStateException(
                    "Falta configurar SUPABASE_URL."
            );
        }


        if (
                serviceRoleKey == null
                        ||
                        serviceRoleKey.isBlank()
        ) {

            throw new IllegalStateException(
                    "Falta configurar SUPABASE_SERVICE_ROLE_KEY."
            );
        }


        if (
                serviceRoleKey.startsWith(
                        "sb_secret_"
                )
                        ||
                        serviceRoleKey.startsWith(
                                "sb_publishable_"
                        )
        ) {

            throw new IllegalStateException(
                    "SUPABASE_SERVICE_ROLE_KEY debe contener "
                            + "la clave legacy service_role JWT."
            );
        }


        if (
                !serviceRoleKey.startsWith(
                        "eyJ"
                )
        ) {

            throw new IllegalStateException(
                    "SUPABASE_SERVICE_ROLE_KEY no parece una service_role JWT válida."
            );
        }


        if (
                bucket == null
                        ||
                        bucket.isBlank()
        ) {

            throw new IllegalStateException(
                    "Falta configurar el bucket de Storage."
            );
        }
    }


    /*
     * =========================================================
     * RUTAS
     * =========================================================
     */

    private String limpiarUrl(
            String url
    ) {

        String resultado =
                url == null
                        ? ""
                        : url.trim();


        while (
                resultado.endsWith("/")
        ) {

            resultado =
                    resultado.substring(
                            0,
                            resultado.length() - 1
                    );
        }


        return resultado;
    }


    private String codificarRuta(
            String ruta
    ) {

        return Arrays
                .stream(
                        ruta.split("/")
                )
                .filter(
                        segmento ->
                                !segmento.isBlank()
                )
                .map(
                        this::codificarSegmento
                )
                .collect(
                        Collectors.joining("/")
                );
    }


    private String codificarSegmento(
            String valor
    ) {

        return URLEncoder
                .encode(
                        valor,
                        StandardCharsets.UTF_8
                )
                .replace(
                        "+",
                        "%20"
                );
    }


    /*
     * =========================================================
     * ERROR
     * =========================================================
     */

    private String limpiarError(
            String cuerpo
    ) {

        if (
                cuerpo == null
                        ||
                        cuerpo.isBlank()
        ) {

            return "Sin detalle.";
        }


        String limpio =
                cuerpo
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();


        if (
                limpio.length() > 350
        ) {

            limpio =
                    limpio.substring(
                            0,
                            350
                    );
        }


        return limpio;
    }
}