package com.example.ecofood.Util;

import org.springframework.stereotype.Component;

@Component
public class AppContext {
    private boolean hasAdmin = false;

    public boolean isHasAdmin() {
        return hasAdmin;
    }

    public void setHasAdmin(boolean hasAdmin) {
        this.hasAdmin = hasAdmin;
    }
}