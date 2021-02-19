package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class HelloApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(HelloApplication.class, args);

		if(Desktop.isDesktopSupported()){
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI("http://localhost:8087"));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}else{
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("rundll32 url.dll,FileProtocolHandler " + "http://localhost:8087");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
