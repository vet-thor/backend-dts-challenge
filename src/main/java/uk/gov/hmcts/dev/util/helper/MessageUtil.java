package uk.gov.hmcts.dev.util.helper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static java.util.Objects.nonNull;

@Slf4j
@Component
@RequiredArgsConstructor
class MessageUtil {
    private final MessageSource messageSource;
    private final HttpServletRequest request;

    public String message(@NonNull String code){
        return messageSource.getMessage(code, null, getLocale());
    }

    public String message(@NonNull String code, Locale locale){
        return messageSource.getMessage(code, null, locale);
    }

    private Locale getLocale(){
        var localeParam = request.getParameter("locale");

        if(nonNull(localeParam)){
            try {
                return Locale.forLanguageTag(localeParam);
            }catch (Exception ignored){}
        }

        return Locale.getDefault();
    }
}
