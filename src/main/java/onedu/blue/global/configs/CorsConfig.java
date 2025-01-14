package onedu.blue.global.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Cross Origin Resource Sharing 정책
 *
 * 다른 출처간의 자원의 공유 정책
 */
@Configuration
public class CorsConfig {

//    @Value("${cors.allowed}")
//    private List<String> allowedOrigin;

    @Bean
    public CorsFilter corsFilter() {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        // if (allowedOrigin == null || allowedOrigin.isEmpty()) {

            config.addAllowedOrigin("*");

//        } else {
//
//            config.setAllowedOrigins(allowedOrigin);
//
//            // Origin 설정 방식 true 설정 필수
//            config.setAllowCredentials(true);
//        }

        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}