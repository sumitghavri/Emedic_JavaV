package com.emedic.management.services.consultation;

import com.emedic.management.mapper.ConsultationMapper;
import com.emedic.management.model.Appointment;
import com.emedic.management.model.Consultation;
import com.emedic.management.modeldto.consultation.ConsultationRequestDTO;
import com.emedic.management.modeldto.consultation.ConsultationResponseDTO;
import com.emedic.management.repositories.AppointmentRepository;
import com.emedic.management.repositories.ConsultationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import com.emedic.management.util.Pagination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationMapper consultationMapper;

    public ConsultationServiceImpl(ConsultationRepository consultationRepository,
                                  AppointmentRepository appointmentRepository,
                                  ConsultationMapper consultationMapper) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationMapper = consultationMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> getAllConsultations() {
        return consultationRepository.findAll().stream()
                .map(consultationMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultationResponseDTO> getConsultations(int page, int size) {
        return consultationRepository.findAll(Pagination.request(page, size)).map(consultationMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultationResponseDTO> getConsultationsForDoctor(String email, int page, int size) {
        if (email == null || email.isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Your API access token is missing the clinician email claim. Add https://emedic.example/email in the Auth0 Post Login Action, deploy it, then sign out and back in.");
        }
        return consultationRepository.findByAppointment_Doctor_EmailIgnoreCase(email, Pagination.request(page, size))
                .map(consultationMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponseDTO getConsultationById(Long id) {
        return consultationMapper.toResponseDTO(findConsultationById(id));
    }

    @Override
    public ConsultationResponseDTO createConsultation(ConsultationRequestDTO requestDTO) {
        return createConsultation(requestDTO, null);
    }

    @Override
    public ConsultationResponseDTO createConsultationForDoctor(ConsultationRequestDTO requestDTO, String doctorEmail) {
        if (doctorEmail == null || doctorEmail.isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException("Clinician account email is required to verify appointment ownership");
        }
        return createConsultation(requestDTO, doctorEmail);
    }

    private ConsultationResponseDTO createConsultation(ConsultationRequestDTO requestDTO, String doctorEmail) {
        validateClinicalDetails(requestDTO);

        if (requestDTO.getAppointmentId() == null) {
            throw new IllegalArgumentException("Appointment id is required");
        }
        Appointment appointment = appointmentRepository.findLockedByAppointmentId(requestDTO.getAppointmentId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Appointment not found with id: " + requestDTO.getAppointmentId()));

        if (appointment.getStatus() != com.emedic.management.model.AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("A consultation can only be created for a confirmed appointment");
        }
        if (doctorEmail != null && !doctorEmail.equalsIgnoreCase(appointment.getDoctor().getEmail())) {
            throw new org.springframework.security.access.AccessDeniedException("Clinicians can only consult their own appointments");
        }
        if (consultationRepository.existsByAppointment_AppointmentId(appointment.getAppointmentId())) {
            throw new IllegalStateException("A consultation already exists for this appointment");
        }

        Consultation consultation = consultationMapper.toEntity(requestDTO);
        consultation.setAppointment(appointment);
        consultation.setConsultationDatetime(LocalDateTime.now());

        Consultation saved = consultationRepository.save(consultation);
        appointment.setStatus(com.emedic.management.model.AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now());
        return consultationMapper.toResponseDTO(saved);
    }

    @Override
    public ConsultationResponseDTO updateConsultation(Long id, ConsultationRequestDTO requestDTO) {
        throw new IllegalStateException(
                "Clinical records cannot be edited without an approved correction and audit workflow");
    }

    @Override
    public void deleteConsultation(Long id) {
        findConsultationById(id);
        throw new IllegalStateException("Clinical records cannot be hard-deleted; use the clinical correction process");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultationResponseDTO> getConsultationsByPatientId(Long patientId, int page, int size) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient id cannot be null");
        }
        return consultationRepository.findByAppointment_Patient_PatientIdOrderByConsultationDatetimeDesc(
                patientId, Pagination.request(page, size)).map(consultationMapper::toResponseDTO);
    }

    private Consultation findConsultationById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Consultation not found with id: " + id));
    }

    private void validateClinicalDetails(ConsultationRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Consultation request cannot be null");
        }
        if (requestDTO.getChiefComplaint() == null || requestDTO.getChiefComplaint().isBlank()
                || requestDTO.getSymptoms() == null || requestDTO.getSymptoms().isBlank()
                || requestDTO.getClinicalNotes() == null || requestDTO.getClinicalNotes().isBlank()
                || requestDTO.getDiagnosis() == null || requestDTO.getDiagnosis().isBlank()
                || requestDTO.getExaminationFinding() == null || requestDTO.getExaminationFinding().isBlank()
                || requestDTO.getTreatmentPlan() == null || requestDTO.getTreatmentPlan().isBlank()) {
            throw new IllegalArgumentException("Required clinical details must be provided");
        }
    }
}
