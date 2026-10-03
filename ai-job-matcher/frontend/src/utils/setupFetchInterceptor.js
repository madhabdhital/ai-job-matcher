// Runs once at startup. If the backend answers 401 (expired or invalid token)
// on an API call, clear the saved login and send the user to the login page.
const originalFetch = window.fetch.bind(window)

window.fetch = async (...args) => {
  const response = await originalFetch(...args)

  const input = args[0]
  const url = typeof input === 'string' ? input : input?.url ?? ''

  const isApiCall = url.includes('/api/')
  const isAuthCall = url.includes('/api/v1/auth/')

  // The token check also prevents redirect loops when nobody is logged in.
  if (response.status === 401 && isApiCall && !isAuthCall && localStorage.getItem('token')) {
    localStorage.removeItem('token')

    const path = window.location.pathname

    if (path === '/') {
      // Public homepage: reload so the jobs load again without the old token.
      window.location.reload()
    } else if (path !== '/login' && path !== '/register') {
      const redirect = encodeURIComponent(path + window.location.search)
      window.location.assign(`/login?redirect=${redirect}`)
    }
  }

  return response
}