package com.malkeet.ExpenseTracker.config;

import java.beans.BeanProperty;

@Configuration
public class AppConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
