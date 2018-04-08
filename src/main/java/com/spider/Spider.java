package com.spider;

import com.spider.client.ApacheHTTPClient;
import com.spider.crawler.HostCrawler;

public class Spider {
    public static void main(String[] args) {
        // args = new String[]{"https://www.usa.gov/"};
        if (args.length == 0) {
            throw new IllegalArgumentException("args are empty");
        }
        HostCrawler hostCrawler = new HostCrawler(
                ApacheHTTPClient.Factory.createNew());
        hostCrawler.doCrawl(args[0]);
        System.out.println(hostCrawler);
    }
}
