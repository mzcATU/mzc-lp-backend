package com.mzc.lp.common.context;

import com.mzc.lp.common.exception.TenantNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TenantContext 단위 테스트")
class TenantContextTest {

    @AfterEach
    void tearDown() {
        // 각 테스트 후 TenantContext 초기화
        TenantContext.clear();
    }

    @Test
    @DisplayName("tenantId 설정 후 조회 성공")
    void setAndGet_Success() {
        // given
        Long tenantId = 123L;

        // when
        TenantContext.setTenantId(tenantId);

        // then
        assertThat(TenantContext.getCurrentTenantId()).isEqualTo(123L);
        assertThat(TenantContext.isSet()).isTrue();
    }

    @Test
    @DisplayName("null tenantId 설정 시 IllegalArgumentException 발생")
    void setTenantId_ThrowsException_WhenNull() {
        // when & then
        assertThatThrownBy(() -> TenantContext.setTenantId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("TenantId cannot be null");
    }

    @Test
    @DisplayName("tenantId 미설정 시 getCurrentTenantId() 호출하면 TenantNotFoundException 발생")
    void getCurrentTenantId_ThrowsException_WhenNotSet() {
        // when & then
        assertThatThrownBy(() -> TenantContext.getCurrentTenantId())
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessage("TenantId not found in current context");
    }

    @Test
    @DisplayName("getCurrentTenantIdOrNull()은 미설정 시 null 반환")
    void getCurrentTenantIdOrNull_ReturnsNull_WhenNotSet() {
        // when
        Long tenantId = TenantContext.getCurrentTenantIdOrNull();

        // then
        assertThat(tenantId).isNull();
        assertThat(TenantContext.isSet()).isFalse();
    }

    @Test
    @DisplayName("tenantId 설정 후 clear() 호출하면 제거됨")
    void clear_RemovesTenantId() {
        // given
        TenantContext.setTenantId(123L);
        assertThat(TenantContext.isSet()).isTrue();

        // when
        TenantContext.clear();

        // then
        assertThat(TenantContext.getCurrentTenantIdOrNull()).isNull();
        assertThat(TenantContext.isSet()).isFalse();
    }

    @Test
    @DisplayName("서로 다른 tenantId를 순차적으로 설정 가능")
    void setTenantId_Multiple_Sequential() {
        // given & when
        TenantContext.setTenantId(100L);
        assertThat(TenantContext.getCurrentTenantId()).isEqualTo(100L);

        TenantContext.setTenantId(200L);
        assertThat(TenantContext.getCurrentTenantId()).isEqualTo(200L);

        TenantContext.setTenantId(300L);
        assertThat(TenantContext.getCurrentTenantId()).isEqualTo(300L);
    }

    @Test
    @DisplayName("clear() 여러 번 호출해도 예외 발생하지 않음")
    void clear_MultipleTimes_NoException() {
        // given
        TenantContext.setTenantId(123L);

        // when & then
        TenantContext.clear();
        TenantContext.clear();
        TenantContext.clear();

        assertThat(TenantContext.isSet()).isFalse();
    }
}
