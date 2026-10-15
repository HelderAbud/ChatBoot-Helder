package com.chatboot.assistant;

import java.util.List;

public record ChatDraft(String answer, List<String> citedChunkIds) {}
