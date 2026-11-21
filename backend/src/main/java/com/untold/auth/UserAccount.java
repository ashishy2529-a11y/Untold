package com.untold.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name="users")
public class UserAccount implements UserDetails {
    @Id
    @Column(name="id", nullable=false, columnDefinition="BINARY(16)")
    private UUID id;
    @Column(name="email_normalized", nullable=false, unique=true, length=254)
    private String email;
    @Column(name="password_hash", nullable=false, length=255)
    private String passwordHash;
    @Column(name="status", nullable=false, length=16)
    private String status = "ACTIVE";
    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    protected UserAccount() {}
    public UserAccount(String email, String passwordHash) {
        this.id=UUID.randomUUID(); this.email=email; this.passwordHash=passwordHash;
        this.status="ACTIVE"; this.createdAt=Instant.now();
    }
    public UUID getId(){ return id; }
    public String getEmail(){ return email; }
    @Override public String getUsername(){ return email; }
    @Override public String getPassword(){ return passwordHash; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities(){ return List.of(new SimpleGrantedAuthority("ROLE_USER")); }
    @Override public boolean isAccountNonExpired(){ return true; }
    @Override public boolean isAccountNonLocked(){ return "ACTIVE".equals(status); }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled(){ return "ACTIVE".equals(status); }
}