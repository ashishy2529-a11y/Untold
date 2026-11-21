package com.untold.writing;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "private_writings", indexes = {
    @Index(name = "idx_private_writings_owner_type_created", columnList = "owner_id, writing_type, created_at")
})
public class PrivateWriting {
    @Id
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    @Column(name = "owner_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID ownerId;
    @Column(name = "writing_type", nullable = false, length = 16)
    private String writingType;
    @Column(name = "encrypted_title", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] encryptedTitle;
    @Column(name = "encrypted_content", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] encryptedContent;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PrivateWriting() {}
    public PrivateWriting(UUID ownerId, String writingType, byte[] encryptedTitle, byte[] encryptedContent) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.writingType = writingType;
        this.encryptedTitle = encryptedTitle;
        this.encryptedContent = encryptedContent;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    public UUID getId(){ return id; }
    public UUID getOwnerId(){ return ownerId; }
    public String getWritingType(){ return writingType; }
    public byte[] getEncryptedTitle(){ return encryptedTitle; }
    public byte[] getEncryptedContent(){ return encryptedContent; }
    public Instant getCreatedAt(){ return createdAt; }
    public Instant getUpdatedAt(){ return updatedAt; }
    public void update(byte[] encryptedTitle, byte[] encryptedContent) {
        this.encryptedTitle = encryptedTitle;
        this.encryptedContent = encryptedContent;
        this.updatedAt = Instant.now();
    }
}