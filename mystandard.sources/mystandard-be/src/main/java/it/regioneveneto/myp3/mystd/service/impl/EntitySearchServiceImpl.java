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

import it.regioneveneto.myp3.clients.common.models.ProxyModel;
import it.regioneveneto.myp3.mystd.bean.filter.MyStandardFilter;
import it.regioneveneto.myp3.mystd.bean.individual.MyStandardEntity;
import it.regioneveneto.myp3.mystd.bean.search.SearchOperation;
import it.regioneveneto.myp3.mystd.bean.search.SearchResult;
import it.regioneveneto.myp3.mystd.bean.search.Transformer;
import it.regioneveneto.myp3.mystd.service.EntitySearchService;
import it.regioneveneto.myp3.mystd.service.EntityService;
import it.regioneveneto.myp3.mystd.utils.MyStandardUtil;
import nl.altindag.ssl.SSLFactory;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 
 * @author Regione del Veneto
 *
 */
@Service
public class EntitySearchServiceImpl implements EntitySearchService {

	private static final Logger LOG = LoggerFactory.getLogger(EntitySearchServiceImpl.class);

	private static String internalIdPropertyName = "__internal_id__";

	private static String internalVersionPropertyName = "__internal_version__";

	private static String internalDomainPropertyName = "__internal_domain__";

	private static String internalTypeName = "__internal_type__";


	@Value("${mysearch.indexName}")
	private String indexName;

	@Value("${mysearch.hosts}")
	private String hosts;

	@Value("${mysearch.hostsUser:#{null}}")
	private String hostsUser;

	@Value("${mysearch.hostsPwd:#{null}}")
	private String hostsPwd;

	@Value("${mysearch.entitiesMappingConfigurationAbsolutePath}")
	private String entitiesMappingConfigurationAbsolutePathappingConfigurationAbsolutePath;

	@Autowired
	private EntityService entityService;

	/**
	 * Check index existence and, if not existing, create it
	 */
	public void createIndexes() {
		this.createIndex(this.getLatestVersionsIndexName());
		this.createIndex(this.getAllVersionsIndexName());
	}
	
	/**
	 * Drop indexes 
	 */
	public void dropIndexes() {
		this.dropIndex(this.getLatestVersionsIndexName());
		this.dropIndex(this.getAllVersionsIndexName());
	}

