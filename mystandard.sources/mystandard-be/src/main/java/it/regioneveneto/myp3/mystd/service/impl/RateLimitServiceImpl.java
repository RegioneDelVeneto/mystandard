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
package it.regioneveneto.myp3.mystd.service.impl;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import it.regioneveneto.myp3.mystd.service.RateLimitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitServiceImpl implements RateLimitService {

  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  @Override
  public int getBucketCapacity() {
    return bucketCapacity;
  }

  @Value("${bucket4j.capacity}")
  private int bucketCapacity;

  @Value("${bucket4j.duration}")
  private Duration bucketDuration;


  public Bucket resolveBucket(String apiKey) {
    return cache.computeIfAbsent(apiKey, this::newBucket);
  }

  private Bucket newBucket(String apiKey) {
    return bucket(getLimit());
  }

  private Bucket bucket(Bandwidth limit) {
    return Bucket4j.builder()
            .addLimit(limit)
            .build();
  }

  private Bandwidth getLimit() {
    return Bandwidth.classic(bucketCapacity, Refill.intervally(bucketCapacity, bucketDuration));
  }
}
