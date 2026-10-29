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
 * ============================================================================
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("app_lang");
        resolver.setDefaultLocale(new Locale("vi"));
        resolver.setCookieMaxAge(Duration.ofDays(30));
        resolver.setCookiePath("/");
        return resolver;
    }

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
     * Cấu hình định tuyến tài nguyên tĩnh tải lên (Upload) phục vụ Avatar, Ký gửi, Sách, Chi nhánh
     * Ưu tiên đọc trực tiếp từ thư mục vật lý trên ổ đĩa trước khi đọc trong JAR (classpath)
     * để ảnh vừa tải lên hiển thị ngay lập tức mà không cần khởi động lại Server.
     */
    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        String userHome = System.getProperty("user.home");
        java.nio.file.Path uploadDir = java.nio.file.Paths.get(userHome, "upload");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath + "/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "file:src/main/resources/static/uploads/",
                        "file:target/classes/static/uploads/",
                        "classpath:/static/uploads/"
                );

        registry.addResourceHandler("/images/**")
                .addResourceLocations(
                        "file:src/main/resources/static/images/",
                        "file:target/classes/static/images/",
                        "classpath:/static/images/"
                );
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
}
