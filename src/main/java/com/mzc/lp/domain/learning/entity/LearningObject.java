package com.mzc.lp.domain.learning.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.content.entity.Content;
import com.mzc.lp.domain.learning.constant.CompletionCriteria;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "learning_object", indexes = {
        @Index(name = "idx_lo_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_lo_content", columnList = "content_id"),
        @Index(name = "idx_lo_folder", columnList = "folder_id"),
        @Index(name = "idx_lo_name", columnList = "name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningObject extends TenantEntity {

    @Version
    private Long version;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private ContentFolder folder;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_criteria", length = 20)
    private CompletionCriteria completionCriteria = CompletionCriteria.PERCENT_100;

    // 정적 팩토리 메서드
    public static LearningObject create(String name, Content content, ContentFolder folder) {
        LearningObject lo = new LearningObject();
        lo.name = name;
        lo.content = content;
        lo.folder = folder;
        return lo;
    }

    // 정적 팩토리 메서드 - 폴더 없이 생성
    public static LearningObject create(String name, Content content) {
        return create(name, content, null);
    }

    // 비즈니스 메서드 - 이름 수정
    public void updateName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    // 비즈니스 메서드 - 폴더 변경
    public void moveToFolder(ContentFolder folder) {
        this.folder = folder;
    }

    // 비즈니스 메서드 - 최상위로 이동
    public void moveToRoot() {
        this.folder = null;
    }

    // 비즈니스 메서드 - 완료 기준 변경
    public void updateCompletionCriteria(CompletionCriteria completionCriteria) {
        if (completionCriteria != null) {
            this.completionCriteria = completionCriteria;
        }
    }
}
