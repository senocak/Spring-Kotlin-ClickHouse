package com.github.senocak.skch.controller

import com.github.senocak.skch.domain.User
import com.github.senocak.skch.domain.dto.UpdateUserDto
import com.github.senocak.skch.domain.dto.UserResponse
import com.github.senocak.skch.domain.dto.UserWrapperResponse
import com.github.senocak.skch.exception.ServerException
import com.github.senocak.skch.security.Authorize
import com.github.senocak.skch.service.UserService
import com.github.senocak.skch.util.AppConstants.ADMIN
import com.github.senocak.skch.util.AppConstants.SECURITY_SCHEME_NAME
import com.github.senocak.skch.util.AppConstants.USER
import com.github.senocak.skch.util.OmaErrorMessageType
import com.github.senocak.skch.util.convertEntityToDto
import com.github.senocak.skch.util.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@Authorize
@RequestMapping(BaseController.V1_USER_URL)
@Tag(name = "User", description = "User Controller")
class UserController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
): BaseController() {
    private val log: Logger by logger()

    @Throws(ServerException::class)
    @Operation(summary = "Get me", tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = UserWrapperResponse::class)))),
        ],
        security = [SecurityRequirement(name = SECURITY_SCHEME_NAME, scopes = [ADMIN, USER])]
    )
    @GetMapping("/me")
    fun me(): UserResponse =
        userService.loggedInUser().convertEntityToDto()

    @PatchMapping("/me")
    @Operation(summary = "Update user by username", tags = ["User"],
        responses = [
            ApiResponse(responseCode = "200", description = "successful operation",
                content = arrayOf(Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = HashMap::class)))),
        ],
        security = [SecurityRequirement(name = SECURITY_SCHEME_NAME, scopes = [ADMIN, USER])]
    )
    @Throws(ServerException::class)
    fun patchMe(request: HttpServletRequest,
                @Parameter(description = "Request body to update", required = true) @Validated @RequestBody userDto: UpdateUserDto,
                resultOfValidation: BindingResult
    ): UserResponse {
        validate(resultOfValidation = resultOfValidation)
        val user: User = userService.loggedInUser()
        val name: String? = userDto.name
        if (!name.isNullOrEmpty())
            user.name = name
        val password: String? = userDto.password
        val passwordConfirmation: String? = userDto.passwordConfirmation
        if (!password.isNullOrEmpty()) {
            if (passwordConfirmation.isNullOrEmpty()) {
                "password_confirmation_not_provided"
                    .apply { log.error(this) }
                    .run { throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                        variables = arrayOf(this), statusCode = HttpStatus.BAD_REQUEST) }
            }
            if (passwordConfirmation != password) {
                "password_and_confirmation_not_matched"
                    .apply { log.error(this) }
                    .run { throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                        variables = arrayOf(this), statusCode = HttpStatus.BAD_REQUEST) }
            }
            user.password = passwordEncoder.encode(password)
        }
        return userService.save(user = user)
            .run user@ {
                this@user.convertEntityToDto()
            }
    }
}
