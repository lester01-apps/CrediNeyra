(() => {
  const form = document.getElementById('contact-form');
  if (!form) return;

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    if (!form.reportValidity()) return;

    const name = form.querySelector('input[type="text"]').value.trim();
    const email = form.querySelector('input[type="email"]').value.trim();
    const subject = document.getElementById('contact-subject').value;
    const message = document.getElementById('contact-message').value.trim();
    const text = `Hola, soy ${name}.\n\nCorreo: ${email}\nAsunto: ${subject}\n\nMensaje:\n${message}`;
    window.open(`https://wa.me/51973860581?text=${encodeURIComponent(text)}`, '_blank', 'noopener');
  });
})();
