package com.chatboot.documents;

import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private final DocumentService documents;
    private final MarkdownUploadValidator validator;

    public DocumentController(DocumentService documents, MarkdownUploadValidator validator) {
        this.documents = documents;
        this.validator = validator;
    }

    @PostMapping
    public ResponseEntity<StoredDocument> upload(@RequestPart("file") MultipartFile file) throws IOException {
        byte[] content = file.getBytes();
        try {
            validator.validate(file.getOriginalFilename(), content);
        } catch (UploadRejectedException ex) {
            return ResponseEntity.badRequest().build();
        }
        StoredDocument stored = documents.store(currentWorkspaceId(), file.getOriginalFilename(), content);
        if ("FAILED".equals(stored.status())) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(stored);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(stored);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetails> get(@PathVariable UUID id) {
        return documents
                .find(currentWorkspaceId(), id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private UUID currentWorkspaceId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String workspaceId = jwt.getClaimAsString("workspaceId");
        if (workspaceId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return UUID.fromString(workspaceId);
    }
}
