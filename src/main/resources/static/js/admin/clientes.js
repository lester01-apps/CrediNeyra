document.addEventListener(
    "DOMContentLoaded",
    function () {

        /* =====================================================
           SIDEBAR MOBILE
           ===================================================== */

        const sidebar =
            document.getElementById("sidebar");

        const overlay =
            document.getElementById("sidebarOverlay");

        const botonMenu =
            document.getElementById("botonMenu");

        const botonMasMobile =
            document.getElementById("botonMasMobile");


        function abrirSidebar() {

            if (!sidebar || !overlay) {
                return;
            }

            sidebar.classList.add("abierto");

            overlay.classList.add("visible");

            document.body.style.overflow =
                "hidden";
        }


        function cerrarSidebar() {

            if (!sidebar || !overlay) {
                return;
            }

            sidebar.classList.remove("abierto");

            overlay.classList.remove("visible");

            document.body.style.overflow =
                "";
        }


        if (botonMenu) {

            botonMenu.addEventListener(
                "click",
                abrirSidebar
            );
        }


        if (botonMasMobile) {

            botonMasMobile.addEventListener(
                "click",
                abrirSidebar
            );
        }


        if (overlay) {

            overlay.addEventListener(
                "click",
                cerrarSidebar
            );
        }



        /* =====================================================
           FECHA
           ===================================================== */

        const fechaActual =
            document.getElementById(
                "fechaActual"
            );

        const horaActual =
            document.getElementById(
                "horaActual"
            );


        function actualizarFecha() {

            const ahora =
                new Date();


            if (fechaActual) {

                fechaActual.textContent =
                    ahora.toLocaleDateString(
                        "es-PE",
                        {
                            weekday: "long",
                            year: "numeric",
                            month: "long",
                            day: "numeric"
                        }
                    );
            }


            if (horaActual) {

                horaActual.textContent =
                    ahora.toLocaleTimeString(
                        "es-PE",
                        {
                            hour: "2-digit",
                            minute: "2-digit"
                        }
                    );
            }
        }


        actualizarFecha();

        setInterval(
            actualizarFecha,
            30000
        );



        /* =====================================================
           FILTROS
           ===================================================== */

        const buscador =
            document.getElementById(
                "buscarCliente"
            );

        const filtroEstado =
            document.getElementById(
                "filtroEstado"
            );

        const filtroSituacion =
            document.getElementById(
                "filtroSituacion"
            );

        const filtroOrigen =
            document.getElementById(
                "filtroOrigen"
            );

        const botonBuscar =
            document.getElementById(
                "botonBuscarClientes"
            );

        const botonLimpiar =
            document.getElementById(
                "botonLimpiarClientes"
            );

        const sinResultados =
            document.getElementById(
                "sinResultadosClientes"
            );

        const informacionPaginacion =
            document.getElementById(
                "informacionPaginacion"
            );

        const botonesPaginacion =
            document.getElementById(
                "botonesPaginacion"
            );


        const filas =
            Array.from(
                document.querySelectorAll(
                    ".cliente-fila"
                )
            );


        const registrosPorPagina = 8;

        let paginaActual = 1;

        let filasFiltradas = [];


        function normalizar(texto) {

            return String(texto || "")
                .normalize("NFD")
                .replace(
                    /[\u0300-\u036f]/g,
                    ""
                )
                .toLowerCase()
                .trim();
        }


        function aplicarFiltros() {

            const texto =
                normalizar(
                    buscador
                        ? buscador.value
                        : ""
                );

            const estado =
                filtroEstado
                    ? filtroEstado.value
                    : "";

            const situacion =
                filtroSituacion
                    ? filtroSituacion.value
                    : "";

            const origen =
                filtroOrigen
                    ? filtroOrigen.value
                    : "";


            filasFiltradas =
                filas.filter(
                    function (fila) {

                        const busquedaFila =
                            normalizar(
                                fila.dataset.busqueda
                            );

                        const estadoFila =
                            fila.dataset.estado || "";

                        const situacionFila =
                            fila.dataset.situacion || "";

                        const origenFila =
                            fila.dataset.origen || "";


                        const coincideTexto =
                            !texto
                            ||
                            busquedaFila.includes(
                                texto
                            );


                        const coincideEstado =
                            !estado
                            ||
                            estadoFila === estado;


                        const coincideSituacion =
                            !situacion
                            ||
                            situacionFila === situacion;


                        const coincideOrigen =
                            !origen
                            ||
                            origenFila === origen;


                        return coincideTexto
                            &&
                            coincideEstado
                            &&
                            coincideSituacion
                            &&
                            coincideOrigen;
                    }
                );


            paginaActual = 1;

            renderizarPagina();
        }


        function renderizarPagina() {

            filas.forEach(
                function (fila) {

                    fila.style.display =
                        "none";
                }
            );


            const total =
                filasFiltradas.length;


            const totalPaginas =
                Math.max(
                    1,
                    Math.ceil(
                        total
                        /
                        registrosPorPagina
                    )
                );


            if (
                paginaActual
                >
                totalPaginas
            ) {

                paginaActual =
                    totalPaginas;
            }


            const inicio =
                (
                    paginaActual - 1
                )
                *
                registrosPorPagina;


            const fin =
                inicio
                +
                registrosPorPagina;


            filasFiltradas
                .slice(
                    inicio,
                    fin
                )
                .forEach(
                    function (fila) {

                        fila.style.display =
                            "";
                    }
                );


            if (sinResultados) {

                sinResultados.hidden =
                    total !== 0;
            }


            if (informacionPaginacion) {

                if (total === 0) {

                    informacionPaginacion
                        .textContent =
                        "0 clientes";

                } else {

                    const desde =
                        inicio + 1;

                    const hasta =
                        Math.min(
                            fin,
                            total
                        );


                    informacionPaginacion
                        .textContent =
                        "Mostrando "
                        + desde
                        + " - "
                        + hasta
                        + " de "
                        + total;
                }
            }


            renderizarBotonesPaginacion(
                totalPaginas
            );
        }


        function renderizarBotonesPaginacion(
            totalPaginas
        ) {

            if (!botonesPaginacion) {
                return;
            }


            botonesPaginacion.innerHTML =
                "";


            for (
                let pagina = 1;
                pagina <= totalPaginas;
                pagina++
            ) {

                const boton =
                    document.createElement(
                        "button"
                    );


                boton.type =
                    "button";


                boton.className =
                    "boton-pagina";


                if (
                    pagina
                    ===
                    paginaActual
                ) {

                    boton.classList.add(
                        "activa"
                    );
                }


                boton.textContent =
                    pagina;


                boton.addEventListener(
                    "click",
                    function () {

                        paginaActual =
                            pagina;

                        renderizarPagina();
                    }
                );


                botonesPaginacion
                    .appendChild(
                        boton
                    );
            }
        }


        if (botonBuscar) {

            botonBuscar.addEventListener(
                "click",
                aplicarFiltros
            );
        }


        if (buscador) {

            buscador.addEventListener(
                "input",
                aplicarFiltros
            );
        }


        if (filtroEstado) {

            filtroEstado.addEventListener(
                "change",
                aplicarFiltros
            );
        }


        if (filtroSituacion) {

            filtroSituacion.addEventListener(
                "change",
                aplicarFiltros
            );
        }


        if (filtroOrigen) {

            filtroOrigen.addEventListener(
                "change",
                aplicarFiltros
            );
        }


        if (botonLimpiar) {

            botonLimpiar.addEventListener(
                "click",
                function () {

                    if (buscador) {
                        buscador.value = "";
                    }

                    if (filtroEstado) {
                        filtroEstado.value = "";
                    }

                    if (filtroSituacion) {
                        filtroSituacion.value = "";
                    }

                    if (filtroOrigen) {
                        filtroOrigen.value = "";
                    }


                    aplicarFiltros();
                }
            );
        }



        /* =====================================================
           MODALES
           ===================================================== */

        const modalContacto =
            document.getElementById(
                "modalContacto"
            );

        const modalEstado =
            document.getElementById(
                "modalEstado"
            );


        function abrirModal(modal) {

            if (!modal) {
                return;
            }


            modal.classList.add(
                "visible"
            );


            document.body.style.overflow =
                "hidden";
        }


        function cerrarModales() {

            document
                .querySelectorAll(
                    ".modal-fondo"
                )
                .forEach(
                    function (modal) {

                        modal.classList.remove(
                            "visible"
                        );
                    }
                );


            document.body.style.overflow =
                "";
        }


        document
            .querySelectorAll(
                "[data-cerrar-modal]"
            )
            .forEach(
                function (boton) {

                    boton.addEventListener(
                        "click",
                        cerrarModales
                    );
                }
            );


        document
            .querySelectorAll(
                ".modal-fondo"
            )
            .forEach(
                function (modal) {

                    modal.addEventListener(
                        "click",
                        function (event) {

                            if (
                                event.target
                                ===
                                modal
                            ) {

                                cerrarModales();
                            }
                        }
                    );
                }
            );



        /* =====================================================
           TOAST
           ===================================================== */

        const toast =
            document.getElementById(
                "toastClientes"
            );

        const toastTexto =
            document.getElementById(
                "toastClientesTexto"
            );

        let temporizadorToast;


        function mostrarToast(
            mensaje
        ) {

            if (
                !toast
                ||
                !toastTexto
            ) {

                return;
            }


            clearTimeout(
                temporizadorToast
            );


            toastTexto.textContent =
                mensaje;


            toast.classList.add(
                "visible"
            );


            temporizadorToast =
                setTimeout(
                    function () {

                        toast.classList.remove(
                            "visible"
                        );

                    },
                    2300
                );
        }



        /* =====================================================
           CONTACTAR
           ===================================================== */

        let telefonoContacto = "";

        let emailContacto = "";

        let nombreContacto = "";


        document
            .querySelectorAll(
                ".js-contactar"
            )
            .forEach(
                function (boton) {

                    boton.addEventListener(
                        "click",
                        function () {

                            nombreContacto =
                                boton.dataset.nombre
                                ||
                                "Cliente";


                            telefonoContacto =
                                boton.dataset.telefono
                                ||
                                "";


                            emailContacto =
                                boton.dataset.email
                                ||
                                "";


                            const nombre =
                                document.getElementById(
                                    "contactoNombre"
                                );


                            const telefono =
                                document.getElementById(
                                    "contactoTelefono"
                                );


                            const email =
                                document.getElementById(
                                    "contactoEmail"
                                );


                            if (nombre) {

                                nombre.textContent =
                                    nombreContacto;
                            }


                            if (telefono) {

                                telefono.textContent =
                                    telefonoContacto
                                    ||
                                    "No registrado";
                            }


                            if (email) {

                                email.textContent =
                                    emailContacto
                                    ||
                                    "No registrado";
                            }


                            abrirModal(
                                modalContacto
                            );
                        }
                    );
                }
            );



        const btnWhatsApp =
            document.getElementById(
                "btnWhatsApp"
            );


        if (btnWhatsApp) {

            btnWhatsApp.addEventListener(
                "click",
                function () {

                    if (!telefonoContacto) {

                        mostrarToast(
                            "El cliente no tiene teléfono registrado."
                        );

                        return;
                    }


                    let numero =
                        telefonoContacto
                            .replace(
                                /\D/g,
                                ""
                            );


                    /*
                     * Perú:
                     * si se guardó solamente
                     * el celular de 9 dígitos,
                     * agregamos +51.
                     */
                    if (
                        numero.length
                        ===
                        9
                    ) {

                        numero =
                            "51"
                            +
                            numero;
                    }


                    const mensaje =
                        "Hola "
                        +
                        nombreContacto
                        +
                        ", te contactamos de crediNeyra.";


                    window.open(
                        "https://wa.me/"
                        +
                        numero
                        +
                        "?text="
                        +
                        encodeURIComponent(
                            mensaje
                        ),
                        "_blank",
                        "noopener,noreferrer"
                    );
                }
            );
        }



        const btnCorreo =
            document.getElementById(
                "btnCorreo"
            );


        if (btnCorreo) {

            btnCorreo.addEventListener(
                "click",
                function () {

                    if (!emailContacto) {

                        mostrarToast(
                            "El cliente no tiene correo registrado."
                        );

                        return;
                    }


                    window.location.href =
                        "mailto:"
                        +
                        emailContacto
                        +
                        "?subject="
                        +
                        encodeURIComponent(
                            "crediNeyra - Comunicación"
                        );
                }
            );
        }



        const btnCopiarTelefono =
            document.getElementById(
                "btnCopiarTelefono"
            );


        if (btnCopiarTelefono) {

            btnCopiarTelefono.addEventListener(
                "click",
                async function () {

                    if (!telefonoContacto) {

                        mostrarToast(
                            "El cliente no tiene teléfono registrado."
                        );

                        return;
                    }


                    try {

                        await navigator
                            .clipboard
                            .writeText(
                                telefonoContacto
                            );


                        mostrarToast(
                            "Teléfono copiado correctamente."
                        );


                    } catch (error) {

                        mostrarToast(
                            "No se pudo copiar el teléfono."
                        );
                    }
                }
            );
        }



        /* =====================================================
           CAMBIAR ESTADO
           ===================================================== */

        document
            .querySelectorAll(
                ".js-cambiar-estado"
            )
            .forEach(
                function (boton) {

                    boton.addEventListener(
                        "click",
                        function () {

                            const id =
                                boton.dataset.id;


                            const codigo =
                                boton.dataset.codigo
                                ||
                                "";


                            const nombre =
                                boton.dataset.nombre
                                ||
                                "Cliente";


                            const estado =
                                boton.dataset.estado;


                            const nuevoEstado =
                                estado === "ACTIVO"
                                    ? "INACTIVO"
                                    : "ACTIVO";


                            const descripcion =
                                document.getElementById(
                                    "estadoDescripcion"
                                );


                            const actual =
                                document.getElementById(
                                    "estadoActual"
                                );


                            const nuevo =
                                document.getElementById(
                                    "estadoNuevo"
                                );


                            const input =
                                document.getElementById(
                                    "inputNuevoEstado"
                                );


                            const formulario =
                                document.getElementById(
                                    "formCambiarEstado"
                                );


                            if (descripcion) {

                                descripcion.textContent =
                                    codigo
                                    +
                                    " - "
                                    +
                                    nombre;
                            }


                            if (actual) {

                                actual.textContent =
                                    estado;
                            }


                            if (nuevo) {

                                nuevo.textContent =
                                    nuevoEstado;
                            }


                            if (input) {

                                input.value =
                                    nuevoEstado;
                            }


                            if (formulario) {

                                formulario.action =
                                    "/admin/clientes/"
                                    +
                                    id
                                    +
                                    "/estado";
                            }


                            abrirModal(
                                modalEstado
                            );
                        }
                    );
                }
            );



        /* =====================================================
           ESCAPE
           ===================================================== */

        document.addEventListener(
            "keydown",
            function (event) {

                if (
                    event.key
                    ===
                    "Escape"
                ) {

                    cerrarModales();

                    cerrarSidebar();
                }
            }
        );



        /* =====================================================
           RESIZE
           ===================================================== */

        window.addEventListener(
            "resize",
            function () {

                if (
                    window.innerWidth
                    >
                    850
                ) {

                    cerrarSidebar();
                }
            }
        );



        /* =====================================================
           INICIO
           ===================================================== */

        filasFiltradas =
            [...filas];


        renderizarPagina();

    }
);