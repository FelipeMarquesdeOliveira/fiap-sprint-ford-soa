package br.com.ford.vinshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VinShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(VinShareApplication.class, args);
    }
}
