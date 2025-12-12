package com.mzc.lp.domain.content.event;

import com.mzc.lp.domain.content.entity.Content;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ContentCreatedEvent extends ApplicationEvent {

    private final Content content;
    private final Long targetFolderId;

    public ContentCreatedEvent(Object source, Content content, Long targetFolderId) {
        super(source);
        this.content = content;
        this.targetFolderId = targetFolderId;
    }

    public ContentCreatedEvent(Object source, Content content) {
        this(source, content, null);
    }
}
