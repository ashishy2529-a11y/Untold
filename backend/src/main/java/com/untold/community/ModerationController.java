package com.untold.community;

import com.untold.auth.UserAccount;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/moderation")
@PreAuthorize("hasRole('MODERATOR')")
public class ModerationController {
    private final JdbcTemplate jdbc;
    public ModerationController(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    @GetMapping("/queue")
    public List<Map<String,Object>> queue() {
        return jdbc.query("""
            SELECT p.id,p.category,p.content,p.created_at,
                   (SELECT COUNT(*) FROM community_reports r WHERE r.post_id=p.id AND r.status='OPEN') AS report_count
            FROM community_posts p WHERE p.status='PENDING'
            ORDER BY report_count DESC,p.created_at ASC LIMIT 200
            """, (rs,n) -> Map.of("id",uuid(rs.getBytes("id")),"category",rs.getString("category"),
                "content",rs.getString("content"),"createdAt",rs.getTimestamp("created_at").toInstant(),
                "reportCount",rs.getInt("report_count")));
    }

    @PostMapping("/posts/{postId}/decision")
    @Transactional
    public Map<String,String> decide(@PathVariable UUID postId, @RequestBody Decision request,
                                     @AuthenticationPrincipal UserAccount moderator) {
        if (request.action()==null || !List.of("APPROVE","REJECT").contains(request.action().toUpperCase()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Action must be APPROVE or REJECT.");
        String action=request.action().toUpperCase();
        String next=action.equals("APPROVE")?"APPROVED":"REJECTED";
        int changed=jdbc.update("UPDATE community_posts SET status=?,moderated_at=CURRENT_TIMESTAMP(6) WHERE id=? AND status='PENDING'",next,bytes(postId));
        if (changed==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Pending story not found.");
        jdbc.update("INSERT INTO moderation_audit(id,moderator_id,post_id,action,note) VALUES(?,?,?,?,?)",
            bytes(UUID.randomUUID()),bytes(moderator.getId()),bytes(postId),action,
            request.note()==null?null:request.note().trim());
        jdbc.update("UPDATE community_reports SET status='REVIEWED' WHERE post_id=? AND status='OPEN'",bytes(postId));
        return Map.of("status",next,"message",action.equals("APPROVE")?"Story approved for the community feed.":"Story rejected and kept out of the feed.");
    }

    public record Decision(String action,String note) {}
    private static byte[] bytes(UUID value) {
        return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()).array();
    }
    private static UUID uuid(byte[] value) {
        ByteBuffer buffer=ByteBuffer.wrap(value);
        return new UUID(buffer.getLong(),buffer.getLong());
    }
}
