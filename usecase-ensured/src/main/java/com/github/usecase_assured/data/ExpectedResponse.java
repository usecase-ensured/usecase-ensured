package com.github.usecase_assured.data;

import com.fasterxml.jackson.databind.JsonNode;

public record ExpectedResponse(JsonNode expectedResponse) {
}
