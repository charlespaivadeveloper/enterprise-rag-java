package dev.charlespaiva.rag.usecase.dataaccess;

import java.util.List;

public record PagedResult<T>(List<T> items, long totalElements) {
    public PagedResult {
        items = List.copyOf(items);
    }
}
