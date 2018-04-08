package com.spider.interfaces;

import java.util.Optional;

public interface IHTTPClient {
    Optional<String> requestPageSource(String url);
}
