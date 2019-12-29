package com.mcu.configuration

import com.mcu.handler.WebInterceptor
import nz.net.ultraq.thymeleaf.LayoutDialect
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect
import org.thymeleaf.spring5.SpringTemplateEngine
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.spring5.view.ThymeleafViewResolver
import org.thymeleaf.templatemode.TemplateMode

@Configuration
class WebMvcConfig : WebMvcConfigurer, ApplicationContextAware {

    lateinit var appContext : ApplicationContext

    @Value("\${spring.thymeleaf.prefix}")
    lateinit var prefix : String

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(WebInterceptor())
                .addPathPatterns("/*")
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/").setViewName("forward:/user/login")
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/css/**","/js/**").addResourceLocations("classpath:/static/css/","classpath:/static/js/")
    }

    @Bean
    fun templateResolver() : SpringResourceTemplateResolver {
        val templateResolver = SpringResourceTemplateResolver()
        templateResolver.setApplicationContext(this.appContext)
        templateResolver.prefix = this.prefix
        templateResolver.suffix = ".html"
        templateResolver.templateMode = TemplateMode.HTML
        templateResolver.characterEncoding = "UTF-8"
        templateResolver.isCacheable = false
        return templateResolver
    }

    /**
     * Spring Security 가 Thymeleaf 에서 적용이 안되 bean 생성
     */
    @Bean
    fun templateEngine() : SpringTemplateEngine {
        val templateEngine = SpringTemplateEngine()
        templateEngine.enableSpringELCompiler = false
        templateEngine.setTemplateResolver(this.templateResolver())
        templateEngine.addDialect(SpringSecurityDialect())
        templateEngine.addDialect(LayoutDialect())
        return templateEngine
    }

    @Bean
    fun viewResolver() : ThymeleafViewResolver {
        val viewResolver = ThymeleafViewResolver()
        viewResolver.templateEngine = this.templateEngine()
        return viewResolver
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.appContext = applicationContext
    }
}
