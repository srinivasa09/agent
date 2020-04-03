package com.peddle.digitals.cobot.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import io.github.bonigarcia.wdm.WebDriverManager;

@EnableResourceServer
@SpringBootApplication
public class AgentApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(AgentApplication.class, args);
		WebDriverManager.chromedriver().setup();
		WebDriverManager.firefoxdriver().setup();
		
	}
	
}
