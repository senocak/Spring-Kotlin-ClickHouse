package com.github.senocak.skch.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.skch.security.AuthorizationInterceptor
import com.github.senocak.skch.util.AppConstants.SECURITY_SCHEME_NAME
import com.github.senocak.skch.util.fromProperties
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.apache.catalina.connector.Connector
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableAsync
@Configuration
class AppConfig(
    private val authorizationInterceptor: AuthorizationInterceptor
): WebMvcConfigurer, WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    override fun customize(factory: TomcatServletWebServerFactory) {
        factory.addConnectorCustomizers(TomcatConnectorCustomizer { connector: Connector ->
            connector.setProperty("maxThreads", "2000")
            connector.setProperty("acceptorThreadCount", "1")
        })
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController("/", "/index.html")
        registry.addRedirectViewController("/swagger", "/swagger-ui/index.html")
    }

    /**
     * Marking the files as resource
     * @param registry -- Stores registrations of resource handlers for serving static resources
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
            .addResourceHandler("//**")
            .addResourceLocations("classpath:/static/")
    }

    /**
     * Add Spring MVC lifecycle interceptors for pre- and post-processing of controller method invocations
     * and resource handler requests.
     * @param registry -- List of mapped interceptors.
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(authorizationInterceptor)
            .addPathPatterns("/api/v1/**")
    }

    @Bean
    fun modelResolver(objectMapper: ObjectMapper): ModelResolver = ModelResolver(objectMapper)

    @Bean
    fun customOpenAPI(
        @Value("\${spring.application.name}") title: String,
        @Value("\${server.port}") port: String,
    ): OpenAPI {
        val securitySchemesItem: SecurityScheme = SecurityScheme()
            .name(SECURITY_SCHEME_NAME)
            .description("JWT auth description")
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .`in`(SecurityScheme.In.HEADER)
            .bearerFormat("JWT")
        val license: Info = Info().title(title).version("appVersion".fromProperties())
            .description(title)
            .termsOfService("https://github.com/senocak")
            .license(License().name("Apache 2.0").url("https://springdoc.org"))
        val server1: Server = Server().url("http://localhost:$port/").description("Local Server")
        return OpenAPI()
            .components(Components().addSecuritySchemes(SECURITY_SCHEME_NAME, securitySchemesItem))
            .info(license)
            .servers(listOf(element = server1))
    }

    @Bean
    fun accountApi(): GroupedOpenApi =
        GroupedOpenApi.builder().displayName("User operations").group("user").pathsToMatch("/api/v1/user/**").build()

    @Bean
    fun actuatorApi(): GroupedOpenApi =
        GroupedOpenApi.builder().displayName("Metric operations").group("actuator").pathsToMatch("/actuator/**").build()

    @Bean
    fun authApi(): GroupedOpenApi =
        GroupedOpenApi.builder().displayName("Auth operations").group("auth").pathsToMatch("/api/v1/auth/**").build()

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

    /**
     * We use the PasswordEncoder that is defined in the Spring Security configuration to encode the password.
     * @return -- singleton instance of PasswordEncoder
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
