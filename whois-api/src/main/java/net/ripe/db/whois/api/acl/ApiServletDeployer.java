package net.ripe.db.whois.api.acl;

import net.ripe.db.whois.api.DefaultExceptionMapper;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import net.ripe.db.whois.api.wsearch.LogSearchService;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Component
public class ApiServletDeployer implements ServletDeployer {
    private final ApiKeyFilter apiKeyFilter;
    private final AclBanService aclBanService;
    private final AclLimitService aclLimitService;
    private final AclMirrorService aclMirrorService;
    private final AclProxyService aclProxyService;
    private final LogSearchService logSearchService;
    private final DefaultExceptionMapper defaultExceptionMapper;

    @Autowired
    public ApiServletDeployer(final ApiKeyFilter apiKeyFilter, final AclBanService aclBanService, final AclLimitService aclLimitService, final AclMirrorService aclMirrorService, final AclProxyService aclProxyService, final LogSearchService logSearchService, final DefaultExceptionMapper defaultExceptionMapper) {
        this.apiKeyFilter = apiKeyFilter;
        this.aclBanService = aclBanService;
        this.aclLimitService = aclLimitService;
        this.aclMirrorService = aclMirrorService;
        this.aclProxyService = aclProxyService;
        this.logSearchService = logSearchService;
        this.defaultExceptionMapper = defaultExceptionMapper;
    }

    @Override
    public Audience getAudience() {
        return Audience.INTERNAL;
    }

    @Override
    public void deploy(final WebAppContext context) {
        context.addFilter(new FilterHolder(apiKeyFilter), "/api/*", EnumSet.of(DispatcherType.REQUEST));

        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(aclBanService);
        resourceConfig.register(aclLimitService);
        resourceConfig.register(aclMirrorService);
        resourceConfig.register(aclProxyService);
        resourceConfig.register(logSearchService);
        resourceConfig.register(defaultExceptionMapper);
        resourceConfig.register(new JacksonJaxbJsonProvider());

        context.addServlet(new ServletHolder("REST API", new ServletContainer(resourceConfig)), "/api/*");
    }
}
