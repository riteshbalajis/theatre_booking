package com.movie_booking.dto.response;

import java.util.List;

public class LogListResponse<T> {
    private List<T> logs;
    private int total;
    private int limit;
    private int offset;
    private boolean hasMore;

    public LogListResponse(List<T> logs, int total, int limit, int offset) {
        this.logs = logs;
        this.total = total;
        this.limit = limit;
        this.offset = offset;
        this.hasMore = (offset + limit) < total;
    }

    public List<T> getLogs() {
        return logs;
    }

    public void setLogs(List<T> logs) {
        this.logs = logs;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    
}