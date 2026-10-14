document.addEventListener(
    "DOMContentLoaded",
    function () {

        /* =====================================================
           SIDEBAR
           ===================================================== */

        const sidebar =
            document.getElementById(
                "sidebar"
            );

        const overlay =
            document.getElementById(
                "sidebarOverlay"
            );

        const botonMenu =
            document.getElementById(
                "botonMenu"
            );


        function abrirSidebar() {

            if (!sidebar || !overlay) {
                return;
            }

            sidebar.classList.add(
                "abierto"
            );

            overlay.classList.add(
                "visible"
            );

            document.body.style.overflow =
                "hidden";
        }


        function cerrarSidebar() {

            if (!sidebar || !overlay) {
                return;
            }

            sidebar.classList.remove(
                "abierto"
            );

            overlay.classList.remove(
                "visible"
            );

            document.body.style.overflow =
                "";
        }


        if (botonMenu) {

            botonMenu.addEventListener(
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
           TOAST
           ===================================================== */

        const toast =
            document.getElementById(
                "toastDetalle"
            );

        const toastTexto =
            document.getElementById(
                "toastDetalleTexto"
            );

        let temporizadorToast;


        function mostrarToast(
            mensaje
        ) {

            if (!toast || !toastTexto) {
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
           MODALES
           ===================================================== */

        const modalContacto =
            document.getElementById(
                "modalContactoDetalle"
            );

        const modalEstado =
            document.getElementById(
                "modalEstadoDetalle"
            );


        function abrirModal(
            modal
        ) {

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
                "[data-cerrar-modal-detalle]"
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
           CONTACTAR
           ===================================================== */

        let telefonoContacto = "";

        let emailContacto = "";

        let nombreContacto = "";


        const botonContactar =
            document.querySelector(
                ".js-contactar-detalle"
            );


        if (botonContactar) {

            botonContactar.addEventListener(
                "click",
                function () {

                    nombreContacto =
                        botonContactar
                            .dataset
                            .nombre
                        ||
                        "Cliente";


                    telefonoContacto =
                        botonContactar
                            .dataset
                            .telefono
                        ||
                        "";


                    emailContacto =
                        botonContactar
                            .dataset
                            .email
                        ||
                        "";


                    const nombre =
                        document.getElementById(
                            "contactoNombreDetalle"
                        );


                    const telefono =
                        document.getElementById(
                            "contactoTelefonoDetalle"
                        );


                    const email =
                        document.getElementById(
                            "contactoEmailDetalle"
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



        /* WHATSAPP */

        const btnWhatsApp =
            document.getElementById(
                "btnWhatsAppDetalle"
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



        /* CORREO */

        const btnCorreo =
            document.getElementById(
                "btnCorreoDetalle"
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



        /* COPIAR */

        const btnCopiar =
            document.getElementById(
                "btnCopiarDetalle"
            );


        if (btnCopiar) {

            btnCopiar.addEventListener(
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

        const botonEstado =
            document.querySelector(
                ".js-cambiar-estado-detalle"
            );


        if (botonEstado) {

            botonEstado.addEventListener(
                "click",
                function () {

                    const id =
                        botonEstado
                            .dataset
                            .id;


                    const codigo =
                        botonEstado
                            .dataset
                            .codigo
                        ||
                        "";


                    const nombre =
                        botonEstado
                            .dataset
                            .nombre
                        ||
                        "Cliente";


                    const estado =
                        botonEstado
                            .dataset
                            .estado;


                    const nuevoEstado =
                        estado === "ACTIVO"
                            ? "INACTIVO"
                            : "ACTIVO";


                    const descripcion =
                        document.getElementById(
                            "estadoDescripcionDetalle"
                        );


                    const actual =
                        document.getElementById(
                            "estadoActualDetalle"
                        );


                    const nuevo =
                        document.getElementById(
                            "estadoNuevoDetalle"
                        );


                    const input =
                        document.getElementById(
                            "inputEstadoDetalle"
                        );


                    const formulario =
                        document.getElementById(
                            "formEstadoDetalle"
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



        /* =====================================================
           FUNCIONES AUN NO IMPLEMENTADAS
           ===================================================== */

        document
            .querySelectorAll(
                "[data-proximo]"
            )
            .forEach(
                function (boton) {

                    boton.addEventListener(
                        "click",
                        function () {

                            const accion =
                                boton.dataset.proximo
                                ||
                                "Esta función";


                            mostrarToast(
                                accion
                                +
                                " será implementado en el módulo correspondiente."
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

    }
);