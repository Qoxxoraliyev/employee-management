package com.muhammadali.employee_management.security.config;
import com.muhammadali.employee_management.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
            Exception {
        http
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/docker"
                        ).permitAll()
                        .requestMatchers("/api/users/generateToken").permitAll()
                        .requestMatchers("/api/users").permitAll()
                        .requestMatchers("/api/users/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/employees/save").hasAuthority("ADMIN")
                        .requestMatchers("/api/employees/*").hasAuthority("ADMIN")
                        .requestMatchers("/api/employees/delete/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/employees/**").hasAnyAuthority("ADMIN", "HR", "MANAGER")
                        .requestMatchers("/api/employees/advanced-search").hasAnyAuthority("ADMIN", "HR", "MANAGER")
                        .requestMatchers("/api/employees/count").hasAuthority("ADMIN")
                        .requestMatchers("/api/employees/active-percentage").hasAnyAuthority("ADMIN", "HR")
                        .requestMatchers("/api/employees/new-employees/month").hasAnyAuthority("ADMIN", "HR")
                        .requestMatchers("/api/employees/*/documents/**")
                        .hasAnyAuthority("ADMIN", "HR", "MANAGER")
                        .requestMatchers("/api/salaries/pdf/**").authenticated()
                        .requestMatchers("/api/salaries/employee/**").authenticated()
                        .requestMatchers("/api/salaries/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/departments/create").hasAuthority("ADMIN")
                        .requestMatchers("/api/departments/update/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/departments/delete/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/departments/*/stats").hasAnyAuthority("ADMIN", "HR", "MANAGER")
                        .requestMatchers("/api/departments/all").authenticated()
                        .requestMatchers("/api/departments/*/employee-count").authenticated()
                        .requestMatchers("/api/departments/employee-count").authenticated()
                        .anyRequest().authenticated()
                );
        http.addFilterBefore(jwtAuthFilter,org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
                return http.build();
    }



    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration
                                                       config) throws Exception{
        return config.getAuthenticationManager();
    }


}
