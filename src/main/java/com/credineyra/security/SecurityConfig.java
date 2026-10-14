package com.credineyra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;


    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService) {

        this.customUserDetailsService =
                customUserDetailsService;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/error",
                                "/favicon.ico",
                                "/recuperar-password/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/admin",
                                "/admin/**"
                        )
                        .hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                "/cliente",
                                "/cliente/**"
                        )
                        .hasRole("CLIENTE")

                        .anyRequest()
                        .authenticated()
                )


                .formLogin(form -> form

                        .loginPage("/login")

                        .loginProcessingUrl("/login")

                        .defaultSuccessUrl(
                                "/inicio",
                                true
                        )

                        .failureUrl(
                                "/login?error"
                        )

                        .permitAll()
                )


                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl(
                                "/login?logout"
                        )

                        .permitAll()
                )


                .userDetailsService(
                        customUserDetailsService
                );


        return http.build();
    }

}