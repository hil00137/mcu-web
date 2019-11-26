package com.mcu.configuration

import com.mcu.handler.AuthFailureHandler
import com.mcu.handler.AuthProvider
import com.mcu.handler.AuthSuccessHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher


@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var authFailureHandler : AuthFailureHandler

    @Autowired
    lateinit var authSuccessHandler: AuthSuccessHandler

    @Autowired
    lateinit var authProvider : AuthProvider

    override fun configure(web: WebSecurity?) {
        web?:return
        web.ignoring().antMatchers("/join")
    }

    override fun configure(http: HttpSecurity?) {
        http?:return
        http.authorizeRequests()
                .antMatchers("/","/user/login","/error**","/css/**","/js/**").permitAll()
                .antMatchers("/**").access("ROLE_USER")
                .antMatchers("/**").access("ROLE_ADMIN")
                .antMatchers("/**").authenticated()
                .and()
                .formLogin()
                .loginPage("/user/login")
                .defaultSuccessUrl("/home",true)
                .failureHandler(authFailureHandler)
                .successHandler(authSuccessHandler)
                .usernameParameter("userId")
                .passwordParameter("password")
                .and()
                .logout().logoutRequestMatcher(AntPathRequestMatcher("/user/logout"))
                .logoutSuccessUrl("/user/login")
                .invalidateHttpSession(true)
                .and()
                .csrf()
                .and()
                .authenticationProvider(authProvider)
    }

}