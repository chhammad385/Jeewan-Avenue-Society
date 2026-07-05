package com.jeewanavenue.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jeewanavenue.entity.Plot;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.PlotRepository;
import com.jeewanavenue.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlotRepository plotRepository; // <-- Add PlotRepository

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // --- SEED PRESIDENT USER ---
        if (userRepository.findByEmail("president@jeewanavenue.com").isEmpty()) {
            User president = new User();
            president.setOwnerName("Asif");
            president.setEmail("president@jeewanavenue.com");
            president.setPassword(passwordEncoder.encode("president123"));
            president.setRole("President");
            president.setStatus("Owned");
            president.setPlotNo("P-001");
            userRepository.save(president);
            System.out.println(">>> President user created <<<");
        }

        // --- THIS IS THE NEW LOGIC TO SEED PLOTS ---
        if (plotRepository.count() == 0) {
            System.out.println(">>> No plots found. Seeding initial plot data... <<<");
            List<Plot> plotsToSave = new ArrayList<>();

            // Create 355 Houses
            for (int i = 1; i <= 355; i++) {
                Plot plot = new Plot();
                String id = "H-" + String.format("%03d", i);
                plot.setId(id);
                plot.setType("house");
                // Set status based on the logic from the HTML file
                if (i == 1) plot.setStatus("completed");
                else if (i == 12) plot.setStatus("under-construction");
                else if (i == 45) plot.setStatus("residential");
                else plot.setStatus("vacant");
                plotsToSave.add(plot);
            }

            // Create 42 Shops
            for (int i = 1; i <= 42; i++) {
                Plot plot = new Plot();
                String id = "S-" + String.format("%03d", i);
                plot.setId(id);
                plot.setType("shop");
                // Set status based on the logic from the HTML file
                if (i == 2) plot.setStatus("under-construction");
                else plot.setStatus("vacant");
                plotsToSave.add(plot);
            }

            plotRepository.saveAll(plotsToSave);
            System.out.println(">>> " + plotsToSave.size() + " plots have been saved to the database. <<<");
        }
    }
}