package com.trial;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.AbstractEndpointMvcAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "endpoints.hello")
public class HelloMvcEndpoint extends AbstractEndpointMvcAdapter<Endpoint<?>> {

	public HelloMvcEndpoint(Endpoint<?> delegate) {
		super(delegate);
	}

}
