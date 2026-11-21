package com.emedic.management.services.doctor;

import com.emedic.management.modeldto.doctor.DoctorRequestDTO;
import com.emedic.management.modeldto.doctor.DoctorResponseDTO;

import java.util.List;
import org.springframework.data.domain.Page;

public interface DoctorService {
    List<DoctorResponseDTO> getAllDoctors();
    Page<DoctorResponseDTO> getDoctors(int page, int size);
    DoctorResponseDTO getDoctorById(Long id);
    DoctorResponseDTO createDoctor(DoctorRequestDTO requestDTO);
    DoctorResponseDTO createDoctorForClinician(DoctorRequestDTO requestDTO, String authenticatedEmail);
    DoctorResponseDTO updateDoctor(Long id, DoctorRequestDTO requestDTO);
    void deleteDoctor(Long id);
}
