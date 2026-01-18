package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.ts.constant.QualityRating;

import java.util.ArrayList;
import java.util.List;

/**
 * 차수 생성/수정 검증 결과 DTO
 */
public record CourseTimeValidationResult(
        boolean valid,
        QualityRating qualityRating,
        List<ValidationError> errors,
        List<ValidationWarning> warnings
) {
    public static CourseTimeValidationResult success(QualityRating qualityRating) {
        return new CourseTimeValidationResult(true, qualityRating, List.of(), List.of());
    }

    public static CourseTimeValidationResult success(QualityRating qualityRating, List<ValidationWarning> warnings) {
        return new CourseTimeValidationResult(true, qualityRating, List.of(), warnings);
    }

    public static CourseTimeValidationResult failure(List<ValidationError> errors) {
        return new CourseTimeValidationResult(false, null, errors, List.of());
    }

    public static CourseTimeValidationResult failure(List<ValidationError> errors, List<ValidationWarning> warnings) {
        return new CourseTimeValidationResult(false, null, errors, warnings);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<ValidationError> errors = new ArrayList<>();
        private final List<ValidationWarning> warnings = new ArrayList<>();
        private QualityRating qualityRating = QualityRating.BEST;

        public Builder addError(ValidationError error) {
            this.errors.add(error);
            return this;
        }

        public Builder addWarning(ValidationWarning warning) {
            this.warnings.add(warning);
            if (warning.qualityRating() != null && warning.qualityRating().ordinal() > this.qualityRating.ordinal()) {
                this.qualityRating = warning.qualityRating();
            }
            return this;
        }

        public Builder setQualityRating(QualityRating qualityRating) {
            if (qualityRating.ordinal() > this.qualityRating.ordinal()) {
                this.qualityRating = qualityRating;
            }
            return this;
        }

        public CourseTimeValidationResult build() {
            boolean valid = errors.isEmpty();
            return new CourseTimeValidationResult(
                    valid,
                    valid ? qualityRating : null,
                    List.copyOf(errors),
                    List.copyOf(warnings)
            );
        }
    }
}
