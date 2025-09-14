package com.github.usecase_ensured.internal.runner.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.usecase_ensured.internal.Request;
import io.restassured.http.Headers;

class UsecaseRequest extends Request {
    private final JsonNode savedVariables;
    private final UsecaseContext context;
    private final String metaVariableRegex = ".*\\{\\{.+}}.*";

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
        var urlNode = (TextNode) request.required("url");
        var resolvedString = new StringBuilder();
        if (containsMetaVariable(urlNode)) {
            String url = urlNode.asText();
            var metaVariablesAsSeparators = url.splitWithDelimiters(metaVariableRegex, -1);

            for (var part : metaVariablesAsSeparators) {
                if (isMetaVariable(part)) {
                    resolvedString.append(context.getVariable(part));
                } else {
                    resolvedString.append(part);
                }
            }
        }
        return resolvedString.toString();
    }

    @Override
    public String body() {
        return "";
    }

    @Override
    public Headers headers() {
        return null;
    }

    @Override
    public String method() {
        return "";
    }

    private boolean containsMetaVariable(JsonNode json) {
        return json.isTextual() && json.textValue().matches(metaVariableRegex);
    }
    private boolean isMetaVariable(String str) {
        return str.startsWith("{{") && str.endsWith("}}");
    }
}
