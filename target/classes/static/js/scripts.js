// compatibility script used by legacy dashboards
// Normalizes localStorage keys used across UIs
(function(){
  // If new keys set by auth.js exist, copy them to legacy keys expected elsewhere
  try {
    const newToken = localStorage.getItem('hirehub_token');
    const newUser = localStorage.getItem('hirehub_user');
    if (newToken && !localStorage.getItem('token')) {
      localStorage.setItem('token', newToken);
    }
    if (newUser && !localStorage.getItem('userId')) {
      try {
        const u = JSON.parse(newUser);
        if (u.id) localStorage.setItem('userId', u.id);
        else if (u.userId) localStorage.setItem('userId', u.userId);
      } catch(e){}
    }
  } catch(e){console.warn('scripts.js init error', e)}
})();
