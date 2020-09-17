package com.borlehandro.labs.network.multicast;

import java.util.Base64;

public class User {
    private String name;
    private boolean online;
    private long lastResponseTime;

    public User(String name, boolean online) {
        this.name = name;
        this.online = online;
        this.lastResponseTime = System.currentTimeMillis();
    }

    public static String generateUid() {
        return new String(Base64.getEncoder().encode(Long.toString(System.nanoTime()).getBytes()));
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastResponseTime() {
        return lastResponseTime;
    }

    public void setLastResponseTime(long lastResponseTime) {
        this.lastResponseTime = lastResponseTime;
    }
}