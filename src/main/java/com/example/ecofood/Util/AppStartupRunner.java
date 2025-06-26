package com.example.ecofood.Util;

import com.example.ecofood.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements ApplicationRunner {

    private final UserService userService;
    private final AppContext appContext;

    public AppStartupRunner(UserService userService, AppContext appContext) {
        this.userService = userService;
        this.appContext = appContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean adminExists = userService.checkIfAdminExists();
        appContext.setHasAdmin(adminExists);
    }
}