import { useEffect, useMemo, useRef, useState, type FormEvent, type ReactNode } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { createPortal } from 'react-dom'
import {
  Activity, ArrowDownToLine, ArrowRight, CalendarDays, Check, ChevronLeft,
  ChevronRight, CircleHelp, Clock3, Copy, FilePlus2, FileText, HeartPulse, LayoutDashboard,
  LogOut, Menu, Pill, Plus, Search, Settings2, ShieldCheck, Stethoscope, Users, X,
} from 'lucide-react'
import { allPages, api, type Appointment, type Consultation, type Doctor, type History,
  type Medicine, type Page, type Patient, type Schedule } from './api'
import './workflows.css'

type Section = 'Overview' | 'Appointments' | 'Patients' | 'Clinical records' | 'Medicines' | 'Doctors' | 'Schedules' | 'Clinic workspace'
const rolesClaim = (import.meta.env.VITE_AUTH0_ROLES_CLAIM as string | undefined) || 'roles'

export default function App({ authConfigured }: { authConfigured: boolean }) {
  if (!authConfigured) return <SetupScreen />
  return <Workspace />
}

function SetupScreen() {
  return <main className="setup-screen">
    <div className="setup-card">
      <div className="brand-mark"><HeartPulse size={22} /></div>
      <span className="eyebrow">eMedic · Clinic workspace</span>
      <h1>Connect your sign-in provider</h1>
      <p>The frontend is ready, but it needs your Auth0 application values before it can request protected API data.</p>
      <div className="setup-steps">
        <span><b>01</b> Create an Auth0 SPA application and API.</span>
        <span><b>02</b> Add the values below to <code>frontend/.env.local</code>.</span>
        <span><b>03</b> Configure the matching issuer, audience, and role claim on the API.</span>
      </div>
      <pre>{`VITE_AUTH0_DOMAIN=your-tenant.region.auth0.com
VITE_AUTH0_CLIENT_ID=your-spa-client-id
VITE_AUTH0_AUDIENCE=https://emedic-api
VITE_AUTH0_ROLES_CLAIM=https://emedic.example/roles`}</pre>
      <a className="text-link" href="https://auth0.com/signup" target="_blank" rel="noreferrer">Create an Auth0 account <ArrowRight size={15} /></a>
      <a className="public-booking-link" href="/book">Open public appointment booking <ArrowRight size={15} /></a>
      <p className="setup-note"><ShieldCheck size={15} /> No patient data is loaded until sign-in is configured.</p>
    </div>
  </main>
}

function Workspace() {
  const { isLoading, isAuthenticated, loginWithRedirect, logout, user, getAccessTokenSilently } = useAuth0()
  const getApiToken = () => getAccessTokenSilently({
    authorizationParams: {
      audience: import.meta.env.VITE_AUTH0_AUDIENCE as string,
      scope: 'openid profile email',
    },
  })
  const [section, setSection] = useState<Section>('Overview')
  const [mobileNav, setMobileNav] = useState(false)
  const [toast, setToast] = useState('')
  const [helpOpen, setHelpOpen] = useState(false)
  const roles = useMemo(() => {
    const raw = user?.[rolesClaim]
    return Array.isArray(raw) ? raw as string[] : []
  }, [user])
  const allowed = (role: string) => roles.includes(role) || roles.includes(`ROLE_${role}`)
  const canSchedule = allowed('ADMIN')
  const canManageDoctors = canSchedule
  const canRegisterAsDoctor = allowed('CLINICIAN')
  const canClinical = allowed('CLINICIAN') || allowed('ADMIN')
  const canPatients = canClinical || allowed('RECEPTIONIST')
  const canManageMedicines = allowed('ADMIN')
  const nav: Array<{ name: Section; icon: typeof LayoutDashboard; visible: boolean }> = [
    { name: 'Overview', icon: LayoutDashboard, visible: true },
    { name: 'Appointments', icon: CalendarDays, visible: canPatients },
    { name: 'Patients', icon: Users, visible: canPatients },
    { name: 'Clinical records', icon: FileText, visible: canClinical },
    { name: 'Medicines', icon: Pill, visible: canClinical || allowed('RECEPTIONIST') },
    { name: 'Doctors', icon: Stethoscope, visible: canManageDoctors || canRegisterAsDoctor },
    { name: 'Schedules', icon: Settings2, visible: canSchedule },
    { name: 'Clinic workspace', icon: Settings2, visible: canSchedule },
  ]

  useEffect(() => {
    if (!toast) return
    const timer = window.setTimeout(() => setToast(''), 3600)
    return () => window.clearTimeout(timer)
  }, [toast])

  if (isLoading) return <div className="screen-state"><div className="spinner" /><span>Preparing your workspace…</span></div>
  if (!isAuthenticated) return <main className="login-screen">
    <div className="login-panel">
      <div className="brand-mark"><HeartPulse size={22} /></div>
      <span className="eyebrow">eMedic · Clinic workspace</span>
      <h1>Care, coordinated.</h1>
      <p>Appointments, patient context, and clinical work in one calm workspace.</p>
      <button className="button button-primary button-wide" onClick={() => loginWithRedirect()}>Sign in securely <ArrowRight size={16} /></button>
      <a className="public-booking-link" href="/book">Need an appointment? Book online <ArrowRight size={15} /></a>
      <div className="login-trust"><ShieldCheck size={16} /> Protected by your organization’s identity provider</div>
    </div>
    <div className="login-aside"><div className="aside-orb orb-one" /><div className="aside-orb orb-two" />
      <div className="aside-content"><span className="aside-tag">CLINICAL OPERATIONS</span><h2>More clarity for every next step.</h2><p>A shared view of today’s care, with patient information available only to authorized staff.</p>
        <div className="aside-stats"><div><b>01</b><span>Schedule</span></div><i /><div><b>02</b><span>Care</span></div><i /><div><b>03</b><span>History</span></div></div>
      </div>
    </div>
  </main>

  const knownRoles = ['ADMIN', 'CLINICIAN', 'RECEPTIONIST']
  const hasClinicRole = roles.some(role => knownRoles.includes(role.replace(/^ROLE_/, '')))
  if (!hasClinicRole) return <main className="access-pending-screen"><section className="access-pending-card">
    <div className="brand-mark"><HeartPulse size={22} /></div>
    <span className="eyebrow">eMedic · Clinic workspace</span>
    <h1>Your account is signed in, but clinic access hasn’t been assigned.</h1>
    <p>New accounts do not receive staff permissions automatically. A clinic administrator must assign an eMedic role to this exact Auth0 account. For doctor setup, that role is <b>ADMIN</b>.</p>
    <div className="access-pending-steps"><b>After the role is assigned</b><span>Sign out, then sign in again. Your new token will include the role and unlock the workspace.</span></div>
    <button className="button button-primary" onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>Sign out</button>
  </section></main>

  const activeNav = nav.find(item => item.name === section && item.visible)
  const PageIcon = activeNav?.icon ?? LayoutDashboard
  const displayName = user?.name || user?.email || 'Care team member'
  const initials = displayName.split(/[\s@.]+/).filter(Boolean).slice(0, 2).map(part => part[0].toUpperCase()).join('')

  return <div className="app-shell">
    <aside className={`sidebar ${mobileNav ? 'sidebar-open' : ''}`}>
      <div className="sidebar-brand"><div className="brand-mark"><HeartPulse size={20} /></div><div><b>emedic</b><span>CARE OPERATIONS</span></div><button className="icon-button mobile-close" onClick={() => setMobileNav(false)} aria-label="Close navigation"><X size={18} /></button></div>
      <div className="workspace-switch"><div className="workspace-avatar">N</div><div className="workspace-name"><b>Northstar Clinic</b><span>Clinical workspace</span></div></div>
      <div className="nav-label">WORKSPACE</div>
      <nav className="main-nav" aria-label="Main navigation">
        {nav.filter(item => item.visible).map(item => {
          const Icon = item.icon
          return <button key={item.name} className={`nav-item ${section === item.name ? 'nav-active' : ''}`} onClick={() => { setSection(item.name); setMobileNav(false) }}><Icon size={18} /><span>{item.name}</span>{item.name === 'Appointments' && section !== item.name && <span className="nav-dot" />}</button>
        })}
      </nav>
      <div className="sidebar-bottom"><button type="button" className="help-card" onClick={() => setHelpOpen(true)}><span className="help-icon"><CircleHelp size={17} /></span><span><b>Need a hand?</b><small>Open the care guide</small></span><ArrowRight size={15} /></button><div className="sidebar-foot"><span>eMedic workspace</span><span>v0.1 · REST API</span></div></div>
    </aside>
    {mobileNav && <button className="nav-backdrop" aria-label="Close navigation" onClick={() => setMobileNav(false)} />}
    <div className="main-area">
      <header className="topbar"><button className="icon-button nav-toggle" onClick={() => setMobileNav(true)} aria-label="Open navigation"><Menu size={20} /></button><div className="breadcrumbs"><span>Workspace</span><ChevronRight size={14} /><b>{section}</b></div><div className="top-actions"><div className="system-state"><span className="live-indicator" />Signed in</div><div className="top-divider" /><button className="icon-button" aria-label="Help" title="Open help" onClick={() => setHelpOpen(true)}><CircleHelp size={18} /></button><div className="profile-wrap"><div className="profile-avatar">{initials}</div><div className="profile-name"><b>{displayName.split(' ')[0]}</b><span>{roles[0]?.replace('ROLE_', '').toLowerCase() || 'staff'}</span></div></div><button className="icon-button logout-button" title="Sign out" onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}><LogOut size={17} /></button></div></header>
      <main className="page-content"><div className="page-heading"><div><div className="section-kicker"><PageIcon size={15} /> {section.toUpperCase()}</div><h1>{section === 'Overview' ? 'Good care starts with a clear view.' : section}</h1><p>{sectionSubtitle(section)}</p></div>{section === 'Appointments' && canPatients && <button className="button button-primary" onClick={() => window.dispatchEvent(new Event('open-booking'))}><Plus size={17} /> New staff booking</button>}</div>
        {roles.length === 0 && <div className="notice notice-warn"><ShieldCheck size={17} /><span>Signed in, but no <code>{rolesClaim}</code> role claim was found. Ask your identity-provider administrator to assign a role before using eMedic.</span></div>}
        {section === 'Overview' && <OverviewPage token={getApiToken} onNavigate={setSection} canClinical={canClinical} />}
        {section === 'Appointments' && canPatients && <AppointmentsPage token={getApiToken} canClinical={canClinical} />}
        {section === 'Patients' && canPatients && <PatientsPage token={getApiToken} canClinical={canClinical} />}
        {section === 'Clinical records' && canClinical && <ClinicalPage token={getApiToken} />}
        {section === 'Medicines' && (canClinical || allowed('RECEPTIONIST')) && <MedicineCatalogPage token={getApiToken} canManage={canManageMedicines} />}
        {section === 'Doctors' && (canManageDoctors || canRegisterAsDoctor) && <DoctorsPage token={getApiToken} canManage={canManageDoctors} clinicianIdentity={{ email: user?.email ?? '', name: user?.name ?? '' }} />}
        {section === 'Schedules' && canSchedule && <SchedulesPage token={getApiToken} onNavigate={setSection} />}
        {section === 'Clinic workspace' && canSchedule && <ClinicWorkspacePage token={getApiToken} />}
      </main>
      <footer className="main-footer"><span>eMedic is a care coordination tool. Clinical decisions remain with the treating clinician.</span><span><ShieldCheck size={14} /> Secure session</span></footer>
    </div>
    {toast && <div className="toast"><Check size={17} />{toast}</div>}
    {helpOpen && <HelpDialog onClose={() => setHelpOpen(false)} onNavigate={name => { setHelpOpen(false); setSection(name) }} />}
  </div>
}

