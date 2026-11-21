package com.emedic.management.mapper;
import com.emedic.management.model.Consultation;
import com.emedic.management.model.Patient;
import com.emedic.management.modeldto.patienthistory.ConsultationHistoryDTO;
import com.emedic.management.modeldto.patienthistory.PatientHistoryPatientDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface PatientHistoryMapper {

    @Mapping(
            target = "age",
            expression = "java(calculateAge(patient.getDob()))"
    )
    PatientHistoryPatientDTO toPatientHistoryPatientDTO(
            Patient patient
    );

    @Mapping(
            target = "consultationDateTime",
            source = "consultationDatetime"
    )
    @Mapping(
            target = "examinationFindings",
            source = "examinationFindings"
    )
    @Mapping(
            target = "followUpDate",
            source = "followUpdate"
    )
    @Mapping(target = "prescriptions", ignore = true)
    ConsultationHistoryDTO toConsultationHistoryDTO(
            Consultation consultation
    );

    default int calculateAge(LocalDate dob) {
        if (dob == null) {
            return 0;
        }

        return java.time.Period
                .between(dob, LocalDate.now())
                .getYears();
    }
}
