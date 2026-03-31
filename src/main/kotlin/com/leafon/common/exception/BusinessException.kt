package com.leafon.common.exception

open class BusinessException(
    message: String = "Business exception",
) : RuntimeException(message)
