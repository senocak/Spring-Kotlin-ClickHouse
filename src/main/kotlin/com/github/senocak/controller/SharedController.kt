package com.github.senocak.controller

import com.github.senocak.util.OmaErrorMessageType
import com.github.senocak.util.RoleName
import com.github.senocak.util.fromProperties
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@Tag(name = "Shared", description = "Shared API")
@RequestMapping(BaseController.V1_PUBLIC_URL)
class SharedController(
    private val environment: Environment
): BaseController() {

    @GetMapping
    fun ping(request: HttpServletRequest): Map<String, Any> =
        mapOf(
            "ip" to (request.getHeader("X-FORWARDED-FOR") ?: request.remoteAddr),
            "appVersion" to "appVersion".fromProperties(),
            "locale" to LocaleContextHolder.getLocale(),
            "activeProfiles" to environment.activeProfiles,
            "defaultProfiles" to environment.defaultProfiles
        )

    @GetMapping("/enums")
    @Operation(summary = "Enums Endpoint",
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = Map::class))]),
        ]
    )
    fun enums(): Map<String, Any> {
        val omaErrorMessageType: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
        for (it: OmaErrorMessageType in OmaErrorMessageType.entries)
            omaErrorMessageType[it.name] =  mutableMapOf(it.messageId to it.text)
        return mapOf(
            "RoleName" to RoleName.entries.map { "$it" },
            "OmaErrorMessageType" to omaErrorMessageType,
        )
    }
}