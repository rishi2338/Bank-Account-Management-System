(() => {
  const reduced = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const init = () => {
    document.body.classList.add('motion-ready');
    if (reduced) return;
    const candidates = document.querySelectorAll('.card, .stat-card, .dashboard-card, .balance-card, .summary-card, .feature-card, .panel, .transaction-card, main section, .container > section');
    candidates.forEach((el, i) => {
      if (el.closest('form')) return;
      el.classList.add('motion-reveal');
      el.style.transitionDelay = Math.min(i % 6, 5) * 55 + 'ms';
    });
    if ('IntersectionObserver' in window) {
      const observer = new IntersectionObserver(entries => entries.forEach(entry => {
        if (entry.isIntersecting) { entry.target.classList.add('motion-visible'); observer.unobserve(entry.target); }
      }), { threshold: .08, rootMargin: '0px 0px -24px 0px' });
      document.querySelectorAll('.motion-reveal').forEach(el => observer.observe(el));
    } else document.querySelectorAll('.motion-reveal').forEach(el => el.classList.add('motion-visible'));
    document.addEventListener('click', event => {
      const button = event.target.closest('button, .btn, input[type="submit"], a.button, .button');
      if (!button || button.disabled || button.type === 'submit' && button.closest('form')?.dataset.noRipple === 'true') return;
      const rect = button.getBoundingClientRect();
      const size = Math.max(rect.width, rect.height);
      const ripple = document.createElement('span');
      ripple.className = 'motion-ripple';
      ripple.style.width = ripple.style.height = size + 'px';
      ripple.style.left = (event.clientX - rect.left - size / 2) + 'px';
      ripple.style.top = (event.clientY - rect.top - size / 2) + 'px';
      button.appendChild(ripple);
      ripple.addEventListener('animationend', () => ripple.remove(), { once: true });
    }, { passive: true });
  };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, { once: true });
  else init();
})();