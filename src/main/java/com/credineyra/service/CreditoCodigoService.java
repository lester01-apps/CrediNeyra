package com.credineyra.service;

import com.credineyra.entity.Credito;
import com.credineyra.repository.CreditoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CreditoCodigoService {

    private static final Pattern PATRON =
            Pattern.compile(
                    "^CRE-(\\d+)$",
                    Pattern.CASE_INSENSITIVE
            );

    private final CreditoRepository creditoRepository;


    public CreditoCodigoService(
            CreditoRepository creditoRepository
    ) {
        this.creditoRepository =
                creditoRepository;
    }


    @Transactional(readOnly = true)
    public synchronized String generarSiguienteCodigo() {

        int mayor = 0;


        for (
                Credito credito :
                creditoRepository.findAll()
        ) {

            String codigo =
                    credito.getCodigoCredito();


            if (
                    codigo == null
                            ||
                            codigo.isBlank()
            ) {
                continue;
            }


            Matcher matcher =
                    PATRON.matcher(
                            codigo.trim()
                    );


            if (!matcher.matches()) {
                continue;
            }


            try {

                int numero =
                        Integer.parseInt(
                                matcher.group(1)
                        );


                if (numero > mayor) {
                    mayor = numero;
                }

            } catch (NumberFormatException ignored) {
            }
        }


        return String.format(
                "CRE-%05d",
                mayor + 1
        );
    }
}