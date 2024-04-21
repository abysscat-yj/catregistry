package com.abysscat.catregistry.cluster;

import com.abysscat.catregistry.config.CatRegistryConfigProperties;
import com.abysscat.catregistry.model.Server;
import com.abysscat.catregistry.service.CatRegistryService;
import com.abysscat.catregistry.service.RegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry cluster.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/17 0:50
 */
@Slf4j
public class Cluster {

	@Value("${server.port}")
	String port;

	String host;

	Server MYSELF;

	CatRegistryConfigProperties registryConfigProperties;
	RegistryService registryService;

	@Getter
	private final List<Server> servers = new ArrayList<>();

	public Cluster(CatRegistryConfigProperties registryConfigProperties, RegistryService registryService) {
		this.registryConfigProperties = registryConfigProperties;
		this.registryService = registryService;
	}

	public void init() {
		try {
			host = new InetUtils(new InetUtilsProperties())
					.findFirstNonLoopbackHostInfo().getIpAddress();
			log.info("findFirstNonLoopbackHostInfo ip:" + host);
		} catch (Exception e) {
			host = "127.0.0.1";
		}

		MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
		log.info(" ===> myself server info: {}", MYSELF);

		initServers();
		// 启动集群调度任务
		new Scheduler(this, registryService).run();
	}

	private void initServers() {
		for (String url : registryConfigProperties.getServers()) {
			Server server = new Server();
			if (url.contains("localhost")) {
				url = url.replace("localhost", host);
			} else if (url.contains("127.0.0.1")) {
				url = url.replace("127.0.0.1", host);
			}
			if (url.equals(MYSELF.getUrl())) {
				servers.add(MYSELF);
			} else {
				server.setUrl(url);
				server.setStatus(false);
				server.setLeader(false);
				server.setVersion(-1L);
				servers.add(server);
			}
		}
	}

	public List<Server> getActiveServers() {
		return this.servers.stream()
				.filter(Server::isStatus).toList();
	}

	public List<Server> getActiveLeaders() {
		return this.servers.stream()
				.filter(Server::isStatus)
				.filter(Server::isLeader).toList();
	}

	public Server leader() {
		return this.servers.stream()
				.filter(Server::isStatus)
				.filter(Server::isLeader)
				.findFirst().orElse(null);
	}

	public Server self() {
		MYSELF.setVersion(CatRegistryService.VERSION.get());
		return MYSELF;
	}

}
