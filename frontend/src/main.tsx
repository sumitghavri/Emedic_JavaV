import React from 'react'
import ReactDOM from 'react-dom/client'
import { Auth0Provider } from '@auth0/auth0-react'
import App from './App'
import PublicBookingPage from './PublicBooking'
import './styles.css'

const auth = {
  domain: import.meta.env.VITE_AUTH0_DOMAIN as string | undefined,
  clientId: import.meta.env.VITE_AUTH0_CLIENT_ID as string | undefined,
  audience: import.meta.env.VITE_AUTH0_AUDIENCE as string | undefined,
}

function Root() {
  if (window.location.pathname === '/book') return <PublicBookingPage />
  const configured = Boolean(auth.domain && auth.clientId && auth.audience)
  if (!configured) return <App authConfigured={false} />

  return (
    <Auth0Provider
      domain={auth.domain!}
      clientId={auth.clientId!}
      authorizationParams={{
        redirect_uri: window.location.origin,
        audience: auth.audience,
        scope: 'openid profile email',
      }}
      cacheLocation="memory"
    >
      <App authConfigured />
    </Auth0Provider>
  )
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode><Root /></React.StrictMode>,
)
