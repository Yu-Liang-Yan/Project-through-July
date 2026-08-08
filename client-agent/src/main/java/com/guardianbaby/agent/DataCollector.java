package com.guardianbaby.agent;

import java.util.*;

public class DataCollector {
    private static final String[] APPS = {
        "微信", "抖音", "王者荣耀", "和平精英", "小红书", "B站", "QQ",
        "百度", "淘宝", "网易云音乐", "腾讯视频", "爱奇艺", "知乎", "微博"
    };
    private static final String[] STUDY_APPS = {"钉钉", "学习通", "百词斩", "作业帮"};
    private final Random rng = new Random();

    public record UsageEvent(String app, int durationSeconds) {}

    public List<UsageEvent> collect() {
        int count = rng.nextInt(3, 6);
        List<UsageEvent> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            boolean isStudy = rng.nextDouble() < 0.25;
            String app = isStudy ? STUDY_APPS[rng.nextInt(STUDY_APPS.length)]
                                 : APPS[rng.nextInt(APPS.length)];
            int duration = rng.nextInt(60, 600);
            events.add(new UsageEvent(app, duration));
        }
        return events;
    }
}
