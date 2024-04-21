package com.abysscat.catregistry.cluster;

import com.abysscat.catregistry.model.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Cluster leader election.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/22 0:14
 */
@Slf4j
public class Election {

	public void electLeader(List<Server> servers) {
		List<Server> leaders = servers.stream()
				.filter(Server::isStatus)
				.filter(Server::isLeader).toList();
		if (leaders.isEmpty()) {
			log.info(" ===>>> no active leader for :{}, electing ...", servers);
			elect(servers);
		} else if (leaders.size() > 1) {
			log.info("===>>> more than one active leader for :{}, electing ...", servers);
			elect(servers);
		} else {
			log.debug(" ===>>> no need election for one leader: " + leaders.get(0));
		}
	}

	public void elect(List<Server> servers) {
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

}
