package com.jeewanavenue.controller;

import com.jeewanavenue.dto.PlotDTO;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plots")
public class PlotController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<PlotDTO>> getAllPlots() {
        List<User> users = userRepository.findAll();
        
        List<PlotDTO> plots = users.stream()
            .filter(user -> user.getPlotNo() != null && !user.getPlotNo().trim().isEmpty())
            .map(this::convertUserToPlot)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(plots);
    }

    @GetMapping("/{plotNo}")
    public ResponseEntity<PlotDTO> getPlotByPlotNo(@PathVariable String plotNo) {
        User user = userRepository.findByPlotNo(plotNo).orElse(null);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        PlotDTO plot = convertUserToPlot(user);
        return ResponseEntity.ok(plot);
    }

    @PutMapping("/{plotNo}")
    public ResponseEntity<PlotDTO> updatePlot(@PathVariable String plotNo, @RequestBody PlotDTO plotData) {
        User user = userRepository.findByPlotNo(plotNo).orElse(null);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Update user fields based on plot data
        user.setBuiltStatus(plotData.getStatus());
        user.setOwnerName(plotData.getOwnerName());
        user.setPhoneNo(plotData.getOwnerPhone());
        user.setRenterName(plotData.getRenterName());
        user.setRenterPhoneNo(plotData.getRenterPhone());
        
        // Update residence status based on checkboxes
        if (plotData.isResidentOwner() && plotData.isResidentRenter()) {
            user.setStatus("Both");
        } else if (plotData.isResidentOwner()) {
            user.setStatus("Owned");
        } else if (plotData.isResidentRenter()) {
            user.setStatus("Rented");
        }
        
        userRepository.save(user);
        
        PlotDTO updatedPlot = convertUserToPlot(user);
        return ResponseEntity.ok(updatedPlot);
    }

    private PlotDTO convertUserToPlot(User user) {
        PlotDTO plot = new PlotDTO();
        plot.setId(user.getPlotNo());
        
        // Convert property_type to lowercase for frontend
        String propertyType = user.getPropertyType();
        if (propertyType != null) {
            plot.setType(propertyType.equalsIgnoreCase("House") ? "house" : "shop");
        } else {
            plot.setType("house"); // default
        }
        
        // Convert built_status to lowercase with hyphens
        String builtStatus = user.getBuiltStatus();
        if (builtStatus != null) {
            switch (builtStatus) {
                case "Completed":
                    plot.setStatus("completed");
                    break;
                case "Under Construction":
                    plot.setStatus("under-construction");
                    break;
                case "Vacant":
                    plot.setStatus("vacant");
                    break;
                case "Residential":
                    plot.setStatus("residential");
                    break;
                default:
                    plot.setStatus("vacant");
            }
        } else {
            plot.setStatus("vacant");
        }
        
        plot.setOwnerName(user.getOwnerName());
        plot.setOwnerPhone(user.getPhoneNo());
        plot.setRenterName(user.getRenterName());
        plot.setRenterPhone(user.getRenterPhoneNo());
        
        // Set resident checkboxes based on status
        String status = user.getStatus();
        if (status != null) {
            switch (status) {
                case "Owned":
                    plot.setResidentOwner(true);
                    plot.setResidentRenter(false);
                    break;
                case "Rented":
                    plot.setResidentOwner(false);
                    plot.setResidentRenter(true);
                    break;
                case "Both":
                    plot.setResidentOwner(true);
                    plot.setResidentRenter(true);
                    break;
                default:
                    plot.setResidentOwner(false);
                    plot.setResidentRenter(false);
            }
        }
        
        plot.setUserId(user.getId());
        
        return plot;
    }
}