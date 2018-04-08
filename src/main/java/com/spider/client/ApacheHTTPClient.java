package com.spider.client;

import com.spider.interfaces.IHTTPClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

public class ApacheHTTPClient implements IHTTPClient {
    private CloseableHttpClient client;

    public ApacheHTTPClient(CloseableHttpClient client) {
        this.client = client;
    }

    @Override
    public Optional<String> requestPageSource(String url) {
        HttpGet request = new HttpGet(url);
        String pageSource = null;
        try (CloseableHttpResponse response = client.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    ContentType contentType = ContentType.get(entity);
                    if (contentType.getMimeType().equals(ContentType.TEXT_HTML.getMimeType())) {
                        pageSource = extractSource(entity);
                    }
                } finally {
                    EntityUtils.consume(entity);
                }
            }
        } catch (IOException e) {
        }
        return Optional.ofNullable(pageSource);
    }

    private String extractSource(HttpEntity httpEntity) throws IOException {
        Scanner s = new Scanner(httpEntity.getContent()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static class Factory {
        private static final int MAX_TOTAL_CONNECTIONS = 10;

        public static ApacheHTTPClient createNew() {
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
            connectionManager.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);
            return new ApacheHTTPClient(
                    HttpClients.custom()
                            .setConnectionManager(connectionManager)
                            .build());
        }
    }
}
