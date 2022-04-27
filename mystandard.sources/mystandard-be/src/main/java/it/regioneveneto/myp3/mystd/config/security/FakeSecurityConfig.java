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
package it.regioneveneto.myp3.mystd.config.security;


import it.regioneveneto.myp3.mystd.config.interceptor.RateLimitInterceptor;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.security.JwtAuthenticationEntryPoint;
import it.regioneveneto.myp3.mystd.security.JwtCookieLogoutHandler;
import it.regioneveneto.myp3.mystd.security.JwtTokenUtil;
import it.regioneveneto.myp3.mystd.security.UserWithAdditionalInfo;
import it.regioneveneto.myp3.mystd.security.fake.FakeUserDetailsService;
import it.regioneveneto.myp3.mystd.security.fake.JwtAuthenticationSuccessHandler;
import it.regioneveneto.myp3.mystd.security.fake.JwtAuthorizationFilter;
import it.regioneveneto.myp3.mystd.service.CacheService;
import it.regioneveneto.myp3.mystd.service.ProfileService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
@ConditionalOnProperty(name="auth.fake.enabled", havingValue="true")
public class FakeSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    private static final String[] SECURITY_WHITELIST = { "/favicon.ico","/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"};
    //error path is added dynamically
    private static final String[] AUTH_WHITELIST = {"/public/**", "/ws/**", "/saml/**"};
    private static final Logger logger = LoggerFactory.getLogger(FakeSecurityConfig.class);


    @Value("${cors.enabled:false}")
    private String corsEnabled;
    @Value("${auth.fake.enabled:false}")
    private String fakeAuthEnabled;

    @Value("${static.serve.enabled:false}")
    private String staticContentEnabled;
    @Value("${static.serve.path:/staticContent}")
    private String staticContentPath;
    @Value("${static.serve.location:/staticLocation}")
    private String staticContentLocation;

    @Value("${server.error.path:/error}")
    private String errorPath;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.validity.seconds:36000}") //default: 10 hours
    private long jwtTokenValidity;

    @Autowired
    private MyStandardProperties myStandardProperties;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${saml.entry.login}")
    private String samlEntrypointLogin;

    @Value("${saml.logout-url}")
    private String samlLogoutUrl;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private FakeUserDetailsService fakeUserDetailsService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    @Lazy
    private RateLimitInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/**");
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) {
        String[] securityWhitelist = ArrayUtils.addAll(SECURITY_WHITELIST, errorPath);
        if("true".equalsIgnoreCase(staticContentEnabled)) {
            logger.warn("serving static content at path: " + staticContentPath);
            securityWhitelist = ArrayUtils.addAll(securityWhitelist, staticContentPath, staticContentPath + "/**");
        }
        web.ignoring().antMatchers(securityWhitelist);
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        //HttpSecurity httpSecurityRef = httpSecurity;
        if("true".equalsIgnoreCase(corsEnabled)) {
            logger.warn("enabling CORS (security)");
            httpSecurity = httpSecurity.cors().and();
        }
        httpSecurity.csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/query/management/**").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers(HttpMethod.PUT, "/query/management/**").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers(HttpMethod.DELETE, "/query/management/**").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers("/**/postIPA").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers(HttpMethod.POST, "/allegati/**").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers("/entities/**/publish").hasAnyAuthority("ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers("/entities/**/delete").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers(HttpMethod.POST, "/entities/**/").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers(HttpMethod.PUT, "/entities/**/").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers(HttpMethod.GET, "/entities/**/nuovo").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers(HttpMethod.GET, "/entities/bacheca/**/").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")
                .antMatchers(HttpMethod.POST, "/search/reindex").hasAnyAuthority("ROLE_RESPONSABILE_STANDARD")

                .antMatchers("/saml/logout").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_EN", "ROLE_RESPONSABILE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")

                .antMatchers(samlEntrypointLogin).authenticated()
                .anyRequest().permitAll()
                .and().formLogin()
                .and()
                .logout()
                .addLogoutHandler(customLogoutHandler())
                .logoutRequestMatcher(new AntPathRequestMatcher(samlLogoutUrl))
                .and()
                // make sure we use stateless session; session won't be used to store user's state.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        httpSecurity.addFilterAt(new JwtAuthorizationFilter(authenticationManager(), jwtTokenUtil, cacheService), BasicAuthenticationFilter.class);
        httpSecurity.addFilterAt(getAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }


    protected UsernamePasswordAuthenticationFilter getAuthenticationFilter() throws Exception {
        UsernamePasswordAuthenticationFilter authFilter = new UsernamePasswordAuthenticationFilter();
        authFilter.setAuthenticationManager(this.authenticationManagerBean());
        authFilter.setAuthenticationSuccessHandler(successRedirectHandler());

        return authFilter;
    }

    @Bean
    public AuthenticationSuccessHandler successRedirectHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
                //redirect to callback url specified when initiating login procedure, passing JWT token as query param

                try {
                    UserWithAdditionalInfo userDetails = (UserWithAdditionalInfo) authentication.getPrincipal();
                    logger.info("principal: " + userDetails);

                    final String token = jwtTokenUtil.generateToken(userDetails.getUsername(), userDetails.getClaims());

                    // store token as valid

                    cacheService.storeTokenInCache(token);

                    // set cookie
                    Cookie cookie = new Cookie(MyStandardConstants.COOKIE_NAME_ACCESS_TOKEN, token);
                    cookie.setHttpOnly(false);
                    cookie.setMaxAge((int) jwtTokenValidity);
                    cookie.setSecure(false);
                    cookie.setPath("/");
                    response.addCookie(cookie);

                    new JwtAuthenticationSuccessHandler().onAuthenticationSuccess(request, response, authentication);
                } catch (Exception e) {
                    logger.error("MyStandard - Errore nella memorizzazione del token in Redis", e);
                    throw e;
                }
            }

        };
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Bean
    public PasswordEncoder passwordEncoder() {
        return this.passwordEncoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider
                = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(fakeUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public LogoutHandler customLogoutHandler() {
        return new JwtCookieLogoutHandler(MyStandardConstants.COOKIE_NAME_ACCESS_TOKEN, MyStandardConstants.COOKIE_PATH);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if("true".equalsIgnoreCase(corsEnabled)) {
            logger.warn("enabling CORS");
            registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST","PUT", "DELETE");
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        logger.warn("enabling staticContentPath {}", staticContentPath);
        logger.warn("enabling staticContentLocation {}", staticContentLocation);
        if("true".equalsIgnoreCase(staticContentEnabled)) {

            registry
                    .addResourceHandler(staticContentPath + "/**")
                    .addResourceLocations("classpath:/static/")
                    .resourceChain(false)
                    .addResolver(new PathResourceResolver() {
                        @Override
                        protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
                            Resource resource = super.resolveResourceInternal(request, requestPath, locations, chain);
                            if (resource == null) {
                                resource = super.resolveResourceInternal(request, "index.html", locations, chain);
                            }
                            return resource;
                        }
                    });
        }
    }



}
