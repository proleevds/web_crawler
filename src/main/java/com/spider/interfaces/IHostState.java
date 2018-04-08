package com.spider.interfaces;

public interface IHostState {
    void onCompletedRequest(String url, boolean isSucceeded);

    boolean isHostDown();
}
