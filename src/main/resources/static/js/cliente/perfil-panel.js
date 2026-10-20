(() => {
  const input = document.getElementById('fotoPerfil');
  const form = document.getElementById('formFoto');
  const button = document.getElementById('seleccionarFoto');
  if (!input || !form || !button) return;
  button.addEventListener('click', () => input.click());
  input.addEventListener('change', () => {
    if (input.files && input.files.length) form.requestSubmit();
  });
})();
