package org.example.shopping._core.config;

import lombok.RequiredArgsConstructor;
import org.example.shopping._core.interceptor.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;
    private final SessionInterceptor sessionInterceptor;
    private final OwnerInterceptor ownerInterceptor;
    private final AdminInterceptor adminInterceptor;
    private final CategoryInterceptor categoryInterceptor;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(categoryInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**");

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/order/**", "/payment/**", "/review/**", "/user/**", "/cart/**", "/products/**", "/api/**")
                .excludePathPatterns(
                        "/user/login", "/user/join", "/user/email/**", "/user/join/**", "/user/logout", "/user/username-check", "/", "/css/**",
                        "/static/js/**", "/images/**", "/favicon.io", "/user/popup/**", "/h2-console/**", "/products/{productId}", "/products/{productId}/reviews", "/products/{productId}/reviews",
                        "/products/{productId}/statistics"
                );

        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/**");
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/products/**");

        registry.addInterceptor(ownerInterceptor)
                .addPathPatterns("/owner/**")
                .addPathPatterns("/chatRoom/delete")
                .excludePathPatterns("/owner/login", "/owner/join");
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 저장 경로(FileUtil)와 같은 설정값을 써야 저장한 이미지가 보인다
        String location = Paths.get(uploadPath).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/"; // 폴더가 아직 없으면 toUri() 가 끝에 / 를 붙이지 않음
        }
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