function sectionSubtitle(section: Section) {
  const copy: Record<Section, string> = {
    Overview: 'A thoughtful snapshot of today’s clinic and the people who need care.',
    Appointments: 'Book, confirm, and coordinate the day’s visits.',
    Patients: 'Find patient records and review their care timeline.',
    'Clinical records': 'Document confirmed visits and manage prescription history.',
    Medicines: 'Search the clinic medicine catalog and review the prescription workflow.',
    Doctors: 'Add and maintain the doctors patients can book.',
    Schedules: 'Publish weekly doctor hours and manage booking capacity.',
    'Clinic workspace': 'Set the clinic identity and contact details for this workspace.',
  }
  return copy[section]
}

type TokenGetter = () => Promise<string | undefined>
function useTokenApi(tokenGetter: TokenGetter) {
  return async <T,>(path: string, init?: RequestInit) => {
    const token = await tokenGetter()
    if (!token) throw new Error('Your session could not provide an API access token. Sign in again or contact your administrator.')
    return api<T>(path, token, init)
  }
}
async function tokenPages<T>(tokenGetter: TokenGetter, path: string) {
  const token = await tokenGetter()
  if (!token) throw new Error('Your session could not provide an API access token. Sign in again or contact your administrator.')
  return allPages<T>(path, token)
}

function OverviewPage({ token, onNavigate, canClinical }: { token: TokenGetter; onNavigate: (s: Section) => void; canClinical: boolean }) {
  const request = useTokenApi(token)
  const [appointments, setAppointments] = useState<Appointment[]>([])
  const [patients, setPatients] = useState<Page<Patient> | null>(null)
  const [doctors, setDoctors] = useState<Page<Doctor> | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  useEffect(() => { let live = true
    Promise.all([request<Page<Appointment>>('/appointments?page=0&size=100'), request<Page<Patient>>('/patients?page=0&size=1'), request<Page<Doctor>>('/doctors?page=0&size=1')])
      .then(([a, p, d]) => { if (!live) return; setAppointments(a.content); setPatients(p); setDoctors(d) })
      .catch(e => live && setError(e.message)).finally(() => live && setLoading(false))
    return () => { live = false }
  }, [])
  const today = new Date().toDateString()
  const todayVisits = appointments.filter(item => new Date(item.appointmentDateTime).toDateString() === today && !['CANCELLED', 'COMPLETED', 'NO_SHOW'].includes(item.status))
  const nextVisits = appointments.filter(item => new Date(item.appointmentDateTime) >= new Date() && !['CANCELLED', 'COMPLETED', 'NO_SHOW'].includes(item.status)).sort((a, b) => a.appointmentDateTime.localeCompare(b.appointmentDateTime)).slice(0, 5)
  const confirmed = appointments.filter(item => item.status === 'CONFIRMED').length
  return <>
    {error && <ErrorBanner message={error} />}
    <div className="metric-grid">
      <MetricCard label="Visits today" value={loading ? '—' : String(todayVisits.length)} note="Scheduled for today" icon={<CalendarDays />} tone="blue" />
      <MetricCard label="Awaiting confirmation" value={loading ? '—' : String(appointments.filter(a => a.status === 'REQUESTED').length)} note="Needs a staff review" icon={<Clock3 />} tone="amber" />
      <MetricCard label="Confirmed visits" value={loading ? '—' : String(confirmed)} note="Ready for the care team" icon={<Check />} tone="green" />
      <MetricCard label="Care network" value={loading ? '—' : `${doctors?.totalElements ?? 0} doctors`} note={`${patients?.totalElements ?? 0} patient records`} icon={<Users />} tone="violet" />
    </div>
    <div className="dashboard-grid">
      <section className="panel schedule-panel"><div className="panel-heading"><div><span className="eyebrow">YOUR DAY</span><h2>Upcoming visits</h2></div><button className="button button-quiet" onClick={() => onNavigate('Appointments')}>All appointments <ArrowRight size={15} /></button></div>
        {loading ? <LoadingRows /> : nextVisits.length === 0 ? <EmptyState icon={<CalendarDays />} title="Your schedule is clear" text="New bookings will appear here as they come in." /> : <div className="visit-list">{nextVisits.map((a, index) => <VisitRow key={a.appointmentId} appointment={a} index={index} />)}</div>}
      </section>
      <section className="panel focus-panel"><div className="focus-art"><div className="focus-ring ring-one" /><div className="focus-ring ring-two" /><div className="focus-core"><Activity size={26} /></div></div><span className="eyebrow">CARE TEAM FOCUS</span><h2>{canClinical ? 'The patient story, in context.' : 'Every visit, in view.'}</h2><p>{canClinical ? 'Open a patient record to review prior encounters and prescriptions before the next visit.' : 'Keep the day moving with a shared, up-to-date appointment list.'}</p><button className="button button-light" onClick={() => onNavigate(canClinical ? 'Patients' : 'Appointments')}>{canClinical ? 'Browse patient records' : 'View appointments'} <ArrowRight size={15} /></button></section>
    </div>
    <div className="bottom-note"><ShieldCheck size={16} /><span>Patient and clinical information is shown only to users with the appropriate role.</span><span className="note-link">Role-based access enabled</span></div>
  </>
}

function MetricCard({ label, value, note, icon, tone }: { label: string; value: string; note: string; icon: ReactNode; tone: string }) {
  return <article className="metric-card"><div className={`metric-icon ${tone}`}>{icon}</div><div className="metric-copy"><span>{label}</span><b>{value}</b><small>{note}</small></div><div className="metric-mark">↗</div></article>
}

function VisitRow({ appointment, index }: { appointment: Appointment; index: number }) {
  const colors = ['blue', 'green', 'violet', 'amber']
  const patientInitials = appointment.patientName?.split(' ').slice(0, 2).map(s => s[0]).join('') || 'P'
  return <div className="visit-row"><div className={`patient-avatar ${colors[index % colors.length]}`}>{patientInitials}</div><div className="visit-person"><b>{appointment.patientName || `Patient ${appointment.patientId}`}</b><span>{appointment.reason || 'General consultation'}</span></div><div className="visit-doctor"><span>DOCTOR</span><b>{appointment.doctorName || `Doctor ${appointment.doctorId}`}</b></div><div className="visit-time"><b>{formatTime(appointment.appointmentDateTime)}</b><span>{formatRelativeDay(appointment.appointmentDateTime)}</span></div><StatusPill status={appointment.status} /></div>
}

