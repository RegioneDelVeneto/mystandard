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


import it.regioneveneto.myp3.clients.common.models.ProxyModel;
import it.regioneveneto.myp3.mystd.config.interceptor.RateLimitInterceptor;
import it.regioneveneto.myp3.mystd.config.properties.MyStandardProperties;
import it.regioneveneto.myp3.mystd.security.*;
import it.regioneveneto.myp3.mystd.security.fake.FakeUserDetailsService;
import it.regioneveneto.myp3.mystd.security.saml.EasySSLProtocolSocketFactory;
import it.regioneveneto.myp3.mystd.security.saml.SAMLUserDetailsServiceImpl;
import it.regioneveneto.myp3.mystd.security.saml.SpringResourceWrapperOpenSAMLResource;
import it.regioneveneto.myp3.mystd.service.CacheService;
import it.regioneveneto.myp3.mystd.service.ProfileService;
import it.regioneveneto.myp3.mystd.utils.MyStandardConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.metadata.provider.*;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.security.BasicSecurityConfiguration;
import org.opensaml.xml.signature.SignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.saml.*;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.*;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.*;
import org.springframework.security.saml.storage.EmptyStorageFactory;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.net.ssl.KeyManagerFactory;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.CRC32;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
@ConditionalOnProperty(name="auth.fake.enabled", havingValue="false")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    private static final String SAML2_PASSWORD_SECURE_REMOTE = "urn:oasis:names:tc:SAML:2.0:ac:classes:SecureRemotePassword";
    private static final String SAML2_PASSWORD_SMARTCARD = "urn:oasis:names:tc:SAML:2.0:ac:classes:Smartcard";
    private static final String SAML2_PASSWORD_PROTECTED_TRANSPORT = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";



    private static final String[] SECURITY_WHITELIST = { "/favicon.ico","/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"};
    private static final String[] AUTH_WHITELIST = {"/public/**", "/ws/**", "/saml/**"};

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private MyStandardProperties myStandardProperties;

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

    // SAML

    @Value("${saml.proxy.enabled:false}")
    private boolean samlProxyEnabled;
    @Value("${saml.proxy.scheme}")
    private String samlProxyScheme;
    @Value("${saml.proxy.server-name}")
    private String samlProxyServerName;
    @Value("${saml.proxy.server-port}")
    private int samlProxyServerPort;
    @Value("${saml.proxy.include-port}")
    private boolean samlProxyIncludePort;
    @Value("${saml.proxy.context-path}")
    private String samlProxyContextPath;

    @Value("${saml.key-store}")
    private String samlKeystore;
    @Value("${saml.key-store-password}")
    private String samlKeystorePassword;
    @Value("${saml.key-alias}")
    private String samlAlias;
    @Value("${saml.key-password}")
    private String samlAliasPassword;

    @Value("${saml.app-base-url}")
    private String samlAppBaseUrl;
    @Value("${saml.app-entity-id}")
    private String samlAppEntityId;
    @Value("${saml.idp-metadata-url:}")
    private String samlIdpMetadataUrl;
    @Value("${saml.idp-metadata-https-cert:}")
    private String samlIdpMetadataHttpsCert;
    @Value("${saml.idp-metadata-resource:}")
    private String samlIdpMetadataResource;
    @Value("${saml.idp-selection-path}")
    private String setIdpSelectionPath;

    @Value("${saml.failure-url}")
    private String samlFailureUrl;
    @Value("${saml.logout-url}")
    private String samlLogoutUrl;

    @Value("${saml.entry.logout}")
    private String samlEntrypointLogout;
    @Value("${saml.entry.metadata}")
    private String samlEntrypointMetadata;
    @Value("${saml.entry.login}")
    private String samlEntrypointLogin;
    @Value("${saml.entry.SSO}")
    private String samlEntrypointSSO;
    @Value("${saml.entry.SSOHoK}")
    private String samlEntrypointSSOHoK;
    @Value("${saml.entry.SingleLogout}")
    private String samlEntrypointSingleLogout;
    @Value("${saml.entry.discovery}")
    private String samlEntrypointDiscovery;

    @Value("${jwt.validity.seconds:36000}") //default: 10 hours
    private long jwtTokenValidity;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private SAMLUserDetailsServiceImpl samlUserDetailsServiceImpl;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Autowired
    private FakeUserDetailsService fakeUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private ProfileService profileService;

    @Autowired
    private CacheManager cacheManager;

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
    public MethodInvokingFactoryBean methodInvokingFactoryBean() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
        methodInvokingFactoryBean.setTargetMethod("setStrategyName");
        methodInvokingFactoryBean.setArguments(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        return methodInvokingFactoryBean;
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

                .antMatchers("/saml/logout").hasAnyAuthority("ROLE_OPERATORE_EN", "ROLE_OPERATORE_EE_LL", "ROLE_RESPONSABILE_DOMINIO", "ROLE_RESPONSABILE_STANDARD")

                .anyRequest().permitAll().and()
                .logout()
                .addLogoutHandler(customLogoutHandler())
                .logoutRequestMatcher(new AntPathRequestMatcher(samlLogoutUrl))
                .and()
                // make sure we use stateless session; session won't be used to store user's state.
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if("true".equalsIgnoreCase(corsEnabled)) {
            logger.warn("enabling CORS");
            registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST","PUT", "DELETE");
        }
    }

    @Bean
    public LogoutHandler customLogoutHandler() {
        return new JwtCookieLogoutHandler(MyStandardConstants.COOKIE_NAME_ACCESS_TOKEN, MyStandardConstants.COOKIE_PATH);
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

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(samlAuthenticationProvider());
    }

    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }

    @Bean
    @ConditionalOnProperty(name="auth.fake.enabled", havingValue="false")
    protected SessionRegistry sessionRegistryImpl() {
        return new SessionRegistryImpl();
    }

    // SAML being XML based protocol, XML parser pools should be initialized to read
    // metadata and assertions that are in XML format.
    // Initialization of the velocity engine
    @Bean
    public VelocityEngine velocityEngine() {
        return VelocityFactory.getEngine();
    }

    // XML parser pool needed for OpenSAML parsing
    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }

    @Bean(name = "parserPoolHolder")
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }

    // Bindings, encoders and decoders used for creating and parsing messages
    @Bean
    public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
        return new MultiThreadedHttpConnectionManager();
    }

    @Bean
    public HttpClient httpClient() {
        return new HttpClient(multiThreadedHttpConnectionManager());
    }

    // SAML Authentication Provider responsible for validating of received SAML
    // messages
    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(samlUserDetailsServiceImpl);
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }



    /**
     * Provider of default SAML Context This configuration is for the application that is not behind a Reverse Proxy. Alternatively,
     * SAMLContextProviderLB can be used, which is a Context provider that overrides request attributes with values of the load-balancer or
     * reverse-proxy in front of the local application. The settings help to provide correct redirect URls and verify destination URLs during SAML
     * processing.
     */
    @Bean
    public SAMLContextProvider contextProvider() {
        SAMLContextProvider provider;
        if(samlProxyEnabled) {
            SAMLContextProviderLB samlContextProviderLB = new SAMLContextProviderLB();
            samlContextProviderLB.setScheme(samlProxyScheme);
            samlContextProviderLB.setServerName(samlProxyServerName);
            samlContextProviderLB.setServerPort(samlProxyServerPort);
            samlContextProviderLB.setIncludeServerPortInRequestURL(samlProxyIncludePort);
            samlContextProviderLB.setContextPath(samlProxyContextPath);
            samlContextProviderLB.setStorageFactory(new EmptyStorageFactory());
            provider = samlContextProviderLB;
        } else {
            provider = new SAMLContextProviderImpl();
        }
        return provider;
    }

    // Initialization of OpenSAML library
    @Bean
    public static SAMLBootstrap samlBootstrap() {
        return new SAMLBootstrap(){
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                super.postProcessBeanFactory(beanFactory);
                BasicSecurityConfiguration config = (BasicSecurityConfiguration) org.opensaml.xml.Configuration.getGlobalSecurityConfiguration();

                config.registerSignatureAlgorithmURI("RSA", SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
                config.setSignatureReferenceDigestMethod(SignatureConstants.ALGO_ID_DIGEST_SHA1);
            }
        };
    }

    // Logger for SAML messages and events
    @Bean
    public SAMLDefaultLogger samlLogger() {
        return new SAMLDefaultLogger();
    }

    // SAML 2.0 WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
        WebSSOProfileConsumerImpl sso = new WebSSOProfileConsumerImpl();
        sso.setMaxAssertionTime(72000);
        sso.setResponseSkew(600);
        return new WebSSOProfileConsumerImpl();
    }

    // SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    // SAML 2.0 Web SSO profile
    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }

    // SAML 2.0 Holder-of-Key Web SSO profile
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    // SAML 2.0 ECP profile
    @Bean
    public WebSSOProfileECPImpl ecpprofile() {
        return new WebSSOProfileECPImpl();
    }

    @Bean
    public SingleLogoutProfile logoutprofile() {
        return new SingleLogoutProfileImpl();
    }

    /**
     * Metadata generation requires a keyManager, it is responsible to encrypt the saml assertion sent to IdP. A self-signed key and keystore can be
     * generated with the JRE keytool command: keytool -genkeypair -alias mykeyalias -keypass mykeypass -storepass samlstorepass -keystore
     * saml-keystore.jks
     */
    @Bean
    public KeyManager keyManager() {
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource storeFile = loader.getResource(samlKeystore);
        Map<String, String> passwords = new HashMap<>();
        passwords.put(samlAlias, samlAliasPassword);
        return new JKSKeyManager(storeFile, samlKeystorePassword, passwords, samlAlias);
    }

    @Bean
    public WebSSOProfileOptions defaultWebSSOProfileOptions() {

        WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
        webSSOProfileOptions.setIncludeScoping(false);
        webSSOProfileOptions.setProxyCount(0);
        webSSOProfileOptions.setForceAuthN(true);
        webSSOProfileOptions.setNameID(NameIDType.TRANSIENT);

        webSSOProfileOptions.setAllowCreate(true);
        webSSOProfileOptions.setAuthnContextComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
        webSSOProfileOptions.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);

        Collection<String> contexts = new ArrayList<>();
        contexts.add(SAML2_PASSWORD_PROTECTED_TRANSPORT);
        contexts.add(SAML2_PASSWORD_SMARTCARD);
        webSSOProfileOptions.setAuthnContexts(contexts);
        return webSSOProfileOptions;
    }

    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        return new SAMLEntryPoint(){
            @Override
            protected WebSSOProfileOptions getProfileOptions(SAMLMessageContext context, AuthenticationException exception) {
                WebSSOProfileOptions defaultOptions = defaultWebSSOProfileOptions();
                String callbackUrl = ((HttpServletRequestAdapter)context.getInboundMessageTransport()).getWrappedRequest().getParameter("callbackUrl");
                defaultOptions.setRelayState(callbackUrl);
                return defaultOptions;
            }
        };
    }

    @Bean
    public SAMLDiscovery samlIDPDiscovery() {
        SAMLDiscovery idpDiscovery = new SAMLDiscovery();
        idpDiscovery.setIdpSelectionPath(setIdpSelectionPath);
        return idpDiscovery;
    }

    @Bean
    @Qualifier("idp-ssocircle")
    @ConditionalOnProperty(name="auth.fake.enabled", havingValue="false")
    public ExtendedMetadataDelegate ssoCircleExtendedMetadataProvider(ResourceLoader resourceLoader, ParserPool parserPool) throws MetadataProviderException, ResourceException {
        MetadataProvider provider;
        if(StringUtils.isNotBlank(samlIdpMetadataUrl)) {
            Timer taskTimer = new Timer("idpMetadataProviderTimer", true);
            KeyStore keyStore = null;
            javax.net.ssl.KeyManager[] keyManagers=null;
            if(StringUtils.isNotBlank(samlIdpMetadataHttpsCert))
                try {
                    //create empty keystore
                    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    char[] keyStorePass = UUID.randomUUID().toString().toCharArray();
                    keyStore.load(null, keyStorePass);
                    //load the trusted cert of MyID server
                    CertificateFactory fact = CertificateFactory.getInstance("X.509");
                    Resource myIdCertFile = resourceLoader.getResource(samlIdpMetadataHttpsCert);
                    X509Certificate myIdCert = (X509Certificate) fact.generateCertificate(myIdCertFile.getInputStream());
                    keyStore.setCertificateEntry("idp", myIdCert);
                    KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    factory.init(keyStore, keyStorePass);
                    keyManagers = factory.getKeyManagers();
                } catch (Exception e){
                    throw new ResourceException("error loading MyID server certificate",e);
                }

            HttpClientBuilder builder = new HttpClientBuilder();
            setProxyInfoToBuilder(builder, samlIdpMetadataUrl);//Set info proxy se presenti nelle variabili ambiente

            builder.setHttpsProtocolSocketFactory(new EasySSLProtocolSocketFactory(keyStore, keyManagers));
            builder.setConnectionTimeout(10000); //timeout: 10s
            HttpClient httpClient = builder.buildClient();
            String urlHash;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(samlIdpMetadataUrl.getBytes());
                byte[] digest = md.digest();
                urlHash = DatatypeConverter.printHexBinary(digest).toLowerCase();
            } catch (NoSuchAlgorithmException e){
                CRC32 crc = new CRC32();
                crc.update(samlIdpMetadataUrl.getBytes());
                urlHash = Long.toHexString(crc.getValue());
            }
            String backupPath = System.getProperty("java.io.tmpdir") + File.separator + urlHash + ".xml";
            logger.info("creating FileBackedHTTPMetadataProvider for IDP metadata, url: "+samlIdpMetadataUrl+" - pathBackup: "+backupPath);
            FileBackedHTTPMetadataProvider httpProvider = new FileBackedHTTPMetadataProvider(taskTimer, httpClient, samlIdpMetadataUrl, backupPath);
            httpProvider.setParserPool(parserPool);
            provider = httpProvider;
        } else if(StringUtils.isNotBlank(samlIdpMetadataResource)) {
            Resource storeFile = resourceLoader.getResource(samlIdpMetadataResource);
            Timer taskTimer = new Timer("idpMetadataProviderTimer", true);
            logger.info("creating ResourceBackedMetadataProvider for IDP metadata, resource: "+samlIdpMetadataResource);
            ResourceBackedMetadataProvider resourceProvider = new ResourceBackedMetadataProvider(taskTimer, new SpringResourceWrapperOpenSAMLResource(storeFile));
            resourceProvider.setParserPool(parserPool);
            provider = resourceProvider;
        } else {
            throw new MetadataProviderException("missing both params saml.idp-metadata-url and saml.idp-metadata-resource");
        }
        ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(provider, extendedMetadata());
        extendedMetadataDelegate.setMetadataTrustCheck(false);
        extendedMetadataDelegate.setMetadataRequireSignature(false);
        return extendedMetadataDelegate;
    }

    /**
     * Set Proxy info se settate nelle variabili d'ambiente
     * @param builder, httpclientbuilder con cui chiamare
     * @param url ,url da cui ricavare il proxy
     * @throws ResourceException se url malformed
     */
    private void setProxyInfoToBuilder(HttpClientBuilder builder, String url) throws ResourceException {
        try {
            ProxyModel proxyModel = new ProxyModel(url);

            String proxyHost = proxyModel.getProxyHost();
            Integer proxyPort = proxyModel.getProxyPort();

            if (StringUtils.isNotEmpty(proxyHost) && proxyPort != null) {
                builder.setProxyHost(proxyHost);
                builder.setProxyPort(proxyPort);
            }

            //Set authentication info if present
            String proxyUsername = proxyModel.getProxyUsername();
            if (StringUtils.isNotEmpty(proxyUsername)) {
                builder.setProxyUsername(proxyUsername);
            }

            String proxyPassword = proxyModel.getProxyPassword();
            if (StringUtils.isNotEmpty(proxyPassword)) {
                builder.setProxyPassword(proxyPassword);
            }

        }  catch (URISyntaxException e) {
            throw new ResourceException("Error searching proxy from java system properties",e);
        }
    }

    @Bean
    @Qualifier("metadata")
    @ConditionalOnProperty(name="auth.fake.enabled", havingValue="false")
    public CachingMetadataManager metadata(ResourceLoader resourceLoader, ParserPool parserPool) throws MetadataProviderException, ResourceException {
        List<MetadataProvider> providers = new ArrayList<>();
        providers.add(ssoCircleExtendedMetadataProvider(resourceLoader, parserPool));
        providers.add(extendedMetadataDelegate());
        return new CachingMetadataManager(providers);
    }

    @Bean
    @ConditionalOnProperty(name="auth.fake.enabled", havingValue="false")
    public MetadataGenerator metadataGenerator() {
        MetadataGenerator metadataGenerator = new MetadataGenerator();
        // APP_ENTITY_ID – This is the name of the application/ audience field in the
        // application set-up for the IDP
        metadataGenerator.setEntityId(samlAppEntityId);
        // APP_BASE_URL –This is the application’s base url after deployment, it varies
        // according to the environment the application is deployed in.
        metadataGenerator.setEntityBaseURL(samlAppBaseUrl);
        metadataGenerator.setNameID(Collections.singletonList("urn:oasis:names:tc:SAML:2.0:nameid-format:transient"));
        metadataGenerator.setExtendedMetadata(extendedMetadata());
        metadataGenerator.setIncludeDiscoveryExtension(false);
        metadataGenerator.setKeyManager(keyManager());
        return metadataGenerator;
    }

    @Bean
    @ConditionalOnProperty(name="auth.fake.enabled", havingValue="false")
    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(false);
        extendedMetadata.setDigestMethodAlgorithm("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
        extendedMetadata.setSigningAlgorithm("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
        extendedMetadata.setSignMetadata(false);
        extendedMetadata.setRequireLogoutRequestSigned(false);
        return extendedMetadata;
    }

    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        return new MetadataDisplayFilter();
    }



    @Bean
    public AuthenticationSuccessHandler successRedirectHandler() {
        return new AuthenticationSuccessHandler() {
            private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
                //redirect to callback url specified when initiating login procedure, passing JWT token as query param
                try {
                    final SAMLCredential credential = (SAMLCredential) authentication.getCredentials();
                    Authentication innerAuth = (Authentication) authentication.getPrincipal();
                    UserWithAdditionalInfo userDetails = (UserWithAdditionalInfo) innerAuth.getPrincipal();
                    logger.info("principal: " + userDetails);
                    final String token = jwtTokenUtil.generateToken(userDetails.getUsername(), userDetails.getClaims());

                    // store token as valid
                    cacheService.storeTokenInCache(token);

                    //url encode token
                    // String targetUrl = credential.getRelayState() + "?access_token="+URLEncoder.encode(token, StandardCharsets.UTF_8);
                    String targetUrl = credential.getRelayState();
                    logger.debug("success authentication url: " + targetUrl);

                    // set cookie
                    Cookie cookie = new Cookie(MyStandardConstants.COOKIE_NAME_ACCESS_TOKEN, token);
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge((int) jwtTokenValidity);
                    cookie.setSecure(true);
                    cookie.setPath(MyStandardConstants.COOKIE_PATH);
                    response.addCookie(cookie);

                    redirectStrategy.sendRedirect(request, response, targetUrl);
                } catch (Exception e) {
                    logger.error("MyStandard - Errore nella memorizzazione del token in Redis", e);
                    throw e;
                }
            }

        };
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setUseForward(false);
        failureHandler.setDefaultFailureUrl(samlFailureUrl);
        return failureHandler;
    }

    @Bean
    public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
        SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter = new SAMLWebSSOHoKProcessingFilter();
        samlWebSSOHoKProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOHoKProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOHoKProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOHoKProcessingFilter;
    }

    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {

        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());

        return samlWebSSOProcessingFilter;
    }

    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter() {
        return new MetadataGeneratorFilter(metadataGenerator());
    }

    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
        successLogoutHandler.setDefaultTargetUrl(samlLogoutUrl);
        return successLogoutHandler;
    }

    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }

    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
    }

    @Bean
    public LogoutHandler cookieLogoutHandler() {
        return new JwtCookieLogoutHandler(MyStandardConstants.COOKIE_NAME_ACCESS_TOKEN, MyStandardConstants.COOKIE_PATH);
    }

    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(), new LogoutHandler[]{logoutHandler(), cookieLogoutHandler()},
                new LogoutHandler[]{logoutHandler(), cookieLogoutHandler()});
    }

    private ArtifactResolutionProfile artifactResolutionProfile() {
        final ArtifactResolutionProfileImpl artifactResolutionProfile = new ArtifactResolutionProfileImpl(httpClient());
        artifactResolutionProfile.setProcessor(new SAMLProcessorImpl(soapBinding()));
        return artifactResolutionProfile;
    }

    @Bean
    public HTTPArtifactBinding artifactBinding(ParserPool parserPool, VelocityEngine velocityEngine) {
        return new HTTPArtifactBinding(parserPool, velocityEngine, artifactResolutionProfile());
    }

    @Bean
    public HTTPSOAP11Binding soapBinding() {
        return new HTTPSOAP11Binding(parserPool());
    }

    @Bean
    public HTTPPostBinding httpPostBinding() {
        return new HTTPPostBinding(parserPool(), velocityEngine());
    }

    @Bean
    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
        return new HTTPRedirectDeflateBinding(parserPool());
    }

    @Bean
    public HTTPSOAP11Binding httpSOAP11Binding() {
        return new HTTPSOAP11Binding(parserPool());
    }

    @Bean
    public HTTPPAOS11Binding httpPAOS11Binding() {
        return new HTTPPAOS11Binding(parserPool());
    }

    @Bean
    public SAMLProcessorImpl processor() {
        Collection<SAMLBinding> bindings = new ArrayList<>();
        bindings.add(httpRedirectDeflateBinding());
        bindings.add(httpPostBinding());
        bindings.add(artifactBinding(parserPool(), velocityEngine()));
        bindings.add(httpSOAP11Binding());
        bindings.add(httpPAOS11Binding());
        return new SAMLProcessorImpl(bindings);
    }

    @Bean
    public FilterChainProxy samlFilter() throws Exception {

        List<SecurityFilterChain> chains = new ArrayList<>();

        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(samlEntrypointLogout), samlLogoutFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(samlEntrypointMetadata), metadataDisplayFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(samlEntrypointLogin), samlEntryPoint()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(samlEntrypointSSO), samlWebSSOProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(samlEntrypointSSOHoK), samlWebSSOHoKProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(samlEntrypointSingleLogout), samlLogoutProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(samlEntrypointDiscovery), samlIDPDiscovery()));

        return new FilterChainProxy(chains);
    }

    @Bean
    @ConditionalOnProperty(name="auth.fake.enabled", havingValue="false")
    public ExtendedMetadataDelegate extendedMetadataDelegate() {

        AbstractMetadataProvider provider = new AbstractMetadataProvider() {
            @Override
            protected XMLObject doGetMetadata() throws MetadataProviderException {

                MetadataGenerator mdg = metadataGenerator();
                XMLObject ed =  mdg.generateMetadata();
                return ed;
            }
        };

        ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(provider, spExtendedMetadata());
        extendedMetadataDelegate.setMetadataTrustCheck(false);
        extendedMetadataDelegate.setMetadataRequireSignature(false);
        return extendedMetadataDelegate;
    }

    @Bean
    public ExtendedMetadata spExtendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setLocal(true);
        extendedMetadata.setSecurityProfile("metaiop");
        extendedMetadata.setSslSecurityProfile("pkix");
        extendedMetadata.setSignMetadata(false);
        extendedMetadata.setSigningKey(samlAlias);
        extendedMetadata.setRequireArtifactResolveSigned(false);
        extendedMetadata.setRequireLogoutRequestSigned(false);
        extendedMetadata.setRequireLogoutResponseSigned(false);
        extendedMetadata.setIdpDiscoveryEnabled(false);
        extendedMetadata.setSigningAlgorithm("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
        return extendedMetadata;
    }


}
