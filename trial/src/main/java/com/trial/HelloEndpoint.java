package com.trial;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.stereotype.Component;

@Component
public class HelloEndpoint implements Endpoint<String> {

	@Override
	public String getId() {
		return "hello";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isSensitive() {
		return true;
	}

	@Override
	public String invoke() {
		return "Hello Alex! Congrats you've added a new endpoint!";
	}

}
