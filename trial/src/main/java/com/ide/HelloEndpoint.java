package com.ide;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.compiler.runner.javac.RuntimeJavaCompiler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloEndpoint {

	@Autowired
	RuntimeCompilationManager rcm;
	 
	@RequestMapping("/hello")
	@ResponseBody
	public String hello() {
		rcm.stop();
		return "Hello Alex!";
	}
	
}
