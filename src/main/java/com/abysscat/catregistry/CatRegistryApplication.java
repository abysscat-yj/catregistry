package com.abysscat.catregistry;

import com.abysscat.catregistry.config.CatRegistryConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/14 1:48
 */
@SpringBootApplication
@EnableConfigurationProperties({CatRegistryConfigProperties.class})
public class CatRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatRegistryApplication.class, args);
	}

}
