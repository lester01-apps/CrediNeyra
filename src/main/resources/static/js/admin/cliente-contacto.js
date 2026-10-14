document.addEventListener(
    "DOMContentLoaded",
    function () {

        /* =====================================================
           ELEMENTOS
           ===================================================== */

        const modal =
            document.getElementById(
                "contactoModal"
            );


        if (!modal) {
            return;
        }


        const cerrar =
            document.getElementById(
                "contactoCerrar"
            );


        const cancelar =
            document.getElementById(
                "cancelarContacto"
            );


        const canalWhatsApp =
            document.getElementById(
                "canalWhatsApp"
            );


        const canalCorreo =
            document.getElementById(
                "canalCorreo"
            );


        const grupoAsunto =
            document.getElementById(
                "grupoAsunto"
            );


        const asunto =
            document.getElementById(
                "contactoAsunto"
            );


        const mensaje =
            document.getElementById(
                "contactoMensaje"
            );


        const formulario =
            document.getElementById(
                "formContactoCorreo"
            );


        const volver =
            document.getElementById(
                "contactoVolver"
            );


        const accionWhatsApp =
            document.getElementById(
                "accionWhatsApp"
            );


        const accionCorreo =
            document.getElementById(
                "accionCorreo"
            );


        const regenerar =
            document.getElementById(
                "regenerarMensaje"
            );


        const copiar =
            document.getElementById(
                "copiarMensajeContacto"
            );


        const contador =
            document.getElementById(
                "contadorContacto"
            );


        const toast =
            document.getElementById(
                "contactoToast"
            );


        const toastTexto =
            document.getElementById(
                "contactoToastTexto"
            );


        let clienteActual = null;

        let canalActual =
            "WHATSAPP";

        let temporizadorToast;


        /* =====================================================
           HELPERS
           ===================================================== */

        function textoSeguro(
            valor,
            alternativo = "-"
        ) {

            if (
                valor === null
                ||
                valor === undefined
                ||
                String(valor).trim() === ""
            ) {

                return alternativo;
            }


            return String(valor)
                .trim();
        }


        function situacionLegible(
            situacion
        ) {

            const valores = {

                SIN_CREDITO:
                    "Sin crédito",

                AL_DIA:
                    "Al día",

                POR_VENCER:
                    "Por vencer",

                EN_GRACIA:
                    "En período de gracia",

                EN_MORA:
                    "En mora"
            };


            return valores[situacion]
                ||
                textoSeguro(
                    situacion
                );
        }


        function formatearFecha(
            fecha
        ) {

            if (!fecha) {
                return "Sin fecha registrada";
            }


            const partes =
                fecha.split("-");


            if (partes.length !== 3) {
                return fecha;
            }


            return partes[2]
                + "/"
                + partes[1]
                + "/"
                + partes[0];
        }


        function formatearMonto(
            monto
        ) {

            const numero =
                Number(monto);


            if (!Number.isFinite(numero)) {
                return "0.00";
            }


            return numero.toLocaleString(
                "es-PE",
                {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                }
            );
        }


        function mostrarToast(
            texto
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
                texto;


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
                    2400
                );
        }


        /* =====================================================
           ASUNTO AUTOMATICO
           ===================================================== */

        function construirAsunto(
            situacion
        ) {

            switch (situacion) {

                case "EN_MORA":

                    return "Aviso de pago pendiente - crediNeyra";


                case "EN_GRACIA":

                    return "Aviso de cuota en período de gracia - crediNeyra";


                case "POR_VENCER":

                    return "Recordatorio de próxima cuota - crediNeyra";


                case "AL_DIA":

                    return "Información de tu crédito - crediNeyra";


                default:

                    return "Comunicación de crediNeyra";
            }
        }


        /* =====================================================
           MENSAJE EMPRESARIAL
           ===================================================== */

        function construirMensaje(
            cliente
        ) {

            const nombre =
                textoSeguro(
                    cliente.nombre,
                    "cliente"
                );


            const dni =
                textoSeguro(
                    cliente.dni,
                    "No registrado"
                );


            const codigo =
                textoSeguro(
                    cliente.codigo,
                    "-"
                );


            const situacion =
                textoSeguro(
                    cliente.situacion,
                    "SIN_CREDITO"
                );


            const monto =
                formatearMonto(
                    cliente.monto
                );


            const fecha =
                formatearFecha(
                    cliente.fecha
                );


            let bloquePago = "";


            if (
                situacion !== "SIN_CREDITO"
                &&
                cliente.fecha
            ) {

                bloquePago =
                    "\n"
                    + "💳 *Información de tu próxima cuota*\n"
                    + "💰 Monto pendiente: *S/ "
                    + monto
                    + "*\n"
                    + "📅 Fecha de vencimiento: *"
                    + fecha
                    + "*\n"
                    + "📊 Situación actual: *"
                    + situacionLegible(
                        situacion
                    )
                    + "*\n";
            }


            let textoSituacion = "";


            switch (situacion) {

                case "POR_VENCER":

                    textoSituacion =
                        "\n🔔 Queremos recordarte que tu próxima cuota está próxima a vencer. "
                        + "Te recomendamos realizar el pago dentro de la fecha indicada "
                        + "para mantener tu crédito al día.\n";

                    break;


                case "EN_GRACIA":

                    textoSituacion =
                        "\n⏳ Hemos identificado que tu cuota ya alcanzó su fecha de vencimiento "
                        + "y actualmente se encuentra dentro del período de gracia. "
                        + "Te recomendamos regularizarla cuanto antes.\n";

                    break;


                case "EN_MORA":

                    textoSituacion =
                        "\n⚠️ Hemos identificado una cuota pendiente que actualmente se encuentra en mora. "
                        + "Te pedimos regularizar el pago lo antes posible o comunicarte con nosotros "
                        + "para revisar tu situación.\n";

                    break;


                case "AL_DIA":

                    textoSituacion =
                        "\n✅ Actualmente tu situación se encuentra al día. "
                        + "Este mensaje es parte del seguimiento de tu crédito.\n";

                    break;


                default:

                    textoSituacion =
                        "\n📌 Nos comunicamos contigo como parte de la atención y seguimiento "
                        + "de clientes de crediNeyra.\n";
            }


            return (
                "👋 Hola, *"
                + nombre
                + "*.\n\n"

                + "Te saludamos de parte de *crediNeyra* 💙\n"
                + "Esperamos que te encuentres muy bien.\n\n"

                + "📌 *Datos de referencia*\n"
                + "👤 Cliente: *"
                + codigo
                + "*\n"
                + "🪪 DNI: *"
                + dni
                + "*\n"

                + bloquePago

                + textoSituacion

                + "\nSi ya realizaste el pago correspondiente, "
                + "puedes omitir este recordatorio o comunicarte con nosotros "
                + "para proceder con la validación de la operación.\n\n"

                + "Si tienes alguna consulta sobre tu crédito, cuota o pago, "
                + "estamos disponibles para ayudarte. 🤝\n\n"

                + "Muchas gracias por tu responsabilidad y por confiar en *crediNeyra*. 🙌\n\n"

                + "💙 *crediNeyra*\n"
                + "Gestión de créditos y seguimiento de pagos."
            );
        }


        /* =====================================================
           CARGAR CLIENTE EN PANEL
           ===================================================== */

        function cargarCliente(
            boton
        ) {

            clienteActual = {

                id:
                    boton.dataset.id,

                nombre:
                    textoSeguro(
                        boton.dataset.nombre,
                        "Cliente"
                    ),

                codigo:
                    textoSeguro(
                        boton.dataset.codigo
                    ),

                dni:
                    textoSeguro(
                        boton.dataset.dni
                    ),

                telefono:
                    textoSeguro(
                        boton.dataset.telefono,
                        ""
                    ),

                email:
                    textoSeguro(
                        boton.dataset.email,
                        ""
                    ),

                situacion:
                    textoSeguro(
                        boton.dataset.situacion,
                        "SIN_CREDITO"
                    ),

                fecha:
                    textoSeguro(
                        boton.dataset.fecha,
                        ""
                    ),

                monto:
                    textoSeguro(
                        boton.dataset.monto,
                        "0"
                    ),

                volver:
                    textoSeguro(
                        boton.dataset.volver,
                        "lista"
                    )
            };


            const nombreElemento =
                document.getElementById(
                    "contactoClienteNombre"
                );


            const codigoElemento =
                document.getElementById(
                    "contactoClienteCodigo"
                );


            const dniElemento =
                document.getElementById(
                    "contactoClienteDni"
                );


            const telefonoElemento =
                document.getElementById(
                    "contactoClienteTelefono"
                );


            const emailElemento =
                document.getElementById(
                    "contactoClienteEmail"
                );


            const montoElemento =
                document.getElementById(
                    "contactoMonto"
                );


            const fechaElemento =
                document.getElementById(
                    "contactoFecha"
                );


            const situacionElemento =
                document.getElementById(
                    "contactoSituacion"
                );


            const avatar =
                document.getElementById(
                    "contactoAvatar"
                );


            nombreElemento.textContent =
                clienteActual.nombre;


            codigoElemento.textContent =
                clienteActual.codigo;


            dniElemento.textContent =
                clienteActual.dni;


            telefonoElemento.textContent =
                clienteActual.telefono
                ||
                "No registrado";


            emailElemento.textContent =
                clienteActual.email
                ||
                "No registrado";


            montoElemento.textContent =
                clienteActual.fecha
                ? "S/ "
                    + formatearMonto(
                        clienteActual.monto
                    )
                : "Sin cuota";


            fechaElemento.textContent =
                clienteActual.fecha
                ? "Vence "
                    + formatearFecha(
                        clienteActual.fecha
                    )
                : "Sin cuota pendiente";


            situacionElemento.textContent =
                situacionLegible(
                    clienteActual.situacion
                );


            avatar.textContent =
                clienteActual.nombre
                    .charAt(0)
                    .toUpperCase();


            asunto.value =
                construirAsunto(
                    clienteActual.situacion
                );


            mensaje.value =
                construirMensaje(
                    clienteActual
                );


            volver.value =
                clienteActual.volver;


            formulario.action =
                "/admin/clientes/"
                + clienteActual.id
                + "/correo";


            actualizarContador();
        }


        /* =====================================================
           ABRIR / CERRAR
           ===================================================== */

        function abrirModal(
            boton
        ) {

            cargarCliente(
                boton
            );


            seleccionarCanal(
                "WHATSAPP"
            );


            modal.classList.add(
                "visible"
            );


            document.body.style.overflow =
                "hidden";
        }


        function cerrarModal() {

            modal.classList.remove(
                "visible"
            );


            document.body.style.overflow =
                "";
        }


        document
            .querySelectorAll(
                ".js-abrir-contacto"
            )
            .forEach(
                function (boton) {

                    boton.addEventListener(
                        "click",
                        function () {

                            abrirModal(
                                boton
                            );
                        }
                    );
                }
            );


        if (cerrar) {

            cerrar.addEventListener(
                "click",
                cerrarModal
            );
        }


        if (cancelar) {

            cancelar.addEventListener(
                "click",
                cerrarModal
            );
        }


        modal.addEventListener(
            "click",
            function (event) {

                if (
                    event.target
                    ===
                    modal
                ) {

                    cerrarModal();
                }
            }
        );


        /* =====================================================
           CANALES
           ===================================================== */

        function seleccionarCanal(
            canal
        ) {

            canalActual =
                canal;


            canalWhatsApp
                .classList
                .toggle(
                    "activo",
                    canal === "WHATSAPP"
                );


            canalCorreo
                .classList
                .toggle(
                    "activo",
                    canal === "CORREO"
                );


            /*
             * WhatsApp no necesita asunto.
             */
            grupoAsunto.hidden =
                canal !== "CORREO";


            accionWhatsApp.hidden =
                canal !== "WHATSAPP";


            accionCorreo.hidden =
                canal !== "CORREO";
        }


        canalWhatsApp.addEventListener(
            "click",
            function () {

                seleccionarCanal(
                    "WHATSAPP"
                );
            }
        );


        canalCorreo.addEventListener(
            "click",
            function () {

                seleccionarCanal(
                    "CORREO"
                );
            }
        );


        /* =====================================================
           REGENERAR MENSAJE
           ===================================================== */

        if (regenerar) {

            regenerar.addEventListener(
                "click",
                function () {

                    if (!clienteActual) {
                        return;
                    }


                    asunto.value =
                        construirAsunto(
                            clienteActual
                                .situacion
                        );


                    mensaje.value =
                        construirMensaje(
                            clienteActual
                        );


                    actualizarContador();


                    mostrarToast(
                        "Mensaje regenerado."
                    );
                }
            );
        }


        /* =====================================================
           CONTADOR
           ===================================================== */

        function actualizarContador() {

            if (!contador) {
                return;
            }


            contador.textContent =
                mensaje.value.length
                +
                " / 4000";
        }


        mensaje.addEventListener(
            "input",
            actualizarContador
        );


        /* =====================================================
           COPIAR
           ===================================================== */

        copiar.addEventListener(
            "click",
            async function () {

                if (!mensaje.value.trim()) {

                    mostrarToast(
                        "No hay mensaje para copiar."
                    );

                    return;
                }


                try {

                    await navigator
                        .clipboard
                        .writeText(
                            mensaje.value
                        );


                    mostrarToast(
                        "Mensaje copiado."
                    );


                } catch (error) {

                    mostrarToast(
                        "No se pudo copiar el mensaje."
                    );
                }
            }
        );


        /* =====================================================
           WHATSAPP
           ===================================================== */

        accionWhatsApp.addEventListener(
            "click",
            function () {

                if (!clienteActual) {
                    return;
                }


                if (
                    !clienteActual.telefono
                ) {

                    mostrarToast(
                        "El cliente no tiene teléfono registrado."
                    );

                    return;
                }


                if (
                    !mensaje.value.trim()
                ) {

                    mostrarToast(
                        "El mensaje no puede estar vacío."
                    );

                    return;
                }


                let numero =
                    clienteActual
                        .telefono
                        .replace(
                            /\D/g,
                            ""
                        );


                /*
                 * Número peruano móvil guardado
                 * normalmente con 9 dígitos.
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


                const url =
                    "https://wa.me/"
                    +
                    numero
                    +
                    "?text="
                    +
                    encodeURIComponent(
                        mensaje.value
                    );


                window.open(
                    url,
                    "_blank",
                    "noopener,noreferrer"
                );
            }
        );


        /* =====================================================
           VALIDAR CORREO
           ===================================================== */

        formulario.addEventListener(
            "submit",
            function (event) {

                if (
                    canalActual
                    !==
                    "CORREO"
                ) {

                    event.preventDefault();

                    return;
                }


                if (
                    !clienteActual
                    ||
                    !clienteActual.email
                ) {

                    event.preventDefault();


                    mostrarToast(
                        "El cliente no tiene correo registrado."
                    );


                    return;
                }


                if (
                    !asunto.value.trim()
                ) {

                    event.preventDefault();


                    mostrarToast(
                        "El asunto es obligatorio."
                    );


                    asunto.focus();

                    return;
                }


                if (
                    !mensaje.value.trim()
                ) {

                    event.preventDefault();


                    mostrarToast(
                        "El mensaje es obligatorio."
                    );


                    mensaje.focus();

                    return;
                }


                accionCorreo.disabled =
                    true;


                accionCorreo.textContent =
                    "Enviando...";
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
                    &&
                    modal.classList
                        .contains(
                            "visible"
                        )
                ) {

                    cerrarModal();
                }
            }
        );

    }
);