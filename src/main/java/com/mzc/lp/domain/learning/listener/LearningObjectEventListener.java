package com.mzc.lp.domain.learning.listener;

import com.mzc.lp.domain.content.entity.Content;
import com.mzc.lp.domain.content.event.ContentCreatedEvent;
import com.mzc.lp.domain.learning.entity.ContentFolder;
import com.mzc.lp.domain.learning.entity.LearningObject;
import com.mzc.lp.domain.learning.repository.ContentFolderRepository;
import com.mzc.lp.domain.learning.repository.LearningObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningObjectEventListener {

    private final LearningObjectRepository learningObjectRepository;
    private final ContentFolderRepository contentFolderRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleContentCreated(ContentCreatedEvent event) {
        Content content = event.getContent();
        Long targetFolderId = event.getTargetFolderId();

        ContentFolder folder = null;
        if (targetFolderId != null) {
            folder = contentFolderRepository.findByIdAndTenantId(targetFolderId, content.getTenantId())
                    .orElse(null);
        }

        LearningObject learningObject = LearningObject.create(
                content.getOriginalFileName(),
                content,
                folder
        );

        LearningObject saved = learningObjectRepository.save(learningObject);
        log.info("LearningObject auto-created: id={}, contentId={}, folderId={}",
                saved.getId(), content.getId(), targetFolderId);
    }
}
