package org.glytoucan.admin;

import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ VirtSesameTransactionConfig.class })
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.SpringBootWebSecurityConfiguration.class })
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}