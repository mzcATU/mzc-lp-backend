package com.mzc.lp.domain.learning.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "content_folder", indexes = {
        @Index(name = "idx_folder_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_folder_parent", columnList = "parent_id"),
        @Index(name = "idx_folder_depth", columnList = "depth")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentFolder extends TenantEntity {

    public static final int MAX_DEPTH = 2; // 0, 1, 2 -> 3단계

    @Version
    private Long version = 0L;

    @Column(name = "folder_name", nullable = false, length = 255)
    private String folderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ContentFolder parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentFolder> children = new ArrayList<>();

    @Column(name = "depth", nullable = false)
    private Integer depth = 0;

    @OneToMany(mappedBy = "folder")
    private List<LearningObject> learningObjects = new ArrayList<>();

    // 정적 팩토리 메서드 - 최상위 폴더 생성
    public static ContentFolder createRoot(String folderName) {
        ContentFolder folder = new ContentFolder();
        folder.folderName = folderName;
        folder.depth = 0;
        return folder;
    }

    // 정적 팩토리 메서드 - 하위 폴더 생성
    public static ContentFolder createChild(String folderName, ContentFolder parent) {
        ContentFolder folder = new ContentFolder();
        folder.folderName = folderName;
        folder.parent = parent;
        folder.depth = parent.getDepth() + 1;
        parent.getChildren().add(folder);
        return folder;
    }

    // 비즈니스 메서드 - 폴더명 수정
    public void updateName(String folderName) {
        if (folderName != null && !folderName.isBlank()) {
            this.folderName = folderName;
        }
    }

    // 비즈니스 메서드 - 부모 폴더 변경
    public void moveTo(ContentFolder newParent) {
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
        }
        this.parent = newParent;
        if (newParent != null) {
            this.depth = newParent.getDepth() + 1;
            newParent.getChildren().add(this);
        } else {
            this.depth = 0;
        }
        updateChildrenDepth();
    }

    // 비즈니스 메서드 - 자식 폴더 depth 재계산
    private void updateChildrenDepth() {
        for (ContentFolder child : children) {
            child.depth = this.depth + 1;
            child.updateChildrenDepth();
        }
    }

    // 비즈니스 메서드 - 폴더가 비어있는지 확인
    public boolean isEmpty() {
        return children.isEmpty() && learningObjects.isEmpty();
    }

    // 비즈니스 메서드 - 하위 폴더 개수
    public int getChildCount() {
        return children.size();
    }

    // 비즈니스 메서드 - 학습객체 개수 (하위 폴더 포함)
    public int getItemCount() {
        int count = learningObjects.size();
        for (ContentFolder child : children) {
            count += child.getItemCount();
        }
        return count;
    }
}
