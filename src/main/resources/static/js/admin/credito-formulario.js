document.addEventListener(
    "DOMContentLoaded",
    () => {

        const form =
            document.getElementById(
                "creditoForm"
            );

        if (!form) {
            return;
        }


        let simulacionValida = false;

        let timer = null;


        /* =====================================================
           CLIENTE
           ===================================================== */

        const idCliente =
            document.getElementById(
                "idCliente"
            );

        const buscarCliente =
            document.getElementById(
                "buscarCliente"
            );

        const resultados =
            document.getElementById(
                "clienteResultados"
            );

        const opciones =
            Array.from(
                document.querySelectorAll(
                    ".cliente-resultado"
                )
            );

        const sinResultados =
            document.getElementById(
                "clienteSinResultados"
            );

        const seleccionado =
            document.getElementById(
                "clienteSeleccionado"
            );

        const seleccionadoNombre =
            document.getElementById(
                "clienteSeleccionadoNombre"
            );

        const seleccionadoDatos =
            document.getElementById(
                "clienteSeleccionadoDatos"
            );

        const quitarCliente =
            document.getElementById(
                "quitarCliente"
            );


        function seleccionarCliente(
            opcion
        ) {

            idCliente.value =
                opcion.dataset.id;


            buscarCliente.value =
                opcion.dataset.nombre;


            seleccionadoNombre.textContent =
                opcion.dataset.nombre;


            seleccionadoDatos.textContent =
                opcion.dataset.codigo
                +
                " · DNI "
                +
                opcion.dataset.dni;


            seleccionado.classList.add(
                "visible"
            );


            resultados.classList.remove(
                "visible"
            );


            actualizarBoton();
        }


        function cargarClienteInicial() {

            if (!idCliente.value) {
                return;
            }


            const opcion =
                opciones.find(
                    item =>
                        String(item.dataset.id)
                        ===
                        String(idCliente.value)
                );


            if (opcion) {

                seleccionarCliente(
                    opcion
                );
            }
        }


        function filtrarClientes() {

            const consulta =
                normalizar(
                    buscarCliente.value
                );


            let encontrados =
                0;


            opciones.forEach(
                opcion => {

                    const contenido =
                        normalizar(
                            opcion.dataset.nombre
                            + " "
                            + opcion.dataset.dni
                            + " "
                            + opcion.dataset.codigo
                        );


                    const mostrar =
                        consulta === ""
                        ||
                        contenido.includes(
                            consulta
                        );


                    opcion.style.display =
                        mostrar
                            ? "flex"
                            : "none";


                    if (mostrar) {
                        encontrados++;
                    }
                }
            );


            sinResultados.style.display =
                encontrados === 0
                    ? "block"
                    : "none";
        }


        buscarCliente.addEventListener(
            "focus",
            () => {

                resultados.classList.add(
                    "visible"
                );

                filtrarClientes();
            }
        );


        buscarCliente.addEventListener(
            "input",
            () => {

                idCliente.value =
                    "";


                seleccionado.classList.remove(
                    "visible"
                );


                resultados.classList.add(
                    "visible"
                );


                filtrarClientes();

                actualizarBoton();
            }
        );


        opciones.forEach(
            opcion => {

                opcion.addEventListener(
                    "click",
                    () =>
                            seleccionarCliente(
                                    opcion
                            )
                );
            }
        );


        quitarCliente.addEventListener(
            "click",
            () => {

                idCliente.value =
                    "";

                buscarCliente.value =
                    "";

                seleccionado.classList.remove(
                    "visible"
                );

                resultados.classList.add(
                    "visible"
                );

                buscarCliente.focus();

                filtrarClientes();

                actualizarBoton();
            }
        );


        document.addEventListener(
            "click",
            event => {

                if (
                    !resultados.contains(
                            event.target
                    )
                    &&
                    event.target
                            !== buscarCliente
                ) {

                    resultados.classList.remove(
                        "visible"
                    );
                }
            }
        );


        /* =====================================================
           ARCHIVO
           ===================================================== */

        const archivo =
            document.getElementById(
                "archivoGarantia"
            );

        const archivoBox =
            document.getElementById(
                "archivoGarantiaSeleccionado"
            );

        const archivoNombre =
            document.getElementById(
                "archivoNombre"
            );

        const archivoDatos =
            document.getElementById(
                "archivoDatos"
            );

        const quitarArchivo =
            document.getElementById(
                "quitarArchivo"
            );


        archivo.addEventListener(
            "change",
            () => {

                mostrarArchivo();

                actualizarGarantia();

                actualizarBoton();
            }
        );


        quitarArchivo.addEventListener(
            "click",
            () => {

                archivo.value =
                    "";

                mostrarArchivo();

                actualizarGarantia();

                actualizarBoton();
            }
        );


        function mostrarArchivo() {

            const seleccionado =
                archivo.files?.[0];


            if (!seleccionado) {

                archivoBox.classList.remove(
                    "visible"
                );

                return;
            }


            archivoNombre.textContent =
                seleccionado.name;


            archivoDatos.textContent =
                formatearPeso(
                    seleccionado.size
                );


            archivoBox.classList.add(
                "visible"
            );
        }


        /* =====================================================
           SIMULACIÓN
           ===================================================== */

        const simulacionCampos = [

            "capitalOriginal",
            "plazoMeses",
            "tasaInteres",
            "tipoCalculo",
            "periodicidadTasa",
            "frecuenciaPago",
            "fechaInicio",
            "fechaPrimerPago"

        ];


        simulacionCampos.forEach(
            id => {

                const campo =
                    document.getElementById(id);


                campo.addEventListener(
                    "input",
                    programar
                );


                campo.addEventListener(
                    "change",
                    programar
                );
            }
        );


        [
            "tipoGarantia",
            "valorEstimadoGarantia",
            "descripcionGarantia"
        ]
        .forEach(
            id => {

                const campo =
                    document.getElementById(id);


                campo.addEventListener(
                    "input",
                    () => {

                        actualizarGarantia();

                        actualizarBoton();
                    }
                );
            }
        );


        function programar() {

            actualizarGarantia();


            simulacionValida =
                false;


            actualizarBoton();


            clearTimeout(
                timer
            );


            timer =
                setTimeout(
                    simular,
                    350
                );
        }


        async function simular() {

            if (!datosSimulacionCompletos()) {

                simulacionValida =
                    false;

                limpiarResumen();

                estado(
                    "Completa los datos",
                    ""
                );

                mensaje(
                    "Completa capital, tasa, plazo y fechas.",
                    ""
                );

                actualizarBoton();

                return;
            }


            estado(
                "Calculando...",
                ""
            );


            try {

                const token =
                    document
                        .querySelector(
                            'meta[name="_csrf"]'
                        )
                        ?.content;


                const header =
                    document
                        .querySelector(
                            'meta[name="_csrf_header"]'
                        )
                        ?.content;


                const headers = {
                    "Content-Type":
                        "application/json"
                };


                if (
                    token
                    &&
                    header
                ) {

                    headers[header] =
                        token;
                }


                const response =
                    await fetch(
                        "/admin/api/creditos/simular",
                        {
                            method:
                                "POST",

                            headers,

                            body:
                                JSON.stringify(
                                    payload()
                                )
                        }
                    );


                const texto =
                    await response.text();


                let data = {};


                try {

                    data =
                        texto
                            ? JSON.parse(texto)
                            : {};

                } catch {
                }


                if (!response.ok) {

                    throw new Error(
                        data.message
                        ||
                        "No se pudo calcular el crédito."
                    );
                }


                pintar(
                    data
                );


                simulacionValida =
                    true;


                estado(
                    "Simulación lista",
                    "ok"
                );


                mensaje(
                    "Cronograma calculado correctamente.",
                    "ok"
                );


            } catch (error) {

                simulacionValida =
                    false;


                limpiarResumen();


                estado(
                    "Error de simulación",
                    "error"
                );


                mensaje(
                    error.message,
                    "error"
                );
            }


            actualizarBoton();
        }


        function payload() {

            return {

                idCliente:
                    entero(
                        valor(
                            "idCliente"
                        )
                    ),

                codigoCredito:
                    valor(
                        "codigoCredito"
                    ),

                idTasaOrigen:
                    null,

                capitalOriginal:
                    decimal(
                        valor(
                            "capitalOriginal"
                        )
                    ),

                tipoCalculo:
                    valor(
                        "tipoCalculo"
                    ),

                tasaInteres:
                    decimal(
                        valor(
                            "tasaInteres"
                        )
                    ),

                periodicidadTasa:
                    valor(
                        "periodicidadTasa"
                    ),

                frecuenciaPago:
                    valor(
                        "frecuenciaPago"
                    ),

                plazoMeses:
                    entero(
                        valor(
                            "plazoMeses"
                        )
                    ),

                fechaInicio:
                    valor(
                        "fechaInicio"
                    ),

                fechaPrimerPago:
                    valor(
                        "fechaPrimerPago"
                    ),

                permitePagoSoloInteres:
                    document
                        .getElementById(
                            "permitePagoSoloInteres"
                        )
                        .checked,

                observaciones:
                    valor(
                        "observaciones"
                    ),

                tipoGarantia:
                    valor(
                        "tipoGarantia"
                    ),

                descripcionGarantia:
                    valor(
                        "descripcionGarantia"
                    ),

                valorEstimadoGarantia:
                    decimalOpcional(
                        valor(
                            "valorEstimadoGarantia"
                        )
                    ),

                observacionesGarantia:
                    valor(
                        "observacionesGarantia"
                    )
            };
        }


        function datosSimulacionCompletos() {

            const capital =
                decimal(
                    valor(
                        "capitalOriginal"
                    )
                );


            const tasa =
                decimal(
                    valor(
                        "tasaInteres"
                    )
                );


            const plazo =
                entero(
                    valor(
                        "plazoMeses"
                    )
                );


            return capital !== null
                &&
                capital > 0
                &&
                tasa !== null
                &&
                tasa >= 0
                &&
                plazo !== null
                &&
                plazo > 0
                &&
                valor("tipoCalculo") !== ""
                &&
                valor("periodicidadTasa") !== ""
                &&
                valor("frecuenciaPago") !== ""
                &&
                valor("fechaInicio") !== ""
                &&
                valor("fechaPrimerPago") !== "";
        }


        /* =====================================================
           GARANTÍA
           ===================================================== */

        function garantiaObligatoria() {

            const capital =
                decimal(
                    valor(
                        "capitalOriginal"
                    )
                )
                ?? 0;


            return capital >= 500;
        }


        function hayGarantiaIniciada() {

            return valor("tipoGarantia") !== ""
                ||
                valor("valorEstimadoGarantia") !== ""
                ||
                valor("descripcionGarantia") !== ""
                ||
                archivo.files.length > 0;
        }


        function actualizarGarantia() {

            const obligatoria =
                garantiaObligatoria();


            const exigir =
                obligatoria
                ||
                hayGarantiaIniciada();


            const card =
                document.getElementById(
                    "garantiaCard"
                );


            const status =
                document.getElementById(
                    "garantiaStatus"
                );


            const texto =
                document.getElementById(
                    "garantiaMensaje"
                );


            [
                "tipoGarantia",
                "valorEstimadoGarantia",
                "descripcionGarantia"
            ]
            .forEach(
                id => {

                    document
                        .getElementById(id)
                        .required =
                        exigir;
                }
            );


            archivo.required =
                exigir;


            document
                .querySelectorAll(
                    ".marca-garantia"
                )
                .forEach(
                    marca => {

                        marca.style.display =
                            exigir
                                ? "inline"
                                : "none";
                    }
                );


            card.classList.toggle(
                "obligatoria",
                obligatoria
            );


            if (obligatoria) {

                status.textContent =
                    "Obligatoria";


                texto.textContent =
                    "Desde S/500 todos los datos y el documento son obligatorios.";

            } else if (exigir) {

                status.textContent =
                    "Completar";


                texto.textContent =
                    "Si registras garantía, debes completar todos sus datos.";

            } else {

                status.textContent =
                    "Opcional";


                texto.textContent =
                    "Opcional para créditos menores a S/500.";
            }


            document
                .getElementById(
                    "resGarantia"
                )
                .textContent =
                obligatoria
                    ? "Obligatoria"
                    : exigir
                        ? "Incluida"
                        : "Opcional";
        }


        function garantiaValida() {

            const exigir =
                garantiaObligatoria()
                ||
                hayGarantiaIniciada();


            if (!exigir) {
                return true;
            }


            const f =
                archivo.files?.[0];


            return valor("tipoGarantia") !== ""
                &&
                valor("descripcionGarantia") !== ""
                &&
                (
                    decimal(
                        valor(
                            "valorEstimadoGarantia"
                        )
                    )
                    ?? 0
                ) > 0
                &&
                archivoValido(f);
        }


        function archivoValido(
            f
        ) {

            if (!f) {
                return false;
            }


            if (
                f.size
                >
                10 * 1024 * 1024
            ) {

                return false;
            }


            const nombre =
                f.name
                    .toLowerCase();


            return nombre.endsWith(".pdf")
                ||
                nombre.endsWith(".jpg")
                ||
                nombre.endsWith(".jpeg")
                ||
                nombre.endsWith(".png");
        }


        /* =====================================================
           REGISTRO
           ===================================================== */

        function datosRegistroCompletos() {

            return idCliente.value !== ""
                &&
                valor("codigoCredito") !== ""
                &&
                datosSimulacionCompletos()
                &&
                garantiaValida();
        }


        function actualizarBoton() {

            const boton =
                document.getElementById(
                    "btnRegistrar"
                );


            const ayuda =
                document.getElementById(
                    "mensajeBoton"
                );


            const listo =
                simulacionValida
                &&
                datosRegistroCompletos();


            boton.disabled =
                !listo;


            ayuda.textContent =
                listo
                    ? "Todo listo para registrar."
                    : "Completa todos los campos obligatorios.";
        }


        form.addEventListener(
            "submit",
            event => {

                actualizarGarantia();


                if (
                    !simulacionValida
                    ||
                    !datosRegistroCompletos()
                ) {

                    event.preventDefault();


                    form.reportValidity();


                    mensaje(
                        "Completa todos los campos obligatorios antes de registrar.",
                        "error"
                    );
                }
            }
        );


        /* =====================================================
           PINTAR
           ===================================================== */

        function pintar(
            data
        ) {

            texto(
                "resCapital",
                moneda(
                    data.capital
                )
            );

            texto(
                "resInteres",
                moneda(
                    data.interesTotal
                )
            );

            texto(
                "resTotalPagar",
                moneda(
                    data.totalPagar
                )
            );

            texto(
                "resNumeroCuotas",
                data.numeroCuotas
                ?? 0
            );

            texto(
                "resPrimeraCuota",
                moneda(
                    data.primeraCuota
                )
            );

            texto(
                "resUltimaCuota",
                moneda(
                    data.ultimaCuota
                )
            );

            texto(
                "resUltimaFecha",
                fecha(
                    data.fechaUltimoPago
                )
            );


            const tbody =
                document.getElementById(
                    "cronogramaBody"
                );


            const wrapper =
                document.getElementById(
                    "cronogramaWrapper"
                );


            const vacio =
                document.getElementById(
                    "cronogramaEmpty"
                );


            tbody.innerHTML =
                "";


            const filas =
                data.cronograma
                ?? [];


            if (filas.length === 0) {

                wrapper.style.display =
                    "none";

                vacio.style.display =
                    "block";

                return;
            }


            filas.forEach(
                fila => {

                    const tr =
                        document.createElement(
                            "tr"
                        );


                    [
                        fila.numero,
                        fecha(fila.fechaVencimiento),
                        moneda(fila.saldoInicial),
                        moneda(fila.capital),
                        moneda(fila.interes),
                        moneda(fila.totalCuota),
                        moneda(fila.saldoFinal)
                    ]
                    .forEach(
                        valor => {

                            const td =
                                document.createElement(
                                    "td"
                                );


                            td.textContent =
                                valor;


                            tr.appendChild(
                                td
                            );
                        }
                    );


                    tbody.appendChild(
                        tr
                    );
                }
            );


            vacio.style.display =
                "none";

            wrapper.style.display =
                "block";
        }


        function limpiarResumen() {

            texto("resCapital", "S/ 0.00");
            texto("resInteres", "S/ 0.00");
            texto("resTotalPagar", "S/ 0.00");
            texto("resNumeroCuotas", "0");
            texto("resPrimeraCuota", "S/ 0.00");
            texto("resUltimaCuota", "S/ 0.00");
            texto("resUltimaFecha", "—");


            document.getElementById(
                "cronogramaWrapper"
            ).style.display =
                "none";


            document.getElementById(
                "cronogramaEmpty"
            ).style.display =
                "block";
        }


        /* =====================================================
           UTILIDADES
           ===================================================== */

        function valor(id) {

            return String(
                document
                    .getElementById(id)
                    ?.value
                ?? ""
            ).trim();
        }


        function entero(v) {

            if (v === "") {
                return null;
            }


            const n =
                Number.parseInt(v, 10);


            return Number.isFinite(n)
                ? n
                : null;
        }


        function decimal(v) {

            if (v === "") {
                return null;
            }


            const n =
                Number(v);


            return Number.isFinite(n)
                ? n
                : null;
        }


        function decimalOpcional(v) {

            return v === ""
                ? null
                : decimal(v);
        }


        function moneda(v) {

            return new Intl.NumberFormat(
                "es-PE",
                {
                    style:
                        "currency",

                    currency:
                        "PEN",

                    minimumFractionDigits:
                        2,

                    maximumFractionDigits:
                        2
                }
            )
            .format(
                Number(v ?? 0)
            );
        }


        function fecha(v) {

            if (!v) {
                return "—";
            }


            const p =
                String(v)
                    .split("-");


            return p.length === 3
                ? `${p[2]}/${p[1]}/${p[0]}`
                : v;
        }


        function texto(
            id,
            contenido
        ) {

            const e =
                document.getElementById(id);


            if (e) {

                e.textContent =
                    contenido;
            }
        }


        function estado(
            contenido,
            clase
        ) {

            const e =
                document.getElementById(
                    "simulationState"
                );


            e.textContent =
                contenido;


            e.classList.remove(
                "ok",
                "error"
            );


            if (clase) {

                e.classList.add(
                    clase
                );
            }
        }


        function mensaje(
            contenido,
            clase
        ) {

            const e =
                document.getElementById(
                    "simulationMessage"
                );


            e.textContent =
                contenido;


            e.classList.remove(
                "ok",
                "error"
            );


            if (clase) {

                e.classList.add(
                    clase
                );
            }
        }


        function normalizar(v) {

            return String(v ?? "")
                .toLowerCase()
                .normalize("NFD")
                .replace(
                    /[\u0300-\u036f]/g,
                    ""
                )
                .trim();
        }


        function formatearPeso(bytes) {

            if (
                bytes
                <
                1024 * 1024
            ) {

                return (
                    bytes / 1024
                ).toFixed(1)
                + " KB";
            }


            return (
                bytes
                /
                (
                    1024
                    *
                    1024
                )
            ).toFixed(2)
            + " MB";
        }


        cargarClienteInicial();

        actualizarGarantia();

        programar();

    }
);