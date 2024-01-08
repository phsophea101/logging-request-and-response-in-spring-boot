package com.sample.spring.logging;

import com.sample.spring.util.ContextUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AntPathMatchUrl {

    private Set<String> setAntPathMatching = new HashSet<>();
    private boolean ignoreRequest = false;
    private boolean ignoreResponse = false;
    private String contextPath;

    public static final String[] DEFAULT_EXCLUDE_ANT_PATH_MATCH = {
            "/swagger-ui.html", "/v2/api-docs/**", "/swagger/**",
            "/webjars/**", "/v3/api-docs/**", "/swagger-resources/**",
            "/configuration/security/**", "/swagger-ui/index.html",
            "/configuration/ui/**", "/swagger-ui/**",
            "/css/**",
            "/js/**",
            "/image/**",
            "/scss/**",
            "/actuator/**"
    };

    public AntPathMatchUrl() {
        addMatching(DEFAULT_EXCLUDE_ANT_PATH_MATCH);
        contextPath = ContextUtil.getProperty("server.servlet.context-path");
    }

    public Set<String> getPatternMatching() {
        return setAntPathMatching;
    }

    void addMatching(List<String> matches) {
        List<String> list = matches.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        if (!list.isEmpty()) setAntPathMatching.addAll(matches);
    }

    public void addMatching(String... matches) {
        addMatching(Arrays.asList(matches));
    }

    public void setIgnoreRequest(boolean ignoreRequest) {
        this.ignoreRequest = ignoreRequest;
    }

    public void setIgnoreResponse(boolean ignoreResponse) {
        this.ignoreResponse = ignoreResponse;
    }

    public boolean isIgnoreRequest(String endPoint) {
        boolean match = isIgnoreMatching(endPoint);
        return ignoreRequest || match;
    }

    public boolean isIgnoreResponse(String endPoint) {
        boolean match = isIgnoreMatching(endPoint);
        return ignoreResponse || match;
    }

    private boolean isIgnoreMatching(String endpoint) {
        if (setAntPathMatching.isEmpty()) return false;
        for (String pattern : setAntPathMatching) {
            String path = StringUtils.isEmpty(contextPath) ? pattern : contextPath + pattern;
            if (LoggingProperties.ANT_PATH_MATCHER.match(path, endpoint))
                return true;
        }
        return false;
    }
}
