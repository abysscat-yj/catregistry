package com.abysscat.catregistry.service;

import com.abysscat.catregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * CatRegistry service implementation.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/14 22:30
 */
@Slf4j
public class CatRegistryService implements RegistryService {

	final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
	final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
	public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
	public final static AtomicLong VERSION = new AtomicLong(0);

	@Override
	public InstanceMeta register(String service, InstanceMeta instance) {
		List<InstanceMeta> instanceMetas = REGISTRY.get(service);
		if (instanceMetas != null && !instanceMetas.isEmpty()) {
			if (instanceMetas.contains(instance)) {
				log.info(" ===> instance {} already registered", instance.toUrl());
				instance.setStatus(true);
				return instance;
			}
		}
		log.info(" ===> register instance {}", instance.toUrl());
		REGISTRY.add(service, instance);
		instance.setStatus(true);
		VERSIONS.put(service, VERSION.incrementAndGet());
		renew(instance, service);
		return instance;
	}

	@Override
	public InstanceMeta unregister(String service, InstanceMeta instance) {
		List<InstanceMeta> metas = REGISTRY.get(service);
		if (metas == null || metas.isEmpty()) {
			return null;
		}
		log.info(" ====> unregister instance {}", instance.toUrl());
		metas.remove(instance);
		instance.setStatus(false);
		VERSIONS.put(service, VERSION.incrementAndGet());
		renew(instance, service);
		return instance;
	}

	@Override
	public List<InstanceMeta> getAllInstances(String service) {
		return REGISTRY.get(service);
	}

	@Override
	public long renew(InstanceMeta instance, String... services) {
		long now = System.currentTimeMillis();
		for (String service : services) {
			TIMESTAMPS.put(service + "@" + instance.toUrl(), now);
		}
		return now;
	}

	@Override
	public Long version(String service) {
		return VERSIONS.get(service);
	}

	@Override
	public Map<String, Long> versions(String... services) {
		return Arrays.stream(services)
				.collect(Collectors.toMap(x -> x, VERSIONS::get, (a, b) -> b));
	}

}