function AppointmentsPage({ token, canClinical }: { token: TokenGetter; canClinical: boolean }) {
  const request = useTokenApi(token)
  const [items, setItems] = useState<Appointment[]>([])
  const [doctors, setDoctors] = useState<Doctor[]>([])
  const [availableMedicines, setAvailableMedicines] = useState<Medicine[]>([])
  const [consultationAppointmentIds, setConsultationAppointmentIds] = useState<Set<number>>(new Set())
  const [consulting, setConsulting] = useState<Appointment | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [verification, setVerification] = useState<Appointment | null>(null)
  const [query, setQuery] = useState('')
  const [filter, setFilter] = useState('ALL')
  const [linkCopied, setLinkCopied] = useState(false)
  const load = async () => {
    setLoading(true)
    try {
      const [a, d, consultations, medicines] = await Promise.all([request<Page<Appointment>>('/appointments?page=0&size=100'), request<Page<Doctor>>('/doctors?page=0&size=100'), canClinical ? tokenPages<Consultation>(token, '/consultations') : Promise.resolve([]), canClinical ? tokenPages<Medicine>(token, '/medicines') : Promise.resolve([])])
      setItems(a.content.sort((x, y) => x.appointmentDateTime.localeCompare(y.appointmentDateTime)))
      setDoctors(d.content)
      setConsultationAppointmentIds(new Set(consultations.map(c => c.appointmentId)))
      setAvailableMedicines(medicines)
      setError('')
    } catch (e) { setError((e as Error).message) } finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [])
  useEffect(() => { const open = () => setShowForm(true); window.addEventListener('open-booking', open); return () => window.removeEventListener('open-booking', open) }, [])
  const pendingOnline = items.filter(item => item.bookingSource === 'ONLINE' && item.status === 'REQUESTED')
  const filtered = items.filter(item => (filter === 'ONLINE_REQUESTS'
    ? item.bookingSource === 'ONLINE' && item.status === 'REQUESTED'
    : filter === 'ALL' || item.status === filter)
    && `${item.patientName} ${item.patientPhone} ${item.doctorName} ${item.reason}`.toLowerCase().includes(query.toLowerCase()))
  const copyBookingLink = async () => {
    try {
      await navigator.clipboard.writeText(`${window.location.origin}/book`)
      setLinkCopied(true); window.setTimeout(() => setLinkCopied(false), 2500)
    } catch { setError('Could not copy the link. Copy this address: ' + `${window.location.origin}/book`) }
  }
  const action = async (appointment: Appointment, operation: 'confirm' | 'cancel'): Promise<boolean> => {
    try { await request(`/appointments/${appointment.appointmentId}/${operation}`, { method: 'POST' }); await load(); return true }
    catch (e) { setError((e as Error).message); return false }
  }
  return <>
    <div className="toolbar"><div className="search-box"><Search size={17} /><input value={query} onChange={e => setQuery(e.target.value)} placeholder="Search patient, doctor, or reason" /></div><select className="select-control" value={filter} onChange={e => setFilter(e.target.value)}><option value="ALL">All appointments</option><option value="ONLINE_REQUESTS">Online requests ({pendingOnline.length})</option>{['REQUESTED', 'CONFIRMED', 'RESCHEDULED', 'COMPLETED', 'CANCELLED'].map(s => <option key={s}>{s}</option>)}</select><button className="button button-outline" title="Local preview address only; deploy the frontend before sharing outside this computer" onClick={() => void copyBookingLink()}><Copy size={15} />{linkCopied ? 'Link copied' : 'Copy booking link'}</button><button className="button button-primary" onClick={() => setShowForm(true)}><Plus size={16} /> New staff booking</button></div>
    {pendingOnline.length > 0 && <div className="notice notice-info"><CalendarDays size={16} /><span><b>{pendingOnline.length} online request{pendingOnline.length === 1 ? '' : 's'}</b> are waiting for receptionist review. Confirming a request moves it to the confirmed appointments queue.</span><button className="button button-small button-soft" onClick={() => setFilter('ONLINE_REQUESTS')}>Review requests</button></div>}
    {error && <ErrorBanner message={error} />}
    <section className="panel data-panel"><div className="table-heading"><div><h2>Appointment list</h2><span>{filtered.length} visits in this view</span></div><div className="table-date"><CalendarDays size={15} /> Sorted by appointment time</div></div>
    {loading ? <LoadingRows /> : filtered.length === 0 ? <EmptyState icon={<CalendarDays />} title="No appointments found" text={filter === 'ONLINE_REQUESTS' ? 'There are no online requests waiting for review.' : 'Try another search or create a new appointment.'} /> : <div className="table-scroll"><table><thead><tr><th>PATIENT</th><th>DOCTOR</th><th>DATE & TIME</th><th>REASON</th><th>SOURCE</th><th>STATUS</th><th /></tr></thead><tbody>{filtered.map(item => <tr key={item.appointmentId}><td><div className="table-person"><div className="patient-avatar blue small-avatar">{item.patientName?.split(' ').map(s => s[0]).slice(0, 2).join('') || 'P'}</div><div><b>{item.patientName || `Patient ${item.patientId}`}</b><span>{item.patientPhone}</span></div></div></td><td>{item.doctorName || `Doctor ${item.doctorId}`}</td><td><b>{formatDate(item.appointmentDateTime)}</b><span className="table-sub">{formatTime(item.appointmentDateTime)}</span></td><td className="reason-cell">{item.reason}</td><td><span className={`source-pill source-${item.bookingSource?.toLowerCase()}`}>{item.bookingSource === 'ONLINE' ? 'Online request' : item.bookingSource === 'RECEPTIONIST' ? 'Receptionist' : 'Walk-in'}</span></td><td><StatusPill status={item.status} /></td><td><div className="row-actions">{canClinical && item.status === 'CONFIRMED' && !consultationAppointmentIds.has(item.appointmentId) && <button className="button button-small button-primary" onClick={() => setConsulting(item)}><Stethoscope size={14} /> Consult</button>}{canClinical && consultationAppointmentIds.has(item.appointmentId) && <span className="consultation-recorded">Consultation recorded</span>}{['REQUESTED', 'RESCHEDULED'].includes(item.status) && <button className="button button-small button-soft" onClick={() => item.bookingSource === 'ONLINE' && item.status === 'REQUESTED' ? setVerification(item) : void action(item, 'confirm')}>{item.bookingSource === 'ONLINE' && item.status === 'REQUESTED' ? 'Verify & confirm' : 'Confirm'}</button>}{['REQUESTED', 'RESCHEDULED', 'CONFIRMED'].includes(item.status) && <button className="icon-button danger-icon" title="Cancel appointment" onClick={() => void action(item, 'cancel')}><X size={16} /></button>}</div></td></tr>)}</tbody></table></div>}
    </section>
    {showForm && <BookingModal doctors={doctors} onClose={() => setShowForm(false)} onSuccess={() => { setShowForm(false); void load() }} request={request} />}
    {verification && <OnlineVerificationModal appointment={verification} doctors={doctors} request={request} onClose={() => setVerification(null)} onConfirm={async (doctorId, appointmentDateTime) => {
      try {
        await request(`/appointments/${verification.appointmentId}`, { method: 'PUT', body: JSON.stringify({ doctorId, appointmentDateTime, reason: verification.reason, phone: verification.patientPhone }) })
      } catch (e) { setError((e as Error).message); return false }
      const confirmed = await action(verification, 'confirm')
      if (confirmed) setVerification(null)
      return confirmed
    }} />}
    {consulting && <ConsultationModal appointment={consulting} medicines={availableMedicines} onClose={() => setConsulting(null)} onSuccess={() => { setConsulting(null); void load() }} request={request} />}
  </>
}

function OnlineVerificationModal({ appointment, doctors, onClose, onConfirm }: { appointment: Appointment; doctors: Doctor[]; request: <T,>(path: string, init?: RequestInit) => Promise<T>; onClose: () => void; onConfirm: (doctorId: number, appointmentDateTime: string) => Promise<boolean> }) {
  const [acknowledged, setAcknowledged] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!acknowledged) return
    const data = new FormData(event.currentTarget)
    const dateTime = `${data.get('date')}T${data.get('time')}:00`
    setBusy(true)
    setError('')
    try { if (!await onConfirm(Number(data.get('doctorId')), dateTime)) setError('This slot could not be confirmed. Choose another available time.') } finally { setBusy(false) }
  }
  return <Modal title="Verify online request" subtitle="Confirm the doctor and agree a suitable date and time with the patient." onClose={onClose}>
    <form className="form-grid" onSubmit={submit}>
      <div className="notice notice-info span-2"><ShieldCheck size={17} /><span><b>{appointment.patientName}</b> · {appointment.patientPhone}<br />{appointment.doctorName} · {formatDate(appointment.appointmentDateTime)} at {formatTime(appointment.appointmentDateTime)}</span></div>
      {error && <div className="form-error span-2">{error}</div>}
      <label className="span-2">Doctor<select name="doctorId" required defaultValue={appointment.doctorId}>{doctors.map(d => <option key={d.doctorId} value={d.doctorId}>{d.name} · {d.specialization}</option>)}</select></label>
      <label>Agreed date<input type="date" name="date" required min={new Date().toISOString().slice(0, 10)} defaultValue={appointment.appointmentDateTime.slice(0, 10)} /></label>
      <label>Agreed time<input type="time" name="time" required defaultValue={appointment.appointmentDateTime.slice(11, 16)} /></label>
      <label className="span-2 verify-check"><input type="checkbox" checked={acknowledged} onChange={event => setAcknowledged(event.target.checked)} /><span>I contacted the patient and verified their identity, phone number, and appointment details.</span></label>
      <div className="form-foot span-2"><span>The request stays pending until you confirm.</span><button className="button button-primary" disabled={!acknowledged || busy}>{busy ? 'Confirming…' : 'Confirm appointment'}</button></div>
    </form>
  </Modal>
}

function BookingModal({ doctors, onClose, onSuccess, request }: { doctors: Doctor[]; onClose: () => void; onSuccess: () => void; request: <T,>(path: string, init?: RequestInit) => Promise<T> }) {
  const [firstVisit, setFirstVisit] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError('')
    const data = new FormData(event.currentTarget)
    const local = `${data.get('date')}T${data.get('time')}:00`
    const body = {
      doctorId: Number(data.get('doctorId')), appointmentDateTime: local, reason: data.get('reason'), phone: data.get('phone'),
      ...(firstVisit ? { firstName: data.get('firstName'), lastName: data.get('lastName'), email: data.get('email'), dob: data.get('dob'), gender: data.get('gender'), bloodType: data.get('bloodType'), address: data.get('address'), allergies: data.get('allergies') } : {}),
    }
    try { await request('/appointments', { method: 'POST', body: JSON.stringify(body) }); onSuccess() }
    catch (e) { setError((e as Error).message) } finally { setBusy(false) }
  }
  return <Modal title="Book an appointment" subtitle="Reserve a visit within a doctor’s published schedule." onClose={onClose}>
    <form className="form-grid" onSubmit={submit}>
      {error && <div className="form-error span-2">{error}</div>}
      <label className="span-2">Doctor<select name="doctorId" required defaultValue=""><option value="" disabled>Select a doctor</option>{doctors.map(d => <option key={d.doctorId} value={d.doctorId}>{d.name} · {d.specialization}</option>)}</select></label>
      <label>Date<input type="date" name="date" required min={new Date().toISOString().slice(0, 10)} /></label><label>Start time<input type="time" name="time" required /></label>
      <label className="span-2">Reason for visit<input name="reason" required placeholder="e.g. Follow-up consultation" /></label>
      <label className="span-2">Patient mobile number<input name="phone" required inputMode="numeric" pattern="[6-9][0-9]{9}" placeholder="10-digit mobile number" /></label>
      <label className="check-row span-2"><input type="checkbox" checked={firstVisit} onChange={e => setFirstVisit(e.target.checked)} /><span>This is a first visit — register patient details</span></label>
      {firstVisit && <>
        <div className="form-hint span-2">If the number already belongs to a patient, their saved profile will be used.</div>
        <label>First name<input name="firstName" required /></label><label>Last name<input name="lastName" required /></label>
        <label>Email<input type="email" name="email" required /></label><label>Date of birth<input type="date" name="dob" required max={new Date().toISOString().slice(0, 10)} /></label>
        <label>Gender<select name="gender" required defaultValue=""><option value="" disabled>Select</option><option value="FEMALE">Female</option><option value="MALE">Male</option><option value="OTHER">Other</option></select></label>
        <label>Blood type<select name="bloodType" required defaultValue=""><option value="" disabled>Select</option>{['A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE'].map(v => <option key={v} value={v}>{v.replace('_', ' ').replace('_', ' ')}</option>)}</select></label>
        <label className="span-2">Address<input name="address" required /></label><label className="span-2">Allergies or “None reported”<input name="allergies" required /></label>
      </>}
      <div className="form-foot span-2"><span>Slot availability is checked when you submit.</span><button className="button button-primary" disabled={busy}>{busy ? 'Booking…' : 'Create booking'} <ArrowRight size={15} /></button></div>
    </form>
  </Modal>
}

