package com.trial;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.AbstractEndpointMvcAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@ConfigurationProperties(prefix = "endpoints.hello")
public class HelloMvcEndpoint extends AbstractEndpointMvcAdapter<Endpoint<?>> {

	public HelloMvcEndpoint(Endpoint<?> delegate) {
		super(delegate);
	}
	
	@ResponseBody
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello() {
		return (String) getDelegate().invoke();
	}

}
