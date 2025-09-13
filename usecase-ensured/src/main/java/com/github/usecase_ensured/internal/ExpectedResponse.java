package com.github.usecase_ensured.internal;

import com.fasterxml.jackson.databind.JsonNode;

public record ExpectedResponse(JsonNode expectedResponse) {
}
