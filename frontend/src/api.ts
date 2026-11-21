export type Page<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export type Appointment = {
  appointmentId: number
  patientId: number
  patientName: string
  patientPhone: string
  doctorId: number
  doctorName: string
  appointmentDateTime: string
  reason: string
  status: 'REQUESTED' | 'CONFIRMED' | 'RESCHEDULED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW'
  bookingSource: string
}

export type Patient = {
  patientId: number
  firstName: string
  lastName: string
  phone: string
  email: string
  dob: string
  gender: string
  bloodType: string
  allergies: string
  address: string
}

export type Doctor = {
  doctorId: number
  name: string
  specialization: string
  email: string
  phone: string
  licenseNo: string
}

export type Schedule = {
  scheduleId: number
  doctorId: number
  dayOfWeek: string
  startTime: string
  endTime: string
  slotDuration: number
  slotDurationEditable: boolean
  slotDurationEditMessage: string | null
}

export type PublicDoctor = Pick<Doctor, 'doctorId' | 'name' | 'specialization'>
export type PublicAvailability = { doctorId: number; date: string; availableSlots: string[] }
export type PublicBookingReceipt = {
  doctorName: string
  appointmentDateTime: string
  status: string
  message: string
}

export type Medicine = {
  medicineId: number
  medicineName: string
  strength: string
  genericUse: string
  activeIng: string
  medicineDescription: string
}

export type Consultation = {
  consultationId: number
  appointmentId: number
  consultationDatetime: string
  chiefComplaint: string
  diagnosis: string
  treatmentPlan: string
}

export type History = {
  patient: Pick<Patient, 'patientId' | 'firstName' | 'lastName' | 'phone' | 'dob' | 'gender' | 'bloodType' | 'allergies'> & { age?: number }
  consultations: Array<{
    consultationId: number
    consultationDateTime: string
    chiefComplaint: string
    symptoms: string
    diagnosis: string
    examinationFindings: string
    clinicalNotes: string
    treatmentPlan: string
    followUpDate: string | null
    prescriptions?: Array<{ prescriptionId: number; status: string; prescribedDate: string; items: Array<{ medicineName: string; dosage: string; frequency: string; instructions: string }> }>
  }>
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type ApiError = {
  status?: number
  message?: string
  fieldErrors?: Record<string, string>
  grantedAuthorities?: string[]
  bearerTokenPresent?: boolean
  failureType?: string
  jwtResourceServerEnabled?: boolean
}

function apiFailureMessage(response: Response, problem: ApiError): string {
  const validation = problem.fieldErrors ? Object.values(problem.fieldErrors).join(' ') : undefined
  if (response.status === 401 && typeof problem.bearerTokenPresent === 'boolean') {
    if (problem.jwtResourceServerEnabled === false) {
      return problem.message || 'Backend JWT validation is disabled. Restart Spring from the management folder so it loads the backend .env.'
    }
    return problem.bearerTokenPresent
      ? `${problem.message || 'The API rejected the access token.'}${problem.failureType ? ` (${problem.failureType})` : ''}`
      : problem.message || 'The API request did not include a bearer access token.'
  }
  if (response.status === 403 && problem.message && problem.message !== 'You are not permitted to access this resource') {
    return problem.message
  }
  if (response.status === 403 && problem.grantedAuthorities) {
    const roles = problem.grantedAuthorities.filter(authority => authority.startsWith('ROLE_'))
    return roles.length
      ? `API access denied. The API received roles: ${roles.join(', ')}. Doctor management requires ROLE_ADMIN.`
      : `API access denied. The API received no application roles${problem.grantedAuthorities.length ? ` (authorities: ${problem.grantedAuthorities.join(', ')})` : ''}. The access token is missing the configured ADMIN role claim.`
  }
  if (response.status === 403) {
    return problem.message || 'The API denied this request. Check the signed-in role and access-token claims.'
  }
  if (validation || problem.message) return validation || problem.message!
  if (response.status === 401) return 'Sign-in token was not accepted. Sign out and sign in again, then check the Auth0 issuer and API audience.'
  return `Request failed (${response.status})`
}

export async function api<T>(path: string, token: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || '/api'}${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init.body ? { 'Content-Type': 'application/json' } : {}),
      Authorization: `Bearer ${token}`,
      ...init.headers,
    },
  })

  if (!response.ok) {
    const problem = await response.json().catch(() => ({})) as ApiError
    throw new Error(apiFailureMessage(response, problem))
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export async function publicApi<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || '/api'}${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init.body ? { 'Content-Type': 'application/json' } : {}),
      ...init.headers,
    },
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => ({})) as ApiError
    throw new Error(apiFailureMessage(response, problem))
  }
  return response.json() as Promise<T>
}

export async function allPages<T>(path: string, token: string): Promise<T[]> {
  const first = await api<Page<T>>(`${path}?page=0&size=100`, token)
  if (first.totalPages <= 1) return first.content
  const rest = await Promise.all(Array.from({ length: first.totalPages - 1 }, (_, index) =>
    api<Page<T>>(`${path}?page=${index + 1}&size=100`, token).then(page => page.content)))
  return first.content.concat(...rest)
}
