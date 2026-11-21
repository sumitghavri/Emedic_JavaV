import { useEffect, useState, type FormEvent } from 'react'
import { CalendarDays, CheckCircle2, Clock3, HeartPulse, ShieldCheck } from 'lucide-react'
import { publicApi, type PublicAvailability, type PublicBookingReceipt, type PublicDoctor } from './api'

export default function PublicBookingPage() {
  const [doctors, setDoctors] = useState<PublicDoctor[]>([])
  const [doctorId, setDoctorId] = useState('')
  const [date, setDate] = useState('')
  const [slots, setSlots] = useState<string[]>([])
  const [selectedSlot, setSelectedSlot] = useState('')
  const [receipt, setReceipt] = useState<PublicBookingReceipt | null>(null)
  const [loadingDoctors, setLoadingDoctors] = useState(true)
  const [loadingSlots, setLoadingSlots] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    publicApi<PublicDoctor[]>('/public/booking/doctors')
      .then(result => active && setDoctors(result))
      .catch(problem => active && setError((problem as Error).message))
      .finally(() => active && setLoadingDoctors(false))
    return () => { active = false }
  }, [])

  useEffect(() => {
    if (!doctorId || !date) { setSlots([]); setSelectedSlot(''); return }
    let active = true
    setLoadingSlots(true); setError(''); setSelectedSlot('')
    publicApi<PublicAvailability>(`/public/booking/doctors/${doctorId}/availability?date=${encodeURIComponent(date)}`)
      .then(result => active && setSlots(result.availableSlots))
      .catch(problem => active && setError((problem as Error).message))
      .finally(() => active && setLoadingSlots(false))
    return () => { active = false }
  }, [doctorId, date])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!doctorId || !date || !selectedSlot) { setError('Choose a doctor, date, and available time first.'); return }
    setBusy(true); setError('')
    const form = new FormData(event.currentTarget)
    const request = {
      doctorId: Number(doctorId), appointmentDateTime: `${date}T${selectedSlot.slice(0, 5)}:00`,
      reason: form.get('reason'), phone: form.get('phone'), firstName: form.get('firstName'),
      lastName: form.get('lastName'), email: form.get('email'), dob: form.get('dob'),
      gender: form.get('gender'), bloodType: form.get('bloodType') || null,
      address: form.get('address'), allergies: form.get('allergies'),
    }
    try {
      const result = await publicApi<PublicBookingReceipt>('/public/booking/appointments', {
        method: 'POST', body: JSON.stringify(request),
      })
      setReceipt(result)
    } catch (problem) { setError((problem as Error).message) }
    finally { setBusy(false) }
  }

  if (receipt) return <main className="public-booking-shell"><section className="public-booking-card receipt-card">
    <div className="public-success-icon"><CheckCircle2 size={28} /></div><p className="public-kicker">REQUEST RECEIVED</p>
    <h1>We’ve sent your appointment request.</h1><p className="public-lead">{receipt.message}</p>
    <div className="receipt-summary"><span>DOCTOR</span><b>Dr. {receipt.doctorName}</b><span>REQUESTED TIME</span><b>{new Date(receipt.appointmentDateTime).toLocaleString()}</b><span>STATUS</span><b className="receipt-status">Waiting for clinic confirmation</b></div>
    <p className="public-privacy"><ShieldCheck size={15} /> The clinic will contact you using the information you provided.</p>
    <a className="public-back-link" href="/book">Request another appointment</a>
  </section></main>

  return <main className="public-booking-shell"><header className="public-booking-top"><a className="public-brand" href="/"><span className="brand-mark"><HeartPulse size={20} /></span><span><b>eMedic</b><small>CLINIC APPOINTMENTS</small></span></a><a className="public-staff-link" href="/">Staff sign in</a></header>
    <div className="public-booking-layout"><section className="public-booking-intro"><p className="public-kicker">ONLINE APPOINTMENT REQUEST</p><h1>Let’s find a time that works for you.</h1><p className="public-lead">Choose a doctor and an open time. The clinic will review your request and contact you to confirm.</p>{['localhost', '127.0.0.1'].includes(window.location.hostname) && <div className="notice notice-warn public-local-notice">This is a local preview. People outside this computer cannot open this address until the app is deployed.</div>}<div className="public-promise"><span><CalendarDays size={16} /> Choose from published clinic hours</span><span><Clock3 size={16} /> Your booking stays pending until confirmed</span><span><ShieldCheck size={16} /> Your details go directly to the clinic</span></div></section>
      <section className="public-booking-card"><div className="public-card-heading"><div><p className="public-kicker">BOOK A VISIT</p><h2>Appointment details</h2></div><span className="public-step">01 / 01</span></div>
        {error && <div className="notice notice-error" role="alert">{error}</div>}
        {loadingDoctors ? <div className="screen-state"><div className="spinner" />Loading doctors…</div> : doctors.length === 0 ? <div className="public-no-doctors"><StethoscopeIcon /><h3>Online booking isn’t available yet</h3><p>The clinic has not published doctor schedules. Please contact the clinic directly.</p></div> : <form className="form-grid public-form" onSubmit={submit}>
          <label className="span-2">Doctor<select value={doctorId} onChange={event => setDoctorId(event.target.value)} required><option value="">Choose a doctor</option>{doctors.map(doctor => <option key={doctor.doctorId} value={doctor.doctorId}>Dr. {doctor.name} · {doctor.specialization}</option>)}</select></label>
          <label className="span-2">Date<input type="date" value={date} onChange={event => setDate(event.target.value)} min={localDateInputValue()} required /></label>
          <div className="span-2 public-slot-field"><span className="public-field-label">Available times</span>{!doctorId || !date ? <p className="public-field-help">Choose a doctor and date to see open times.</p> : loadingSlots ? <p className="public-field-help">Checking availability…</p> : slots.length === 0 ? <p className="public-field-help">No times available on this date. Try another date.</p> : <div className="public-slot-grid">{slots.map(slot => <button type="button" key={slot} className={`public-slot ${selectedSlot === slot ? 'public-slot-selected' : ''}`} onClick={() => setSelectedSlot(slot)}>{slot.slice(0, 5)}</button>)}</div>}</div>
          <label className="span-2">Reason for visit<input name="reason" required maxLength={255} placeholder="Briefly describe why you’re booking" /></label>
          <div className="public-form-divider span-2"><b>Your details</b><span>Required to help the clinic identify and contact you.</span></div>
          <label>First name<input name="firstName" autoComplete="given-name" required /></label><label>Last name<input name="lastName" autoComplete="family-name" required /></label>
          <label>Email<input name="email" type="email" autoComplete="email" required /></label><label>Mobile number<input name="phone" inputMode="numeric" autoComplete="tel" pattern="[6-9][0-9]{9}" placeholder="10-digit mobile number" required /></label>
          <label>Date of birth<input name="dob" type="date" max={localDateInputValue()} required /></label><label>Gender<select name="gender" defaultValue="" required><option value="" disabled>Select</option><option value="FEMALE">Female</option><option value="MALE">Male</option><option value="OTHER">Other</option></select></label>
          <label className="span-2">Address<input name="address" autoComplete="street-address" required /></label><label className="span-2">Allergies<input name="allergies" required placeholder="List allergies or enter ‘None reported’" /></label>
          <div className="form-foot span-2"><span>Submitting creates a request, not a confirmed appointment.</span><button className="button button-primary" disabled={busy || !selectedSlot}>{busy ? 'Sending request…' : 'Request appointment'}</button></div>
        </form>}
      </section>
    </div><footer className="public-booking-footer">For urgent or emergency care, contact local emergency services.</footer>
  </main>
}

function StethoscopeIcon() { return <div className="public-no-doctor-icon"><HeartPulse size={24} /></div> }
function localDateInputValue() {
  const today = new Date()
  return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
}
