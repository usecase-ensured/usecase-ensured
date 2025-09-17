package com.github.usecase_ensured.internal.runner.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.usecase_ensured.internal.Request;
import io.restassured.http.Header;
import io.restassured.http.Headers;

import java.util.ArrayList;
import java.util.List;

class UsecaseRequest extends Request {
    private final JsonNode savedVariables;
    private final UsecaseContext context;

    protected UsecaseRequest(JsonNode request, UsecaseContext context) {
        super(request);
        this.savedVariables = request.at("/saved");
        this.context = context;
    }

    public JsonNode savedVariables() {
        return savedVariables;
    }

    @Override
    public String url() {
        var urlNode = (TextNode) request.requiredAt("/url");
        if (UsecaseContext.Helper.containsMetaVariable(urlNode)) {
            return context.resolveMetaVariablePartsOf(urlNode);
        } else {
            return urlNode.asText();
        }
    }

    @Override
    public String body() {
        var body = request.at("/body");
        if (body.isMissingNode()) {
            return null;
        }

        return context.resolve(body).toString();
    }

    @Override
    public Headers headers() {
        var headers = request.at("/headers");

        if (headers.isMissingNode()) {
            return new Headers(List.of());
        }

        headers = context.resolve(headers);
        var acc = new ArrayList<Header>();

        for (var header : headers.properties()) {
            acc.add(new Header(header.getKey(), header.getValue().asText()));
        }

        return new Headers(acc);
    }

    @Override
    public String method() {
        return request.requiredAt("/method").asText();
    }
}
