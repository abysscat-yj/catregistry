package com.abysscat.catregistry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Registry server.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/17 0:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"url"})
public class Server {

	private String url;

	private boolean status;

	private boolean leader;

	private long version;

}
