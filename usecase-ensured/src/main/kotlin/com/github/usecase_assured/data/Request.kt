package com.github.usecase_assured.data

import io.restassured.http.Header

internal data class Request(
    var method: Method,
    var headers: List<Header>,
    var url: String,
    var body: String?
) {

    internal enum class Method {
        GET,
        POST,
        PUT,
        DELETE
    }
}
