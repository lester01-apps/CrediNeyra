document.addEventListener(
    "DOMContentLoaded",
    function () {

        const telefono =
            document.getElementById("telefono");

        const observaciones =
            document.getElementById("observaciones");

        const contador =
            document.getElementById("contadorObservaciones");

        const formulario =
            document.getElementById("formEditarCliente");

        const botonGuardar =
            document.getElementById("btnGuardar");


        /* SOLO NÚMEROS */

        if (telefono) {

            telefono.addEventListener(
                "input",
                function () {

                    telefono.value =
                        telefono.value.replace(/\D/g, "");
                }
            );
        }


        /* CONTADOR */

        function actualizarContador() {

            if (!observaciones || !contador) {
                return;
            }

            contador.textContent =
                observaciones.value.length
                + " / 200";
        }


        if (observaciones) {

            observaciones.addEventListener(
                "input",
                actualizarContador
            );

            actualizarContador();
        }


        /* EVITAR DOBLE ENVÍO */

        if (formulario) {

            formulario.addEventListener(
                "submit",
                function () {

                    if (!botonGuardar) {
                        return;
                    }

                    botonGuardar.disabled = true;

                    const texto =
                        botonGuardar.querySelector("span");

                    if (texto) {
                        texto.textContent = "Guardando...";
                    }
                }
            );
        }


        /* SIDEBAR */

        const sidebar =
            document.getElementById("sidebar");

        const overlay =
            document.getElementById("sidebarOverlay");

        const botonMenu =
            document.getElementById("botonMenu");


        function abrirMenu() {

            if (!sidebar || !overlay) {
                return;
            }

            sidebar.classList.add("abierto");
            overlay.classList.add("visible");
            document.body.style.overflow = "hidden";
        }


        function cerrarMenu() {

            if (!sidebar || !overlay) {
                return;
            }

            sidebar.classList.remove("abierto");
            overlay.classList.remove("visible");
            document.body.style.overflow = "";
        }


        if (botonMenu) {
            botonMenu.addEventListener("click", abrirMenu);
        }

        if (overlay) {
            overlay.addEventListener("click", cerrarMenu);
        }


        document.addEventListener(
            "keydown",
            function (event) {

                if (event.key === "Escape") {
                    cerrarMenu();
                }
            }
        );
    }
);