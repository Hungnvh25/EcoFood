package com.example.ecofood.config;



import com.example.ecofood.auth.JwtAuthenticationEntryPoint;
import com.example.ecofood.auth.JwtAuthenticationFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.payos.PayOS;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

     @Value("${PAYOS_CLIENT_ID}")
     private String clientId;

    @Value("${PAYOS_API_KEY}")
    private  String apiKey;

    @Value("${PAYOS_CHECKSUM_KEY}")
    private  String checksumKey;
    LevenshteinDistance levenshteinDistance;

    @Bean
    public LevenshteinDistance levenshteinDistance() {
        return new LevenshteinDistance();
    }

    @Bean
    public PayOS payOS() {
        return new PayOS(clientId,apiKey,checksumKey);
    }

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // DÙNG session cho OAuth2
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/image/**", "/uploads/**", "/oauth2/**").permitAll()
                        .requestMatchers("/admin/**", "/admin").hasRole("ADMIN")
                        .requestMatchers("/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .loginPage("/login") // Trang login mặc định
                        .defaultSuccessUrl("/oauth2/success", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("jwtToken")
                        .logoutSuccessUrl("/login")
                        .permitAll());

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException, ServletException {
                response.sendRedirect("/403");
            }

        };
    }
}
