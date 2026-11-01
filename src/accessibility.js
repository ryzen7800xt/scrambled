(() => {
  const storageKey = 'scrambled-accessibility';
  const settings = { bold: false, large: false, contrast: false, motion: false };
  const classes = {
    bold: 'accessibility-bold',
    large: 'accessibility-large',
    contrast: 'accessibility-contrast',
    motion: 'accessibility-reduced-motion'
  };

  try {
    Object.assign(settings, JSON.parse(localStorage.getItem(storageKey) || '{}'));
  } catch (error) {
    console.warn('Could not load saved accessibility settings.', error);
  }

  function applySettings() {
    for (const [setting, className] of Object.entries(classes)) {
      document.documentElement.classList.toggle(className, Boolean(settings[setting]));
      const input = document.querySelector(`[data-accessibility-setting="${setting}"]`);
      if (input) input.checked = Boolean(settings[setting]);
    }
  }

  applySettings();

  document.querySelectorAll('[data-accessibility-setting]').forEach(input => {
    input.addEventListener('change', () => {
      settings[input.dataset.accessibilitySetting] = input.checked;
      try {
        localStorage.setItem(storageKey, JSON.stringify(settings));
        applySettings();
      } catch (error) {
        console.error('Could not save accessibility settings.', error);
        const status = document.querySelector('#accessibility-status');
        if (status) status.textContent = 'Your browser could not save this preference.';
      }
    });
  });
})();
