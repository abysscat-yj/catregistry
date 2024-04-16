package com.abysscat.catregistry.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Registry config properties.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/17 0:55
 */
@Data
@ConfigurationProperties(prefix = "catregistry")
public class CatRegistryConfigProperties {

	private List<String> servers;

}
