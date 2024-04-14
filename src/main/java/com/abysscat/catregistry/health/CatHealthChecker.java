package com.abysscat.catregistry.health;

import com.abysscat.catregistry.model.InstanceMeta;
import com.abysscat.catregistry.service.CatRegistryService;
import com.abysscat.catregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/15 2:18
 */
@Slf4j
public class CatHealthChecker implements HealthChecker {

	RegistryService registryService;

	public CatHealthChecker(RegistryService registryService) {
		this.registryService = registryService;
	}

	final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	long timeout = 20_000;

	@Override
	public void start() {
		executor.scheduleWithFixedDelay(
				() -> {
					log.info(" ===> Health checker running...");
					long now = System.currentTimeMillis();
					CatRegistryService.TIMESTAMPS.keySet().forEach(serviceAndInst -> {
						long timestamp = CatRegistryService.TIMESTAMPS.get(serviceAndInst);
						if (now - timestamp > timeout) {
							log.info(" ===> Health checker: {} is down", serviceAndInst);
							int index = serviceAndInst.indexOf("@");
							String service = serviceAndInst.substring(0, index);
							String url = serviceAndInst.substring(index + 1);
							InstanceMeta instance = InstanceMeta.from(url);

							registryService.unregister(service, instance);
							CatRegistryService.TIMESTAMPS.remove(serviceAndInst);
						}
					});

				},
				10, 10, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {
		executor.shutdown();
	}
}
