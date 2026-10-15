package com.chatboot.assistant;

import java.util.List;

public record QuestionResponse(String answer, boolean abstained, List<Citation> citations) {}
