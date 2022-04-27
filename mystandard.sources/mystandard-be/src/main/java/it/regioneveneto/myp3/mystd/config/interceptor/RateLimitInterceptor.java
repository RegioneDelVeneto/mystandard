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
package it.regioneveneto.myp3.mystd.config.interceptor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import it.regioneveneto.myp3.mystd.bean.MyStandardResult;
import it.regioneveneto.myp3.mystd.service.RateLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String HEADER_API_KEY = "X-api-key";
    private static final String HEADER_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    private static final String HEADER_LIMIT = "X-Rate-Limit-Limit";
    private static final String HEADER_LIMIT_RESET = "X-Rate-Limit-Reset";

    private static final String HEADER_RETRY_AFTER = "X-Rate-Limit-Retry-After-Seconds";





    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String apiKey = HEADER_API_KEY;

        Bucket tokenBucket = rateLimitService.resolveBucket(apiKey);

        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);


        if (probe.isConsumed()) {
            response.addHeader(HEADER_LIMIT, String.valueOf(rateLimitService.getBucketCapacity()));
            response.addHeader(HEADER_LIMIT_REMAINING, String.valueOf(probe.getRemainingTokens()));

            return true;

        } else {

            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader(HEADER_LIMIT_RESET, String.valueOf(waitForRefill));
            response.setHeader(HEADER_LIMIT, String.valueOf(rateLimitService.getBucketCapacity()));
            response.setHeader(HEADER_LIMIT_REMAINING, String.valueOf(probe.getRemainingTokens()));

            MyStandardResult myStandardResult = new MyStandardResult(false, "RateLimit superato.");
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            response.getOutputStream().print(mapper.writeValueAsString(myStandardResult));
            return false;
        }
    }
}