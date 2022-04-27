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
package it.regioneveneto.myp3.mystd.service;

import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {

    private final static Logger LOG = LoggerFactory.getLogger(CacheServiceImpl.class);

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void storeTokenInCache(String token) {
        LOG.debug("Storing token in cache");
        Cache cache = cacheManager.getCache(MyStandardConstants.CACHE_NAME_VALID_TOKENS);
        if (cache != null) {
            cache.put(token, null);
            LOG.debug("Token {} stored in cache ", token);
        } else {
            LOG.debug("Cache {} not found", MyStandardConstants.CACHE_NAME_VALID_TOKENS);
        }
    }


    @Override
    public boolean isTokenInCache(String jwtToken) {
        LOG.debug("Looking up token in cache");
        Cache cache = cacheManager.getCache(MyStandardConstants.CACHE_NAME_VALID_TOKENS);
        if (cache != null) {
            Cache.ValueWrapper t = cache.get(jwtToken);
            if (t == null) {
                LOG.error("Token {} not found in cache ", jwtToken);
                return false;
            }
            LOG.debug("Token {} found in cache ", jwtToken);
            return true;
        } else {
            LOG.debug("Cache {} not found", MyStandardConstants.CACHE_NAME_VALID_TOKENS);
            return false;
        }
    }

    public void evictTokenFromCache(String token) {
        LOG.debug("Evicting token from cache");
        Cache cache = cacheManager.getCache(MyStandardConstants.CACHE_NAME_VALID_TOKENS);
        if (cache != null) {
            cache.evict(token);
            LOG.debug("Token {} evicted from cache ", token);
        } else {
            LOG.debug("Cache {} not found", MyStandardConstants.CACHE_NAME_VALID_TOKENS);
        }
    }
}
