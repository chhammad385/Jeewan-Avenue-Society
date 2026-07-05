package com.jeewanavenue.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // --- PUBLIC ACCESS ---
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/"),
                                AntPathRequestMatcher.antMatcher("/index"),
                                AntPathRequestMatcher.antMatcher("/index.html"),
                                AntPathRequestMatcher.antMatcher("/home.html"),
                                AntPathRequestMatcher.antMatcher("/Login Page.html"),
                                AntPathRequestMatcher.antMatcher("/Prayer-Timing.html"), // Allow public access to prayer timings
                                // AntPathRequestMatcher.antMatcher("/Annocement-Pop Up.html"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/announcements"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/announcements/**"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/prayer-timings"), // Allow public access to prayer timings API
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/prayer-timings/**"), // Allow public access to prayer timings API
                                AntPathRequestMatcher.antMatcher("/uploads/**"),
                                AntPathRequestMatcher.antMatcher("/receipts/**"), // Direct receipt access
                                AntPathRequestMatcher.antMatcher("/profiles/**"), // Direct profile access
                                AntPathRequestMatcher.antMatcher("/css/**"),
                                AntPathRequestMatcher.antMatcher("/js/**"),
                                AntPathRequestMatcher.antMatcher("/image/**")
                        ).permitAll()
                        
                        // --- SOCIETY MEMBER ACCESS (Non-admin pages) ---
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/announcements.html"),
                                AntPathRequestMatcher.antMatcher("/Member.html"),
                                AntPathRequestMatcher.antMatcher("/User Profile.html"),
                                AntPathRequestMatcher.antMatcher("/Your Records.html"),
                                AntPathRequestMatcher.antMatcher("/Feedback.html"),
                                AntPathRequestMatcher.antMatcher("/Plots.html"), // Read-only for members
                                AntPathRequestMatcher.antMatcher("/Masjid.html"),
                                AntPathRequestMatcher.antMatcher("/Official Docs.html"),
                                AntPathRequestMatcher.antMatcher("/Accounts.html"), // Read-only for members
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/plots"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/financials"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/users/**"), // Own profile
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/feedback"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/users/**"), // Own profile update
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/auth/current-user"), // User info for role restrictions
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/auth/check-permission") // Permission checking
                        ).hasAnyRole("SOCIETY_MEMBER", "PRESIDENT", "GENERAL_SECRETARY", "FINANCE_SECRETARY", "INFORMATION_SECRETARY", "VICE_PRESIDENT")
                        
                        // --- PLOTS: Edit/Save/Delete - General Secretary & President ---
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/plots"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/plots/**"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/plots/**")
                        ).hasAnyRole("PRESIDENT", "GENERAL_SECRETARY")
                        
                        // --- MANAGE MEMBERS: Edit/Delete - General Secretary & President ---
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/users"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/users/**"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/users/**"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/staff"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/staff/**"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/staff/**")
                        ).hasAnyRole("PRESIDENT", "GENERAL_SECRETARY")
                        
                        // --- FINANCIALS: Add/Edit/Delete - Finance Secretary ONLY ---
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/financials"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/financials/**"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/financials/**")
                        ).hasRole("FINANCE_SECRETARY")
                        
                        // --- ANNOUNCEMENTS: Upload - Information Secretary ONLY ---
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/announcements"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/announcements/**"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/announcements/**")
                        ).hasRole("INFORMATION_SECRETARY")
                        
                        // --- DOCUMENTS: Upload - Information Secretary ONLY ---
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/documents"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/documents/**")
                        ).hasRole("INFORMATION_SECRETARY")
                        
                        // --- RENTER DOCS: Add/Edit - General Secretary ONLY ---
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/renters"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/renters/**"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/renters/**")
                        ).hasRole("GENERAL_SECRETARY")
                        
                        // --- PRAYER TIMER: Settings - Any authenticated role can set ---
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/prayer-timings"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/prayer-timings/**"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/prayer-timings/**")
                        ).hasAnyRole("SOCIETY_MEMBER", "PRESIDENT", "GENERAL_SECRETARY", "FINANCE_SECRETARY", "INFORMATION_SECRETARY", "VICE_PRESIDENT")
                        
                        // --- PRESIDENT CONSOLE: Access - Everyone except Society Members ---
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/President Console.html"),
                                AntPathRequestMatcher.antMatcher("/Announcement-President Console.html"),
                                AntPathRequestMatcher.antMatcher("/Manage User-President Console.html"),
                                AntPathRequestMatcher.antMatcher("/Manage Member-President Console.html"),
                                AntPathRequestMatcher.antMatcher("/Financials-President Console.html"),
                                AntPathRequestMatcher.antMatcher("/Review-President Console.html"),
                                AntPathRequestMatcher.antMatcher("/Renters Docs-President Console.html"),
                                AntPathRequestMatcher.antMatcher("/Admin-Dashboard-Demo.html") // Demo page for tab restrictions
                        ).hasAnyRole("PRESIDENT", "GENERAL_SECRETARY", "FINANCE_SECRETARY", "INFORMATION_SECRETARY", "VICE_PRESIDENT")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/Login Page.html")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/User Profile.html", true)
                        .failureUrl("/Login Page.html?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home.html?logout")
                        .permitAll()
                );
        return http.build();
    }
}
