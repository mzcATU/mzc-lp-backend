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
import com.mzc.lp.domain.course.exception.CourseNotFoundException;
import com.mzc.lp.domain.course.repository.CourseRepository;
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
    private final CourseRepository courseRepository;

    /**
     * 장바구니에 강의 추가
     */
    @Transactional
    public CartItemResponse addToCart(Long userId, CartAddRequest request) {
        Long courseId = request.courseId();
        log.debug("Adding course to cart: userId={}, courseId={}", userId, courseId);

        // 강의 존재 여부 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        // 이미 장바구니에 있는지 확인
        if (cartRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new AlreadyInCartException(courseId);
        }

        // 장바구니에 추가
        CartItem cartItem = CartItem.create(userId, courseId);
        CartItem savedItem = cartRepository.save(cartItem);

        log.info("Course added to cart: userId={}, courseId={}, cartItemId={}", userId, courseId, savedItem.getId());
        return CartItemResponse.from(savedItem, course);
    }

    /**
     * 장바구니에서 강의 삭제
     */
    @Transactional
    public void removeFromCart(Long userId, Long courseId) {
        log.debug("Removing course from cart: userId={}, courseId={}", userId, courseId);

        if (!cartRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new CartItemNotFoundException(courseId);
        }

        cartRepository.deleteByUserIdAndCourseId(userId, courseId);
        log.info("Course removed from cart: userId={}, courseId={}", userId, courseId);
    }

    /**
     * 장바구니에서 여러 강의 삭제
     */
    @Transactional
    public void removeFromCartBulk(Long userId, CartRemoveRequest request) {
        log.debug("Removing courses from cart: userId={}, courseIds={}", userId, request.courseIds());

        cartRepository.deleteByUserIdAndCourseIdIn(userId, request.courseIds());
        log.info("Courses removed from cart: userId={}, count={}", userId, request.courseIds().size());
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

        // 강의 정보 벌크 조회
        List<Long> courseIds = cartItems.stream()
                .map(CartItem::getCourseId)
                .toList();

        Map<Long, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        return cartItems.stream()
                .map(item -> CartItemResponse.from(item, courseMap.get(item.getCourseId())))
                .filter(response -> response.courseTitle() != null) // 삭제된 강의 제외
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
     * 특정 강의가 장바구니에 있는지 확인
     */
    public boolean isInCart(Long userId, Long courseId) {
        return cartRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    /**
     * 여러 강의의 장바구니 여부 일괄 확인
     */
    public Map<Long, Boolean> checkCartStatus(Long userId, List<Long> courseIds) {
        List<Long> inCartCourseIds = cartRepository.findCourseIdsByUserIdAndCourseIdIn(userId, courseIds);

        return courseIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        inCartCourseIds::contains
                ));
    }
}
