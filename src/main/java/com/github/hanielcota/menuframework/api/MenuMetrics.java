package com.github.hanielcota.menuframework.api;

public record MenuMetrics(
    long activeSessions,
    long registeredMenus,
    double sessionHitRate,
    long cachedPages,
    double pageCacheHitRate) {}
