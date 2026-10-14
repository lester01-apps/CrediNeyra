document.addEventListener(
    "DOMContentLoaded",
    () => {

        iniciarFiltros();
        iniciarGrafico();

    }
);


function iniciarFiltros() {

    const buscador =
        document.getElementById(
            "buscarCredito"
        );

    const filtro =
        document.getElementById(
            "filtroEstado"
        );

    const filas =
        Array.from(
            document.querySelectorAll(
                ".credito-row"
            )
        );

    const sinResultados =
        document.getElementById(
            "sinResultados"
        );


    const ejecutarFiltro =
        () => {

            const texto =
                normalizar(
                    buscador?.value || ""
                );

            const estado =
                filtro?.value || "";

            let visibles = 0;


            filas.forEach(
                fila => {

                    const busqueda =
                        normalizar(
                            fila.dataset.search || ""
                        );

                    const estadoFila =
                        fila.dataset.estado || "";


                    const visible =
                        (
                            texto === ""
                            ||
                            busqueda.includes(texto)
                        )
                        &&
                        (
                            estado === ""
                            ||
                            estadoFila === estado
                        );


                    fila.style.display =
                        visible
                            ? ""
                            : "none";


                    if (visible) {
                        visibles++;
                    }
                }
            );


            if (sinResultados) {

                sinResultados.style.display =
                    visibles === 0
                        ? "block"
                        : "none";
            }
        };


    buscador?.addEventListener(
        "input",
        ejecutarFiltro
    );


    filtro?.addEventListener(
        "change",
        ejecutarFiltro
    );
}


function normalizar(valor) {

    return String(valor || "")
        .toLowerCase()
        .normalize("NFD")
        .replace(
            /[\u0300-\u036f]/g,
            ""
        )
        .trim();
}


function iniciarGrafico() {

    const canvas =
        document.getElementById(
            "pagosChart"
        );

    if (!canvas) {
        return;
    }


    const meses =
        window.crediNeyraCreditos?.meses
        || [];

    const pagos =
        (
            window.crediNeyraCreditos?.pagos
            || []
        ).map(
            valor => Number(valor) || 0
        );


    const vacio =
        document.getElementById(
            "chartEmpty"
        );


    if (
        pagos.length === 0
        ||
        !pagos.some(valor => valor > 0)
    ) {

        canvas.style.display =
            "none";

        if (vacio) {
            vacio.style.display =
                "flex";
        }

        return;
    }


    const dibujar =
        () => {

            dibujarGrafico(
                canvas,
                meses,
                pagos
            );
        };


    dibujar();


    window.addEventListener(
        "resize",
        dibujar
    );
}


function dibujarGrafico(
    canvas,
    meses,
    pagos
) {

    const contenedor =
        canvas.parentElement;

    const ancho =
        Math.max(
            contenedor.clientWidth,
            300
        );

    const alto =
        Math.max(
            contenedor.clientHeight,
            220
        );

    const ratio =
        window.devicePixelRatio || 1;


    canvas.width =
        ancho * ratio;

    canvas.height =
        alto * ratio;


    const ctx =
        canvas.getContext("2d");


    ctx.setTransform(
        ratio,
        0,
        0,
        ratio,
        0,
        0
    );


    ctx.clearRect(
        0,
        0,
        ancho,
        alto
    );


    const left = 50;
    const right = 15;
    const top = 20;
    const bottom = 35;

    const usableWidth =
        ancho - left - right;

    const usableHeight =
        alto - top - bottom;

    const maximo =
        Math.max(
            ...pagos,
            1
        );


    for (
        let i = 0;
        i <= 4;
        i++
    ) {

        const y =
            top
            +
            usableHeight
            -
            (
                usableHeight
                * i / 4
            );


        ctx.beginPath();

        ctx.strokeStyle =
            "rgba(148,163,184,.10)";

        ctx.moveTo(
            left,
            y
        );

        ctx.lineTo(
            left + usableWidth,
            y
        );

        ctx.stroke();
    }


    const puntos =
        pagos.map(
            (valor, indice) => {

                const x =
                    pagos.length === 1
                        ? left + usableWidth / 2
                        : left
                        +
                        usableWidth
                        *
                        indice
                        /
                        (pagos.length - 1);

                const y =
                    top
                    +
                    usableHeight
                    -
                    (
                        valor
                        /
                        maximo
                        *
                        usableHeight
                    );

                return {
                    x,
                    y
                };
            }
        );


    ctx.beginPath();


    puntos.forEach(
        (punto, indice) => {

            if (indice === 0) {

                ctx.moveTo(
                    punto.x,
                    punto.y
                );

            } else {

                ctx.lineTo(
                    punto.x,
                    punto.y
                );
            }
        }
    );


    ctx.strokeStyle =
        "#38bdf8";

    ctx.lineWidth = 2.3;

    ctx.stroke();


    puntos.forEach(
        punto => {

            ctx.beginPath();

            ctx.arc(
                punto.x,
                punto.y,
                4,
                0,
                Math.PI * 2
            );

            ctx.fillStyle =
                "#38bdf8";

            ctx.fill();
        }
    );


    ctx.font =
        "11px system-ui";

    ctx.fillStyle =
        "#64748b";

    ctx.textAlign =
        "center";


    meses.forEach(
        (mes, indice) => {

            const x =
                meses.length === 1
                    ? left + usableWidth / 2
                    : left
                    +
                    usableWidth
                    *
                    indice
                    /
                    (meses.length - 1);


            ctx.fillText(
                mes,
                x,
                alto - 10
            );
        }
    );
}