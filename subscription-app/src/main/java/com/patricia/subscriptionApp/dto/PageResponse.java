package com.patricia.subscriptionApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import java.util.List;

@Schema(description = "Paginated response wrapper")
public class PageResponse<T> {

    @Schema(description = "List of items in current page")
    private List<T> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int pageNumber;

    @Schema(description = "Number of items per page", example = "10")
    private int pageSize;

    @Schema(description = "Total number of items across all pages", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "10")
    private int totalPages;

    @Schema(description = "Whether this is the first page")
    private boolean first;

    @Schema(description = "Whether this is the last page")
    private boolean last;

    @Schema(description = "Whether a next page exists")
    private boolean hasNext;

    @Schema(description = "Whether a previous page exists")
    private boolean hasPrevious;

    // ── Constructors ──────────────────────────────────────────────────

    public PageResponse() {}

    public PageResponse(
            List<T> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages
    ) {
        this.content       = content;
        this.pageNumber    = pageNumber;
        this.pageSize      = pageSize;
        this.totalElements = totalElements;
        this.totalPages    = totalPages;
        this.first         = (pageNumber == 0);
        this.last          = (pageNumber >= totalPages - 1);
        this.hasNext       = (pageNumber < totalPages - 1);
        this.hasPrevious   = (pageNumber > 0);
    }

    // ── Factory methods ───────────────────────────────────────────────

    /**
     * Creates a {@code PageResponse} directly from a Spring {@link Page}.
     *
     * <p>Use this overload when the page content is already the correct
     * type (e.g. after calling {@code page.map(Mapper::toDto)}).
     *
     * @param page Spring Data page (content type matches {@code T})
     * @param <T>  DTO type
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public boolean isHasPrevious() { return hasPrevious; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }

    // ── toString ──────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "PageResponse{"
                + "pageNumber=" + pageNumber
                + ", pageSize=" + pageSize
                + ", totalElements=" + totalElements
                + ", totalPages=" + totalPages
                + ", contentSize=" + (content != null ? content.size() : 0)
                + '}';
    }
}
