document.addEventListener(
    "DOMContentLoaded",
    function () {

        /* =====================================================
           CREDINEYRA
           DASHBOARD ADMINISTRADOR
           ===================================================== */


        /* =====================================================
           SIDEBAR - MENÚ LATERAL EN CELULAR
           ===================================================== */

        const sidebar =
            document.getElementById("sidebar");

        const botonMenu =
            document.getElementById("botonMenu");

        const botonMasMobile =
            document.getElementById("botonMasMobile");

        const overlay =
            document.getElementById("sidebarOverlay");


        /* =====================================================
           ABRIR MENÚ
           ===================================================== */

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

            /*
             * Evita que la página del fondo
             * se mueva mientras el menú está abierto.
             */
            document.body.style.overflow =
                "hidden";

        }


        /* =====================================================
           CERRAR MENÚ
           ===================================================== */

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


        /* =====================================================
           BOTÓN HAMBURGUESA SUPERIOR
           ===================================================== */

        if (botonMenu) {

            botonMenu.addEventListener(
                "click",
                abrirMenu
            );

        }


        /* =====================================================
           BOTÓN "MÁS" DEL MENÚ INFERIOR
           ===================================================== */

        if (botonMasMobile) {

            botonMasMobile.addEventListener(
                "click",
                abrirMenu
            );

        }


        /* =====================================================
           CERRAR MENÚ TOCANDO EL FONDO
           ===================================================== */

        if (overlay) {

            overlay.addEventListener(
                "click",
                cerrarMenu
            );

        }



        /* =====================================================
           FECHA Y HORA
           ===================================================== */

        const fechaElemento =
            document.getElementById(
                "fechaActual"
            );

        const horaElemento =
            document.getElementById(
                "horaActual"
            );


        function actualizarFechaHora() {

            const ahora =
                new Date();


            /* FECHA */

            if (fechaElemento) {

                fechaElemento.textContent =
                    ahora.toLocaleDateString(
                        "es-PE",
                        {
                            weekday: "long",
                            day: "2-digit",
                            month: "long",
                            year: "numeric"
                        }
                    );

            }


            /* HORA */

            if (horaElemento) {

                horaElemento.textContent =
                    ahora.toLocaleTimeString(
                        "es-PE",
                        {
                            hour: "2-digit",
                            minute: "2-digit"
                        }
                    );

            }

        }


        /*
         * Mostrar inmediatamente.
         */
        actualizarFechaHora();


        /*
         * Actualizar cada 30 segundos.
         */
        setInterval(
            actualizarFechaHora,
            30000
        );



        /* =====================================================
           GRÁFICO DE PAGOS

           IMPORTANTE:
           Estos valores todavía son TEMPORALES.

           Después serán reemplazados por información
           real obtenida desde PagoService + Supabase.
           ===================================================== */

        const datosPagos = {


            /* =================================================
               SEMANA
               ================================================= */

            semana: {

                titulo:
                    "Pagos registrados durante la semana",

                etiquetas:
                    [
                        "Lun",
                        "Mar",
                        "Mié",
                        "Jue",
                        "Vie",
                        "Sáb",
                        "Dom"
                    ],

                valores:
                    [
                        5,
                        8,
                        6,
                        10,
                        9,
                        12,
                        7
                    ]

            },


            /* =================================================
               MES

               Aquí agrupamos los pagos
               por semanas del mes.
               ================================================= */

            mes: {

                titulo:
                    "Pagos registrados durante el mes",

                etiquetas:
                    [
                        "Sem 1",
                        "Sem 2",
                        "Sem 3",
                        "Sem 4",
                        "Sem 5"
                    ],

                valores:
                    [
                        18,
                        24,
                        17,
                        29,
                        21
                    ]

            },


            /* =================================================
               AÑO

               Aquí mostramos enero - diciembre.
               ================================================= */

            anio: {

                titulo:
                    "Pagos registrados durante el año",

                etiquetas:
                    [
                        "Ene",
                        "Feb",
                        "Mar",
                        "Abr",
                        "May",
                        "Jun",
                        "Jul",
                        "Ago",
                        "Sep",
                        "Oct",
                        "Nov",
                        "Dic"
                    ],

                valores:
                    [
                        8,
                        11,
                        9,
                        14,
                        12,
                        17,
                        20,
                        18,
                        15,
                        13,
                        10,
                        16
                    ]

            }

        };



        /* =====================================================
           ELEMENTOS DEL GRÁFICO
           ===================================================== */

        const graficoPagos =
            document.getElementById(
                "graficoPagos"
            );


        const tituloPeriodo =
            document.getElementById(
                "tituloPeriodoPagos"
            );


        const filtrosPago =
            document.querySelectorAll(
                ".filtro-pago"
            );



        /* =====================================================
           CREAR GRÁFICO
           ===================================================== */

        function renderizarGrafico(periodo) {

            if (!graficoPagos) {
                return;
            }


            const datos =
                datosPagos[periodo];


            if (!datos) {
                return;
            }


            /*
             * Limpiamos el gráfico anterior.
             */
            graficoPagos.innerHTML =
                "";


            /*
             * Cambiar descripción.
             */
            if (tituloPeriodo) {

                tituloPeriodo.textContent =
                    datos.titulo;

            }


            /*
             * Encontrar el valor más grande.
             *
             * Lo usamos para calcular proporcionalmente
             * la altura de cada barra.
             */
            const maximo =
                Math.max(
                    ...datos.valores,
                    1
                );


            /* =================================================
               GENERAR CADA BARRA
               ================================================= */

            datos.etiquetas.forEach(
                function (etiqueta, indice) {

                    const valor =
                        datos.valores[indice];


                    const porcentaje =
                        (valor / maximo) * 100;


                    const columna =
                        document.createElement(
                            "div"
                        );


                    columna.className =
                        "barra-columna";


                    columna.innerHTML = `

                        <div class="barra-area">

                            <div
                                class="barra"
                                style="height: ${porcentaje}%">

                                <span class="barra-valor">
                                    ${valor}
                                </span>

                            </div>

                        </div>

                        <span class="barra-etiqueta">
                            ${etiqueta}
                        </span>

                    `;


                    graficoPagos.appendChild(
                        columna
                    );

                }
            );

        }



        /* =====================================================
           BOTONES:
           SEMANA | MES | AÑO
           ===================================================== */

        filtrosPago.forEach(
            function (boton) {

                boton.addEventListener(
                    "click",
                    function () {

                        /*
                         * Primero quitamos la selección
                         * de todos los botones.
                         */

                        filtrosPago.forEach(
                            function (otroBoton) {

                                otroBoton
                                    .classList
                                    .remove(
                                        "activo"
                                    );

                            }
                        );


                        /*
                         * Activamos el botón seleccionado.
                         */

                        boton
                            .classList
                            .add(
                                "activo"
                            );


                        /*
                         * Obtenemos:
                         *
                         * semana
                         * mes
                         * anio
                         */

                        const periodo =
                            boton.dataset.periodo;


                        /*
                         * Volvemos a dibujar el gráfico.
                         */

                        renderizarGrafico(
                            periodo
                        );

                    }
                );

            }
        );


        /*
         * Cuando entra al dashboard
         * mostramos MES por defecto.
         */
        renderizarGrafico(
            "mes"
        );



        /* =====================================================
           TOAST
           MENSAJES TEMPORALES
           ===================================================== */

        const elementosProximos =
            document.querySelectorAll(
                "[data-proximo]"
            );


        const toast =
            document.getElementById(
                "toast"
            );


        const toastTexto =
            document.getElementById(
                "toastTexto"
            );


        let temporizadorToast;



        /* =====================================================
           MOSTRAR MENSAJE
           ===================================================== */

        function mostrarToast(texto) {

            if (!toast || !toastTexto) {
                return;
            }


            toastTexto.textContent =
                texto +
                " estará disponible en el siguiente paso.";


            toast.classList.add(
                "visible"
            );


            clearTimeout(
                temporizadorToast
            );


            temporizadorToast =
                setTimeout(
                    function () {

                        toast.classList.remove(
                            "visible"
                        );

                    },
                    2500
                );

        }



        /* =====================================================
           OPCIONES TODAVÍA NO CONECTADAS
           ===================================================== */

        elementosProximos.forEach(
            function (elemento) {

                elemento.addEventListener(
                    "click",
                    function (event) {

                        event.preventDefault();


                        /*
                         * Si estamos en celular
                         * y seleccionamos una opción
                         * desde el sidebar,
                         * cerramos el menú.
                         */

                        if (
                            window.innerWidth <= 800
                            &&
                            elemento.closest("#sidebar")
                        ) {

                            cerrarMenu();

                        }


                        mostrarToast(
                            elemento.dataset.proximo
                        );

                    }
                );

            }
        );



        /* =====================================================
           BUSCADOR GENERAL
           ===================================================== */

        const buscador =
            document.getElementById(
                "busquedaGeneral"
            );


        if (buscador) {

            buscador.addEventListener(
                "keydown",
                function (event) {

                    if (
                        event.key === "Enter"
                    ) {

                        event.preventDefault();


                        /*
                         * Por ahora todavía no hacemos
                         * la búsqueda real.
                         */

                        mostrarToast(
                            "Búsqueda general"
                        );

                    }

                }
            );

        }



        /* =====================================================
           TECLA ESCAPE
           CIERRA EL SIDEBAR EN CELULAR
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
           SI PASAMOS DE CELULAR A PC
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