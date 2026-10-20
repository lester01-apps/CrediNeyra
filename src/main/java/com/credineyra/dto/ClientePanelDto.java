package com.credineyra.dto;

import java.util.List;

public class ClientePanelDto {

    /*
     * =========================================================
     * KPI
     * =========================================================
     */

    private long totalClientes;

    private long clientesActivos;

    private long clientesInactivos;

    private long clientesAdvertencia;

    private long clientesMora;


    /*
     * =========================================================
     * LISTADO
     * =========================================================
     */

    private List<ClienteResumenDto> clientes;


    /*
     * =========================================================
     * CONSTRUCTOR VACÍO
     * =========================================================
     */

    public ClientePanelDto() {
    }


    /*
     * =========================================================
     * CONSTRUCTOR COMPLETO
     * =========================================================
     */

    public ClientePanelDto(
            long totalClientes,
            long clientesActivos,
            long clientesInactivos,
            long clientesAdvertencia,
            long clientesMora,
            List<ClienteResumenDto> clientes
    ) {

        this.totalClientes =
                totalClientes;

        this.clientesActivos =
                clientesActivos;

        this.clientesInactivos =
                clientesInactivos;

        this.clientesAdvertencia =
                clientesAdvertencia;

        this.clientesMora =
                clientesMora;

        this.clientes =
                clientes;
    }


    /*
     * =========================================================
     * GETTERS Y SETTERS
     * =========================================================
     */

    public long getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(
            long totalClientes
    ) {
        this.totalClientes =
                totalClientes;
    }


    public long getClientesActivos() {
        return clientesActivos;
    }

    public void setClientesActivos(
            long clientesActivos
    ) {
        this.clientesActivos =
                clientesActivos;
    }


    public long getClientesInactivos() {
        return clientesInactivos;
    }

    public void setClientesInactivos(
            long clientesInactivos
    ) {
        this.clientesInactivos =
                clientesInactivos;
    }


    public long getClientesAdvertencia() {
        return clientesAdvertencia;
    }

    public void setClientesAdvertencia(
            long clientesAdvertencia
    ) {
        this.clientesAdvertencia =
                clientesAdvertencia;
    }


    public long getClientesMora() {
        return clientesMora;
    }

    public void setClientesMora(
            long clientesMora
    ) {
        this.clientesMora =
                clientesMora;
    }


    public List<ClienteResumenDto> getClientes() {
        return clientes;
    }

    public void setClientes(
            List<ClienteResumenDto> clientes
    ) {
        this.clientes =
                clientes;
    }
}