function PatientsPage({ token, canClinical }: { token: TokenGetter; canClinical: boolean }) {
  const request = useTokenApi(token)
  const [page, setPage] = useState(0)
  const [patients, setPatients] = useState<Page<Patient> | null>(null)
  const [selected, setSelected] = useState<Patient | null>(null)
  const [history, setHistory] = useState<History | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  useEffect(() => { let live = true; setLoading(true)
    request<Page<Patient>>(`/patients?page=${page}&size=20`).then(result => live && setPatients(result)).catch(e => live && setError(e.message)).finally(() => live && setLoading(false))
    return () => { live = false }
  }, [page])
  const visible = (patients?.content ?? []).filter(p => `${p.firstName} ${p.lastName} ${p.phone} ${p.email}`.toLowerCase().includes(query.toLowerCase()))
  const openHistory = async (patient: Patient) => { setSelected(patient); setHistory(null); setError(''); try { setHistory(await request<History>(`/patients/${patient.patientId}/history?page=0&size=20`)) } catch (e) { setError((e as Error).message) } }
  return <>
    <div className="toolbar"><div className="search-box"><Search size={17} /><input value={query} onChange={e => setQuery(e.target.value)} placeholder="Search patient name, mobile, or email" /></div><span className="toolbar-caption">Patient information is role-restricted</span></div>
    {error && <ErrorBanner message={error} />}
    <section className="panel data-panel"><div className="table-heading"><div><h2>Patient directory</h2><span>{patients?.totalElements ?? 0} records</span></div><div className="pager"><button disabled={page === 0} onClick={() => setPage(p => p - 1)} aria-label="Previous page"><ChevronLeft size={17} /></button><span>Page {page + 1} of {Math.max(patients?.totalPages ?? 1, 1)}</span><button disabled={page + 1 >= (patients?.totalPages ?? 1)} onClick={() => setPage(p => p + 1)} aria-label="Next page"><ChevronRight size={17} /></button></div></div>
      {loading ? <LoadingRows /> : visible.length === 0 ? <EmptyState icon={<Users />} title="No patient records found" text="Try another name or mobile number." /> : <div className="table-scroll"><table><thead><tr><th>PATIENT</th><th>MOBILE</th><th>DATE OF BIRTH</th><th>ALLERGIES</th><th /></tr></thead><tbody>{visible.map(p => <tr key={p.patientId}><td><div className="table-person"><div className="patient-avatar violet small-avatar">{`${p.firstName?.[0] ?? ''}${p.lastName?.[0] ?? ''}`}</div><div><b>{p.firstName} {p.lastName}</b><span>{p.email}</span></div></div></td><td>{p.phone}</td><td>{p.dob ? formatDate(p.dob) : '—'}</td><td><span className={p.allergies?.toLowerCase().includes('none') ? 'allergy-clear' : 'allergy-alert'}>{p.allergies || 'Not recorded'}</span></td><td>{canClinical && <button className="button button-small button-soft" onClick={() => void openHistory(p)}>View history <ArrowRight size={14} /></button>}</td></tr>)}</tbody></table></div>}
    </section>
    {selected && <Modal title={`${selected.firstName} ${selected.lastName}`} subtitle={`Patient record · ${selected.phone}`} onClose={() => { setSelected(null); setHistory(null) }} wide>
      {!history ? error ? <div className="notice notice-error">{error}</div> : <LoadingRows /> : <>
        <div className="profile-summary"><div className="patient-avatar violet profile-large">{`${selected.firstName?.[0] ?? ''}${selected.lastName?.[0] ?? ''}`}</div><div><span className="eyebrow">PATIENT OVERVIEW</span><h3>{selected.firstName} {selected.lastName}</h3><span>{selected.gender || '—'} · {selected.bloodType?.replaceAll('_', ' ') || 'Blood type not recorded'} · DOB {selected.dob ? formatDate(selected.dob) : '—'}</span></div><div className="allergy-card"><span>ALLERGIES</span><b>{selected.allergies || 'Not recorded'}</b></div></div>
        <div className="history-title"><div><span className="eyebrow">CARE TIMELINE</span><h3>Previous encounters</h3></div><span>{history.totalElements} records</span></div>
        {history.consultations?.length ? <div className="history-list">{history.consultations.map(c => <article className="history-item" key={c.consultationId}><div className="history-date"><CalendarDays size={15} />{formatDate(c.consultationDateTime)}</div><div className="history-detail"><b>{c.chiefComplaint || 'Consultation'}</b><p>{c.diagnosis || 'Diagnosis not provided'}</p><span>{c.treatmentPlan}</span>{c.prescriptions?.flatMap(p => p.items ?? []).map((item, i) => <div className="rx-mini" key={i}><FileText size={14} />{item.medicineName} · {item.dosage} · {item.frequency}</div>)}</div></article>)}</div> : <EmptyState icon={<FileText />} title="No past encounters" text="Clinical history will appear after a consultation is recorded." />}
      </>}
    </Modal>}
  </>
}

function DoctorsPage({ token, canManage, clinicianIdentity }: { token: TokenGetter; canManage: boolean; clinicianIdentity: { email: string; name: string } }) {
  const [doctors, setDoctors] = useState<Doctor[]>([])
  const [editing, setEditing] = useState<Doctor | null | 'new'>(null)
  const [loading, setLoading] = useState(true)
  const ownDoctor = doctors.find(doctor => clinicianIdentity.email && doctor.email.toLowerCase() === clinicianIdentity.email.toLowerCase())
  const [error, setError] = useState('')
  const load = async () => { setLoading(true); try { setDoctors(await tokenPages<Doctor>(token, '/doctors')); setError('') } catch (e) { setError((e as Error).message) } finally { setLoading(false) } }
  useEffect(() => { void load() }, [])
  return <>
    <div className="toolbar"><span className="toolbar-caption">Doctors need published weekly hours before patients can book online.</span>{canManage ? <button className="button button-primary" onClick={() => setEditing('new')}><Plus size={16} /> Add doctor</button> : !ownDoctor && <button className="button button-primary" onClick={() => setEditing('new')}><Plus size={16} /> Register my doctor profile</button>}</div>
    {!canManage && <div className="notice notice-info"><Stethoscope size={17} /><span>{ownDoctor ? `Your doctor profile email matches ${ownDoctor.email}. The API also needs this email in your access token to show your appointment queue. If the queue is empty, check the Post Login Action described in PIS_CLINICAL_WORKFLOW.md; deploy the email claim and sign out and back in.` : 'Create a doctor profile for your signed-in clinician account. Your authenticated email will be linked automatically; an administrator can then publish your schedule.'}</span></div>}
    {error && <ErrorBanner message={error} />}
    <section className="panel data-panel"><div className="table-heading"><div><h2>Clinic doctors</h2><span>{doctors.length} doctor{doctors.length === 1 ? '' : 's'} registered</span></div><button className="button button-quiet" onClick={() => void load()}><ArrowDownToLine size={15} /> Refresh</button></div>
      {loading ? <LoadingRows /> : (canManage ? doctors : ownDoctor ? [ownDoctor] : []).length === 0 ? <EmptyState icon={<Stethoscope />} title={canManage ? 'No doctors have been added' : 'Your doctor profile is not set up'} text={canManage ? 'Add a doctor first, then publish their weekly schedule so patients can request an appointment.' : 'Register your doctor profile using the button above. Your admin can then add weekly hours.'} /> : <div className="doctor-grid">{(canManage ? doctors : ownDoctor ? [ownDoctor] : []).map(doctor => <article className="doctor-card" key={doctor.doctorId}><div className="doctor-avatar"><Stethoscope size={20} /></div><div className="doctor-card-main"><h3>Dr. {doctor.name}</h3><span>{doctor.specialization}</span></div><div className="doctor-details"><span>License <b>{doctor.licenseNo}</b></span><span>Email <b>{doctor.email}</b></span><span>Phone <b>{doctor.phone || 'Not provided'}</b></span></div>{canManage && <button className="button button-outline button-small" onClick={() => setEditing(doctor)}>Edit doctor</button>}</article>)}</div>}
    </section>
    {editing && <DoctorModal doctor={editing === 'new' ? null : editing} onClose={() => setEditing(null)} onSuccess={() => { setEditing(null); void load() }} token={token} clinicianIdentity={clinicianIdentity} selfRegistration={!canManage && editing === 'new'} />}
  </>
}

