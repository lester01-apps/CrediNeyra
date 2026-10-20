package com.credineyra.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SupabaseStorageService {

    @Value("${credineyra.supabase.url}")
    private String supabaseUrl;

    @Value("${credineyra.supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @Value("${credineyra.supabase.storage-bucket}")
    private String bucket;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String subirArchivo(MultipartFile archivo) {

        try {

            if (archivo == null || archivo.isEmpty()) {
                throw new RuntimeException("El archivo está vacío");
            }

            if (supabaseServiceRoleKey == null ||
                    supabaseServiceRoleKey.isBlank()) {

                throw new RuntimeException(
                        "No está configurada SUPABASE_SERVICE_ROLE_KEY"
                );
            }

            String nombreOriginal = archivo.getOriginalFilename();

            if (nombreOriginal == null || nombreOriginal.isBlank()) {
                nombreOriginal = "archivo";
            }

            nombreOriginal = nombreOriginal
                    .replace(" ", "_")
                    .replaceAll("[^a-zA-Z0-9._-]", "");

            String ruta =
                    "pruebas/"
                            + System.currentTimeMillis()
                            + "_"
                            + nombreOriginal;

            String url =
                    supabaseUrl
                            + "/storage/v1/object/"
                            + bucket
                            + "/"
                            + ruta;

            String contentType = archivo.getContentType();

            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(
                            "Authorization",
                            "Bearer " + supabaseServiceRoleKey
                    )
                    .header(
                            "apikey",
                            supabaseServiceRoleKey
                    )
                    .header(
                            "Content-Type",
                            contentType
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofByteArray(
                                    archivo.getBytes()
                            )
                    )
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() >= 200 &&
                    response.statusCode() < 300) {

                return ruta;
            }

            throw new RuntimeException(
                    "Supabase respondió "
                            + response.statusCode()
                            + ": "
                            + response.body()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "No se pudo subir el archivo: "
                            + e.getMessage(),
                    e
            );
        }
    }
}