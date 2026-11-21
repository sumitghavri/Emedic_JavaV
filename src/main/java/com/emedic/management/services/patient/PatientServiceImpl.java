package com.emedic.management.services.patient;

import com.emedic.management.mapper.PatientMapper;
import com.emedic.management.model.Patient;
import com.emedic.management.modeldto.patient.PatientRequestDTO;
import com.emedic.management.modeldto.patient.PatientResponseDTO;
import com.emedic.management.repositories.PatientRepository;
import com.emedic.management.error.DuplicateResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import com.emedic.management.util.Pagination;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final PatientPhoneLockService patientPhoneLockService;

    public PatientServiceImpl(PatientRepository patientRepository,
                              PatientMapper patientMapper,
                              PatientPhoneLockService patientPhoneLockService) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.patientPhoneLockService = patientPhoneLockService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(patientMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponseDTO> getPatients(int page, int size) {
        return patientRepository.findAll(Pagination.request(page, size)).map(patientMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponseDTO> getPatientsForDoctor(String email, int page, int size) {
        requireDoctorEmail(email);
        return patientRepository.findPatientsForDoctor(email, Pagination.request(page, size)).map(patientMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public void assertPatientAccess(Long id, String email) {
        requireDoctorEmail(email);
        if (!patientRepository.existsAppointmentForDoctor(id, email)) {
            throw new org.springframework.security.access.AccessDeniedException("Clinicians can only access patients with an appointment assigned to them");
        }
    }

    private void requireDoctorEmail(String email) {
        if (email == null || email.isBlank()) throw new org.springframework.security.access.AccessDeniedException("Clinician email is required to verify patient ownership");
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id) {
        return patientMapper.toResponseDTO(findPatientById(id));
    }

    @Override
    public PatientResponseDTO createPatient(PatientRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Patient request cannot be null");
        }

        if (requestDTO.getPhone() == null || !requestDTO.getPhone().matches("^[6-9][0-9]{9}$")) {
            throw new IllegalArgumentException("Enter a valid patient phone number");
        }
        patientPhoneLockService.acquire(requestDTO.getPhone());

        if (patientRepository.findByPhoneForUpdate(requestDTO.getPhone()).isPresent()) {
            throw new DuplicateResourceException("Patient with this phone number already exists");
        }

        if (requestDTO.getEmail() != null && patientRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Patient with this email already exists");
        }

        Patient patient = patientMapper.toEntity(requestDTO);
        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toResponseDTO(savedPatient);
    }

    @Override
    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Patient request cannot be null");
        }

        if (requestDTO.getPhone() != null) {
            if (!requestDTO.getPhone().matches("^[6-9][0-9]{9}$")) {
                throw new IllegalArgumentException("Enter a valid patient phone number");
            }
            patientPhoneLockService.acquire(requestDTO.getPhone());
        }

        Patient existingPatient = findPatientById(id);

        if (requestDTO.getPhone() != null && !requestDTO.getPhone().equals(existingPatient.getPhone())
                && patientRepository.findByPhoneForUpdate(requestDTO.getPhone()).isPresent()) {
            throw new DuplicateResourceException("Patient with this phone number already exists");
        }

        if (requestDTO.getEmail() != null && !requestDTO.getEmail().equals(existingPatient.getEmail())
                && patientRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Patient with this email already exists");
        }

        if (requestDTO.getFirstName() != null) {
            existingPatient.setFirstName(requestDTO.getFirstName());
        }
        if (requestDTO.getLastName() != null) {
            existingPatient.setLastName(requestDTO.getLastName());
        }
        if (requestDTO.getBloodType() != null) {
            existingPatient.setBloodType(requestDTO.getBloodType());
        }
        if (requestDTO.getGender() != null) {
            existingPatient.setGender(requestDTO.getGender());
        }
        if (requestDTO.getDob() != null) {
            existingPatient.setDob(requestDTO.getDob());
        }
        if (requestDTO.getAddress() != null) {
            existingPatient.setAddress(requestDTO.getAddress());
        }
        if (requestDTO.getEmail() != null) {
            existingPatient.setEmail(requestDTO.getEmail());
        }
        if (requestDTO.getPhone() != null) {
            existingPatient.setPhone(requestDTO.getPhone());
        }
        if (requestDTO.getAllergies() != null) {
            existingPatient.setAllergies(requestDTO.getAllergies());
        }

        Patient updatedPatient = patientRepository.save(existingPatient);
        return patientMapper.toResponseDTO(updatedPatient);
    }

    @Override
    public void deletePatient(Long id) {
        findPatientById(id);
        throw new IllegalStateException(
                "Patient records cannot be hard-deleted through routine operations; use the approved retention disposition process");
    }

    private Patient findPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Patient not found with id: " + id));
    }
}
