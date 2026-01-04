package com.mzc.lp.domain.certificate.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EnrollmentCompletedEvent extends ApplicationEvent {

    private final Long enrollmentId;
    private final Long userId;
    private final Long courseTimeId;

    public EnrollmentCompletedEvent(Object source, Long enrollmentId, Long userId, Long courseTimeId) {
        super(source);
        this.enrollmentId = enrollmentId;
        this.userId = userId;
        this.courseTimeId = courseTimeId;
    }
}
