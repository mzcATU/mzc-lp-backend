package com.mzc.lp.domain.cart.service;

import com.mzc.lp.domain.cart.dto.request.CartAddRequest;
import com.mzc.lp.domain.cart.dto.request.CartRemoveRequest;
import com.mzc.lp.domain.cart.dto.response.CartCountResponse;
import com.mzc.lp.domain.cart.dto.response.CartItemResponse;
import com.mzc.lp.domain.cart.entity.CartItem;
import com.mzc.lp.domain.cart.exception.AlreadyInCartException;
import com.mzc.lp.domain.cart.exception.CartItemNotFoundException;
import com.mzc.lp.domain.cart.repository.CartRepository;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CourseTimeRepository courseTimeRepository;

    /**
     * 장바구니에 차수 추가
     */
    @Transactional
    public CartItemResponse addToCart(Long userId, CartAddRequest request) {
        Long courseTimeId = request.courseTimeId();
        log.debug("Adding courseTime to cart: userId={}, courseTimeId={}", userId, courseTimeId);

        // 차수 존재 여부 확인
        CourseTime courseTime = courseTimeRepository.findById(courseTimeId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // 이미 장바구니에 있는지 확인
        if (cartRepository.existsByUserIdAndCourseTimeId(userId, courseTimeId)) {
            throw new AlreadyInCartException(courseTimeId);
        }

        // 장바구니에 추가
        CartItem cartItem = CartItem.create(userId, courseTimeId);
        CartItem savedItem = cartRepository.save(cartItem);

        Course course = courseTime.getCourse();
        log.info("CourseTime added to cart: userId={}, courseTimeId={}, cartItemId={}", userId, courseTimeId, savedItem.getId());
        return CartItemResponse.from(savedItem, courseTime, course);
    }

    /**
     * 장바구니에서 차수 삭제
     */
    @Transactional
    public void removeFromCart(Long userId, Long courseTimeId) {
        log.debug("Removing courseTime from cart: userId={}, courseTimeId={}", userId, courseTimeId);

        if (!cartRepository.existsByUserIdAndCourseTimeId(userId, courseTimeId)) {
            throw new CartItemNotFoundException(courseTimeId);
        }

        cartRepository.deleteByUserIdAndCourseTimeId(userId, courseTimeId);
        log.info("CourseTime removed from cart: userId={}, courseTimeId={}", userId, courseTimeId);
    }

    /**
     * 장바구니에서 여러 차수 삭제
     */
    @Transactional
    public void removeFromCartBulk(Long userId, CartRemoveRequest request) {
        log.debug("Removing courseTimes from cart: userId={}, courseTimeIds={}", userId, request.courseTimeIds());

        cartRepository.deleteByUserIdAndCourseTimeIdIn(userId, request.courseTimeIds());
        log.info("CourseTimes removed from cart: userId={}, count={}", userId, request.courseTimeIds().size());
    }

    /**
     * 장바구니 목록 조회
     */
    public List<CartItemResponse> getCart(Long userId) {
        log.debug("Getting cart: userId={}", userId);

        List<CartItem> cartItems = cartRepository.findByUserIdOrderByAddedAtDesc(userId);

        if (cartItems.isEmpty()) {
            return List.of();
        }

        // 차수 정보 벌크 조회
        List<Long> courseTimeIds = cartItems.stream()
                .map(CartItem::getCourseTimeId)
                .toList();

        Map<Long, CourseTime> courseTimeMap = courseTimeRepository.findAllById(courseTimeIds).stream()
                .collect(Collectors.toMap(CourseTime::getId, Function.identity()));

        return cartItems.stream()
                .map(item -> {
                    CourseTime courseTime = courseTimeMap.get(item.getCourseTimeId());
                    if (courseTime == null) {
                        return null;
                    }
                    Course course = courseTime.getCourse();
                    return CartItemResponse.from(item, courseTime, course);
                })
                .filter(response -> response != null)
                .toList();
    }

    /**
     * 장바구니 개수 조회
     */
    public CartCountResponse getCartCount(Long userId) {
        long count = cartRepository.countByUserId(userId);
        return CartCountResponse.of(count);
    }

    /**
     * 특정 차수가 장바구니에 있는지 확인
     */
    public boolean isInCart(Long userId, Long courseTimeId) {
        return cartRepository.existsByUserIdAndCourseTimeId(userId, courseTimeId);
    }

    /**
     * 여러 차수의 장바구니 여부 일괄 확인
     */
    public Map<Long, Boolean> checkCartStatus(Long userId, List<Long> courseTimeIds) {
        List<Long> inCartCourseTimeIds = cartRepository.findCourseTimeIdsByUserIdAndCourseTimeIdIn(userId, courseTimeIds);

        return courseTimeIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        inCartCourseTimeIds::contains
                ));
    }
}
