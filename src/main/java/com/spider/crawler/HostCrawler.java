package com.spider.crawler;

import com.spider.interfaces.IHTTPClient;
import com.spider.interfaces.IHostCrawler;

import java.util.concurrent.*;

public class HostCrawler implements IHostCrawler {
    private static final int MAX_EXECUTION_TIME_IN_SECONDS = 600;

    private final IHTTPClient client;
    private final ForkJoinPool customForkJoinPool;
    private CrawlerContext context;

    public HostCrawler(final IHTTPClient client) {
        this.client = client;
        this.customForkJoinPool = new ForkJoinPool(20);
    }

    public void doCrawl(final String url) {
        context = new CrawlerContext();
        CompletableFuture<String> managingFuture = context.createManagingFuture();
        runCrawlingFuture(url);
        try {
            managingFuture.get(MAX_EXECUTION_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void runCrawlingFuture(String pageRawUrl) {
        context.addCrawlingFuture(pageRawUrl,
                CompletableFuture.supplyAsync(() ->
                        client.requestPageSource(pageRawUrl), customForkJoinPool)
                        .thenAcceptAsync(pageSource -> {
                            if (pageSource.isPresent()) {
                                LinkExtractor.extractRelativeUrls(pageRawUrl, pageSource.get()).stream()
                                        .filter(context::isNotVisited)
                                        .forEach(this::runCrawlingFuture);
                                context.onCompletedRequest(pageRawUrl, true);
                            } else {
                                context.onCompletedRequest(pageRawUrl, false);
                            }
                            context.onCompletedCrawlingFuture(pageRawUrl);
                        }, customForkJoinPool));
    }
}
