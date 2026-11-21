package com.untold.writing;

import com.untold.auth.UserAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/private-writing")
public class PrivateWritingController {
    private final PrivateWritingRepository repository;
    private final ContentEncryptionService encryption;

    public PrivateWritingController(PrivateWritingRepository repository, ContentEncryptionService encryption) {
        this.repository = repository;
        this.encryption = encryption;
    }

    public record WritingRequest(@NotBlank @Size(max=160) String title, @NotBlank @Size(max=30000) String content) {}
    public record WritingResponse(UUID id, String type, String title, String content, java.time.Instant createdAt, java.time.Instant updatedAt) {}

    @PostMapping("/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    public WritingResponse create(@PathVariable String type, @Valid @RequestBody WritingRequest request, @AuthenticationPrincipal UserAccount user) {
        String safeType = normalizeType(type);
        PrivateWriting saved = repository.save(new PrivateWriting(user.getId(), safeType, encryption.encrypt(request.title().trim()), encryption.encrypt(request.content())));
        return toResponse(saved);
    }

    @GetMapping("/{type}")
    public List<WritingResponse> list(@PathVariable String type, @AuthenticationPrincipal UserAccount user) {
        return repository.findAllByOwnerIdAndWritingTypeOrderByUpdatedAtDesc(user.getId(), normalizeType(type))
                .stream().map(this::toResponse).toList();
    }

    @GetMapping("/{type}/{id}")
    public WritingResponse get(@PathVariable String type, @PathVariable UUID id, @AuthenticationPrincipal UserAccount user) {
        return toResponse(findOwned(id, user));
    }

    @PutMapping("/{type}/{id}")
    public WritingResponse update(@PathVariable String type, @PathVariable UUID id, @Valid @RequestBody WritingRequest request, @AuthenticationPrincipal UserAccount user) {
        PrivateWriting writing = findOwned(id, user);
        if (!writing.getWritingType().equals(normalizeType(type))) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        writing.update(encryption.encrypt(request.title().trim()), encryption.encrypt(request.content()));
        return toResponse(repository.save(writing));
    }

    @DeleteMapping("/{type}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String type, @PathVariable UUID id, @AuthenticationPrincipal UserAccount user) {
        PrivateWriting writing = findOwned(id, user);
        if (!writing.getWritingType().equals(normalizeType(type))) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repository.delete(writing);
    }

    private PrivateWriting findOwned(UUID id, UserAccount user) {
        return repository.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private String normalizeType(String type) {
        if ("void".equalsIgnoreCase(type)) return "VOID";
        if ("unsent".equalsIgnoreCase(type)) return "UNSENT";
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    private WritingResponse toResponse(PrivateWriting writing) {
        return new WritingResponse(writing.getId(), writing.getWritingType(), writing.getEncryptedTitle() == null ? writing.getTitle() : encryption.decrypt(writing.getEncryptedTitle()),
                encryption.decrypt(writing.getEncryptedContent()), writing.getCreatedAt(), writing.getUpdatedAt());
    }
}