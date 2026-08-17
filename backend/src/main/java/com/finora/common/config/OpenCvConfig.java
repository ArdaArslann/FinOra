package com.finora.common.config;

import nu.pattern.OpenCV;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenCvConfig {

    static {
        OpenCV.loadLocally();
        System.out.println("OPENCV NATIVE LIBRARY LOADED");
    }
}