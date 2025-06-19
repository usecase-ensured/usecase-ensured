#DOCUMENTATION

### How to assert on things?

Here is a Postman `item` list entry, (which is a single HTTP request). It is
one of the top-level properties of a Postman collection:

```jsx
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
      "host": [
        "localhost"
      ],
      "port": "8080"
    }
  },
  "response": []
}
```

The `script` property is the assertion. It is expected that it contains that
`response` object with the illustrated fields. Its content can be modified 
withing the `Script` tab of a request in the Postman UI, make sure to add 
it in the `Post-response` part.

The `request` property is used in order to figure out what kind of HTTP request
to make.

Once the response is received, the `script` details are used to assert on the
response.