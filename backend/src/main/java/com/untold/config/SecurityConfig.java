package com.untold.config;

import com.untold.auth.AccountDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(12); }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, AccountDetailsService details,
            @Value("${app.cors.allowed-origin:http://localhost:5173}") String allowedOrigin,
            @Value("${app.cookie.secure:false}") boolean secureCookie) throws Exception {
        CookieCsrfTokenRepository csrf=CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrf.setCookiePath("/");
        csrf.setCookieCustomizer(cookie -> cookie.sameSite("Lax").secure(secureCookie));
        http
          .cors(Customizer.withDefaults())
          .csrf(c -> c.csrfTokenRepository(csrf))
          .userDetailsService(details)
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
          .securityContext(c -> c.requireExplicitSave(true).securityContextRepository(new HttpSessionSecurityContextRepository()))
          .authorizeHttpRequests(a -> a
              .requestMatchers("/api/health","/actuator/health","/api/auth/csrf","/api/auth/register","/api/auth/login").permitAll()
              .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
              .anyRequest().authenticated())
          .logout(l -> l.logoutUrl("/api/auth/logout").logoutSuccessHandler((request,response,auth) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT)).deleteCookies("JSESSIONID","XSRF-TOKEN").invalidateHttpSession(true))
          .exceptionHandling(e -> e.authenticationEntryPoint((request,response,ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)));
        return http.build();
    }
    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origin:http://localhost:5173}") String origin) {
        CorsConfiguration config=new CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type","X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);
        return source;
    }
}
