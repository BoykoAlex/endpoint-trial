package com.ide;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloEndpoint {

//	@Override
//	public String getId() {
//		return "hello";
//	}
//
//	@Override
//	public boolean isEnabled() {
//		return true;
//	}
//
//	@Override
//	public boolean isSensitive() {
//		return true;
//	}
//
//	@Override
//	public String invoke() {
//		return "Hello Alex! Congrats you've added a new endpoint!";
//	}

	@RequestMapping("/hello")
	@ResponseBody
	public String hello() {
		return "Hello Alex!";
	}
}
