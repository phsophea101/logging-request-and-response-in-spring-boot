package com.sample.spring.config;

import com.sample.spring.conts.BizErrorCode;
import com.sample.spring.exception.BizException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SmartLocaleResolver extends AcceptHeaderLocaleResolver {
    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(new Locale("km"), new Locale("en"), new Locale("kr"));
    private static final Locale DEFAULT_LOCALE = new Locale("en");
    private static final String ACCEPT_LANGUAGE = "Accept-Language";

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String headerLocale = request.getHeader(ACCEPT_LANGUAGE);
        if (StringUtils.isBlank(headerLocale)) {
            return DEFAULT_LOCALE;
        }
        this.setSupportedLocales(SUPPORTED_LOCALES);
        List<Locale.LanguageRange> list = Locale.LanguageRange.parse(headerLocale);
        Locale locale = Locale.lookup(list, getSupportedLocales());
        if (ObjectUtils.isEmpty(locale))
            return DEFAULT_LOCALE;
        return locale;
    }

    @SneakyThrows
    public static void validateLocale(HttpServletRequest request) {
        String headerLocale = request.getHeader(ACCEPT_LANGUAGE);
        if (ObjectUtils.isEmpty(headerLocale)) {
            return;
        }
        List<Locale.LanguageRange> list = Locale.LanguageRange.parse(headerLocale);
        Locale locale = Locale.lookup(list, SUPPORTED_LOCALES);
        if (ObjectUtils.isEmpty(locale))
            throw new BizException(BizErrorCode.E0001, String.format(BizErrorCode.E0001.getDescription(), list.get(0)));
    }

}