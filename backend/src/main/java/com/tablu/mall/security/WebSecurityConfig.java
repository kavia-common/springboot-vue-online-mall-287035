package com.tablu.mall.security;

import com.tablu.mall.enums.ResponseEnum;
import com.tablu.mall.pojo.User;
import com.tablu.mall.service.UserService;
import com.tablu.mall.utils.JsonUtil;
import com.tablu.mall.vo.ResponseVo;
import com.tablu.mall.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    // PUBLIC_INTERFACE
    /**
     * Password encoder bean used by Spring Security for hashing user passwords.
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    UserService userService;

    @Autowired
    UrlFilterInvocationSecurityMetadataSource urlFilterInvocationSecurityMetadataSource;

    @Autowired
    UrlAccessDecisionManager urlAccessDecisionManager;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**", "/index.html", "/imgs/**", "/fonts/**", "/favicon.ico", "/verifyCode");
    }

    // PUBLIC_INTERFACE
    /**
     * Login filter bean to handle authentication success and failure with JSON responses.
     * This filter processes POST /user/login requests.
     * @return configured LoginFilter
     * @throws Exception when AuthenticationManager cannot be created
     */
    @Bean
    LoginFilter loginFilter() throws Exception {
        LoginFilter loginFilter = new LoginFilter();
        loginFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            User user = (User) authentication.getPrincipal();
            user.setPassword(null);
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            ResponseVo<UserVo> responseVo = ResponseVo.success(userVo);
            JsonUtil.jsonOutput(responseVo, response);
        });
        loginFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            ResponseVo responseVo = ResponseVo.error(ResponseEnum.ERROR, exception.getMessage());
            if (exception instanceof DisabledException) {
                responseVo.setMsg("账户被禁用，请联系管理员!");
            } else if (exception instanceof BadCredentialsException) {
                responseVo = ResponseVo.error(ResponseEnum.USERNAME_OR_PASSWORD_ERROR);
            } else if (responseVo.getMsg().equals(ResponseEnum.VERIFY_CODE_ERROR.getDesc())) {
                responseVo.setStatus(ResponseEnum.VERIFY_CODE_ERROR.getCode());
            }
            JsonUtil.jsonOutput(responseVo, response);
        });
        loginFilter.setAuthenticationManager(authenticationManagerBean());
        loginFilter.setFilterProcessesUrl("/user/login");
        return loginFilter;
    }

    // PUBLIC_INTERFACE
    /**
     * CORS configuration to allow the Vue dev server (and other origins) to call the backend API.
     * Allows all origins, headers, and methods for development. Credentials are disabled.
     * @return CorsConfigurationSource for Spring Security HTTP CORS support
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Use allowedOriginPatterns to support wildcards on Spring Security 5.7 (Boot 2.7)
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply CORS config to all endpoints
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class);

        // Enable CORS so that the frontend (typically running on port 3000) can access the backend
        http.cors();

        http.authorizeRequests()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                        object.setSecurityMetadataSource(urlFilterInvocationSecurityMetadataSource);
                        object.setAccessDecisionManager(urlAccessDecisionManager);
                        return object;
                    }
                })
                .and()
                .rememberMe()
                .and()
                .logout()
                .logoutUrl("/user/logout")
                .logoutSuccessHandler((request, response, authentication)
                        -> JsonUtil.jsonOutput(ResponseVo.successByMsg("退出成功!"), response))
                .permitAll()
                .and()
                .csrf().disable().exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException)
                        -> JsonUtil.jsonOutput(ResponseVo.error(ResponseEnum.AUTHORITY_ERROR), response))
                //没有认证
                .authenticationEntryPoint((request, response, authException)
                        -> JsonUtil.jsonOutput(ResponseVo.error(ResponseEnum.NEED_LOGIN), response));
    }

}
