package com.github.usecase_ensured.data;

import com.fasterxml.jackson.databind.JsonNode;

public record ExpectedResponse(JsonNode expectedResponse) {
}
