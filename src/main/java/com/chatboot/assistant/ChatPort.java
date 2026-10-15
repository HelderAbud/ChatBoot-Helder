package com.chatboot.assistant;

import java.util.List;

public interface ChatPort {
    ChatDraft answer(String question, List<RetrievedChunk> chunks);
}
