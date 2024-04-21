package com.abysscat.catregistry.service;

import com.abysscat.catregistry.cluster.Snapshot;
import com.abysscat.catregistry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * Interface for registry service.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/14 22:16
 */
public interface RegistryService {

	InstanceMeta register(String service, InstanceMeta instance);

	InstanceMeta unregister(String service, InstanceMeta instance);

	List<InstanceMeta> getAllInstances(String service);

	long renew(InstanceMeta instance,String... service);

	Long version(String service);

	Map<String, Long> versions(String... services);

	Snapshot snapshot();

	long restore(Snapshot snapshot);

}
