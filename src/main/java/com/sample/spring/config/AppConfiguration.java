package com.sample.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.sample.spring.jackson.I18NModule;
import com.sample.spring.logging.LoggingInterceptor;
import com.sample.spring.service.impl.I18nServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;

@Configuration
public class AppConfiguration {
    @Bean
    public ObjectMapper mapper(I18nServiceImpl provider) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.failOnUnknownProperties(false);
        ObjectMapper mapper = builder.build();
        SerializerProvider serializerProvider = mapper.getSerializerProvider();
        if (ObjectUtils.isNotEmpty(serializerProvider))
            mapper.setSerializerProvider(new CustomDefaultSerializerProvider());
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        mapper.registerModule(new I18NModule(provider));
        return mapper;
    }

    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        jackson2HttpMessageConverter.setObjectMapper(objectMapper);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().removeIf(m -> m.getClass().equals(MappingJackson2HttpMessageConverter.class));
        restTemplate.getMessageConverters().add(jackson2HttpMessageConverter);
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(new LoggingInterceptor());
        return restTemplate;
    }

    @Bean
    public I18NModule i18NModule(I18nServiceImpl provider) {
        return new I18NModule(provider);
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new SmartLocaleResolver();
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        /* /src/main/resources/i18ns */
        source.setBasename("i18ns/messages");
        source.setUseCodeAsDefaultMessage(true);
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}
