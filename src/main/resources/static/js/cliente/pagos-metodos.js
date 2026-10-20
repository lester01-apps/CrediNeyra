document.querySelector('[data-credit-selector]')?.addEventListener('change', (event) => {
  if (event.target.value) window.location.assign(event.target.value);
});

document.querySelectorAll('[data-metodo]').forEach((boton) => {
  boton.addEventListener('click', () => {
    const metodo = boton.dataset.metodo;
    document.querySelectorAll('[data-metodo]').forEach((item) => item.classList.toggle('activo', item === boton));
    const yape = document.getElementById('panel-yape');
    const efectivo = document.getElementById('panel-efectivo');
    if (yape) yape.hidden = metodo !== 'yape';
    if (efectivo) efectivo.hidden = metodo !== 'efectivo';
  });
});

const paymentTypes = document.querySelector('[data-payment-types]');
const freeAmountInput = paymentTypes?.querySelector('[data-free-amount]');
const selectedPaymentType = () => paymentTypes?.querySelector('[data-payment-type].activo')?.dataset.paymentType || 'CUOTA_COMPLETA';
const formatPaymentAmount = (value) => new Intl.NumberFormat('es-PE', {
  style: 'currency',
  currency: 'PEN',
  minimumFractionDigits: 2,
}).format(value).replace('PEN', 'S/');

const updateSelectedAmount = () => {
  if (!paymentTypes) return;
  const type = selectedPaymentType();
  const full = Number(paymentTypes.dataset.full || 0);
  const interest = Number(paymentTypes.dataset.interest || 0);
  const value = type === 'SOLO_INTERESES' ? interest : type === 'PAGO_LIBRE' ? Number(freeAmountInput?.value || 0) : full;
  document.querySelectorAll('[data-selected-payment-amount]').forEach((item) => {
    item.textContent = formatPaymentAmount(Number.isFinite(value) ? value : 0);
  });
};

paymentTypes?.querySelectorAll('[data-payment-type]').forEach((button) => {
  button.addEventListener('click', () => {
    paymentTypes.querySelectorAll('[data-payment-type]').forEach((item) => item.classList.toggle('activo', item === button));
    const isFree = button.dataset.paymentType === 'PAGO_LIBRE';
    paymentTypes.querySelector('.payment-free-input')?.classList.toggle('visible', isFree);
    if (isFree) freeAmountInput?.focus();
    updateSelectedAmount();
  });
});

freeAmountInput?.addEventListener('input', updateSelectedAmount);
updateSelectedAmount();

document.querySelector('[data-payment-continue]')?.addEventListener('click', (event) => {
  const metodo = document.querySelector('[data-metodo].activo')?.dataset.metodo || 'yape';
  const destino = metodo === 'efectivo' ? event.currentTarget.dataset.efectivoUrl : event.currentTarget.dataset.yapeUrl;
  const tipoPago = selectedPaymentType();
  if (tipoPago === 'PAGO_LIBRE' && (!freeAmountInput?.value || Number(freeAmountInput.value) <= 0)) {
    freeAmountInput?.focus();
    return;
  }
  if (destino) {
    const url = new URL(destino, window.location.origin);
    url.searchParams.set('tipoPago', tipoPago);
    if (tipoPago === 'PAGO_LIBRE') url.searchParams.set('monto', freeAmountInput.value);
    window.location.assign(url.pathname + url.search);
  }
});

document.querySelectorAll('.place-choice input[type="radio"]').forEach((input) => {
  input.addEventListener('change', () => {
    document.querySelectorAll('.place-choice').forEach((item) => item.classList.toggle('active', item.contains(input)));
  });
});

document.querySelector('.copy-yape')?.addEventListener('click', async (event) => {
  const boton = event.currentTarget;
  try {
    await navigator.clipboard.writeText(boton.dataset.yapeNumber || '');
    boton.innerHTML = '<i class="fa-solid fa-check"></i>';
  } catch (_) {
    window.prompt('Copia el número de Yape:', boton.dataset.yapeNumber || '');
  }
});

const calendar = document.querySelector('.cash-calendar');
if (calendar) {
  const form = calendar.closest('form');
  const dateField = form?.querySelector('input[type="date"]');
  const timeField = form?.querySelector('input[type="time"]');
  const title = calendar.querySelector('[data-calendar-title]');
  const days = calendar.querySelector('[data-calendar-days]');
  const initialValue = calendar.dataset.initialDate;
  const parsedInitial = initialValue ? new Date(`${initialValue}T12:00:00`) : new Date();
  let visibleMonth = Number.isNaN(parsedInitial.getTime())
    ? new Date(new Date().getFullYear(), new Date().getMonth(), 1)
    : new Date(parsedInitial.getFullYear(), parsedInitial.getMonth(), 1);

  const isoDate = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  if (dateField && !dateField.value && !Number.isNaN(parsedInitial.getTime())) {
    dateField.value = isoDate(parsedInitial);
  }

  const renderCalendar = () => {
    if (!days || !title) return;
    const year = visibleMonth.getFullYear();
    const month = visibleMonth.getMonth();
    title.textContent = new Intl.DateTimeFormat('es-PE', { month: 'long', year: 'numeric' })
      .format(visibleMonth)
      .replace(/^./, (letter) => letter.toUpperCase());
    days.innerHTML = '';
    const firstDay = (new Date(year, month, 1).getDay() + 6) % 7;
    const lastDay = new Date(year, month + 1, 0).getDate();
    for (let blank = 0; blank < firstDay; blank += 1) {
      days.appendChild(document.createElement('span'));
    }
    for (let day = 1; day <= lastDay; day += 1) {
      const value = isoDate(new Date(year, month, day));
      const button = document.createElement('button');
      button.type = 'button';
      button.textContent = String(day);
      button.dataset.date = value;
      button.classList.toggle('selected', dateField?.value === value);
      button.addEventListener('click', () => {
        if (dateField) dateField.value = value;
        renderCalendar();
      });
      days.appendChild(button);
    }
  };

  calendar.querySelector('[data-calendar-prev]')?.addEventListener('click', () => {
    visibleMonth = new Date(visibleMonth.getFullYear(), visibleMonth.getMonth() - 1, 1);
    renderCalendar();
  });
  calendar.querySelector('[data-calendar-next]')?.addEventListener('click', () => {
    visibleMonth = new Date(visibleMonth.getFullYear(), visibleMonth.getMonth() + 1, 1);
    renderCalendar();
  });
  form?.querySelectorAll('[data-time]').forEach((button) => {
    button.addEventListener('click', () => {
      if (timeField) timeField.value = button.dataset.time || '';
      form.querySelectorAll('[data-time]').forEach((item) => item.classList.toggle('selected', item === button));
    });
  });
  renderCalendar();
}