function DoctorModal({ doctor, onClose, onSuccess, token, clinicianIdentity, selfRegistration = false }: { doctor: Doctor | null; onClose: () => void; onSuccess: () => void; token: TokenGetter; clinicianIdentity: { email: string; name: string }; selfRegistration?: boolean }) {
  const request = useTokenApi(token)
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(''); setBusy(true)
    const data = new FormData(event.currentTarget)
    const body = { name: data.get('name'), specialization: data.get('specialization'), licenseNo: data.get('licenseNo'), email: data.get('email'), phone: data.get('phone') || null }
    try { await request(doctor ? `/doctors/${doctor.doctorId}` : selfRegistration ? '/doctors/me' : '/doctors', { method: doctor ? 'PUT' : 'POST', body: JSON.stringify(body) }); onSuccess() }
    catch (problem) { setError((problem as Error).message) } finally { setBusy(false) }
  }
  return <Modal title={doctor ? 'Edit doctor' : selfRegistration ? 'Register my doctor profile' : 'Add a doctor'} subtitle="Doctors appear in the public booking form after at least one weekly schedule is published." onClose={onClose}>
    <form className="form-grid" onSubmit={submit}>{error && <div className="form-error span-2">{error}</div>}
      <label className="span-2">Full name<input name="name" defaultValue={doctor?.name ?? (selfRegistration ? clinicianIdentity.name : '')} autoComplete="name" required /></label>
      <label>Specialization<input name="specialization" defaultValue={doctor?.specialization ?? ''} required /></label><label>Medical license number<input name="licenseNo" defaultValue={doctor?.licenseNo ?? ''} required /></label>
      <label>Email<input name="email" type="email" defaultValue={doctor?.email ?? (selfRegistration ? clinicianIdentity.email : '')} readOnly={selfRegistration} required /></label><label>Mobile number<input name="phone" inputMode="numeric" pattern="[6-9][0-9]{9}" defaultValue={doctor?.phone ?? ''} placeholder="10-digit number (optional)" /></label>
      <div className="form-foot span-2"><span>Doctor profiles with appointment history cannot be deleted.</span><button className="button button-primary" disabled={busy}>{busy ? 'Saving…' : doctor ? 'Save doctor' : 'Create doctor'}</button></div>
    </form>
  </Modal>
}

function MedicineCatalogPage({ token, canManage }: { token: TokenGetter; canManage: boolean }) {
  const request = useTokenApi(token)
  const [medicines, setMedicines] = useState<Medicine[]>([])
  const [editing, setEditing] = useState<Medicine | null | 'new'>(null)
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [importing, setImporting] = useState(false)
  const load = async () => { setLoading(true); try { setMedicines(await tokenPages<Medicine>(token, '/medicines')); setError('') } catch (e) { setError((e as Error).message) } finally { setLoading(false) } }
  useEffect(() => { void load() }, [])
  const visible = medicines.filter(m => `${m.medicineName} ${m.activeIng} ${m.genericUse} ${m.strength}`.toLowerCase().includes(query.toLowerCase()))
  const remove = async (medicine: Medicine) => {
    if (!window.confirm(`Remove ${medicine.medicineName} from the catalog? The API will prevent removal if prescriptions reference it.`)) return
    try { await request(`/medicines/${medicine.medicineId}`, { method: 'DELETE' }); setNotice(`${medicine.medicineName} removed from the catalog.`); await load() } catch (e) { setError((e as Error).message) }
  }
  const importCsv = async (file?: File) => {
    if (!file) return
    setImporting(true); setError(''); setNotice('')
    try {
      const rows = parseCsv(await file.text())
      const header = rows.shift()?.map(value => value.replace(/^\uFEFF/, '').trim().toLowerCase().replace(/[ _-]/g, '')) ?? []
      const fields = [
        { header: 'medicinename', property: 'medicineName' },
        { header: 'medicinedescription', property: 'medicineDescription' },
        { header: 'genericuse', property: 'genericUse' },
        { header: 'activeing', property: 'activeIng' },
        { header: 'strength', property: 'strength' },
      ]
      const indexes = fields.map(field => header.indexOf(field.header))
      if (indexes.some(index => index < 0)) throw new Error('CSV header must include: medicineName, medicineDescription, genericUse, activeIng, strength.')
      let created = 0; const failures: string[] = []
      for (let index = 0; index < rows.length; index++) {
        const row = rows[index]
        if (row.every(value => !value.trim())) continue
        const body = Object.fromEntries(fields.map((field, column) => [field.property, row[indexes[column]]?.trim() ?? '']))
        if (Object.values(body).some(value => !value)) { failures.push(`Row ${index + 2}: required medicine fields are blank.`); continue }
        try { await request('/medicines', { method: 'POST', body: JSON.stringify(body) }); created++ }
        catch (e) { failures.push(`Row ${index + 2} (${body.medicineName}): ${(e as Error).message}`) }
      }
      await load()
      setNotice(`${created} medicine${created === 1 ? '' : 's'} imported.${failures.length ? ` ${failures.length} row(s) need attention: ${failures.slice(0, 3).join(' ')}` : ''}`)
      if (failures.length) setError(failures.length > 3 ? `${failures.slice(3).join(' ')}${failures.length > 3 ? ` ${failures.length - 3} more row error(s).` : ''}` : '')
    } catch (e) { setError((e as Error).message) } finally { setImporting(false) }
  }
  return <>
    <div className="notice notice-info pis-intro"><Pill size={17} /><span><b>Medicine catalog → prescription</b><br />Clinicians select catalog medicines when issuing prescriptions. This catalog does not track pharmacy stock, batches, expiry, or dispensing.</span></div>
    <div className="toolbar"><div className="search-box"><Search size={17} /><input value={query} onChange={e => setQuery(e.target.value)} placeholder="Search medicine, ingredient, use, or strength" /></div><a className="button button-quiet" href="/medicines-nlem-2022.csv" download><ArrowDownToLine size={15} />Download NLEM 2022 CSV</a>{canManage && <><label className="button button-outline import-button"><ArrowDownToLine size={15} />{importing ? 'Importing…' : 'Import CSV'}<input type="file" accept=".csv,text/csv" disabled={importing} onChange={e => { void importCsv(e.target.files?.[0]); e.currentTarget.value = '' }} /></label><button className="button button-primary" onClick={() => setEditing('new')}><Plus size={16} /> Add medicine</button></>}</div>
    {canManage && <p className="catalog-help">CSV columns: medicineName, medicineDescription, genericUse, activeIng, strength. Duplicate or invalid rows are reported and skipped.</p>}
    {notice && <div className="notice notice-info">{notice}</div>}{error && <ErrorBanner message={error} />}
    <section className="panel data-panel"><div className="table-heading"><div><h2>Medicine catalog</h2><span>{visible.length} of {medicines.length} medicines</span></div><button className="button button-quiet" onClick={() => void load()}><ArrowDownToLine size={15} /> Refresh</button></div>
      {loading ? <LoadingRows /> : visible.length === 0 ? <EmptyState icon={<Pill />} title="No medicines found" text={medicines.length ? 'Try another search.' : canManage ? 'Add a medicine or import a CSV to make it available for prescriptions.' : 'Ask an administrator to add medicines to the clinic catalog.'} /> : <div className="medicine-grid">{visible.map(m => <article className="medicine-card" key={m.medicineId}><div className="medicine-card-title"><div className="medicine-icon"><Pill size={17} /></div><div><h3>{m.medicineName}</h3><span>{m.strength}</span></div></div><p>{m.medicineDescription}</p><div className="medicine-meta"><span><small>ACTIVE INGREDIENT</small><b>{m.activeIng}</b></span><span><small>GENERIC USE</small><b>{m.genericUse}</b></span></div>{canManage && <div className="medicine-actions"><button className="button button-small button-outline" onClick={() => setEditing(m)}>Edit</button><button className="button button-small button-soft" onClick={() => void remove(m)}>Remove</button></div>}</article>)}</div>}
    </section>
    {editing && <MedicineModal medicine={editing === 'new' ? null : editing} onClose={() => setEditing(null)} onSuccess={() => { setEditing(null); void load() }} request={request} />}
  </>
}

function MedicineModal({ medicine, onClose, onSuccess, request }: { medicine: Medicine | null; onClose: () => void; onSuccess: () => void; request: <T,>(path: string, init?: RequestInit) => Promise<T> }) {
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  async function submit(e: FormEvent<HTMLFormElement>) { e.preventDefault(); setBusy(true); setError(''); const data = new FormData(e.currentTarget)
    const body = Object.fromEntries(['medicineName', 'medicineDescription', 'genericUse', 'activeIng', 'strength'].map(key => [key, data.get(key)]))
    try { await request(medicine ? `/medicines/${medicine.medicineId}` : '/medicines', { method: medicine ? 'PUT' : 'POST', body: JSON.stringify(body) }); onSuccess() } catch (e) { setError((e as Error).message) } finally { setBusy(false) }
  }
  return <Modal title={medicine ? 'Edit medicine' : 'Add a medicine'} subtitle="Catalog entries become available for clinician prescriptions." onClose={onClose}>
    <form className="form-grid" onSubmit={submit}>{error && <div className="form-error span-2">{error}</div>}
      <label className="span-2">Medicine name<input name="medicineName" required defaultValue={medicine?.medicineName} /></label>
      <label className="span-2">Description<input name="medicineDescription" required defaultValue={medicine?.medicineDescription} /></label>
      <label>Active ingredient<input name="activeIng" required defaultValue={medicine?.activeIng} /></label><label>Strength<input name="strength" required defaultValue={medicine?.strength} placeholder="e.g. 500 mg" /></label>
      <label className="span-2">Generic use<input name="genericUse" required defaultValue={medicine?.genericUse} /></label>
      {medicine && <p className="form-hint span-2">The API protects medicine identity fields after they are used in a prescription.</p>}
      <div className="form-foot span-2"><span>Prescription records keep the selected catalog reference.</span><button className="button button-primary" disabled={busy}>{busy ? 'Saving…' : medicine ? 'Save changes' : 'Add medicine'}</button></div>
    </form>
  </Modal>
}

function parseCsv(text: string): string[][] {
  const rows: string[][] = []; let row: string[] = []; let value = ''; let quoted = false
  for (let i = 0; i < text.length; i++) {
    const char = text[i]
    if (char === '"' && quoted && text[i + 1] === '"') { value += '"'; i++ }
    else if (char === '"') quoted = !quoted
    else if (char === ',' && !quoted) { row.push(value); value = '' }
    else if ((char === '\n' || char === '\r') && !quoted) { if (char === '\r' && text[i + 1] === '\n') i++; row.push(value); rows.push(row); row = []; value = '' }
    else value += char
  }
  if (quoted) throw new Error('CSV contains an unclosed quoted field.')
  if (value.length || row.length) { row.push(value); rows.push(row) }
  return rows
}

