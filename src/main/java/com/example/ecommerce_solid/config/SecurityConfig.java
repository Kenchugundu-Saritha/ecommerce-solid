package com.example.ecommerce_solid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.ecommerce_solid.security.JwtAuthenticationFilter;
import com.example.ecommerce_solid.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                // Public APIs
                .requestMatchers(
                        "/hello",
                        "/auth/**"
                ).permitAll()

                // Registration is public
                .requestMatchers(
                        HttpMethod.POST,
                        "/users"
                ).permitAll()

                // CUSTOMER + ADMIN can view products
                .requestMatchers(
                        HttpMethod.GET,
                        "/products/**"
                ).hasAnyRole("CUSTOMER", "ADMIN")

                // Only ADMIN can modify products
                .requestMatchers(
                        HttpMethod.POST,
                        "/products/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/products/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/products/**"
                ).hasRole("ADMIN")

                // Other APIs currently require login
             // CUSTOMER can use cart
                .requestMatchers("/cart/**")
                .hasRole("CUSTOMER")

                // CUSTOMER can make payments
                .requestMatchers("/payments/**")
                .hasRole("CUSTOMER")

                // Orders will be configured separately
                .requestMatchers(
                        HttpMethod.PUT,
                        "/orders/*/status"
                )
                .hasRole("ADMIN")

             // ADMIN can view and manage all orders
                .requestMatchers(
                        HttpMethod.GET,
                        "/orders"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/orders/*/status"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.GET,
                        "/orders/my-orders"
                ).hasRole("CUSTOMER")
                
                // Authenticated users can access individual orders
                .requestMatchers(
                        HttpMethod.GET,
                        "/orders/*"
                ).authenticated()

                // Customer order history
                .requestMatchers(
                        HttpMethod.GET,
                        "/orders/user/*"
                ).authenticated()
            )

            .authenticationProvider(
                    authenticationProvider())

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}