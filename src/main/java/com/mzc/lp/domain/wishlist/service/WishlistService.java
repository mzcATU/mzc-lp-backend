package com.mzc.lp.domain.wishlist.service;

import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
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
    private final CourseTimeRepository courseTimeRepository;

    /**
     * 찜 추가
     */
    @Transactional
    public WishlistItemResponse addToWishlist(Long userId, WishlistAddRequest request) {
        Long courseTimeId = request.getCourseTimeId();

        // 차수 존재 확인
        CourseTime courseTime = courseTimeRepository.findById(courseTimeId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // 이미 찜한 차수인지 확인
        if (wishlistRepository.existsByUserIdAndCourseTimeId(userId, courseTimeId)) {
            throw new AlreadyInWishlistException(userId, courseTimeId);
        }

        try {
            // 찜 아이템 생성 및 저장
            WishlistItem item = WishlistItem.create(userId, courseTimeId);
            WishlistItem savedItem = wishlistRepository.save(item);
            wishlistRepository.flush(); // 즉시 DB에 반영하여 제약조건 위반 확인

            Program program = courseTime.getProgram();
            return WishlistItemResponse.of(savedItem, courseTime, program);
        } catch (DataIntegrityViolationException e) {
            // 동시성 이슈로 인한 중복 찜 시도
            log.debug("Concurrent wishlist add attempt detected for courseTime {} by user {}", courseTimeId, userId);
            throw new AlreadyInWishlistException(userId, courseTimeId);
        }
    }

    /**
     * 찜 삭제
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long courseTimeId) {
        if (!wishlistRepository.existsByUserIdAndCourseTimeId(userId, courseTimeId)) {
            throw new WishlistItemNotFoundException(userId, courseTimeId);
        }
        wishlistRepository.deleteByUserIdAndCourseTimeId(userId, courseTimeId);
    }

    /**
     * 찜 목록 조회 (페이징)
     */
    public Page<WishlistItemResponse> getWishlist(Long userId, Pageable pageable) {
        Page<WishlistItem> wishlistItems = wishlistRepository.findByUserId(userId, pageable);

        // 차수 정보를 한 번에 조회
        List<Long> courseTimeIds = wishlistItems.getContent().stream()
                .map(WishlistItem::getCourseTimeId)
                .collect(Collectors.toList());

        Map<Long, CourseTime> courseTimeMap = courseTimeRepository.findAllById(courseTimeIds).stream()
                .collect(Collectors.toMap(CourseTime::getId, ct -> ct));

        return wishlistItems.map(item -> {
            CourseTime courseTime = courseTimeMap.get(item.getCourseTimeId());
            if (courseTime != null) {
                Program program = courseTime.getProgram();
                return WishlistItemResponse.of(item, courseTime, program);
            }
            return WishlistItemResponse.from(item);
        });
    }

    /**
     * 특정 차수 찜 여부 확인
     */
    public boolean isInWishlist(Long userId, Long courseTimeId) {
        return wishlistRepository.existsByUserIdAndCourseTimeId(userId, courseTimeId);
    }

    /**
     * 여러 차수 찜 여부 일괄 확인
     */
    public WishlistCheckResponse checkWishlistStatus(Long userId, WishlistCheckRequest request) {
        List<Long> wishlistedCourseTimeIds = wishlistRepository.findWishlistedCourseTimeIds(userId, request.getCourseTimeIds());
        return WishlistCheckResponse.of(request.getCourseTimeIds(), wishlistedCourseTimeIds);
    }

    /**
     * 사용자의 찜 개수 조회
     */
    public WishlistCountResponse getWishlistCount(Long userId) {
        long count = wishlistRepository.countByUserId(userId);
        return WishlistCountResponse.of(count);
    }

    /**
     * 특정 차수의 찜 개수 조회
     */
    public WishlistCountResponse getCourseTimeWishlistCount(Long courseTimeId) {
        long count = wishlistRepository.countByCourseTimeId(courseTimeId);
        return WishlistCountResponse.of(count);
    }
}
