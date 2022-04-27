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
package it.regioneveneto.myp3.mystd.config;

import it.regioneveneto.myp3.mystd.config.properties.CacheConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(CacheConfigurationProperties.class)
public class CacheConfig extends CachingConfigurerSupport {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    private static RedisCacheConfiguration createCacheConfiguration(long timeoutInSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(timeoutInSeconds));
    }


    @Bean
    @ConditionalOnProperty(name="cache.type", havingValue="sentinel")
    public LettuceConnectionFactory redisConnectionFactorySentinel(CacheConfigurationProperties properties) {
        log.info("MyStandard  - Redis (/Lettuce) sentinel configuration enabled. With cache timeout " + properties.getTimeoutSeconds() + " seconds.");


        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();

        //Set master
        String master = properties.getSentinel().getMaster();
        if (StringUtils.hasText(master)) {
            sentinelConfig.setMaster(properties.getSentinel().getMaster());

        }

        //Set master Password
        String password = properties.getSentinel().getMasterPassword();
        if (StringUtils.hasText(password)) {
            sentinelConfig.setPassword(password);
        }

        //Set sentinel password
        String sentinelPassword = properties.getSentinel().getSentinelPassword();
        if (StringUtils.hasText(sentinelPassword)) {
            sentinelConfig.setSentinelPassword(sentinelPassword);
        }


        //Set nodes
        String nodes = properties.getSentinel().getNodes();
        if (StringUtils.hasText(nodes)) {

            Set<String> hostAndPorts = StringUtils.commaDelimitedListToSet(nodes);
            for (String hostAndPort : hostAndPorts) {
                sentinelConfig.addSentinel(readHostAndPortFromString(hostAndPort));
            }
        }

        //Set database index
        Integer databaseIndex = properties.getSentinel().getDatabaseIndex();
        if (databaseIndex != null) {
            sentinelConfig.setDatabase(databaseIndex);
        }

        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(properties.getRedisCommandTimeoutSeconds())).build();

        return new LettuceConnectionFactory(sentinelConfig, lettuceClientConfiguration);

    }


    private RedisNode readHostAndPortFromString(String hostAndPort) {

        String[] args = StringUtils.split(hostAndPort, ":");
        return new RedisNode(args[0], Integer.valueOf(args[1]).intValue());
    }

    @Bean
    @ConditionalOnProperty(name="cache.type", havingValue="standalone")
    public LettuceConnectionFactory redisConnectionFactory(CacheConfigurationProperties properties) {
        log.info("Redis (/Lettuce) configuration enabled. With cache timeout " + properties.getTimeoutSeconds() + " seconds.");

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(properties.getStandalone().getRedisHost());
        redisStandaloneConfiguration.setPort(properties.getStandalone().getRedisPort());

        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(properties.getRedisCommandTimeoutSeconds())).build();

        return new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);

    }



    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        redisTemplate.setConnectionFactory(cf);
        return redisTemplate;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration(CacheConfigurationProperties properties) {
        return createCacheConfiguration(properties.getTimeoutSeconds());
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, CacheConfigurationProperties properties) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        for (Entry<String, Long> cacheNameAndTimeout : properties.getCacheExpirations().entrySet()) {
            cacheConfigurations.put(cacheNameAndTimeout.getKey(), createCacheConfiguration(cacheNameAndTimeout.getValue()));
        }

        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration(properties))
                .withInitialCacheConfigurations(cacheConfigurations).build();
    }

}
