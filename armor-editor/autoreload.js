(function autoReload() {
  const FILES = ['index.html', 'app.js'];
  const POLL_MS = 800;
  const sigs = {};

  function fetchSig(name, cb) {
    fetch(name + '?_=' + Date.now(), { cache: 'no-store' })
      .then((r) => (r.ok ? r.text() : Promise.reject()))
      .then((t) => cb(t.length + ':' + t.slice(-80)))
      .catch(() => cb(null));
  }

  let ready = false;
  let pending = 0;
  FILES.forEach((f) => {
    fetchSig(f, (s) => {
      sigs[f] = s;
      if (++pending === FILES.length) { ready = true; console.log('[autoreload] watching', FILES.join(', ')); }
    });
  });

  setInterval(() => {
    if (!ready || document.hidden) return;
    FILES.forEach((f) => {
      fetchSig(f, (s) => {
        if (s && sigs[f] && s !== sigs[f]) {
          console.log('[autoreload] ' + f + ' changed — reloading');
          location.reload();
        } else if (s) { sigs[f] = s; }
      });
    });
  }, POLL_MS);
})();
