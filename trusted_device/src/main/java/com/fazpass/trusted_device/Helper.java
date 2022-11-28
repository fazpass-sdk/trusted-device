package com.fazpass.trusted_device;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.sentry.Sentry;

class Helper {

    static void sentryMessage(String method, Object o){
        Sentry.captureMessage(method +"->"+ReflectionToStringBuilder.toString(o, ToStringStyle.JSON_STYLE));

    }

}
