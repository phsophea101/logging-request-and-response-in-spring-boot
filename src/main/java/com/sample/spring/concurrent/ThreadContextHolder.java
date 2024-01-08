package com.sample.spring.concurrent;

import com.sample.spring.util.ObjectUtils;
import com.sample.spring.util.TypeReference;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class ThreadContextHolder {

    private final ThreadLocal<ConcurrentHashMap<Object, Object>> bucket = InheritableThreadLocal.withInitial(ConcurrentHashMap::new);

    public void setObject(final String key, final Object value) {
        bucket.get().put(key, value);
    }

    public Object getObject(final String key) {
        return bucket.get().get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> optObject(String key) {
        return (Optional<T>) Optional.ofNullable(getObject(key));
    }

    /**
     * Should be called for the shared Thread Pool which is implemented in Tomcat, Jetty, Undertow, and etc.
     * <br/>
     * A thread from the pool is used to serve a request and returned back to pool once request is complete.
     */
    public void clear() {
        bucket.remove();
    }


    public Object remove(final String key) {
        return bucket.get().remove(key);
    }

    public <T> T getObject(final String key, final Class<T> _class) {
        return ObjectUtils.cast(getObject(key), _class);
    }

    public <T> T getObject(final String key, final TypeReference<T> valueTypeRef) {
        return ObjectUtils.cast(getObject(key), valueTypeRef);
    }

    public void setString(final String key, final String value) {
        setObject(key, value);
    }

    public String getString(final String key) {
        return getObject(key, String.class);
    }

    public void setDate(final String key, final Date value) {
        setObject(key, value);
    }

    public Date getDate(final String key) {
        return getObject(key, Date.class);
    }

    public void setInteger(final String key, final Integer value) {
        setObject(key, value);
    }

    public Integer getInteger(final String key) {
        return getObject(key, Integer.class);
    }

    public void setLong(final String key, final Long value) {
        setObject(key, value);
    }

    public Long getLong(final String key) {
        return getObject(key, Long.class);
    }

    public void setFloat(final String key, final Float value) {
        setObject(key, value);
    }

    public Float getFloat(final String key) {
        return getObject(key, Float.class);
    }

    public void setDouble(final String key, final Double value) {
        setObject(key, value);
    }

    public Double getDouble(final String key) {
        return getObject(key, Double.class);
    }

    public void setBigDecimal(final String key, final BigDecimal value) {
        setObject(key, value);
    }

    public BigDecimal getBigDecimal(final String key) {
        return getObject(key, BigDecimal.class);
    }

    public void setBoolean(final String key, final Boolean value) {
        setObject(key, value);
    }

    public Boolean getBoolean(final String key) {
        return getObject(key, Boolean.class);
    }
}
