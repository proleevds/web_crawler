package com.spider.crawler;

import com.spider.interfaces.IHostState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlerContext implements IHostState {
    private static final int FAILED_REQUESTS_LIMIT = 10;
    private final Map<String, CompletableFuture> crawlingFutures = new ConcurrentHashMap<>();
    private final Set<String> succeededVisits = new HashSet<>();
    private final Set<String> failedVisits = new HashSet<>();
    private CompletableFuture<String> managingFuture;
    private AtomicInteger lastFailedRequestsCount = new AtomicInteger();

    public CompletableFuture<String> createManagingFuture() {
        return managingFuture = new CompletableFuture<>();
    }

    public void addCrawlingFuture(String url, CompletableFuture future) {
        crawlingFutures.put(url, future);
    }

    public boolean isNotVisited(String url) {
        return !succeededVisits.contains(url) &&
                !failedVisits.contains(url); // avoid revisiting unreachable urls
    }

    public void onCompletedCrawlingFuture(String url) {
        crawlingFutures.remove(url);
        controlProcessState();
    }

    private void controlProcessState() {
        if (isHostDown()) {
            managingFuture.completeExceptionally(
                    new RuntimeException("Requested host is possibly down, " +
                            FAILED_REQUESTS_LIMIT + " last requests were failed."));
        } else if (crawlingFutures.isEmpty()) {
            managingFuture.complete("Done.");
        }
    }

    @Override
    public boolean isHostDown() {
        return lastFailedRequestsCount.get() >= FAILED_REQUESTS_LIMIT;
    }

    @Override
    public void onCompletedRequest(String rawUrl, boolean isSucceeded) {
        if (isSucceeded) {
            lastFailedRequestsCount.set(0);
            succeededVisits.add(rawUrl);
        } else {
            lastFailedRequestsCount.incrementAndGet();
            failedVisits.add(rawUrl);
        }
    }
}
