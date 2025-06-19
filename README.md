# DOCUMENTATION
### How to assert on things?

Here is a Postman `item` list entry, (which is a single HTTP request). It is one of the top-level properties of a Postman collection:

```json
{
  "name": "secret",
  "event": [
    {
      "listen": "test",
      "script": {
        "exec": [
          "const response = {",
          "    \"response\": 201,",
          "    \"content\" : {",
          "        \"id\": any(),",
          "        \"name\": \"Bob\"",
          "    }",
          "}"
        ],
        "type": "text/javascript",
        "packages": {}
      }
    }
  ],
  "request": {
    "method": "POST",
    "header": [],
    "url": {
      "raw": "localhost:8080",
      "host": ["localhost"],
      "port": "8080"
    }
  },
  "response": []
}
```

The `script` property is the assertion. It is expected that it contains that `response` object with the illustrated fields.

The `request` property is used in order to figure out what kind of HTTP request to make.

Once the response is received, the `script` details are used to assert on the response. The contets of `script` can be provided through the Postman UI by adding a `Post-request` script to a given request.