package com.emedic.management.controller;

import com.emedic.management.model.ClinicWorkspace;
import com.emedic.management.repositories.ClinicWorkspaceRepository;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/clinic-workspace")
public class ClinicWorkspaceController {
    private final ClinicWorkspaceRepository repository;
    public ClinicWorkspaceController(ClinicWorkspaceRepository repository) { this.repository = repository; }
    @GetMapping public ClinicWorkspace get() { return repository.findById(1L).orElseGet(() -> { ClinicWorkspace workspace = new ClinicWorkspace(); return repository.save(workspace); }); }
    @PutMapping public ClinicWorkspace save(@RequestBody ClinicWorkspace update) {
        ClinicWorkspace workspace = repository.findById(1L).orElseGet(ClinicWorkspace::new);
        if (update.getName() == null || update.getName().isBlank()) throw new IllegalArgumentException("Clinic name is required");
        workspace.setId(1L); workspace.setName(update.getName().trim()); workspace.setAddress(update.getAddress());
        workspace.setPhone(update.getPhone()); workspace.setEmail(update.getEmail());
        return repository.save(workspace);
    }
}
