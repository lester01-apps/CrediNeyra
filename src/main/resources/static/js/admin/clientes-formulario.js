document.addEventListener(
    "DOMContentLoaded",
    function () {

        /* =====================================================
           SIDEBAR MOBILE
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


        function abrirMenu() {

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


        function cerrarMenu() {

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
                abrirMenu
            );
        }


        if (overlay) {

            overlay.addEventListener(
                "click",
                cerrarMenu
            );
        }



        /* =====================================================
           CAMPOS
           ===================================================== */

        const dni =
            document.getElementById(
                "dni"
            );


        const telefono =
            document.getElementById(
                "telefono"
            );


        const observaciones =
            document.getElementById(
                "observaciones"
            );


        const contadorObservaciones =
            document.getElementById(
                "contadorObservaciones"
            );


        const formulario =
            document.getElementById(
                "formNuevoCliente"
            );


        const botonGuardar =
            document.getElementById(
                "botonGuardarCliente"
            );



        /* =====================================================
           SOLO NUMEROS
           ===================================================== */

        function permitirSoloNumeros(
            input
        ) {

            if (!input) {
                return;
            }


            input.addEventListener(
                "input",
                function () {

                    input.value =
                        input.value.replace(
                            /\D/g,
                            ""
                        );
                }
            );
        }


        permitirSoloNumeros(
            dni
        );


        permitirSoloNumeros(
            telefono
        );



        /* =====================================================
           CONTADOR OBSERVACIONES
           ===================================================== */

        function actualizarContador() {

            if (
                !observaciones
                ||
                !contadorObservaciones
            ) {

                return;
            }


            contadorObservaciones.textContent =
                observaciones.value.length
                +
                " / 200";
        }


        if (observaciones) {

            observaciones.addEventListener(
                "input",
                actualizarContador
            );


            actualizarContador();
        }



        /* =====================================================
           EVITAR DOBLE ENVIO
           ===================================================== */

        if (formulario) {

            formulario.addEventListener(
                "submit",
                function () {

                    if (botonGuardar) {

                        botonGuardar.disabled =
                            true;


                        const texto =
                            botonGuardar.querySelector(
                                "span"
                            );


                        if (texto) {

                            texto.textContent =
                                "Registrando...";
                        }
                    }
                }
            );
        }



        /* =====================================================
           ESCAPE
           ===================================================== */

        document.addEventListener(
            "keydown",
            function (event) {

                if (
                    event.key === "Escape"
                ) {

                    cerrarMenu();
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
                    window.innerWidth > 800
                ) {

                    cerrarMenu();
                }
            }
        );

    }
);