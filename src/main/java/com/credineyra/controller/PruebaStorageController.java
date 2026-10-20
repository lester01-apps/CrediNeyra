package com.credineyra.controller;

import com.credineyra.service.SupabaseStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/prueba-storage")
public class PruebaStorageController {

    private final SupabaseStorageService storageService;

    public PruebaStorageController(
            SupabaseStorageService storageService) {

        this.storageService = storageService;
    }

    @PostMapping("/subir")
    public ResponseEntity<String> subir(
            @RequestParam("archivo") MultipartFile archivo) {

        try {

            String ruta =
                    storageService.subirArchivo(archivo);

            return ResponseEntity.ok(
                    "FUNCIONÓ\nRuta: " + ruta
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "ERROR\n" + e.getMessage()
                    );
        }
    }
}