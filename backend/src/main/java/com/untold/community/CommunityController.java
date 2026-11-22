package com.untold.community;

import com.untold.auth.UserAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final JdbcTemplate jdbc;
    public CommunityController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record CreatePostRequest(
        @NotBlank @Size(max=32) String category,
        @NotBlank @Size(max=4000) String content
    ) {}
    public record ReportRequest(
        @NotBlank String reason,
        @Size(max=500) String details
    ) {}
    public record PostResponse(UUID id, String category, String content, String publishedAs, Instant createdAt) {}

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createPost(@Valid @RequestBody CreatePostRequest request,
                                         @AuthenticationPrincipal UserAccount user) {
        String category = request.category().trim().toUpperCase();
        if (!List.of("LIFE","GROWING UP","SMALL WINS","RELATIONSHIPS","WORK","STUDY","OTHER").contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a supported story category.");
        }
        String content = request.content().trim();
        if (content.length() < 20) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please write at least 20 characters.");
        if (content.matches("(?is).*https?://\\S+.*")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Links are not allowed in community stories.");
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO community_posts(id,author_id,category,content,status) VALUES(?,?,?,?,'PENDING')",
            bytes(id), bytes(user.getId()), category, content);
        return Map.of("id", id, "status", "PENDING", "message", "Your story was submitted for moderation. It is not public yet.");
    }

    @GetMapping("/feed")
    public List<PostResponse> feed(@AuthenticationPrincipal UserAccount user) {
        return jdbc.query("""
            SELECT p.id,p.category,p.content,p.created_at
            FROM community_posts p
            WHERE p.status='APPROVED'
              AND NOT EXISTS (SELECT 1 FROM user_blocks b WHERE b.blocker_id=? AND b.blocked_id=p.author_id)
              AND NOT EXISTS (SELECT 1 FROM user_blocks b WHERE b.blocker_id=p.author_id AND b.blocked_id=?)
            ORDER BY p.created_at DESC LIMIT 100
            """, (rs,n) -> new PostResponse(uuid(rs.getBytes("id")),rs.getString("category"),
                rs.getString("content"),"Someone, somewhere",rs.getTimestamp("created_at").toInstant()),
            bytes(user.getId()), bytes(user.getId()));
    }

    @GetMapping("/mine")
    public List<Map<String,Object>> mine(@AuthenticationPrincipal UserAccount user) {
        return jdbc.query("""
            SELECT id,category,status,created_at FROM community_posts
            WHERE author_id=? ORDER BY created_at DESC LIMIT 100
            """, (rs,n) -> Map.of("id",uuid(rs.getBytes("id")),"category",rs.getString("category"),
                "status",rs.getString("status"),"createdAt",rs.getTimestamp("created_at").toInstant()), bytes(user.getId()));
    }

    @PostMapping("/posts/{postId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> report(@PathVariable UUID postId, @Valid @RequestBody ReportRequest request,
                                     @AuthenticationPrincipal UserAccount user) {
        byte[] author = jdbc.query("SELECT author_id FROM community_posts WHERE id=?",
            rs -> rs.next() ? rs.getBytes(1) : null, bytes(postId));
        if (author == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (java.util.Arrays.equals(author, bytes(user.getId()))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You cannot report your own story.");
        String reason=request.reason().trim().toUpperCase();
        if (!List.of("HARASSMENT","HATE","THREATS","DOXXING","SEXUAL_CONTENT","SPAM","SELF_HARM_RISK","OTHER").contains(reason))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a supported report reason.");
        try {
            jdbc.update("INSERT INTO community_reports(id,post_id,reporter_id,reason,details) VALUES(?,?,?,?,?)",
                bytes(UUID.randomUUID()),bytes(postId),bytes(user.getId()),reason,
                request.details()==null?null:request.details().trim());
        } catch (DuplicateKeyException duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"You have already reported this story.");
        }
        return Map.of("message","Report received. The story has been sent to the safety review queue.");
    }

    @PostMapping("/posts/{postId}/block-author")
    public Map<String,String> blockAuthor(@PathVariable UUID postId, @AuthenticationPrincipal UserAccount user) {
        byte[] author = jdbc.query("SELECT author_id FROM community_posts WHERE id=?",
            rs -> rs.next() ? rs.getBytes(1) : null, bytes(postId));
        if (author == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (java.util.Arrays.equals(author,bytes(user.getId()))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You cannot block yourself.");
        jdbc.update("INSERT IGNORE INTO user_blocks(blocker_id,blocked_id) VALUES(?,?)",bytes(user.getId()),author);
        return Map.of("message","This author is now blocked. Their stories will be hidden from your feed.");
    }

    @DeleteMapping("/blocks/{blockedUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(@PathVariable UUID blockedUserId, @AuthenticationPrincipal UserAccount user) {
        jdbc.update("DELETE FROM user_blocks WHERE blocker_id=? AND blocked_id=?",bytes(user.getId()),bytes(blockedUserId));
    }

    private static byte[] bytes(UUID value) {
        return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()).array();
    }
    private static UUID uuid(byte[] value) {
        ByteBuffer buffer=ByteBuffer.wrap(value);
        return new UUID(buffer.getLong(),buffer.getLong());
    }
}
