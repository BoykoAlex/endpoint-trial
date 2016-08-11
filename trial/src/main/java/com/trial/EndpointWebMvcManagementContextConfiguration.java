package com.trial;

import org.springframework.boot.actuate.autoconfigure.ManagementContextConfiguration;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

@ManagementContextConfiguration
public class EndpointWebMvcManagementContextConfiguration {

	@Bean
	@ConditionalOnBean(HelloEndpoint.class)
	@ConditionalOnEnabledEndpoint("hello")
	public HelloMvcEndpoint helloMvcEndpoint(HelloEndpoint delegate) {
		return new HelloMvcEndpoint(delegate);
	}

}
