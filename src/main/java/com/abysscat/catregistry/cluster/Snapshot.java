package com.abysscat.catregistry.cluster;

import com.abysscat.catregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * Server snapshot data.
 *
 * @Author: abysscat-yj
 * @Create: 2024/4/21 23:32
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Snapshot {

	LinkedMultiValueMap<String, InstanceMeta> registry;

	Map<String, Long> versions;

	Map<String, Long> timestamps;

	long version;

}
