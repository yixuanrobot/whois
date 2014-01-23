package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectClientMapper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Component
public class RestClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);

    private Client client;
    private String restApiUrl;
    private String sourceName;
    private WhoisObjectClientMapper whoisObjectClientMapper;

    public RestClient() {
        this.client = createClient();
    }

    public RestClient(String restApiUrl, String sourceName) {
        this();
        setRestApiUrl(restApiUrl);
        setSource(sourceName);
    }

    @Value("${api.rest.baseurl}")
    public void setRestApiUrl(final String restApiUrl) {
        this.restApiUrl = restApiUrl;
        this.whoisObjectClientMapper = new WhoisObjectClientMapper(restApiUrl);
    }

    @Value("${whois.source}")
    public void setSource(final String sourceName) {
        this.sourceName = sourceName;
    }

    public void setClient(final Client client) {
        this.client = client;
    }


    public RpslObject create(final RpslObject rpslObject, final String... passwords) {
        return create(rpslObject, null, passwords);
    }

    public RpslObject create(final RpslObject rpslObject, final NotifierCallback notifier, final String... passwords) {
            try {
            final WhoisResources whoisResources = client.target(String.format("%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    joinQueryParams(createQueryParams("password", passwords))
            )).request().post(
                    Entity.entity(whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML),
                    WhoisResources.class
            );

            if (notifier!=null){
                notifier.notify(whoisResources.getErrorMessages());
            }
            return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject createOverride(final RpslObject rpslObject, final String override) {
        return createOverride(rpslObject, null, override);
    }

    public RpslObject createOverride(final RpslObject rpslObject, final NotifierCallback notifier, final String override) {
        try {
            final WhoisResources whoisResources = client.target(String.format("%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    joinQueryParams(createQueryParams("override", override))
            )).request().post(
                    Entity.entity(whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML),
                    WhoisResources.class
            );

            if (notifier!=null){
                notifier.notify(whoisResources.getErrorMessages());
            }
            return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject update(final RpslObject rpslObject, final String... passwords) {
        try {
            WhoisResources entity = whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject));
            final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString(),
                    joinQueryParams(createQueryParams("password", passwords))
            )).request().put(Entity.entity(entity, MediaType.APPLICATION_XML), WhoisResources.class);
            return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject updateOverride(final RpslObject rpslObject, final String override) {
        try {
            final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString(),
                    joinQueryParams(createQueryParams("override", override))
            )).request().put(Entity.entity(whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);
            return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public void delete(final RpslObject rpslObject, final String reason, final String... passwords) {
        try {
            client.target(String.format("%s/%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString(),
                    joinQueryParams(
                            createQueryParams("password", passwords),
                            createQueryParams("reason", encode(reason)))))
                    .request().delete(String.class);
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    private static String encode(final String value) {
        try {
            return value != null ? URLEncoder.encode(value, Charsets.ISO_8859_1.name()) : "";
        } catch (UnsupportedEncodingException ignored) {
            return "";
        }
    }

    public void deleteOverride(final RpslObject rpslObject, final String override) {
        try {
            client.target(String.format("%s/%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString(),
                    joinQueryParams(createQueryParams("override", override))
            )).request().delete(String.class);
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject lookup(final ObjectType objectType, final String pkey, final String... passwords) {
        try {
            final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    objectType.getName(),
                    pkey,
                    joinQueryParams(
                            createQueryParams("password", passwords),
                            "unfiltered")
            )).request().get(WhoisResources.class);
            return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public AbuseContact lookupAbuseContact(final String resource) {
        try {
            AbuseResources abuseResources = client.target(String.format("%s/abuse-contact/%s",
                    restApiUrl,
                    resource)
            ).request().get(AbuseResources.class);
            return abuseResources.getAbuseContact();
        } catch (ClientErrorException e) {
            throw createExceptionFromMessage(e);
        }
    }

    // TODO: [AH] make this streaming; result can be gigantic
    public Iterable<RpslObject> search(String searchKey,
                                       Set<String> sources,
                                       Set<AttributeType> inverseAttributes,
                                       Set<String> includeTags,
                                       Set<String> excludeTags,
                                       Set<ObjectType> types,
                                       Set<QueryFlag> flags) {
        try {
            final String uri = String.format(
                    "%s/search%s",
                    restApiUrl,
                    joinQueryParams(
                            createQueryParams("query-string", RestClientUtils.encode(searchKey)),
                            createQueryParams("source", sources),
                            createQueryParams("inverse-attribute", Iterables.transform(inverseAttributes, new Function<AttributeType, String>() {
                                public String apply(final AttributeType input) {
                                    return input.getName();
                                }
                            })),
                            createQueryParams("include-tag", includeTags),
                            createQueryParams("exclude-tag", excludeTags),
                            createQueryParams("type-filter", Iterables.transform(types, new Function<ObjectType, String>() {
                                        public String apply(final ObjectType input) {
                                            return input.getName();
                                        }
                                    })),
                            createQueryParams("flags", Iterables.transform(flags, new Function<QueryFlag, String>() {
                                        public String apply(final QueryFlag input) {
                                            return input.getName();
                                        }
                                    }))
                    ));

            final WhoisResources whoisResources = client.target(uri).request().get(WhoisResources.class);
            return whoisObjectClientMapper.mapWhoisObjects(whoisResources.getWhoisObjects());
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    private static Client createClient() {
        final JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .register(jsonProvider)
                .build();
    }

    private static String joinQueryParams(final String... queryParams) {
        final StringBuilder result = new StringBuilder();
        for (String queryParam : queryParams) {
            if (!StringUtils.isBlank(queryParam)) {
                if (result.length() == 0) {
                    result.append('?').append(queryParam);
                } else {
                    result.append('&').append(queryParam);
                }
            }
        }
        return result.toString();
    }

    private static String createQueryParams(final String key, final String... values) {
        return createQueryParams(key, Arrays.asList(values));
    }

    private static String createQueryParams(final String key, final Iterable<String> values) {
        final StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (!StringUtils.isBlank(value)) {
                if (result.length() > 0) {
                    result.append('&');
                }
                result.append(key).append('=').append(encode(value));
            }
        }
        return result.toString();
    }

    private static RuntimeException createException(final ClientErrorException e) {
        try {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            return new RestClientException(whoisResources.getErrorMessages());
        } catch (ProcessingException | IllegalStateException e1) {
            return createExceptionFromMessage(e);
        }
    }

    private static RuntimeException createExceptionFromMessage(final ClientErrorException e) {
        String message;
        try {
            message = e.getResponse().readEntity(String.class);
        } catch (IllegalStateException e1) {
            // stream has already been closed
            message = e.getMessage();
        }

        final ErrorMessage errorMessage = new ErrorMessage(new Message(Messages.Type.ERROR, message));
        return new RestClientException(Collections.singletonList(errorMessage));
    }
}