function SchedulesPage({ token, onNavigate }: { token: TokenGetter; onNavigate: (s: Section) => void }) {
  const request = useTokenApi(token)
  const [schedules, setSchedules] = useState<Schedule[]>([])
  const [doctors, setDoctors] = useState<Doctor[]>([])
  const [editing, setEditing] = useState<Schedule | null>(null)
  const [creating, setCreating] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const load = async () => { setLoading(true); try { const [s, d] = await Promise.all([tokenPages<Schedule>(token, '/schedules'), tokenPages<Doctor>(token, '/doctors')]); setSchedules(s); setDoctors(d); setError('') } catch (e) { setError((e as Error).message) } finally { setLoading(false) } }
  useEffect(() => { void load() }, [])
  const doctorName = (id: number) => doctors.find(d => d.doctorId === id)?.name || `Doctor ${id}`
  return <>
    <div className="notice notice-info"><ShieldCheck size={17} /><span>Slot duration is locked while active future appointments exist. Other schedule changes are validated against booking rules.</span></div>
    {error && <ErrorBanner message={error} />}
    <section className="panel data-panel"><div className="table-heading"><div><h2>Doctor schedules</h2><span>{schedules.length} weekly schedules</span></div><div className="table-actions"><button className="button button-quiet" onClick={() => void load()}><ArrowDownToLine size={15} /> Refresh</button><button className="button button-primary" disabled={!doctors.length} title={!doctors.length ? 'Add a doctor first' : 'Create weekly schedule'} onClick={() => setCreating(true)}><Plus size={15} /> Add schedule</button></div></div>
      {loading ? <LoadingRows /> : schedules.length === 0 ? <EmptyState icon={<Clock3 />} title={doctors.length ? 'No schedules configured' : 'Add a doctor before scheduling'} text={doctors.length ? 'Create weekly hours so patients can see availability and request appointments.' : 'A doctor profile is required before weekly hours can be added.'} /> : <div className="schedule-cards">{schedules.map(s => <article className="schedule-card" key={s.scheduleId}><div className="schedule-card-top"><div className="day-badge">{s.dayOfWeek.slice(0, 3)}</div><span className="schedule-active"><i /> Published</span></div><h3>Dr. {doctorName(s.doctorId)}</h3><span className="schedule-specialty">{doctors.find(d => d.doctorId === s.doctorId)?.specialization || 'Doctor'}</span><div className="schedule-time"><Clock3 size={15} />{s.startTime.slice(0, 5)} <span>to</span> {s.endTime.slice(0, 5)}</div><div className="schedule-card-foot"><span>{s.slotDuration} min appointments</span><button className="button button-small button-outline" onClick={() => setEditing(s)}>Edit hours</button></div>{!s.slotDurationEditable && <div className="locked-note" title={s.slotDurationEditMessage ?? ''}><ShieldCheck size={14} />Duration locked by bookings</div>}</article>)}</div>}
      {!doctors.length && !loading && <button className="button button-quiet" onClick={() => onNavigate('Doctors')}>Go to doctor management <ArrowRight size={15} /></button>}
    </section>
    {editing && <ScheduleModal schedule={editing} doctorName={doctorName(editing.doctorId)} onClose={() => setEditing(null)} onSuccess={() => { setEditing(null); void load() }} request={request} />}
    {creating && <CreateScheduleModal doctors={doctors} existingSchedules={schedules} onClose={() => { setCreating(false); void load() }} onSuccess={() => { setCreating(false); void load() }} request={request} />}
  </>
}

type ClinicWorkspace = { name: string; address: string; phone: string; email: string; website: string }
function ClinicWorkspacePage({ token }: { token: TokenGetter }) {
  const request = useTokenApi(token)
  const [clinic, setClinic] = useState<ClinicWorkspace>({ name: '', address: '', phone: '', email: '', website: '' })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [saved, setSaved] = useState(false)
  useEffect(() => { request<ClinicWorkspace>('/clinic-workspace').then(setClinic).catch(e => setError(e.message)).finally(() => setLoading(false)) }, [])
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setSaving(true); setError(''); setSaved(false)
    try { setClinic(await request<ClinicWorkspace>('/clinic-workspace', { method: 'PUT', body: JSON.stringify(clinic) })); setSaved(true) }
    catch (e) { setError((e as Error).message) } finally { setSaving(false) }
  }
  if (loading) return <LoadingRows />
  return <section className="panel data-panel"><div className="table-heading"><div><span className="eyebrow">CLINIC PROFILE</span><h2>Workspace details</h2><span>These details identify your clinic to staff.</span></div></div>
    <form className="form-grid" onSubmit={submit}>{error && <div className="form-error span-2">{error}</div>}{saved && <div className="notice notice-info span-2">Clinic details saved.</div>}
      <label className="span-2">Clinic name<input required value={clinic.name} onChange={e => setClinic({ ...clinic, name: e.target.value })} /></label>
      <label className="span-2">Address<input value={clinic.address ?? ''} onChange={e => setClinic({ ...clinic, address: e.target.value })} /></label>
      <label>Phone<input value={clinic.phone ?? ''} onChange={e => setClinic({ ...clinic, phone: e.target.value })} /></label><label>Email<input type="email" value={clinic.email ?? ''} onChange={e => setClinic({ ...clinic, email: e.target.value })} /></label>
      <label className="span-2">Website<input type="url" value={clinic.website ?? ''} onChange={e => setClinic({ ...clinic, website: e.target.value })} /></label>
      <div className="form-foot span-2"><span>Only clinic administrators can edit this profile.</span><button className="button button-primary" disabled={saving}>{saving ? 'Saving…' : 'Save clinic profile'}</button></div>
    </form>
  </section>
}

function ScheduleModal({ schedule, doctorName, onClose, onSuccess, request }: { schedule: Schedule; doctorName: string; onClose: () => void; onSuccess: () => void; request: <T,>(path: string, init?: RequestInit) => Promise<T> }) {
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  async function submit(e: FormEvent<HTMLFormElement>) { e.preventDefault(); setBusy(true); setError(''); const data = new FormData(e.currentTarget)
    const body = { doctorId: schedule.doctorId, dayOfWeek: data.get('day'), startTime: data.get('start'), endTime: data.get('end'), slotDuration: schedule.slotDurationEditable ? Number(data.get('duration')) : schedule.slotDuration }
    try { await request(`/schedules/${schedule.scheduleId}`, { method: 'PUT', body: JSON.stringify(body) }); onSuccess() } catch (err) { setError((err as Error).message) } finally { setBusy(false) }
  }
  return <Modal title="Edit doctor hours" subtitle={`${doctorName} · ${schedule.dayOfWeek}`} onClose={onClose}>
    <form className="form-grid" onSubmit={submit}>{error && <div className="form-error span-2">{error}</div>}
      <label>Day<select name="day" defaultValue={schedule.dayOfWeek}>{['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'].map(day => <option key={day}>{day}</option>)}</select></label>
      <div className="form-spacer" />
      <label>Start time<input name="start" type="time" defaultValue={schedule.startTime.slice(0, 5)} required /></label><label>End time<input name="end" type="time" defaultValue={schedule.endTime.slice(0, 5)} required /></label>
      <label className="span-2">Slot duration (minutes)<input name="duration" type="number" min="1" step="1" defaultValue={schedule.slotDuration} disabled={!schedule.slotDurationEditable} title={schedule.slotDurationEditMessage ?? undefined} /></label>
      {!schedule.slotDurationEditable && <div className="duration-lock span-2"><ShieldCheck size={16} /><span>{schedule.slotDurationEditMessage}</span></div>}
      <div className="form-foot span-2"><span>Changes must preserve valid slot capacity.</span><button className="button button-primary" disabled={busy}>{busy ? 'Saving…' : 'Save schedule'}</button></div>
    </form>
  </Modal>
}

function CreateScheduleModal({ doctors, existingSchedules, onClose, onSuccess, request }: { doctors: Doctor[]; existingSchedules: Schedule[]; onClose: () => void; onSuccess: () => void; request: <T,>(path: string, init?: RequestInit) => Promise<T> }) {
  const weekdays = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY']
  const [doctorId, setDoctorId] = useState('')
  const [hours, setHours] = useState<Record<string, { enabled: boolean; start: string; end: string; duration: number }>>(() => Object.fromEntries(weekdays.map(day => [day, { enabled: false, start: '09:00', end: '17:00', duration: 30 }])))
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  const currentSchedules = existingSchedules.filter(schedule => String(schedule.doctorId) === doctorId)
  const selectDoctor = (id: string) => {
    setDoctorId(id)
    const forDoctor = existingSchedules.filter(schedule => String(schedule.doctorId) === id)
    setHours(Object.fromEntries(weekdays.map(day => {
      const existing = forDoctor.find(schedule => schedule.dayOfWeek === day)
      return [day, { enabled: Boolean(existing), start: existing?.startTime.slice(0, 5) ?? '09:00', end: existing?.endTime.slice(0, 5) ?? '17:00', duration: existing?.slotDuration ?? 30 }]
    })))
  }
  const change = (day: string, key: 'enabled' | 'start' | 'end' | 'duration', value: boolean | string | number) => setHours(previous => ({ ...previous, [day]: { ...previous[day], [key]: value } }))
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError('')
    const selected = weekdays.filter(day => hours[day].enabled)
    if (!selected.length) { setError('Select at least one weekday.'); return }
    setBusy(true)
    try {
      for (const day of selected) {
        const existing = currentSchedules.find(schedule => schedule.dayOfWeek === day)
        const body = { doctorId: Number(doctorId), dayOfWeek: day, startTime: hours[day].start, endTime: hours[day].end, slotDuration: existing && !existing.slotDurationEditable ? existing.slotDuration : hours[day].duration }
        try { await request(existing ? `/schedules/${existing.scheduleId}` : '/schedules', { method: existing ? 'PUT' : 'POST', body: JSON.stringify(body) }) }
        catch (problem) { throw new Error(`${day}: ${(problem as Error).message}. Earlier weekdays in this submission may already have been saved; close this form and refresh to review them.`) }
      }
      onSuccess()
    } catch (problem) { setError((problem as Error).message) } finally { setBusy(false) }
  }
  return <Modal title="Set weekly doctor hours" subtitle="Choose several weekdays and set each day’s hours in one form." onClose={onClose} wide>
    <form className="form-grid" onSubmit={submit}>{error && <div className="form-error span-2">{error}</div>}
      <label className="span-2">Doctor<select required value={doctorId} onChange={event => selectDoctor(event.target.value)}><option value="" disabled>Choose a doctor</option>{doctors.map(d => <option key={d.doctorId} value={d.doctorId}>Dr. {d.name} · {d.specialization}</option>)}</select></label>
      <div className="weekly-hours-list span-2">{weekdays.map(day => {
        const existing = currentSchedules.find(schedule => schedule.dayOfWeek === day)
        const row = hours[day]
        return <div className={`weekly-hours-row ${row.enabled ? 'weekly-hours-enabled' : ''}`} key={day}>
          <label className="weekly-day-toggle"><input type="checkbox" checked={row.enabled} disabled={!doctorId} onChange={event => change(day, 'enabled', event.target.checked)} /><b>{day[0] + day.slice(1).toLowerCase()}</b>{existing && <small>Already configured</small>}</label>
          <label>Start<input type="time" value={row.start} disabled={!row.enabled} required={row.enabled} onChange={event => change(day, 'start', event.target.value)} /></label>
          <label>End<input type="time" value={row.end} disabled={!row.enabled} required={row.enabled} onChange={event => change(day, 'end', event.target.value)} /></label>
          <label>Slot length<select value={row.duration} disabled={!row.enabled || Boolean(existing && !existing.slotDurationEditable)} title={existing && !existing.slotDurationEditable ? existing.slotDurationEditMessage ?? undefined : undefined} onChange={event => change(day, 'duration', Number(event.target.value))}>{[15, 20, 30, 40, 45, 60].map(minutes => <option key={minutes} value={minutes}>{minutes} min</option>)}</select></label>
          {existing && !existing.slotDurationEditable && <span className="weekly-duration-lock" title={existing.slotDurationEditMessage ?? ''}><ShieldCheck size={13} /> Locked</span>}
        </div>
      })}</div>
      <div className="form-foot span-2"><span>Existing weekdays are updated. Slot length stays locked while active future bookings exist.</span><button className="button button-primary" disabled={busy || !doctorId}>{busy ? 'Saving weekly hours…' : 'Save weekly hours'}</button></div>
    </form>
  </Modal>
}

