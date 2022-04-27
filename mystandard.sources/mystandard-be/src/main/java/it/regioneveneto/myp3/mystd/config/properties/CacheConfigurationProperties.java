/**
 *     My Standard
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.myp3.mystd.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "cache")
public class CacheConfigurationProperties {

	private long timeoutSeconds;
	private int redisCommandTimeoutSeconds = 120;
	private Standalone standalone = new Standalone();
	private Sentinel sentinel = new Sentinel();
	// Mapping of cacheNames to timeout in seconds
	private Map<String, Long> cacheExpirations = new HashMap<>();

	public class Sentinel {
		private String master;
		private String masterPassword;
		private String sentinelPassword;
		private String nodes;
		private Integer databaseIndex;

		public String getMaster() {
			return master;
		}

		public void setMaster(String master) {
			this.master = master;
		}

		public String getNodes() {
			return nodes;
		}

		public void setNodes(String nodes) {
			this.nodes = nodes;
		}

		public String getMasterPassword() {
			return masterPassword;
		}

		public void setMasterPassword(String masterPassword) {
			this.masterPassword = masterPassword;
		}

		public String getSentinelPassword() {
			return sentinelPassword;
		}

		public void setSentinelPassword(String sentinelPassword) {
			this.sentinelPassword = sentinelPassword;
		}

		public Integer getDatabaseIndex() {
			return databaseIndex;
		}

		public void setDatabaseIndex(Integer databaseIndex) {
			this.databaseIndex = databaseIndex;
		}
	}

	public class Standalone {
		private int redisPort;
		private String redisHost;


		public int getRedisPort() {
			return redisPort;
		}

		public void setRedisPort(int redisPort) {
			this.redisPort = redisPort;
		}

		public String getRedisHost() {
			return redisHost;
		}

		public void setRedisHost(String redisHost) {
			this.redisHost = redisHost;
		}

	}




	public long getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public void setTimeoutSeconds(long timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

	public int getRedisCommandTimeoutSeconds() {
		return redisCommandTimeoutSeconds;
	}

	public void setRedisCommandTimeoutSeconds(int redisCommandTimeoutSeconds) {
		this.redisCommandTimeoutSeconds = redisCommandTimeoutSeconds;
	}

	public Map<String, Long> getCacheExpirations() {
		return cacheExpirations;
	}

	public void setCacheExpirations(Map<String, Long> cacheExpirations) {
		this.cacheExpirations = cacheExpirations;
	}

	public Sentinel getSentinel() {
		return sentinel;
	}

	public void setSentinel(Sentinel sentinel) {
		this.sentinel = sentinel;
	}

	public Standalone getStandalone() {
		return standalone;
	}

	public void setStandalone(Standalone standalone) {
		this.standalone = standalone;
	}
}
