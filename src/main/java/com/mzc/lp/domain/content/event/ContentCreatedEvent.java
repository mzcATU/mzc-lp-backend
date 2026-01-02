package com.mzc.lp.domain.content.event;

import com.mzc.lp.domain.content.entity.Content;
import com.mzc.lp.domain.learning.constant.CompletionCriteria;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ContentCreatedEvent extends ApplicationEvent {

    private final Content content;
    private final Long targetFolderId;
    private final CompletionCriteria completionCriteria;

    public ContentCreatedEvent(Object source, Content content, Long targetFolderId, CompletionCriteria completionCriteria) {
        super(source);
        this.content = content;
        this.targetFolderId = targetFolderId;
        this.completionCriteria = completionCriteria;
    }

    public ContentCreatedEvent(Object source, Content content, Long targetFolderId) {
        this(source, content, targetFolderId, null);
    }

    public ContentCreatedEvent(Object source, Content content) {
        this(source, content, null, null);
    }
}
