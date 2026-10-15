package com.chatboot.assistant;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<StoredQuestion, UUID> {}
