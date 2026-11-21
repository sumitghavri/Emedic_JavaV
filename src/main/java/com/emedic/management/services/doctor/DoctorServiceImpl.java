package com.emedic.management.services.doctor;

import com.emedic.management.mapper.DoctorMapper;
import com.emedic.management.model.Doctor;
import com.emedic.management.modeldto.doctor.DoctorRequestDTO;
import com.emedic.management.modeldto.doctor.DoctorResponseDTO;
import com.emedic.management.repositories.DoctorRepository;
import com.emedic.management.repositories.AppointmentRepository;
import com.emedic.management.repositories.DoctorScheduleRepository;
import com.emedic.management.error.DuplicateResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import com.emedic.management.util.Pagination;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository,
                             DoctorMapper doctorMapper,
                             AppointmentRepository appointmentRepository,
                             DoctorScheduleRepository doctorScheduleRepository) {
        this.doctorRepository = doctorRepository;
        this.doctorMapper = doctorMapper;
        this.appointmentRepository = appointmentRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(doctorMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> getDoctors(int page, int size) {
        return doctorRepository.findAll(Pagination.request(page, size)).map(doctorMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(Long id) {
        return doctorMapper.toResponseDTO(findDoctorById(id));
    }

    @Override
    public DoctorResponseDTO createDoctor(DoctorRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Doctor request cannot be null");
        }

        if (requestDTO.getLicenseNo() != null && doctorRepository.findByLicenseNo(requestDTO.getLicenseNo()).isPresent()) {
            throw new DuplicateResourceException("Doctor with this license number already exists");
        }

        if (requestDTO.getEmail() != null && doctorRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Doctor with this email already exists");
        }

        Doctor doctor = doctorMapper.toEntity(requestDTO);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDTO(savedDoctor);
    }

    @Override
    public DoctorResponseDTO createDoctorForClinician(DoctorRequestDTO requestDTO, String authenticatedEmail) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Doctor request cannot be null");
        }
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Your access token must include an email claim to link your clinician account to a doctor profile");
        }
        requestDTO.setEmail(authenticatedEmail);
        return createDoctor(requestDTO);
    }

    @Override
    public DoctorResponseDTO updateDoctor(Long id, DoctorRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Doctor request cannot be null");
        }

        Doctor existingDoctor = findDoctorById(id);

        if (requestDTO.getLicenseNo() != null && !requestDTO.getLicenseNo().equals(existingDoctor.getLicenseNo())
                && doctorRepository.findByLicenseNo(requestDTO.getLicenseNo()).isPresent()) {
            throw new DuplicateResourceException("Doctor with this license number already exists");
        }

        if (requestDTO.getEmail() != null && !requestDTO.getEmail().equals(existingDoctor.getEmail())
                && doctorRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Doctor with this email already exists");
        }

        if (requestDTO.getName() != null) {
            existingDoctor.setName(requestDTO.getName());
        }
        if (requestDTO.getSpecialization() != null) {
            existingDoctor.setSpecialization(requestDTO.getSpecialization());
        }
        if (requestDTO.getLicenseNo() != null) {
            existingDoctor.setLicenseNo(requestDTO.getLicenseNo());
        }
        if (requestDTO.getEmail() != null) {
            existingDoctor.setEmail(requestDTO.getEmail());
        }
        if (requestDTO.getPhone() != null) {
            existingDoctor.setPhone(requestDTO.getPhone());
        }

        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        return doctorMapper.toResponseDTO(updatedDoctor);
    }

    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = findDoctorById(id);
        if (appointmentRepository.existsByDoctor_DoctorId(id)
                || doctorScheduleRepository.existsByDoctor_DoctorId(id)) {
            throw new IllegalStateException(
                    "A doctor with schedule or appointment history cannot be hard-deleted");
        }
        doctorRepository.delete(doctor);
    }

    private Doctor findDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found with id: " + id));
    }
}
