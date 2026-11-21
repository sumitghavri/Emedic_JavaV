package com.emedic.management.mapper;

import com.emedic.management.model.Appointment;
import com.emedic.management.model.Doctor;
import com.emedic.management.model.Patient;
import com.emedic.management.modeldto.appointment.AppointmentBookingResponseDTO;
import com.emedic.management.modeldto.appointment.AppointmentQueueResponseDTO;
import com.emedic.management.modeldto.appointment.AppointmentResponseDTO;
import com.emedic.management.modeldto.appointment.DoctorBookingResponseDTO;
import com.emedic.management.modeldto.appointment.PatientBookingResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.Period;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    /*
     * Complete booking response
     *
     * Appointment
     *     ├── Patient → PatientBookingResponseDTO
     *     └── Doctor  → DoctorBookingResponseDTO
     */
    @Mapping(target = "patient", source = "patient")
    @Mapping(target = "doctor", source = "doctor")
    AppointmentBookingResponseDTO toBookingResponseDTO(
            Appointment appointment
    );


    /*
     * General appointment response
     */
    @Mapping(
            target = "patientName",
            expression = "java(getPatientName(appointment.getPatient()))"
    )
    @Mapping(target = "patientId", source = "patient.patientId")
    @Mapping(target = "patientPhone", source = "patient.phone")
    @Mapping(target = "doctorId", source = "doctor.doctorId")
    @Mapping(target = "doctorName", source = "doctor.name")
    AppointmentResponseDTO toResponseDTO(
            Appointment appointment
    );


    /*
     * Today's appointment queue
     */
    @Mapping(
            target = "patientName",
            expression = "java(getPatientName(appointment.getPatient()))"
    )
    @Mapping(target = "patientId", source = "patient.patientId")
    AppointmentQueueResponseDTO toQueueResponseDTO(
            Appointment appointment
    );


    /*
     * Patient nested inside AppointmentBookingResponseDTO
     */
    @Mapping(
            target = "age",
            expression = "java(calculateAge(patient.getDob()))"
    )
    PatientBookingResponseDTO toPatientBookingDTO(
            Patient patient
    );


    /*
     * Doctor nested inside AppointmentBookingResponseDTO
     */
    DoctorBookingResponseDTO toDoctorBookingDTO(
            Doctor doctor
    );


    /*
     * Helper method for patient name
     */
    default String getPatientName(Patient patient) {

        if (patient == null) {
            return null;
        }

        String firstName = patient.getFirstName();
        String lastName = patient.getLastName();

        if (firstName == null) {
            return lastName;
        }

        if (lastName == null) {
            return firstName;
        }

        return firstName + " " + lastName;
    }


    /*
     * Helper method for calculating patient's age
     */
    default Integer calculateAge(LocalDate dob) {

        if (dob == null) {
            return null;
        }

        return Period.between(
                dob,
                LocalDate.now()
        ).getYears();
    }
}