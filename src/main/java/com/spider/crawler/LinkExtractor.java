package com.spider.crawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkExtractor {
    private static final Pattern pattern = Pattern.compile("<a\\s+href=(?:\"([^\"]+)\"|'([^']+)').*?>.*?</a>",
            Pattern.CASE_INSENSITIVE);

    public static List<String> extractRelativeUrls(final String rawPageUrl, final String pageSource) {
        return toURI(rawPageUrl)
                .map(pageUrl -> extractRelativeUrls(pageUrl, pageSource))
                .orElse(new ArrayList<>());
    }

    private static List<String> extractRelativeUrls(final URI pageUrl, final String pageSource) {
        final List<String> foundLinks = new ArrayList<>();
        Matcher matcher = pattern.matcher(pageSource);
        while (matcher.find()) {
            String foundUrl = matcher.group(1);
            toURI(foundUrl).ifPresent(foundUri -> {
                String nextUrl;
                if (foundUri.isAbsolute()) {
                    if (Objects.equals(pageUrl.getHost(), foundUri.getHost())) {
                        nextUrl = foundUri.normalize().toString();
                    } else {
                        return;
                    }
                } else {
                    nextUrl = pageUrl.resolve(foundUri).toString();
                }
                String urlWithoutFragment = nextUrl.split("#", 2)[0]; // remove fragment part if exists
                foundLinks.add(urlWithoutFragment);
            });
        }
        return foundLinks;
    }

    private static Optional<URI> toURI(final String rawUrl) {
        try {
            return Optional.of(new URI(rawUrl));
        } catch (URISyntaxException e) {
            // found url is generating via js
            return Optional.empty();
        }
    }
}
