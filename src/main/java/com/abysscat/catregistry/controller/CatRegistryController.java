package com.abysscat.catregistry.controller;

import com.abysscat.catregistry.cluster.Cluster;
import com.abysscat.catregistry.model.InstanceMeta;
import com.abysscat.catregistry.model.Server;
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

	@Autowired
	Cluster cluster;

	@RequestMapping("/reg")
	public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance) {
		log.info(" ===> register service: {} @ {}", service, instance);
		checkLeader();
		return registryService.register(service, instance);
	}

	@RequestMapping("/unreg")
	public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instance) {
		log.info(" ===> unregister service: {} @ {}", service, instance);
		checkLeader();
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
		checkLeader();
		return registryService.renew(instance, service);
	}

	@RequestMapping("/renews")
	public long renews(@RequestParam String services, @RequestBody InstanceMeta instance) {
		log.info(" ===> renew {} @ {}", services, instance);
		checkLeader();
		return registryService.renew(instance, services.split(","));
	}

	@RequestMapping("/version")
	public Long version(@RequestParam String service) {
		log.info(" ===> version {}", service);
		return registryService.version(service);
	}

	@RequestMapping("/versions")
	public Map<String, Long> versions(@RequestParam String services) {
		log.info(" ===> versions {}", services);
		return registryService.versions(services.split(","));
	}

	@RequestMapping("/info")
	public Server info() {
		log.info(" ===> info: {}", cluster.self());
		return cluster.self();
	}

	@RequestMapping("/cluster")
	public List<Server> cluster()
	{
		log.info(" ===> info: {}", cluster.getServers());
		return cluster.getServers();
	}

	@RequestMapping("/leader")
	public Server leader()
	{
		log.info(" ===> leader: {}", cluster.leader());
		return cluster.leader();
	}

	@RequestMapping("/sl")
	public Server sl()
	{
		cluster.self().setLeader(true);
		log.info(" ===> leader: {}", cluster.self());
		return cluster.self();
	}

	private void checkLeader() {
		if (cluster.self().isLeader()) {
			return;
		}
		String checkLeaderErrorFormat = "current server {%s} is not leader, leader is {%s}";
		throw new RuntimeException(String.format(checkLeaderErrorFormat, cluster.self().getUrl(), cluster.leader().getUrl()));
	}

}
