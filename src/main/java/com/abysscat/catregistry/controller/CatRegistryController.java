package com.abysscat.catregistry.controller;

import com.abysscat.catregistry.model.InstanceMeta;
import com.abysscat.catregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Registry controller.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/15 0:19
 */
@RestController
@Slf4j
public class CatRegistryController {

	@Autowired
	RegistryService registryService;

	@RequestMapping("/reg")
	public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance) {
		log.info(" ===> register service: {} @ {}", service, instance);
		return registryService.register(service, instance);
	}

	@RequestMapping("/unreg")
	public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instance) {
		log.info(" ===> unregister service: {} @ {}", service, instance);
		return registryService.unregister(service, instance);
	}

	@RequestMapping("/findAll")
	public List<InstanceMeta> findAllInstances(@RequestParam String service) {
		log.info(" ===> findAllInstances: {}", service);
		return registryService.getAllInstances(service);
	}

	@RequestMapping("/renew")
	public long renew(@RequestParam String service, @RequestBody InstanceMeta instance) {
		log.info(" ===> renew {} @ {}", service, instance);
		return registryService.renew(instance, service);
	}

	@RequestMapping("/renews")
	public long renews(@RequestParam String services, @RequestBody InstanceMeta instance) {
		log.info(" ===> renew {} @ {}", services, instance);
		return registryService.renew(instance, services.split(","));
	}

	@RequestMapping("/version")
	public long version(@RequestParam String service) {
		log.info(" ===> version {}", service);
		return registryService.version(service);
	}

	@RequestMapping("/versions")
	public Map<String, Long> versions(@RequestParam String services) {
		log.info(" ===> versions {}", services);
		return registryService.versions(services.split(","));
	}

}
