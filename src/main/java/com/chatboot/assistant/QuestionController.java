package com.chatboot.assistant;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {
    private final QuestionService questions;
    private final double minimumScore;

    public QuestionController(
            QuestionService questions, @Value("${chatboot.evidence.minimum-score:1.01}") double minimumScore) {
        this.questions = questions;
        this.minimumScore = minimumScore;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> ask(@RequestBody QuestionRequest request) {
        try {
            return ResponseEntity.ok(
                    questions.ask(currentUserId(), currentWorkspaceId(), request.question(), minimumScore));
        } catch (EmbeddingUnavailableException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    private UUID currentUserId() {
        return UUID.fromString(currentJwt().getSubject());
    }

    private UUID currentWorkspaceId() {
        String workspaceId = currentJwt().getClaimAsString("workspaceId");
        if (workspaceId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return UUID.fromString(workspaceId);
    }

    private Jwt currentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return jwt;
    }
}
