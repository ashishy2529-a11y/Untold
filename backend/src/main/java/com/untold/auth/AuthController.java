package com.untold.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAccountRepository users;
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(UserAccountRepository users, org.springframework.security.crypto.password.PasswordEncoder encoder, AuthenticationManager authenticationManager) {
        this.users=users; this.encoder=encoder; this.authenticationManager=authenticationManager;
    }
    public record RegisterRequest(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(min=12,max=72) String password) {}
    public record LoginRequest(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(max=72) String password) {}

    @GetMapping("/csrf")
    public Map<String,String> csrf(CsrfToken token){ return Map.of("token", token.getToken()); }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) return ResponseEntity.badRequest().body(Map.of("error","Password is too long."));
        String email=request.email().trim().toLowerCase(Locale.ROOT);
        if(users.existsByEmail(email)) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","An account with this email already exists."));
        try {
            users.save(new UserAccount(email, encoder.encode(request.password())));
        } catch (DataIntegrityViolationException duplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","An account with this email already exists."));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","Account created. Please sign in."));
    }

    @PostMapping("/login")
    public Map<String,String> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Email or password is incorrect.");
        String email=request.email().trim().toLowerCase(Locale.ROOT);
        final Authentication authentication;
        try {
            authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,request.password()));
        } catch (BadCredentialsException invalid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Email or password is incorrect.");
        }
        if (servletRequest.getSession(false) != null) servletRequest.changeSessionId();
        SecurityContext context=SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context,servletRequest,response);
        return Map.of("message","Signed in");
    }

    @GetMapping("/me")
    public Map<String,String> me(@org.springframework.security.core.annotation.AuthenticationPrincipal UserAccount user) {
        return Map.of("email",user.getEmail());
    }
}