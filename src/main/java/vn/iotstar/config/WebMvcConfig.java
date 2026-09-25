package vn.iotstar.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

/**
 * ============================================================================
 * CẤU HÌNH ĐA NGÔN NGỮ (INTERNATIONALIZATION - I18N) & WEB MVC
 * Phụ trách: Phúc (24162096) - Nhánh: feature/client-phuc
 * Đối tác kiểm tra: Cường (24162013) - Nhánh: feature/admin-cuong
 *
 * Ý nghĩa nghiệp vụ:
 * 1. Hỗ trợ chuyển đổi song ngữ: Tiếng Việt (vi) và Tiếng Anh (en) chuẩn sàn Shopee.
 * 2. Lưu trữ ngôn ngữ đã chọn vào Cookie 'app_lang' trong 30 ngày để khách quay lại
 *    vẫn giữ nguyên ngôn ngữ đã chọn.
 * 3. Cho phép đổi ngôn ngữ linh hoạt qua tham số URL: ?lang=vi hoặc ?lang=en.
 * 4. Tải các tệp từ điển i18n/messages_vi.properties và i18n/messages_en.properties.
 * ============================================================================
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Cấu hình CookieLocaleResolver lưu ngôn ngữ vào Cookie trình duyệt
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("app_lang");
        resolver.setDefaultLocale(new Locale("vi"));
        resolver.setCookieMaxAge(Duration.ofDays(30));
        resolver.setCookiePath("/");
        return resolver;
    }

    /**
     * Interceptor bắt tham số URL ?lang=vi hoặc ?lang=en để đổi ngôn ngữ tức thì
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Nạp nguồn tài nguyên đa ngôn ngữ từ thư mục i18n/messages
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
}
