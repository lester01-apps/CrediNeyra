document.addEventListener(
    "DOMContentLoaded",
    function () {

        /* =====================================================
           CREDINEYRA
           DASHBOARD ADMINISTRADOR
           ===================================================== */


        /* =====================================================
           SIDEBAR MOBILE
           ===================================================== */

        const sidebar =
            document.getElementById("sidebar");

        const botonMenu =
            document.getElementById("botonMenu");

        const botonMasMobile =
            document.getElementById("botonMasMobile");

        const overlay =
            document.getElementById("sidebarOverlay");


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


        if (botonMasMobile) {

            botonMasMobile.addEventListener(
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


        actualizarFechaHora();


        setInterval(
            actualizarFechaHora,
            30000
        );



        /* =====================================================
           DATOS REALES DE SUPABASE
           ===================================================== */

        const datosDashboard =
            document.getElementById(
                "datosDashboard"
            );


        function convertirLista(
            valor
        ) {

            if (!valor) {

                return [];

            }


            try {

                return JSON.parse(
                    valor
                );

            } catch (error) {

                console.error(
                    "No se pudo convertir información del dashboard:",
                    valor
                );


                return [];

            }

        }



        let pagosSemana = [];
        let pagosMes = [];
        let pagosAnio = [];


        if (datosDashboard) {

            pagosSemana =
                convertirLista(
                    datosDashboard.dataset.pagosSemana
                );


            pagosMes =
                convertirLista(
                    datosDashboard.dataset.pagosMes
                );


            pagosAnio =
                convertirLista(
                    datosDashboard.dataset.pagosAnio
                );

        }



        /* =====================================================
           CONFIGURACIÓN REAL DEL GRÁFICO
           ===================================================== */

        const datosPagos = {


            semana: {

                titulo:
                    "Pagos aprobados durante la semana",

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
                    pagosSemana

            },


            mes: {

                titulo:
                    "Pagos aprobados durante el mes",

                etiquetas:
                    [
                        "Sem 1",
                        "Sem 2",
                        "Sem 3",
                        "Sem 4",
                        "Sem 5"
                    ],

                valores:
                    pagosMes

            },


            anio: {

                titulo:
                    "Pagos aprobados durante el año",

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
                    pagosAnio

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
           FORMATO MONEDA
           ===================================================== */

        const formatoMoneda =
            new Intl.NumberFormat(
                "es-PE",
                {
                    style: "currency",
                    currency: "PEN",
                    minimumFractionDigits: 0,
                    maximumFractionDigits: 0
                }
            );



        /* =====================================================
           RENDERIZAR GRÁFICO
           ===================================================== */

        function renderizarGrafico(
            periodo
        ) {

            if (!graficoPagos) {
                return;
            }


            const datos =
                datosPagos[periodo];


            if (!datos) {
                return;
            }


            graficoPagos.innerHTML =
                "";


            if (tituloPeriodo) {

                tituloPeriodo.textContent =
                    datos.titulo;

            }


            /*
             * Convertimos todo a número.
             */
            const valores =
                datos.valores.map(
                    function (valor) {

                        const numero =
                            Number(valor);


                        return Number.isFinite(numero)
                            ? numero
                            : 0;

                    }
                );


            /*
             * Si faltara algún dato,
             * completamos con ceros.
             */
            while (
                valores.length <
                datos.etiquetas.length
            ) {

                valores.push(0);

            }


            const maximo =
                Math.max(
                    ...valores,
                    1
                );


            datos.etiquetas.forEach(
                function (
                    etiqueta,
                    indice
                ) {

                    const valor =
                        valores[indice] || 0;


                    const porcentaje =
                        valor === 0
                            ? 0
                            : (valor / maximo) * 100;


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
                                    ${formatoMoneda.format(valor)}
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
           FILTROS:
           SEMANA | MES | AÑO
           ===================================================== */

        filtrosPago.forEach(
            function (boton) {

                boton.addEventListener(
                    "click",
                    function () {

                        filtrosPago.forEach(
                            function (
                                otroBoton
                            ) {

                                otroBoton
                                    .classList
                                    .remove(
                                        "activo"
                                    );

                            }
                        );


                        boton
                            .classList
                            .add(
                                "activo"
                            );


                        renderizarGrafico(
                            boton.dataset.periodo
                        );

                    }
                );

            }
        );


        /*
         * Mes será la vista inicial.
         */
        renderizarGrafico(
            "mes"
        );



        /* =====================================================
           TOAST
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


        function mostrarToast(
            texto
        ) {

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
           OPCIONES NO CONECTADAS TODAVÍA
           ===================================================== */

        elementosProximos.forEach(
            function (elemento) {

                elemento.addEventListener(
                    "click",
                    function (event) {

                        event.preventDefault();


                        if (
                            window.innerWidth <= 800
                            &&
                            elemento.closest(
                                "#sidebar"
                            )
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
           BUSCADOR
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


                        mostrarToast(
                            "Búsqueda general"
                        );

                    }

                }
            );

        }



        /* =====================================================
           ESCAPE CIERRA SIDEBAR
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
           PASAR DE CELULAR A PC
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