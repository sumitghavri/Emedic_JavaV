package com.emedic.management.modeldto.patienthistory;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientHistoryDTO {
    private PatientHistoryPatientDTO patient;

    private List<ConsultationHistoryDTO> consultations;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

}
