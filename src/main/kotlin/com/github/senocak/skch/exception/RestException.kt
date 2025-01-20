package com.github.senocak.skch.exception

import com.github.senocak.skch.util.OmaErrorMessageType
import org.springframework.http.HttpStatus

open class RestException(msg: String, t: Throwable? = null): Exception(msg, t)

class ServerException(var omaErrorMessageType: OmaErrorMessageType, var variables: Array<String?>, var statusCode: HttpStatus = HttpStatus.BAD_REQUEST):
    RestException(msg = "OmaErrorMessageType: $omaErrorMessageType, variables: $variables, statusCode: $statusCode")
