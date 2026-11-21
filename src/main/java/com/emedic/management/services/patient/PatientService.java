package com.emedic.management.services.patient;

import com.emedic.management.modeldto.patient.PatientRequestDTO;
import com.emedic.management.modeldto.patient.PatientResponseDTO;

import java.util.List;
import org.springframework.data.domain.Page;

public interface PatientService {
    List<PatientResponseDTO> getAllPatients();
    Page<PatientResponseDTO> getPatients(int page, int size);
    Page<PatientResponseDTO> getPatientsForDoctor(String email, int page, int size);
    void assertPatientAccess(Long id, String email);
    PatientResponseDTO getPatientById(Long id);
    PatientResponseDTO createPatient(PatientRequestDTO requestDTO);
    PatientResponseDTO updatePatient(Long id, PatientRequestDTO requestDTO);
    void deletePatient(Long id);
}
