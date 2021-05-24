package com.codelodon.backendscaffold.server.config;

import com.codelodon.backendscaffold.common.config.BaseResourceConfig;
import com.codelodon.backendscaffold.common.entity.SubSystemAnno;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Arrays;

@Configuration("applicationConfig")
@EnableJpaAuditing
public class ApplicationConfig {
    final private Environment environment;

    public ApplicationConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @SubSystemAnno(systemName = "根系统", packageName = "com.codelodon.backendscaffold.server.module.main.controller")
    public ServletRegistrationBean<ServletContainer> rootJersey() {
        BaseResourceConfig brc = new BaseResourceConfig();

        // 如果是 dev 环境，就不要开启默认的鉴权过滤器，其他环境正常开启
        if (!Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            brc.enableDefaultAuthenticationFilter();
        }

        brc.packages("com.codelodon.backendscaffold.server.module.main.controller");

        ServletRegistrationBean<ServletContainer> rootJersey = new ServletRegistrationBean<>(new ServletContainer(brc));
        rootJersey.addUrlMappings("/backend/api/v1/root/*");
        rootJersey.setName("RootSystem");
        rootJersey.setLoadOnStartup(0);
        return rootJersey;
    }

}
