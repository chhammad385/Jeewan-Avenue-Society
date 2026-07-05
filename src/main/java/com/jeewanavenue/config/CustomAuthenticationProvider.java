package com.jeewanavenue.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        // Get the role from the HTTP request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String roleFromForm = request.getParameter("role");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // --- ACCOUNT STATUS CHECK ---
        if ("Inactive".equals(user.getAccountStatus())) {
            System.out.println("AUTHENTICATION FAILED: Account is inactive");
            throw new BadCredentialsException("Your account has been deactivated. Please contact administrator.");
        }

        // --- ROLE CHECK ---
        // Normalize both roles by converting to uppercase and replacing hyphens with underscores
        String userRoleNormalized = user.getRole().toUpperCase().replace("-", "_").replace(" ", "_");
        String formRoleNormalized = roleFromForm.toUpperCase().replace("-", "_").replace(" ", "_");

        System.out.println("=== LOGIN ATTEMPT DEBUG ===");
        System.out.println("Email: " + email);
        System.out.println("Account Status: " + user.getAccountStatus());
        System.out.println("User role from DB: '" + user.getRole() + "' -> normalized: '" + userRoleNormalized + "'");
        System.out.println("Form role selected: '" + roleFromForm + "' -> normalized: '" + formRoleNormalized + "'");
        System.out.println("Roles match: " + userRoleNormalized.equals(formRoleNormalized));

        if (!userRoleNormalized.equals(formRoleNormalized)) {
            System.out.println("AUTHENTICATION FAILED: Role mismatch");
            throw new BadCredentialsException("Role mismatch - User role '" + user.getRole() + "' does not match selected role '" + roleFromForm + "'");
        }

        // Use ROLE_ prefix for Spring Security with normalized role name
        String finalRole = "ROLE_" + userRoleNormalized;
        System.out.println("Final Spring Security role: '" + finalRole + "'");
        System.out.println("AUTHENTICATION SUCCESS for " + email);
        System.out.println("===============================");
        
        return new UsernamePasswordAuthenticationToken(
                email,
                password,
                Collections.singletonList(new SimpleGrantedAuthority(finalRole))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
