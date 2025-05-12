package com.example.fuse.config;

import com.example.fuse.FuseProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FuseProperties.class)
@ConditionalOnProperty(prefix = "fuse", name = "enabled", matchIfMissing = true)
public class FuseAutoConfiguration {

}
