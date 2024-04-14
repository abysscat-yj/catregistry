package com.abysscat.catregistry.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Instance Meta class.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/14 22:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"scheme", "host", "port", "context"})
public class InstanceMeta {

	private String scheme;
	private String host;
	private Integer port;
	private String context;

	private boolean status; // online or offline
	private Map<String, String> parameters = new HashMap<>();

	public InstanceMeta(String scheme, String host, Integer port, String context) {
		this.scheme = scheme;
		this.host = host;
		this.port = port;
		this.context = context;
	}

	public static InstanceMeta from(String url) {
		URI uri = URI.create(url);
		return new InstanceMeta(uri.getScheme(),
				uri.getHost(),
				uri.getPort(),
				uri.getPath().substring(1));
	}

	public String toPath() {
		return String.format("%s_%d", host, port);
	}

	public String toUrl() {
		return String.format("%s://%s:%d/%s", scheme, host, port, context);
	}

	public String toMetas() {
		return JSON.toJSONString(this.parameters);
	}

	public InstanceMeta addParams(Map<String, String> params) {
		this.getParameters().putAll(params);
		return this;
	}

	public static InstanceMeta http(String host, Integer port) {
		return new InstanceMeta("http", host, port, "catrpc");
	}

}