function HelpDialog({ onClose, onNavigate }: { onClose: () => void; onNavigate: (section: Section) => void }) {
  return <Modal title="eMedic care guide" subtitle="Quick paths for setting up the clinic and handling booking requests." onClose={onClose}>
    <div className="help-guide-list">
      <button onClick={() => onNavigate('Doctors')}><Stethoscope size={17} /><span><b>1. Add doctors</b><small>Enter each doctor's identity and contact details.</small></span><ArrowRight size={15} /></button>
      <button onClick={() => onNavigate('Schedules')}><Clock3 size={17} /><span><b>2. Publish weekly schedules</b><small>Set weekdays, hours, and appointment lengths.</small></span><ArrowRight size={15} /></button>
      <button onClick={() => onNavigate('Appointments')}><CalendarDays size={17} /><span><b>3. Review online requests</b><small>Confirm a request after the receptionist verifies it.</small></span><ArrowRight size={15} /></button>
      <a href="/book" target="_blank" rel="noreferrer"><ArrowRight size={17} /><span><b>Preview public booking</b><small>Open the link patients can use to request a visit.</small></span><ArrowRight size={15} /></a>
    </div>
  </Modal>
}

function ClinicalPage({ token }: { token: TokenGetter }) {
  const request = useTokenApi(token)
  const [appointments, setAppointments] = useState<Appointment[]>([])
  const [consultations, setConsultations] = useState<Consultation[]>([])
  const [medicines, setMedicines] = useState<Medicine[]>([])
  const [selectedAppointment, setSelectedAppointment] = useState<Appointment | null>(null)
  const [prescribing, setPrescribing] = useState<Consultation | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const load = async () => { setLoading(true); try {
    const [a, c, m] = await Promise.all([tokenPages<Appointment>(token, '/appointments'), tokenPages<Consultation>(token, '/consultations'), tokenPages<Medicine>(token, '/medicines')])
    setAppointments(a); setConsultations(c); setMedicines(m); setError('')
  } catch (e) { setError((e as Error).message) } finally { setLoading(false) } }
  useEffect(() => { void load() }, [])
  const completedAppointments = new Set(consultations.map(c => c.appointmentId))
  const ready = appointments.filter(a => a.status === 'CONFIRMED' && !completedAppointments.has(a.appointmentId))
  return <>
    {error && <ErrorBanner message={error} />}
    <div className="clinical-grid">
      <section className="panel data-panel"><div className="table-heading"><div><span className="eyebrow">VISIT WORKFLOW</span><h2>Ready for consultation</h2><span>Only confirmed visits can start a clinical record.</span></div><span className="count-badge">{ready.length}</span></div>
        {loading ? <LoadingRows /> : ready.length ? <div className="ready-list">{ready.map(a => <article className="ready-card" key={a.appointmentId}><div className="ready-icon"><Stethoscope size={18} /></div><div className="ready-info"><b>{a.patientName || `Patient ${a.patientId}`}</b><span>{a.reason} · {formatDate(a.appointmentDateTime)} at {formatTime(a.appointmentDateTime)}</span><small>{a.doctorName}</small></div><button className="button button-small button-primary" onClick={() => setSelectedAppointment(a)}><FilePlus2 size={15} /> Record visit</button></article>)}</div> : <EmptyState icon={<Check />} title="No visits waiting" text="Confirmed appointments without a consultation will appear here." />}
      </section>
      <section className="panel data-panel clinical-history-panel"><div className="table-heading"><div><span className="eyebrow">RECENT DOCUMENTATION</span><h2>Consultations</h2><span>{consultations.length} records loaded</span></div></div>
        {loading ? <LoadingRows /> : consultations.slice().sort((x, y) => y.consultationDatetime.localeCompare(x.consultationDatetime)).slice(0, 8).map(c => <article className="consultation-row" key={c.consultationId}><div className="consultation-date"><span>{new Date(c.consultationDatetime).toLocaleDateString('en', { day: '2-digit' })}</span><small>{new Date(c.consultationDatetime).toLocaleDateString('en', { month: 'short' })}</small></div><div><b>{c.chiefComplaint}</b><span>{c.diagnosis}</span><small>Appointment #{c.appointmentId}</small></div><button className="icon-button" title="Create a replacement prescription" onClick={() => setPrescribing(c)}><FilePlus2 size={16} /></button></article>)}
      </section>
    </div>
    {selectedAppointment && <ConsultationModal appointment={selectedAppointment} medicines={medicines} onClose={() => setSelectedAppointment(null)} onSuccess={() => { setSelectedAppointment(null); void load() }} request={request} />}
    {prescribing && <PrescriptionModal consultation={prescribing} medicines={medicines} onClose={() => setPrescribing(null)} onSuccess={() => { setPrescribing(null); void load() }} request={request} />}
  </>
}

type DraftPrescriptionItem = { medicineId: number; dosage: string; frequency: string; durationDays: string; instructions: string }

function ConsultationModal({ appointment, medicines, onClose, onSuccess, request }: { appointment: Appointment; medicines: Medicine[]; onClose: () => void; onSuccess: () => void; request: <T,>(path: string, init?: RequestInit) => Promise<T> }) {
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  const [prescriptionItems, setPrescriptionItems] = useState<DraftPrescriptionItem[]>([])
  const [medicinePickerOpen, setMedicinePickerOpen] = useState(false)
  const [prescriptionExpiry, setPrescriptionExpiry] = useState('')
  const updatePrescriptionItem = (index: number, key: keyof DraftPrescriptionItem, value: string) => setPrescriptionItems(rows => rows.map((row, rowIndex) => rowIndex === index ? { ...row, [key]: value } : row))
  async function submit(e: FormEvent<HTMLFormElement>) { e.preventDefault(); setBusy(true); setError(''); const data = new FormData(e.currentTarget)
    const consultation = { appointmentId: appointment.appointmentId, chiefComplaint: data.get('complaint'), symptoms: data.get('symptoms'), clinicalNotes: data.get('notes'), diagnosis: data.get('diagnosis'), examinationFinding: data.get('exam'), treatmentPlan: data.get('plan'), followUpdate: data.get('followUp') ? `${data.get('followUp')}T09:00:00` : null }
    const body = { consultation, prescriptionItems: prescriptionItems.map(item => ({ ...item, durationDays: Number(item.durationDays) })), prescriptionExpiryDate: prescriptionExpiry ? `${prescriptionExpiry}T23:59:00` : null }
    try { await request('/consultations/record', { method: 'POST', body: JSON.stringify(body) }); onSuccess() } catch (err) { setError((err as Error).message) } finally { setBusy(false) }
  }
  return <Modal title="Document consultation" subtitle={`${appointment.patientName} · ${formatDate(appointment.appointmentDateTime)}`} onClose={onClose} wide>
    <div className="clinical-safety"><ShieldCheck size={16} /> This note is immutable after creation. Review the details before saving.</div>
    <form className="form-grid" onSubmit={submit}>{error && <div className="form-error span-2">{error}</div>}
      <label>Chief complaint<input name="complaint" required /></label><label>Diagnosis<input name="diagnosis" required /></label>
      <label className="span-2">Symptoms<textarea name="symptoms" rows={2} required /></label><label className="span-2">Examination findings<textarea name="exam" rows={2} required /></label>
      <label className="span-2">Clinical notes<textarea name="notes" rows={3} required /></label><label className="span-2">Treatment plan<textarea name="plan" rows={2} required /></label>
      <section className="consultation-medicines span-2"><div className="consultation-medicine-heading"><div><b>Medicines for this visit</b><span>Select from the catalog and add prescription instructions.</span></div><button type="button" className="button button-small button-outline" onClick={() => setMedicinePickerOpen(true)}><Plus size={14} /> Add medicine</button></div>
        {!prescriptionItems.length ? <p className="medicine-empty-hint">No medicine selected. You can save the consultation without a prescription.</p> : <div className="consultation-medicine-list">{prescriptionItems.map((item, index) => {
          const medicine = medicines.find(candidate => candidate.medicineId === item.medicineId)
          return <article className="consultation-medicine-item" key={item.medicineId}><div className="consultation-medicine-title"><div><b>{medicine?.medicineName ?? `Medicine ${item.medicineId}`}</b><span>{medicine?.strength}</span></div><button type="button" className="icon-button danger-icon" aria-label={`Remove ${medicine?.medicineName ?? 'medicine'}`} onClick={() => setPrescriptionItems(rows => rows.filter((_, rowIndex) => rowIndex !== index))}><X size={15} /></button></div><div className="form-grid"><label>Dosage<input required value={item.dosage} onChange={event => updatePrescriptionItem(index, 'dosage', event.target.value)} placeholder="e.g. 1 tablet" /></label><label>Frequency<input required value={item.frequency} onChange={event => updatePrescriptionItem(index, 'frequency', event.target.value)} placeholder="e.g. Twice daily" /></label><label>Duration (days)<input type="number" min="1" required value={item.durationDays} onChange={event => updatePrescriptionItem(index, 'durationDays', event.target.value)} /></label><label>Instructions<input required value={item.instructions} onChange={event => updatePrescriptionItem(index, 'instructions', event.target.value)} placeholder="e.g. After food" /></label></div></article>
        })}<label className="consultation-expiry">Prescription expiry (optional)<input type="date" min={new Date().toISOString().slice(0, 10)} value={prescriptionExpiry} onChange={event => setPrescriptionExpiry(event.target.value)} /></label></div>}
      </section>
      <label>Follow-up date<input name="followUp" type="date" min={new Date().toISOString().slice(0, 10)} /></label>
      <div className="form-foot span-2"><span>Consultation and any prescription are saved together.</span><button className="button button-primary" disabled={busy}>{busy ? 'Saving clinical record…' : 'Save clinical record'}</button></div>
    </form>
    {medicinePickerOpen && <MedicinePickerModal medicines={medicines} selectedIds={new Set(prescriptionItems.map(item => item.medicineId))} onClose={() => setMedicinePickerOpen(false)} onSelect={medicine => { setPrescriptionItems(rows => [...rows, { medicineId: medicine.medicineId, dosage: '', frequency: '', durationDays: '1', instructions: '' }]) }} />}
  </Modal>
}

function MedicinePickerModal({ medicines, selectedIds, onClose, onSelect }: { medicines: Medicine[]; selectedIds: Set<number>; onClose: () => void; onSelect: (medicine: Medicine) => void }) {
  const [query, setQuery] = useState('')
  const visible = medicines.filter(medicine => `${medicine.medicineName} ${medicine.activeIng} ${medicine.strength} ${medicine.genericUse}`.toLowerCase().includes(query.toLowerCase()))
  return <Modal title="Select a medicine" subtitle="Search the clinic catalog and add one or more medicines to this consultation." onClose={onClose} wide nested>
    <div className="search-box medicine-picker-search"><Search size={16} /><input autoFocus value={query} onChange={event => setQuery(event.target.value)} placeholder="Search name, active ingredient, or strength" /></div>
    {medicines.length === 0 ? <EmptyState icon={<Pill />} title="Medicine catalog is empty" text="Ask an administrator to add medicines before prescribing." /> : visible.length === 0 ? <EmptyState icon={<Search />} title="No medicines match" text="Try another name or ingredient." /> : <div className="medicine-picker-list">{visible.map(medicine => <article className="medicine-picker-row" key={medicine.medicineId}><div className="medicine-icon"><Pill size={16} /></div><div className="medicine-picker-info"><b>{medicine.medicineName}</b><span>{medicine.strength}</span><small>{medicine.activeIng} · {medicine.genericUse}</small></div><button type="button" className={`button button-small ${selectedIds.has(medicine.medicineId) ? 'button-soft' : 'button-primary'}`} disabled={selectedIds.has(medicine.medicineId)} onClick={() => onSelect(medicine)}>{selectedIds.has(medicine.medicineId) ? 'Added' : 'Select'}</button></article>)}</div>}
    <div className="form-foot picker-footer"><span>{selectedIds.size} medicine{selectedIds.size === 1 ? '' : 's'} selected</span><button type="button" className="button button-primary" onClick={onClose}>Done</button></div>
  </Modal>
}

function PrescriptionModal({ consultation, medicines, onClose, onSuccess, request }: { consultation: Consultation; medicines: Medicine[]; onClose: () => void; onSuccess: () => void; request: <T,>(path: string, init?: RequestInit) => Promise<T> }) {
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  const [items, setItems] = useState([{ medicineId: '', dosage: '', frequency: '', durationDays: '1', instructions: '' }])
  const change = (index: number, key: string, value: string) => setItems(rows => rows.map((row, i) => i === index ? { ...row, [key]: value } : row))
  async function submit(e: FormEvent<HTMLFormElement>) { e.preventDefault(); setBusy(true); setError(''); const data = new FormData(e.currentTarget)
    const body = { consultationId: consultation.consultationId, expiryDate: data.get('expiry') ? `${data.get('expiry')}T23:59:00` : null, items: items.map(item => ({ ...item, medicineId: Number(item.medicineId), durationDays: Number(item.durationDays) })) }
    try { await request('/prescriptions', { method: 'POST', body: JSON.stringify(body) }); onSuccess() } catch (err) { setError((err as Error).message) } finally { setBusy(false) }
  }
  return <Modal title="Create prescription" subtitle={`Consultation #${consultation.consultationId} · Cancel an active prescription before replacing it.`} onClose={onClose} wide>
    <form className="form-grid" onSubmit={submit}>{error && <div className="form-error span-2">{error}</div>}<label>Expires on<input name="expiry" type="date" min={new Date().toISOString().slice(0, 10)} /></label><div className="form-spacer" />
      {items.map((item, index) => <div className="rx-form-item span-2" key={index}><div className="rx-form-header"><b>Medicine {index + 1}</b>{items.length > 1 && <button type="button" className="icon-button danger-icon" onClick={() => setItems(rows => rows.filter((_, i) => i !== index))}><X size={15} /></button>}</div>
        <div className="form-grid"><label className="span-2">Medicine<select required value={item.medicineId} onChange={e => change(index, 'medicineId', e.target.value)}><option value="" disabled>Select from catalog</option>{medicines.map(m => <option key={m.medicineId} value={m.medicineId}>{m.medicineName} · {m.strength}</option>)}</select></label><label>Dosage<input required value={item.dosage} onChange={e => change(index, 'dosage', e.target.value)} placeholder="e.g. 1 tablet" /></label><label>Frequency<input required value={item.frequency} onChange={e => change(index, 'frequency', e.target.value)} placeholder="e.g. Twice daily" /></label><label>Duration (days)<input type="number" min="1" required value={item.durationDays} onChange={e => change(index, 'durationDays', e.target.value)} /></label><label>Instructions<input required value={item.instructions} onChange={e => change(index, 'instructions', e.target.value)} placeholder="e.g. After food" /></label></div>
      </div>)}
      <button type="button" className="button button-quiet span-2 add-item" onClick={() => setItems(rows => [...rows, { medicineId: '', dosage: '', frequency: '', durationDays: '1', instructions: '' }])}><Plus size={15} /> Add another medicine</button>
      <div className="form-foot span-2"><span>Prescription items cannot be edited after issuing.</span><button className="button button-primary" disabled={busy || !items.length}>{busy ? 'Issuing…' : 'Issue prescription'}</button></div>
    </form>
  </Modal>
}

function Modal({ title, subtitle, onClose, children, wide = false, nested = false }: { title: string; subtitle?: string; onClose: () => void; children: ReactNode; wide?: boolean; nested?: boolean }) {
  const backdropRef = useRef<HTMLDivElement>(null)
  useEffect(() => { const close = (e: KeyboardEvent) => {
    if (e.key !== 'Escape') return
    const backdrops = document.querySelectorAll('.modal-backdrop')
    if (backdrops[backdrops.length - 1] === backdropRef.current) onClose()
  }; window.addEventListener('keydown', close); return () => window.removeEventListener('keydown', close) }, [onClose])
  return createPortal(<div ref={backdropRef} className={`modal-backdrop ${nested ? 'modal-backdrop-nested' : ''}`} onMouseDown={e => e.target === e.currentTarget && onClose()}><section className={`modal-card ${wide ? 'modal-wide' : ''}`} role="dialog" aria-modal="true" aria-labelledby="modal-title"><header className="modal-header"><div><span className="eyebrow">EMEDIC WORKFLOW</span><h2 id="modal-title">{title}</h2>{subtitle && <p>{subtitle}</p>}</div><button className="icon-button modal-close" onClick={onClose} aria-label="Close dialog"><X size={19} /></button></header><div className="modal-body">{children}</div></section></div>, document.body)
}

function StatusPill({ status }: { status: string }) {
  const tone = `status-${status.toLowerCase()}`
  return <span className={`status-pill ${tone}`}><i />{status.replace('_', ' ').toLowerCase().replace(/^./, c => c.toUpperCase())}</span>
}

function ErrorBanner({ message }: { message: string }) { return <div className="notice notice-error"><CircleHelp size={17} /><span>{message}</span></div> }
function EmptyState({ icon, title, text }: { icon: ReactNode; title: string; text: string }) { return <div className="empty-state"><div>{icon}</div><b>{title}</b><span>{text}</span></div> }
function LoadingRows() { return <div className="loading-rows"><div /><div /><div /></div> }

function formatTime(value: string) { return new Date(value).toLocaleTimeString('en', { hour: 'numeric', minute: '2-digit' }) }
function formatDate(value: string) { return new Date(value.length === 10 ? `${value}T12:00:00` : value).toLocaleDateString('en', { month: 'short', day: 'numeric', year: 'numeric' }) }
function formatRelativeDay(value: string) { const date = new Date(value); const today = new Date(); const tomorrow = new Date(); tomorrow.setDate(today.getDate() + 1); return date.toDateString() === today.toDateString() ? 'Today' : date.toDateString() === tomorrow.toDateString() ? 'Tomorrow' : date.toLocaleDateString('en', { weekday: 'short' }) }
