package com.hospital_management.hotpital_management.service;

import com.hospital_management.hotpital_management.model.Department;
import com.hospital_management.hotpital_management.model.Doctor;
import com.hospital_management.hotpital_management.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorService doctorService;


    public Department createDepartment(Department department) {
        log.info("Creating new department: {}", department.getName());

        // Check for duplicate name
        if (departmentRepository.existsByName(department.getName())) {
            throw new IllegalArgumentException("Department with name " + department.getName() + " already exists");
        }

        Department savedDepartment = departmentRepository.save(department);
        log.info("Department created successfully with ID: {}", savedDepartment.getId());

        return savedDepartment;
    }

    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentById(Long departmentId) {
        log.debug("Retrieving department with ID: {}", departmentId);
        return departmentRepository.findById(departmentId);
    }

    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentByName(String name) {
        log.debug("Retrieving department with name: {}", name);
        return departmentRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        log.debug("Retrieving all departments");
        return departmentRepository.findAll();
    }

    public Department updateDepartment(Long departmentId, Department updatedDepartment) {
        log.info("Updating department with ID: {}", departmentId);

        Department existingDepartment = departmentRepository.findById(departmentId).orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));


        // Check name uniqueness if name is being changed
        if (!existingDepartment.getName().equals(updatedDepartment.getName()) && departmentRepository.existsByName(updatedDepartment.getName())) {
            throw new IllegalArgumentException("Department name already exists: " + updatedDepartment.getName());
        }

        // Update fields
        existingDepartment.setName(updatedDepartment.getName());

        Department savedDepartment = departmentRepository.save(existingDepartment);
        log.info("Department updated successfully: {}", savedDepartment.getId());

        return savedDepartment;
    }

    public Department assignHeadDoctor(Long departmentId, Long doctorId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

        if (!department.getDoctors().contains(doctor)) {
            throw new IllegalArgumentException("Doctor must be a member of the department before becoming head");
        }

        department.setHeadDoctor(doctor);

        Department savedDepartment = departmentRepository.save(department);
        log.info("Head doctor assigned successfully to department: {}", departmentId);

        return savedDepartment;
    }

    public Department addDoctorToDepartment(Long departmentId, Long doctorId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

        if (department.getDoctors().contains(doctor)) {
            throw new IllegalArgumentException("Doctor is already a member of this department");
        }

        department.getDoctors().add(doctor);
        Department savedDepartment = departmentRepository.save(department);
        log.info("Doctor added successfully to department: {}", departmentId);

        return savedDepartment;
    }

    public Department removeDoctorFromDepartment(Long departmentId, Long doctorId) {
        log.info("Removing doctor {} from department {}", doctorId, departmentId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

        if (department.getHeadDoctor() != null && department.getHeadDoctor().equals(doctor)) {
            throw new IllegalArgumentException("Cannot remove head doctor from department. Assign a new head first.");
        }

        if (!department.getDoctors().remove(doctor)) {
            throw new IllegalArgumentException("Doctor is not a member of this department");
        }

        Department savedDepartment = departmentRepository.save(department);
        log.info("Doctor removed successfully from department: {}", departmentId);

        return savedDepartment;
    }

    @Transactional(readOnly = true)
    public Set<Doctor> getDepartmentDoctors(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        return department.getDoctors();
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDepartmentHeadDoctor(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        return Optional.ofNullable(department.getHeadDoctor());
    }

    public void deleteDepartment(Long departmentId) {
        log.info("Deleting department with ID: {}", departmentId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        // Check if department has any doctors
        if (!department.getDoctors().isEmpty()) {
            throw new IllegalStateException("Cannot delete department with assigned doctors");
        }

        departmentRepository.delete(department);
        log.info("Department deleted successfully: {}", departmentId);
    }

    private void validateDepartmentData(Department department) {
        if (department.getName() == null || department.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Department name is required");
        }
    }
}
