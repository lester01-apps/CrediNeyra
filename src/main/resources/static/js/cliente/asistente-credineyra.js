(() => {
  const chat = document.getElementById('assistant-chat');
  const question = document.getElementById('assistant-question');
  const form = document.getElementById('assistant-form');

  const responses = {
    cuota: 'Para consultar tu cuota oficial entra a “Mis créditos”. Allí verás el monto exigible, número de cuota, fecha de vencimiento y estado. Ese es el monto que debes considerar antes de pagar.',
    interes: 'El interés es el cargo acordado por el uso del crédito. Se determina con el capital pendiente, la tasa pactada y el periodo. La calculadora de esta pantalla da una estimación simple; tu monto oficial aparece en “Mis créditos”.',
    pago: 'Para pagar por Yape entra a “Mis pagos”, selecciona el crédito con cuota exigible y sube un comprobante claro. También puedes agendar pago en efectivo. Todo pago queda pendiente hasta que CrediNeyra lo valide.',
    comprobante: 'El comprobante debe mostrar con claridad el monto, fecha, hora, operación y destinatario. Si fue observado, vuelve a subir una imagen completa desde “Mis pagos”. Conserva siempre tu comprobante.',
    regularizacion: 'Regularización indica que hay una cuota pendiente o que se está revisando una alternativa de pago. Antes de abonar un monto distinto, contáctanos para confirmar el procedimiento y evitar diferencias.',
    recomendacion: 'Recomendación: revisa tu cuota antes del vencimiento, paga exactamente el monto oficial, guarda el comprobante y consulta primero si deseas adelantar, dividir o regularizar un pago.',
    historial: 'Puedes ver pagos, comprobantes y movimientos anteriores desde “Historial”. Allí se muestra el estado de cada operación: completado, pendiente, observado o programado.',
    contacto: 'Puedes comunicarte con CrediNeyra por WhatsApp o llamada al 973 860 581, o por correo a credineyra@gmail.com. Atención: domingo 6:00 p.m.; lunes a jueves de 5:00 a.m. a 8:00 p.m.; viernes de 5:00 a.m. a 5:00 p.m.; sábado cerrado.',
    saludo: '¡Bienvenido al Asistente CrediNeyra! Estoy listo para conversar y orientarte. Puedes consultarme sobre el portal, tus pagos, intereses, cuotas, regularización o escribir tu duda con tus propias palabras.',
    identidad: 'Me llamo Asistente CrediNeyra. ¡Bienvenido! Puedo conversar contigo y orientarte de forma segura sobre el portal, créditos, cuotas y pagos.',
    bienestar: 'Estoy muy bien, gracias por preguntar. ¿En qué puedo ayudarte hoy? Si quieres, podemos revisar cómo consultar una cuota, cómo funciona un pago o una estimación de interés.',
    general: 'Claro, podemos conversarlo. Si se trata de una cuota, monto, fecha o estado oficial, revisa “Mis créditos” o comunícate con CrediNeyra para confirmarlo.'
  };

  const addMessage = (text, author) => {
    if (!chat) return;
    const message = document.createElement('div');
    message.className = `assistant-message ${author}`;
    message.textContent = text;
    chat.append(message);
    chat.scrollTop = chat.scrollHeight;
  };

  const hasSensitiveRequest = (value) => {
    const asksSecrets = /contraseñ|password|clave\s*(del sistema|admin|usuario)|token|\bapi\b|credencial|secret|base de datos|database|sql|conexion|supabase|servidor/.test(value);
    const asksOtherPerson = /(otro|otra|otros|otras)\s+(cliente|persona|usuario|cuenta)|datos\s+(personales|privados)|dni\s+de|tel[eé]fono\s+de|correo\s+de/.test(value);
    return asksSecrets || asksOtherPerson;
  };

  const answerQuestion = (rawQuestion) => {
    const value = rawQuestion.toLocaleLowerCase('es').trim();
    if (!value) return;

    addMessage(rawQuestion, 'user');
    let response;
    if (hasSensitiveRequest(value)) {
      response = 'Lo siento, no puedo brindar contraseñas, credenciales, datos de otras personas, información de la base de datos ni acceso a la API. Sí puedo ayudarte con orientación general y segura de CrediNeyra.';
    } else if (value.includes('como te llamas') || value.includes('cómo te llamas') || value.includes('cual es tu nombre') || value.includes('cuál es tu nombre') || value.includes('quien eres') || value.includes('quién eres')) response = responses.identidad;
    else if (/c[oó]mo est[aá]s|como estas|qu[eé] tal|todo bien/.test(value)) response = responses.bienestar;
    else if (/hola|buenas|buenos d[ií]as|buenas tardes|buenas noches/.test(value)) response = responses.saludo;
    else if (/cuota|debo|vencimiento|monto exigible/.test(value)) response = responses.cuota;
    else if (/inter[eé]s|tasa|capital|calcula/.test(value)) response = responses.interes;
    else if (/yape|pago|efectivo|abonar|adelantar/.test(value)) response = responses.pago;
    else if (/comprobante|captura|imagen|observado|rechazado/.test(value)) response = responses.comprobante;
    else if (/regulariza|atraso|pendiente|mora/.test(value)) response = responses.regularizacion;
    else if (/recomienda|consejo|organizar/.test(value)) response = responses.recomendacion;
    else if (/historial|movimiento/.test(value)) response = responses.historial;
    else if (/whatsapp|tel[eé]fono|correo|horario|contact/.test(value)) response = responses.contacto;
    else response = responses.general;

    window.setTimeout(() => addMessage(response, 'bot'), 180);
  };

  document.querySelectorAll('[data-answer]').forEach((button) => {
    button.addEventListener('click', () => answerQuestion(button.textContent));
  });

  form?.addEventListener('submit', (event) => {
    event.preventDefault();
    const value = question.value.trim();
    if (!value) return;
    question.value = '';
    answerQuestion(value);
  });

  document.getElementById('calculate-interest')?.addEventListener('click', () => {
    const capital = Number(document.getElementById('interest-capital').value);
    const rate = Number(document.getElementById('interest-rate').value);
    const months = Number(document.getElementById('interest-months').value);
    const result = document.getElementById('interest-result');

    if (!Number.isFinite(capital) || !Number.isFinite(rate) || !Number.isFinite(months) || capital <= 0 || rate < 0 || months <= 0) {
      result.textContent = 'Completa capital, tasa mensual y meses con valores válidos para realizar la estimación.';
      return;
    }

    const interest = capital * (rate / 100) * months;
    const total = capital + interest;
    const format = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' });
    result.textContent = `Estimación: interés ${format.format(interest)} · total aproximado ${format.format(total)}. El monto oficial se consulta en Mis créditos.`;
  });

  document.getElementById('faq-search')?.addEventListener('input', (event) => {
    const term = event.target.value.trim().toLocaleLowerCase('es');
    document.querySelectorAll('.faq-panel details').forEach((item) => {
      item.hidden = term.length > 0 && !item.textContent.toLocaleLowerCase('es').includes(term);
    });
  });
})();
