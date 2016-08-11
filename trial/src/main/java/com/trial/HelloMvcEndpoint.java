package com.trial;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;

public class HelloMvcEndpoint extends EndpointMvcAdapter {

	public HelloMvcEndpoint(Endpoint<?> delegate) {
		super(delegate);
	}

}
