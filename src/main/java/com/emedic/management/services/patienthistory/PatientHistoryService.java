package com.emedic.management.services.patienthistory;

import com.emedic.management.modeldto.patienthistory.PatientHistoryDTO;

public interface PatientHistoryService {
    PatientHistoryDTO getPatientHistory(Long patientId, int page, int size);
}
