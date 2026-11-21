package com.emedic.management.util;

import org.springframework.data.domain.PageRequest;

public final class Pagination {
    private Pagination() { }

    public static PageRequest request(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        return PageRequest.of(page, size);
    }
}
