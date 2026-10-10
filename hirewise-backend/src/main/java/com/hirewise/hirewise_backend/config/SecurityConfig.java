package com.hirewise.hirewise_backend.config;

import com.hirewise.hirewise_backend.security.JwtAccessDeniedHandler;
import com.hirewise.hirewise_backend.security.JwtAuthenticationEntryPoint;
import com.hirewise.hirewise_backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/jobs/*/applications").hasRole("JOB_SEEKER")
                .requestMatchers(HttpMethod.GET, "/api/jobs/*/applications").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.GET, "/api/jobs/my-jobs").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.GET, "/api/jobs", "/api/jobs/*").permitAll()
                .requestMatchers("/api/jobs/**").hasRole("RECRUITER")
                .requestMatchers("/api/applications/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/resumes/upload").hasRole("JOB_SEEKER")
                .requestMatchers(HttpMethod.PUT, "/api/resumes/*").hasRole("JOB_SEEKER")
                .requestMatchers(HttpMethod.DELETE, "/api/resumes/*").hasRole("JOB_SEEKER")
                .requestMatchers(HttpMethod.GET, "/api/resumes/my").hasRole("JOB_SEEKER")
                .requestMatchers(HttpMethod.GET, "/api/resumes/*/analyze").hasRole("JOB_SEEKER")
                .requestMatchers("/api/resumes/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

