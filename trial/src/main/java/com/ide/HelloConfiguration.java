package com.ide;

import org.springframework.context.annotation.Bean;

public class HelloConfiguration {
	
    @Bean
    public HelloEndpoint helloEndpoint() {
    	return new HelloEndpoint();
    }

}
