package com.example.employeeapi.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.employeeapi.model.Employee;
import com.example.employeeapi.service.EmployeeService;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    private boolean isImageFile(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        return extension != null && List.of("jpg", "jpeg", "png").contains(extension.toLowerCase());
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            Employee emp = new Employee();
            emp.setName(name);
            emp.setEmail(email);
            emp.setPosition(position);

            if (image != null && !image.isEmpty()) {
                if (!isImageFile(image)) {
                    return ResponseEntity.badRequest().body("Only image files (jpg, jpeg, png) are allowed.");
                }

                String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(image.getOriginalFilename());
                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) uploadPath.mkdirs();

                image.transferTo(new File(uploadDir + fileName));
                emp.setImageUrl("/files/" + fileName);
            }

            return ResponseEntity.ok(service.save(emp));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving file: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) MultipartFile image
    ) {
        try {
            Optional<Employee> optEmp = service.findById(id);
            if (optEmp.isEmpty()) return ResponseEntity.notFound().build();

            Employee emp = optEmp.get();
            emp.setName(name);
            emp.setEmail(email);
            emp.setPosition(position);

            if (image != null && !image.isEmpty()) {
                if (!isImageFile(image)) {
                    return ResponseEntity.badRequest().body("Only image files (jpg, jpeg, png) are allowed.");
                }

                if (emp.getImageUrl() != null) {
                    File oldFile = new File(uploadDir + new File(emp.getImageUrl()).getName());
                    if (oldFile.exists()) oldFile.delete();
                }

                String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(image.getOriginalFilename());
                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) uploadPath.mkdirs();

                image.transferTo(new File(uploadDir + fileName));
                emp.setImageUrl("/files/" + fileName);
            }

            return ResponseEntity.ok(service.save(emp));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating employee: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<Employee> empOpt = service.findById(id);
        empOpt.ifPresent(emp -> {
            if (emp.getImageUrl() != null) {
                File oldFile = new File(uploadDir + new File(emp.getImageUrl()).getName());
                if (oldFile.exists()) oldFile.delete();
            }
        });

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
