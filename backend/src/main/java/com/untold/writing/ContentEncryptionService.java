package com.untold.writing;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;

@Service
public class ContentEncryptionService {
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private final String encodedKey;
    private final SecureRandom random = new SecureRandom();
    private SecretKeySpec key;

    public ContentEncryptionService(@Value("${app.encryption.key}") String encodedKey) {
        this.encodedKey = encodedKey;
    }

    @PostConstruct
    void initialize() {
        byte[] raw;
        try { raw = Base64.getDecoder().decode(encodedKey); }
        catch (IllegalArgumentException ex) { throw new IllegalStateException("Encryption key must be valid Base64.", ex); }
        if (raw.length != 32) throw new IllegalStateException("Encryption key must decode to exactly 32 bytes.");
        key = new SecretKeySpec(raw, "AES");
        Arrays.fill(raw, (byte) 0);
    }

    public byte[] encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
        } catch (Exception ex) { throw new IllegalStateException("Unable to protect private writing.", ex); }
    }

    public String decrypt(byte[] payload) {
        try {
            if (payload == null || payload.length <= IV_LENGTH + 16) throw new IllegalArgumentException("Invalid encrypted content.");
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) { throw new IllegalStateException("Unable to read private writing. Check encryption key configuration.", ex); }
    }
}