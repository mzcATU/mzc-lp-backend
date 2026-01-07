package com.mzc.lp.domain.user.dto.request;

import java.util.List;

/**
 * 파일 파싱 결과를 담는 DTO
 */
public record FileParseResult(
        List<FileUserRow> users,
        List<ParseError> errors,
        boolean hasHeaderError
) {
    public record ParseError(
            int rowNumber,
            String field,
            String value,
            String message
    ) {}

    public static FileParseResult success(List<FileUserRow> users) {
        return new FileParseResult(users, List.of(), false);
    }

    public static FileParseResult withErrors(List<FileUserRow> users, List<ParseError> errors) {
        return new FileParseResult(users, errors, false);
    }

    public static FileParseResult headerError(String message) {
        return new FileParseResult(
                List.of(),
                List.of(new ParseError(1, "header", "", message)),
                true
        );
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
