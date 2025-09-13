package com.hospital_management.hotpital_management;

import com.hospital_management.hotpital_management.model.Appointment;
import com.hospital_management.hotpital_management.model.Insurance;
import com.hospital_management.hotpital_management.model.Patient;
import com.hospital_management.hotpital_management.repository.AppointmentRepository;
import com.hospital_management.hotpital_management.service.AppointmentService;
import com.hospital_management.hotpital_management.service.InsuranceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
public class InsuranceTest {

    @Autowired
    private InsuranceService insuranceService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    public void testInsurance() {
        Insurance insurance = Insurance.builder()
                .policyNumber("NNR_332")
                .provider("NNR")
                .validUntil(LocalDate.of(2030, 12, 12))
                .build();

        Patient patient = insuranceService.assignInsuranceToPatient(insurance, 1L);
        System.out.println(patient);
    }

    @Test
    public void testCreateAppointment() {
        Appointment appointment = Appointment.builder()
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .reason("Gas")
                .build();

        var newAppointment = appointmentService.createNewAppointment(appointment, 1L, 1L);
        System.out.println(newAppointment);


    }
}
