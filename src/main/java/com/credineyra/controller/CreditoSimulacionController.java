package com.credineyra.controller;

import com.credineyra.dto.CreditoRegistroDto;
import com.credineyra.dto.CreditoSimulacionDto;
import com.credineyra.service.CreditoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/creditos")
public class CreditoSimulacionController {

    private final CreditoService creditoService;

    public CreditoSimulacionController(
            CreditoService creditoService
    ) {
        this.creditoService =
                creditoService;
    }

    @PostMapping("/simular")
    public ResponseEntity<CreditoSimulacionDto> simular(
            @Valid
            @RequestBody
            CreditoRegistroDto dto
    ) {

        CreditoSimulacionDto resultado =
                creditoService.simular(
                        dto
                );

        return ResponseEntity.ok(
                resultado
        );
    }
}