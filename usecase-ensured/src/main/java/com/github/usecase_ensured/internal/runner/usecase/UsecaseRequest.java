package com.github.usecase_ensured.internal.runner.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.usecase_ensured.internal.Request;
import io.restassured.http.Header;
import io.restassured.http.Headers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class UsecaseRequest extends Request {
    private final JsonNode savedVariables;
    private final UsecaseContext context;
    private static final Pattern metaVariableRegex = Pattern.compile("\\{\\{(?:(?!\\{\\{).)*}}");

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
        if (containsMetaVariable(urlNode)) {
            var resolvedString = new StringBuilder();
            String url = urlNode.asText();
            var metaVariablesAsSeparators = url.splitWithDelimiters(metaVariableRegex.pattern(), -1);

            for (var part : metaVariablesAsSeparators) {
                if (isMetaVariable(part)) {
                    resolvedString.append(context.getVariable(part).asText());
                } else {
                    resolvedString.append(part);
                }
            }
            return resolvedString.toString();
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

        String text = context.resolve(body).toString();
        return text;
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

        Headers headers1 = new Headers(acc);
        return headers1;
    }

    @Override
    public String method() {
        return request.requiredAt("/method").asText();
    }

    private boolean containsMetaVariable(JsonNode json) {
        return json.isTextual() && metaVariableRegex.matcher(json.textValue()).find();
    }

    private boolean isMetaVariable(String str) {
        return str.startsWith("{{") && str.endsWith("}}");
    }

    private boolean isMetaVariable(JsonNode json) {
        return json.isTextual() && isMetaVariable(json.asText());
    }
}
