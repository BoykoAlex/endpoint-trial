package com.ide;

import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@EnableZuulProxy
public class HelloConfiguration {
	
    @Bean
    public HelloEndpoint helloEndpoint() {
    	return new HelloEndpoint();
    }
    
    @Bean
    public RuntimeCompilationManager runtimeCompilationManager() {
    	return new RuntimeCompilationManager();
    }

}
