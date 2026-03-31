package com.leafon.common.response

data class PageResponse<T>(
    val items: List<T> = emptyList(),
)
