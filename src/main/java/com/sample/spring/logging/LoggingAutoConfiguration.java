package com.sample.spring.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.Arrays;

@Configuration
@ConditionalOnClass(value = {LoggingFilter.class})
@EnableConfigurationProperties(value = {LoggingProperties.class})
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "custom.logging", name = {"enable"}, havingValue = "true")
    @ConditionalOnMissingBean(LoggingFilter.class)
    public LoggingFilter customLoggingFilter(LoggingProperties prop) {
        LoggingFilter filter = new LoggingFilter();
        filter.maxRequest(prop.getMaxRequest());
        filter.maxResponse(prop.getMaxResponse());
        filter.ignoreRequest(prop.isIgnoreRequest());
        filter.ignoreResponse(prop.isIgnoreResponse());
        filter.setOrder(prop.getOrder());
        filter.includeRequestHeader(prop.isIncludeRequestHeader());
        filter.includeRequestBody(prop.isIncludeRequestBody());
        filter.includeResponseHeader(prop.isIncludeResponseHeader());
        filter.includeResponseBody(prop.isIncludeResponseBody());
        filter.ignoreAntPathMatch(prop.getIgnoreAntMatches().split(","));
        filter.setTraceIdLength(prop.getTraceIdLength());
        filter.setExcludeRequestHeader(Arrays.asList(prop.getExcludeRequestHeader().split(",")));
        filter.setExcludeResponseHeader(Arrays.asList(prop.getExcludeResponseHeader().split(",")));
        return filter;
    }

    @Lazy
    @Bean
    @ConditionalOnProperty(name = "custom.logging.enable", havingValue = "true")
    @ConditionalOnMissingBean(CommonsRequestLoggingFilter.class)
    public CommonsRequestLoggingFilter commonsRequestLoggingFilter() {
        CommonsRequestLoggingFilter filter  = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }
}
