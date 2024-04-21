package com.abysscat.catregistry.cluster;

import com.abysscat.catregistry.http.HttpInvoker;
import com.abysscat.catregistry.model.Server;
import com.abysscat.catregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Cluster scheduler.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/22 0:02
 */
@Slf4j
public class Scheduler {

	final Cluster cluster;
	final RegistryService registryService;

	public Scheduler(Cluster cluster, RegistryService registryService) {
		this.cluster = cluster;
		this.registryService = registryService;
	}

	final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private static final long SCHEDULE_INTERVAL = 5_000;

	public void run() {
		// 启动定时任务，处理集群节点状态
		executor.scheduleAtFixedRate(() -> {
			try {
				// 1.探索集群节点，更新节点列表
				updateServers();
				// 2.选主
				doElectLeader();
				// 3.同步主节点数据快照
				syncLeaderSnapshot();
			} catch (Exception ex) {
				log.error("executor schedule failed. ERROR:", ex);
			}
		}, 0, SCHEDULE_INTERVAL, TimeUnit.MILLISECONDS);
	}

	private void doElectLeader() {
		Election election = new Election();
		election.electLeader(cluster.getServers());
	}

	private void updateServers() {
		List<Server> servers = cluster.getServers();
		Server self = cluster.self();
		// 探活
		servers.parallelStream().forEach(server -> {
			try {
				// 无需探活自身
				if (self.equals(server)) {
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

	private void syncLeaderSnapshot() {
		Server self = cluster.self();
		if (self.isLeader()) {
			return;
		}
		Server leader = cluster.leader();
		if (leader == null) {
			throw new RuntimeException("no leader for sync snapshot");
		}
		// 如果当前节点版本号小于leader，则同步leader快照
		if (self.getVersion() >= leader.getVersion()) {
			return;
		}
		log.debug(" ===>>> sync snapshot from leader: {}", leader);
		Snapshot leaderSnapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);
		log.debug(" ===>>> sync leaderSnapshot: {}", leaderSnapshot);
		if (leaderSnapshot == null) {
			return;
		}
		registryService.restore(leaderSnapshot);
	}


}
