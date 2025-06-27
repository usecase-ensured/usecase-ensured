package progressive_testing.data

data class Request(
    var method: Method,
    var headers: Map<String, String>,
    var url: String,
    var body: String?
) {

    enum class Method {
        GET,
        POST,
        PUT,
        DELETE
    }
}
