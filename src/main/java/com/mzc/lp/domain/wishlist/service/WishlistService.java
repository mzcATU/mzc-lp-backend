package com.mzc.lp.domain.wishlist.service;

import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.exception.CourseNotFoundException;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.wishlist.dto.request.WishlistAddRequest;
import com.mzc.lp.domain.wishlist.dto.request.WishlistCheckRequest;
import com.mzc.lp.domain.wishlist.dto.response.WishlistCheckResponse;
import com.mzc.lp.domain.wishlist.dto.response.WishlistCountResponse;
import com.mzc.lp.domain.wishlist.dto.response.WishlistItemResponse;
import com.mzc.lp.domain.wishlist.entity.WishlistItem;
import com.mzc.lp.domain.wishlist.exception.AlreadyInWishlistException;
import com.mzc.lp.domain.wishlist.exception.WishlistItemNotFoundException;
import com.mzc.lp.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CourseRepository courseRepository;

    /**
     * 찜 추가
     */
    @Transactional
    public WishlistItemResponse addToWishlist(Long userId, WishlistAddRequest request) {
        Long courseId = request.getCourseId();

        // 강의 존재 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(CourseNotFoundException::new);

        // 이미 찜한 강의인지 확인
        if (wishlistRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new AlreadyInWishlistException(userId, courseId);
        }

        try {
            // 찜 아이템 생성 및 저장
            WishlistItem item = WishlistItem.create(userId, courseId);
            WishlistItem savedItem = wishlistRepository.save(item);
            wishlistRepository.flush(); // 즉시 DB에 반영하여 제약조건 위반 확인

            return WishlistItemResponse.of(
                    savedItem,
                    course.getTitle(),
                    course.getThumbnailUrl(),
                    course.getLevel() != null ? course.getLevel().name() : null,
                    course.getType() != null ? course.getType().name() : null,
                    course.getEstimatedHours()
            );
        } catch (DataIntegrityViolationException e) {
            // 동시성 이슈로 인한 중복 찜 시도
            log.debug("Concurrent wishlist add attempt detected for course {} by user {}", courseId, userId);
            throw new AlreadyInWishlistException(userId, courseId);
        }
    }

    /**
     * 찜 삭제
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long courseId) {
        if (!wishlistRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new WishlistItemNotFoundException(userId, courseId);
        }
        wishlistRepository.deleteByUserIdAndCourseId(userId, courseId);
    }

    /**
     * 찜 목록 조회 (페이징)
     */
    public Page<WishlistItemResponse> getWishlist(Long userId, Pageable pageable) {
        Page<WishlistItem> wishlistItems = wishlistRepository.findByUserId(userId, pageable);

        // 강의 정보를 한 번에 조회
        List<Long> courseIds = wishlistItems.getContent().stream()
                .map(WishlistItem::getCourseId)
                .collect(Collectors.toList());

        Map<Long, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        return wishlistItems.map(item -> {
            Course course = courseMap.get(item.getCourseId());
            if (course != null) {
                return WishlistItemResponse.of(
                        item,
                        course.getTitle(),
                        course.getThumbnailUrl(),
                        course.getLevel() != null ? course.getLevel().name() : null,
                        course.getType() != null ? course.getType().name() : null,
                        course.getEstimatedHours()
                );
            }
            return WishlistItemResponse.from(item);
        });
    }

    /**
     * 특정 강의 찜 여부 확인
     */
    public boolean isInWishlist(Long userId, Long courseId) {
        return wishlistRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    /**
     * 여러 강의 찜 여부 일괄 확인
     */
    public WishlistCheckResponse checkWishlistStatus(Long userId, WishlistCheckRequest request) {
        List<Long> wishlistedCourseIds = wishlistRepository.findWishlistedCourseIds(userId, request.getCourseIds());
        return WishlistCheckResponse.of(request.getCourseIds(), wishlistedCourseIds);
    }

    /**
     * 사용자의 찜 개수 조회
     */
    public WishlistCountResponse getWishlistCount(Long userId) {
        long count = wishlistRepository.countByUserId(userId);
        return WishlistCountResponse.of(count);
    }

    /**
     * 특정 강의의 찜 개수 조회
     */
    public WishlistCountResponse getCourseWishlistCount(Long courseId) {
        long count = wishlistRepository.countByCourseId(courseId);
        return WishlistCountResponse.of(count);
    }
}
