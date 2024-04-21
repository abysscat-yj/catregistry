package com.abysscat.catregistry.config;

import com.abysscat.catregistry.cluster.Cluster;
import com.abysscat.catregistry.health.CatHealthChecker;
import com.abysscat.catregistry.health.HealthChecker;
import com.abysscat.catregistry.service.CatRegistryService;
import com.abysscat.catregistry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for all beans.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/15 0:18
 */
@Configuration
public class CatRegistryConfig {

	@Bean
	public RegistryService registryService() {
		return new CatRegistryService();
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public HealthChecker healthChecker(@Autowired RegistryService registryService) {
		return new CatHealthChecker(registryService);
	}

	@Bean(initMethod = "init")
	public Cluster cluster(@Autowired CatRegistryConfigProperties registryConfigProperties,
						   @Autowired RegistryService registryService) {
		return new Cluster(registryConfigProperties, registryService);
	}

}
