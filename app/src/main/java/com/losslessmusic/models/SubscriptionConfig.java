package com.losslessmusic.models;

import java.util.List;

public class SubscriptionConfig {
    private String serviceName;
    private ServiceType type;
    private boolean linked;
    private boolean enabled = true;
    private boolean premiumActive;
    private String userName;
    private String accessToken;
    private String refreshToken;
    private long tokenExpiry;
    private List<String> features;

    public enum ServiceType {
        LOCAL("Local Files", true),
        INTERNET_ARCHIVE("Internet Archive", true),
        ITUNES("iTunes Music", true),
        RADIO_BROWSER("Live Radio", true),
        JIOSAAVN("JioSaavn", false),
        GAANA("Gaana", false),
        SPOTIFY("Spotify", false),
        YOUTUBE_MUSIC("YouTube Music", false);

        private final String displayName;
        private final boolean alwaysAvailable;

        ServiceType(String displayName, boolean alwaysAvailable) {
            this.displayName = displayName;
            this.alwaysAvailable = alwaysAvailable;
        }

        public String getDisplayName() { return displayName; }
        public boolean isAlwaysAvailable() { return alwaysAvailable; }
    }

    public SubscriptionConfig(ServiceType type) {
        this.type = type;
        this.serviceName = type.getDisplayName();
        this.linked = type.isAlwaysAvailable();
        this.premiumActive = false;
    }

    public String getServiceName() { return serviceName; }
    public ServiceType getType() { return type; }
    public boolean isLinked() { return linked; }
    public void setLinked(boolean linked) { this.linked = linked; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isPremiumActive() { return premiumActive; }
    public void setPremiumActive(boolean premiumActive) { this.premiumActive = premiumActive; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public long getTokenExpiry() { return tokenExpiry; }
    public void setTokenExpiry(long tokenExpiry) { this.tokenExpiry = tokenExpiry; }
    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public boolean isAvailable() {
        return linked && (type.isAlwaysAvailable() || (premiumActive && !isTokenExpired()));
    }

    private boolean isTokenExpired() {
        return System.currentTimeMillis() > tokenExpiry;
    }
}
