package com.untold.writing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivateWritingRepository extends JpaRepository<PrivateWriting, UUID> {
    List<PrivateWriting> findAllByOwnerIdAndWritingTypeOrderByUpdatedAtDesc(UUID ownerId, String writingType);
    Optional<PrivateWriting> findByIdAndOwnerId(UUID id, UUID ownerId);
}