	/**
	 * Index all entities
	 */
	public void indexAllEntities() {
		MyStandardFilter dummyFilter = new MyStandardFilter();
		
		try {
			JSONObject menu = entityService.getDynamicEntitesByDomain().getJSONObject("items");
			Iterator<String> domains = menu.keys();
			List<JSONObject> domainsAndEntityTypes = new ArrayList<>();

			LOG.debug("EntitySearchServiceImpl --> Generazione elenco dei tipi di entità.");

			while(domains.hasNext()) {
				String menuKey = domains.next();
				JSONArray elements = (JSONArray) menu.get(menuKey);

				for (int i = 0; i < elements.length(); i++) {
					JSONObject element = elements.getJSONObject(i);
					String entityType = element.getString("localName");

					JSONObject item = new JSONObject();
					item.put("domain", menuKey);
					item.put("entityType", entityType);
					domainsAndEntityTypes.add(item);
				}
			}

			for (JSONObject item : domainsAndEntityTypes) {
				String domain = item.getString("domain");
				String entityType = item.getString("entityType");
				
				LOG.debug("EntitySearchServiceImpl --> Indicizzazione delle entità in dominio {} di tipo {}.", domain, entityType);

				ArrayList<JSONObject> entities = (ArrayList<JSONObject>) entityService.findAll(domain, entityType, dummyFilter);
				for (JSONObject entity : entities) {
					String entityCode = entity.getString("CodiceEntita");
					JSONObject maxVersionRaw = entityService.findMaxVersioneByCodice(entityType, entityCode);
					Integer maxVersion = Integer.parseInt(maxVersionRaw.getString("Versione"));
					
					for (Integer i = 1; i <= maxVersion; i++ ){
						JSONObject versionedEntity = entityService.getRawEntityByCodiceAndVersione(entityType, entityCode, i);

						if (versionedEntity != null) {
							LOG.debug("EntitySearchServiceImpl --> Indicizzazione codice: {} versione: {} del dominio: {} di tipo: {}.", entityCode, i, domain, entityType);
							Iterator<String> versionedEntityKeys = versionedEntity.keys();
							Map<String, Object> entityDataProperties = new HashMap<>();
	
							while(versionedEntityKeys.hasNext()) {
								String key = versionedEntityKeys.next();
								Object property = versionedEntity.get(key);
								if (property instanceof JSONObject) {
									JSONObject jsonProperty = (JSONObject) property;
									if (jsonProperty.has("_dataPropertyIRI") && EntitySearchServiceImpl.IS_TO_NOT_EXCLUDE.test(jsonProperty.getString("_dataPropertyIRI"))) {
										String data = jsonProperty.getString("_dataPropertyValue");
										if(data.contains("^^") && !data.contains("true") && !data.contains("false")){
											data = MyStandardUtil.parseLocalDate(jsonProperty.getString("_dataPropertyValue"), DateTimeFormatter.ofPattern(MyStandardUtil.ALTERNATIVE_DATE_PATTERN));
										}
										entityDataProperties.put(jsonProperty.getString("_dataPropertyIRI"), data);
									}
								}
							}
	
							MyStandardEntity myStandardEntity = new MyStandardEntity();
							myStandardEntity.setDataProperty(entityDataProperties);
							Long version = Long.parseLong(Integer.toString(i));

							this.indexNewEntity(entityCode, version, entityType, domain, myStandardEntity);
						}
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private static List<String> EXCLUDED_KEYS = List.of("https://mystandard.regione.veneto.it/onto/BPO#dtUpdate",
			                                            "https://mystandard.regione.veneto.it/onto/BPO#dtIns");

	private static Predicate<String> IS_TO_NOT_EXCLUDE = (keyToTest) -> !EntitySearchServiceImpl.EXCLUDED_KEYS.contains(keyToTest);
	/**
	 * Index an entity
	 *
	 * @param id
	 * @param entity
	 * @return
	 */
	public void indexNewEntity(final String id, final long version, final String entityType, final String domain, final MyStandardEntity entity) {
		this.indexEntity(this.getLatestVersionsIndexName(), id, version, entityType, domain, entity);
		this.indexEntity(this.getAllVersionsIndexName(), 
				String.format("%s$$%s", id, version), null, entityType, domain, entity);
	}
	
	/**
	 * Update an entity
	 * 
	 * @param id
	 * @param version
	 * @param entityType
	 * @param entity
	 * @return
	 */
	public void updateExistingEntity(final String id, final long version, final String entityType, final MyStandardEntity entity) {		
		// Update just if it's the latest version
		GetResponse result = this.getEntity(this.getLatestVersionsIndexName(), id);
		
		if (result.isExists() && Long.parseLong((String) result.getSourceAsMap().get(internalVersionPropertyName)) == version)
			this.updateEntity(this.getLatestVersionsIndexName(), id, entity);

		this.updateEntity(this.getAllVersionsIndexName(), 
				String.format("%s$$%s", id, version), entity);
	}
	
	/**
	 * Remove an entity from index
	 * 
	 * @param indexName
	 * @param id
	 * @param entityType
	 * @return
	 */
	public void removeEntityFromIndex(final String id, final long version, final String entityType) {
		// Remove the versioned entity
		this.removeEntity(this.getAllVersionsIndexName(), 
				String.format("%s$$%s", id, version));

		this.refreshIndex(this.getAllVersionsIndexName());
		
		// If it's the latest version, remove the entity and index the new latest version
		GetResponse response = this.getEntity(this.getLatestVersionsIndexName(), id);
		
		if (response.isExists() && Long.parseLong((String)response.getSourceAsMap().get(internalVersionPropertyName)) == version) {
			// Get new latest version
			List<SearchResult> versions = Arrays.asList(this.getAllVersionedEntities(id, entityType).getHits().getHits())
				.stream()
				.map(r -> new SearchResult() {{
					this.setId(r.getId().substring(0, r.getId().indexOf("$$")));
					this.setVersion(Long.parseLong(r.getId().substring(r.getId().indexOf("$$") + 2)));
					this.setEntityType(r.getType());
					this.setDataProperties(r.getSourceAsMap());
				}})
				.sorted(Comparator.comparing(SearchResult::getVersion).reversed())
				.collect(Collectors.toList());
			
			if (versions.size() == 0) {
				// There are no versions at all, so remove latest version
				this.removeEntity(this.getLatestVersionsIndexName(), id);

				return;
			}
			
			this.executeOperation((client, restClient) -> {
				final Map<String, Object> entityData = versions.get(0).getDataProperties();
				
				entityData.put(internalIdPropertyName, id);
				entityData.put(internalVersionPropertyName, String.valueOf(versions.get(0).getVersion()));

				// Perform indexing operation
				UpdateRequest request = new UpdateRequest(getLatestVersionsIndexName(), id)
						.doc(entityData);
				
				client.update(request, RequestOptions.DEFAULT);
				
				return null;
			});
		}
	}
	
	/**
	 * Perform search by type on latest entities versions. Results are sorted by score.
	 * 
	 * @param query
	 * @param entityType
	 * @param offset
	 * @param size
	 * @return
	 */
	public it.regioneveneto.myp3.mystd.bean.search.SearchResponse searchOnLatestVersions(final String query, 
			final String entityType, final int offset, final int size) {
		return this.search(this.getLatestVersionsIndexName(), query, entityType, offset, size,
				r -> r.getId(), r -> Long.parseLong((String) r.getSourceAsMap().get(internalVersionPropertyName)),
				r -> (String) r.getSourceAsMap().get(internalTypeName));
	}
	
	/**
	 * Perform search on latest entity versions. Results are sorted by score.
	 * 
	 * @param query
	 * @param entityType
	 * @param offset
	 * @param size
	 */
	public it.regioneveneto.myp3.mystd.bean.search.SearchResponse searchOnLatestVersions(final String query, 
			final int offset, final int size) {
		return this.search(this.getLatestVersionsIndexName(), query, null, offset, size,
				r -> r.getId(), r -> Long.parseLong((String) r.getSourceAsMap().get(internalVersionPropertyName)),
				r -> (String) r.getSourceAsMap().get(internalTypeName));
	}

	/**
	 * Perform search by type on all entities versions. Results are sorted by score.
	 * 
	 * @param query
	 * @param entityType
	 * @param offset
	 * @param size
	 * @return
	 */
	public it.regioneveneto.myp3.mystd.bean.search.SearchResponse searchOnAllVersions(final String query, 
			final String entityType, final int offset, final int size) {
		return this.search(this.getAllVersionsIndexName(), query, entityType, offset, size,
				r -> r.getId().substring(0, r.getId().indexOf("$$")), 
				r -> Long.parseLong(r.getId().substring(r.getId().indexOf("$$") + 2)), r -> (String) r.getSourceAsMap().get(internalTypeName));
	}

	/**
	 * Perform search on all entities versions. Results are sorted by score.
	 * 
	 * @param query
	 * @param offset
	 * @param size
	 * @return
	 */
	public it.regioneveneto.myp3.mystd.bean.search.SearchResponse searchOnAllVersions(final String query, 
			final int offset, final int size) {
		return this.search(this.getAllVersionsIndexName(), query, null, offset, size,
				r -> r.getId().substring(0, r.getId().indexOf("$$")), 
				r -> Long.parseLong(r.getId().substring(r.getId().indexOf("$$") + 2)), r -> (String) r.getSourceAsMap().get(internalTypeName));
	}

	private void indexEntity(final String index, final String id, 
			final Long version, final String entityType, final String domain, final MyStandardEntity entity) {
		this.executeOperation((client, restClient) -> {
			final Map<String, Object> entityData = new HashMap<String, Object>() {{
				// Keep an internal id in order to make multiple documents query easier to perform
				this.put(internalIdPropertyName, id);
				this.put(internalVersionPropertyName, version == null ? null : String.valueOf(version));
				this.put(internalDomainPropertyName, domain);
				this.put(internalTypeName, entityType);
			}};
			
			// Map entity data to document. Mapping just entity searchable properties
			entity.getDataProperty().entrySet().forEach(e -> {
				entityData.put(String.format("dataProperty.%s", e.getKey()), e.getValue());
			});
			
			// Perform indexing operation
			IndexRequest request = new IndexRequest(index).id(id)
					.source(entityData);
			
			client.index(request, RequestOptions.DEFAULT);
			
			return null;
		});
	}

	private void updateEntity(final String index,final String id, final MyStandardEntity entity) {
		this.executeOperation((client, restClient) -> {
			final Map<String, Object> entityData = new HashMap<>();
			
			// Map entity data to document. Mapping just entity searchable properties
			entity.getDataProperty().entrySet().forEach(e -> {
				entityData.put(String.format("dataProperty.%s", e.getKey()), e.getValue());
			});
			
			// Perform indexing operation
			UpdateRequest request = new UpdateRequest(index, id)
					.doc(entityData);
			
			client.update(request, RequestOptions.DEFAULT);
			
			return null;
		});
	}

	private void removeEntity(final String index, final String id) {
		this.executeOperation((client, restClient) -> {
			DeleteRequest request = new DeleteRequest(index, id);

			client.delete(request, RequestOptions.DEFAULT);
			
			return null;
		});
	}

	private GetResponse getEntity(final String index, final String id) {
		return this.executeOperation((client, restClient) -> {
			GetRequest request = new GetRequest(index)
					.id(id);

			return client.get(request, RequestOptions.DEFAULT);
		});
	}
	
	private void refreshIndex(final String name) {
		this.executeOperation((client, restClient) -> {
			// Check if index exists
			Response response = null; 
			
			try {
				response = restClient.performRequest(new Request("POST", String.format("/%s/_refresh", name)));				
			} catch (ResponseException e) {
				response = e.getResponse();
			}
			
			if (response.getStatusLine().getStatusCode() != 200)
				throw new RuntimeException(String.format("Error while refreshing index. Status code %s", 
						response.getStatusLine().getStatusCode()));
			 			
			return null;
		});
	}	
	
	private void dropIndex(final String name) {
		try {
			this.executeOperation((client, restClient) -> {
				restClient.performRequest(new Request("DELETE", String.format("/%s", name)));

				return null;
			});
		} catch (Exception e) {//Errore nel drop dell'indice non bloccante
			LOG.info("MyStandard - Errore nell'operazione di drop dell'indice {}", name, e);
		}
	} 

	private void createIndex(final String name) {
		this.executeOperation((client, restClient) -> {
			// Check if index exists
			Response response = null; 
			
			try {
				response = restClient.performRequest(new Request("HEAD", String.format("/%s", name)));				
			} catch (ResponseException e) {
				response = e.getResponse();
			}
			
			if (response.getStatusLine().getStatusCode() == 200)
				return null;
			 
			// Create index
			File mappingFile = new File(this.entitiesMappingConfigurationAbsolutePathappingConfigurationAbsolutePath);
			
			String definition = mappingFile.exists() 
				? FileUtils.readFileToString(mappingFile)
				: org.apache.commons.io.IOUtils.toString(this.getClass()
						.getResourceAsStream("/index_definition.json"));

			Request request = new Request("PUT", String.format("/%s", name));
			
			request.setEntity(new NStringEntity(definition, ContentType.APPLICATION_JSON));
			restClient.performRequest(request);
			
			return null;
		});
	}

	private it.regioneveneto.myp3.mystd.bean.search.SearchResponse search(final String index, final String query, 
			final String entityType, final int offset, final int size,
			final Transformer<String> idTransformer, final Transformer<Long> versionTransformer, final Transformer<String> typeTransformer) {
		return this.executeOperation((client, restClient) -> {
			// Create search query
			Request request = new Request("POST", 
					String.format("/%s/_search", index));

			request.addParameter("from", String.valueOf(offset));
			request.addParameter("size", String.valueOf(size));

			String queryBuilder = StringUtils.hasText(entityType) ? buildQueryWithTypeFilter(query, entityType) : buildQuery(query);

			request.setEntity(new NStringEntity(queryBuilder, ContentType.APPLICATION_JSON));

			// Perform search operation
			Response response = restClient.performRequest(request);				

			if (response.getStatusLine().getStatusCode() != 200)
				throw new RuntimeException(String.format("Error while querying index. Status code %s", 
						response.getStatusLine().getStatusCode()));
			
			// Deserialize response
			NamedXContentRegistry registry = new NamedXContentRegistry(getDefaultNamedXContents());
    	    XContentParser parser = JsonXContent.jsonXContent.createParser(registry, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, 
    	    		EntityUtils.toString(response.getEntity()));
		    SearchResponse result = SearchResponse.fromXContent(parser);

			it.regioneveneto.myp3.mystd.bean.search.SearchResponse searchResponse = new it.regioneveneto.myp3.mystd.bean.search.SearchResponse() {{
				this.setTotaltems(result.getHits().getTotalHits().value);
				this.setResults(Arrays.asList(result.getHits().getHits())
						.stream()
						.map(r -> new SearchResult() {{
							this.setId(idTransformer.transform(r));
							this.setVersion(versionTransformer.transform(r));
							this.setEntityType(typeTransformer.transform(r));
							this.setDataProperties(r.getSourceAsMap());
						}})
						.collect(Collectors.toList()));
			}};
			return searchResponse;
		});		
	}
	
	private SearchResponse getAllVersionedEntities(final String id, final String entityType) {
		return this.executeOperation((client, restClient) -> {
			Request request = new Request("POST", 
					String.format("/%s/%s/_search", this.getAllVersionsIndexName(), entityType));

			request.setEntity(new NStringEntity(buildInternalIdPrefixQuery(String.format("%s$$", id)), 
					ContentType.APPLICATION_JSON));

			// Perform search operation
			Response response = restClient.performRequest(request);				

			if (response.getStatusLine().getStatusCode() != 200)
				throw new RuntimeException(String.format("Error while querying index. Status code %s", 
						response.getStatusLine().getStatusCode()));
			
			// Deserialize response
			NamedXContentRegistry registry = new NamedXContentRegistry(getDefaultNamedXContents());
    	    XContentParser parser = JsonXContent.jsonXContent.createParser(registry, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, 
    	    		EntityUtils.toString(response.getEntity()));
    	    
		    return SearchResponse.fromXContent(parser);
		});		
	}

	private String buildQuery(final String value) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		
		builder.startObject();
		builder.startObject("query");
		builder.startObject("multi_match");

		builder.field("query", value);
		builder.array("fields", new String[] { "dataProperty.*" });
		builder.field("fuzziness", "AUTO");
		builder.field("prefix_length", 2);
		
		builder.endObject();
		builder.endObject();
		builder.endObject();
	    
		return Strings.toString(builder);
	}

	private String buildQueryWithTypeFilter(final String queryValue, final String entityType) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();


		builder.startObject();
		{
			builder.startObject("query");
			{
				builder.startObject("bool");
				{
					builder.startObject("must");
					{
						builder.startObject("multi_match");
						{
							builder.field("query", queryValue);
							builder.array("fields", new String[]{"dataProperty.*"});
							builder.field("fuzziness", "AUTO");
							builder.field("prefix_length", 2);
						}
						builder.endObject();
					}
					builder.endObject();


					builder.startObject("filter");
					{
						builder.startObject("match");
						{
							builder.field(internalTypeName, entityType);
						}
						builder.endObject();
					}
					builder.endObject();
				}
				builder.endObject();
			}
			builder.endObject();
		}
		builder.endObject();

		return Strings.toString(builder);
	}

	private String buildInternalIdPrefixQuery(final String value) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		
		builder.startObject();
		builder.startObject("query");
		builder.startObject("prefix");
		builder.startObject(internalIdPropertyName);

		builder.field("value", value);
		
		builder.endObject();
		builder.endObject();
		builder.endObject();
		builder.endObject();
	    
		return Strings.toString(builder);
	}
	
	private List<NamedXContentRegistry.Entry> getDefaultNamedXContents() {
	    Map<String, ContextParser<Object, ? extends Aggregation>> map = new HashMap<>();
	    
	    map.put(TopHitsAggregationBuilder.NAME, (p, c) -> ParsedTopHits.fromXContent(p, (String) c));
	    map.put(StringTerms.NAME, (p, c) -> ParsedStringTerms.fromXContent(p, (String) c));
	    
	    List<NamedXContentRegistry.Entry> entries = map.entrySet().stream()
	            .map(entry -> new NamedXContentRegistry.Entry(Aggregation.class, new ParseField(entry.getKey()), entry.getValue()))
	            .collect(Collectors.toList());
	    
	    return entries;
	}
	
	private String getLatestVersionsIndexName() {
		return String.format("%s_latestversions", this.indexName);
	}

	private String getAllVersionsIndexName() {
		return String.format("%s_allversions", this.indexName);
	}
	
	private <T> T executeOperation(final SearchOperation<T> operation) {
		final String[] hostsConfiguration = this.hosts.split(",");

		final List<HttpHost> hosts = Arrays.asList(hostsConfiguration).stream().map(e -> {
			String[] configuration = e.split(":");

			return new HttpHost(configuration[0], Integer.parseInt(configuration[1]), configuration[2]);
		}).collect(Collectors.toList());


		T result;

		try {
			final RestClientBuilder restClient = getRestClientBuilder(hosts.get(0));

			try (RestHighLevelClient client = new RestHighLevelClient(restClient)) {
				result = operation.perform(client, client.getLowLevelClient());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * Set proxy and proxy auth if present, and optional basic auth for host
	 * @param httpHost, host info
	 * @return clientBuilder with optionally proxy and basic auth
	 * @throws URISyntaxException
	 */
	private RestClientBuilder getRestClientBuilder(HttpHost httpHost) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {


		SSLFactory sslFactory = SSLFactory.builder()
				.withTrustingAllCertificatesWithoutValidation()
				.withHostnameVerifier((host, session) -> true).build();

		final RestClientBuilder restClient = RestClient
				.builder(httpHost);

		ProxyModel proxyModel = new ProxyModel(httpHost.toURI());
		if (proxyModel != null) {

			restClient.setHttpClientConfigCallback(httpClientBuilder -> {

				LOG.debug("RestClientBuilder with proxy.");

				String proxyHost = proxyModel.getProxyHost();
				Integer proxyPort = proxyModel.getProxyPort();

				//Set proxy
				if (StringUtils.hasText(proxyHost) && proxyPort != null) {
					httpClientBuilder.setProxy(new HttpHost(proxyModel.getProxyHost(), proxyModel.getProxyPort()));

					String proxyUsername = proxyModel.getProxyUsername();
					String proxyPassword = proxyModel.getProxyPassword();

					//Set proxy auth by authscope
					if (StringUtils.hasText(proxyUsername) && StringUtils.hasText(proxyPassword)) {
						final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
						credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
								new UsernamePasswordCredentials(proxyUsername, proxyPassword));

						httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
					}

				}

				//Set basic auth by authScope
				if (StringUtils.hasText(hostsUser) && StringUtils.hasText(hostsPwd)) {
					final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
					credentialsProvider.setCredentials(new AuthScope(httpHost.getHostName(), httpHost.getPort()),
							new UsernamePasswordCredentials(hostsUser, hostsPwd));
					httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

				}

				httpClientBuilder.setSSLContext(sslFactory.getSslContext());
				httpClientBuilder.setSSLHostnameVerifier(sslFactory.getHostnameVerifier());


				return httpClientBuilder;

			});

		} else {
			LOG.debug("RestClientBuilder with no proxy.");
			restClient.setHttpClientConfigCallback(httpClientBuilder -> {

				httpClientBuilder.setSSLContext(sslFactory.getSslContext());
				httpClientBuilder.setSSLHostnameVerifier(sslFactory.getHostnameVerifier());


				return httpClientBuilder;

			});
		}

		return restClient;

	}

}
