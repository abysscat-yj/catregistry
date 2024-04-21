package com.abysscat.catregistry.cluster;

import com.abysscat.catregistry.config.CatRegistryConfigProperties;
import com.abysscat.catregistry.http.HttpInvoker;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

	final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private static final long SCHEDULE_INTERVAL = 5_000;

	public Cluster(CatRegistryConfigProperties registryConfigProperties, RegistryService registryService) {
		this.registryConfigProperties = registryConfigProperties;
		this.registryService = registryService;
	}

	public void init() {
		try {
			host = new InetUtils(new InetUtilsProperties())
					.findFirstNonLoopbackHostInfo().getIpAddress();
			System.out.println(" ===> findFirstNonLoopbackHostInfo = " + host);
		} catch (Exception e) {
			host = "127.0.0.1";
		}

		MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
		log.info(" ===> myself server info: {}", MYSELF);

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

		// 启动定时任务，处理集群节点状态
		executor.scheduleAtFixedRate(() -> {
			try {
				// 1.探索集群节点，更新节点列表
				updateServers();
				// 2.选主
				electLeader();
				// 3.同步主节点数据快照
				syncLeaderSnapshot();
			} catch (Exception ex) {
				log.error("executor schedule failed. ERROR:", ex);
			}
		}, 0, SCHEDULE_INTERVAL, TimeUnit.MILLISECONDS);

	}

	private void syncLeaderSnapshot() {
		if (MYSELF.isLeader()) {
			return;
		}
		Server leader = leader();
		if (leader == null) {
			throw new RuntimeException("no leader for sync snapshot");
		}
		// 如果当前节点版本号小于leader，则同步leader快照
		if (MYSELF.getVersion() >= leader.getVersion()) {
			return;
		}
		log.debug(" ===>>> sync snapshot from leader: {}", leader);
		Snapshot leaderSnapshot = HttpInvoker.httpGet(leader().getUrl() + "/snapshot", Snapshot.class);
		log.debug(" ===>>> sync leaderSnapshot: {}", leaderSnapshot);
		if (leaderSnapshot == null) {
			return;
		}
		registryService.restore(leaderSnapshot);
	}

	private void electLeader() {
		List<Server> leaders = getActiveLeaders();
		if (leaders.isEmpty()) {
			log.info(" ===>>> no active leader for :{}, electing ...", servers);
			elect();
		} else if (leaders.size() > 1) {
			log.info("===>>> more than one active leader for :{}, electing ...", servers);
			elect();
		} else {
			log.debug(" ===>>> no need election for one leader: " + leaders.get(0));
		}
	}

	private void elect() {
		// 1.各种节点自己选，算法保证大家选的是同一个
		// 2.外部有一个分布式锁，谁拿到锁，谁是主
		// 3.分布式一致性算法，比如paxos、raft

		Server candidate = null;
		for (Server server : servers) {
			server.setLeader(false);
			if (server.isStatus()) {
				if (candidate == null) {
					candidate = server;
				} else {
					// 取巧：重写hashcode方法，保证相同url的节点，hashCode相同，由此不同节点计算一致
					if (candidate.hashCode() > server.hashCode()) {
						candidate = server;
					}
				}
			}
		}
		if (candidate != null) {
			candidate.setLeader(true);
			log.info(" ===>>> elect for leader: " + candidate);
		} else {
			log.info(" ===>>> elect failed for no leaders: " + servers);
		}

	}

	private void updateServers() {
		// 探活
		servers.parallelStream().forEach(server -> {
			try {
				// 无需探活自身
				if (MYSELF.equals(server)) {
					return;
				}
				Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
				log.debug(" ===> health check success for: {}", serverInfo);
				if (serverInfo != null) {
					server.setStatus(true);
					server.setLeader(serverInfo.isLeader());
					server.setVersion(serverInfo.getVersion());
				}
			} catch (Exception ex) {
				log.debug(" ===> health check failed for: {}", server);
				server.setStatus(false);
				server.setLeader(false);
			}
		});
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
