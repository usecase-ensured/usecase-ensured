# PROGRESSIVE TESTING

What is progressive testing? Progressive testing is a way of testing your
application in a more continuous way.

In most applications there is a big
disconnect between exploratory testing and more structured testing (anything
you would do with a testing framework or libary). There are no tools to
help you bridge the gab between your unit testing framework and the tools
you use for exploratory testing like Postman or manual interaction with the
browser.

But what if such tools _did_ exist? What if there was a more
thought-through way to move from exploratory testing to more structured
testing?

Those are some quite open-ended questions so let's narrow it all down to
RESTful APIs and Postman. Let's also list some concrete pain points.

# TABLE OF CONTENTS

- [PAINPOINTS ILLUSTRATED](#h2-paintpoints-illustrated)
- [HOW TO USE THE TOOL](#h2-how-to-use-the-tool)

## PAINPOINTS ILLUSTRATED

### Double work on tests

You write Postman collections to ease interaction with your API. These
collections give you valuable feedback about the behavior of your code but
you don't leverage any of this! Your test suite is unaware of the data that
your Postman collections give you. It's not present in the test coverage
reports, it does not affect the outcome of a CI pipeline either.

Now, Postman does offer Newman, a cli client you could use in your CI. But
is it really necessary to be tying yourself to a company for
your CI builds to this extent? Newman also does not address the issue of
test reports, it does not integrate with your existing testing workflow,
it's a fully disconnected tool.

But what if there _WAS_ a tool for that?

1. use Postman to define the requests that are possible
2. copy-paste them into separate Postman collections that represent
   specific acceptance criteria. Your PO could even offer some
   feedback here!
3. Use this tool to make this data part of your test suite, locally and in CI.

### Messy handover when filing bug reports

A technical consultant found a bug and is writing a bug ticket with all the
usual info. Reproducing the bug is tricky though, many steps need to be taken
in order to get to a state where the bug can happen. The engineer who ends
up working on the ticket will need to figure all that out and then maybe
even repeat the process when updating the test suite.

But what if there _WAS_ a tool for that?

The consultant probably knows how to use Postman. They could format their
collection correctly and add it to the bug ticket. The programmer then uses
the tool in their branch to programmatically run all the steps inside their
testing framework.

## HOW TO USE THE TOOL

The tool is used in the `dummy-api` subproject in this repo so if you want
to dive straight into an example you should proceed there. Here, I will
walk you through the functionality of the tool.

### Prerequisites

- Your test suite is set up to accept connections on `localhost` and the
  port that you will be using in your Postman collections in the upcoming
  tests.
- The dependency for this project is added (currently not possible since I
  haven't published it to a Maven repository)

### Setup

Annotate your test class with `@ExtendWith(ProgressiveTestingExtension::class)`

Annotate a given test method with `@TestFile("a-test.json")` AND the
standard `@Test` (tests work without this but it ensures that your IDE still
displays the button for running that test method).

The tool expects to find the `a-test.json` file under
`src/test/resources/postman`.

Among other things, the `a-test.json` file contains the field `item`. It 
represents a list of HTTP requests.

```json
{
  ...
  ...
  ...
  "item": [
    {
      "name": "create dummy",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "const response = {",
              "    \"response\": 201,",
              "    \"content\" : {",
              "        \"id\": 0,",
              "        \"name\": \"Bob\"",
              "    }",
              "}"
            ],
            "type": "text/javascript",
            "packages": {
            }
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "a",
            "value": "a",
            "type": "text"
          },
          {
            "key": "b",
            "value": "b",
            "type": "text"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n    \"name\": \"Bob\"\n}",
          "options": {
            "raw": {
              "language": "json"
            }
          }
        },
        "url": {
          "raw": "http://localhost:8080/dummy",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "8080",
          "path": [
            "dummy"
          ]
        }
      },
      "response": []
    }
  ]
}
```

The `item/[i]/event/script/exec` property is used in order to add 
assertions to any given step 
of your Postman collection. Its contents have to be written manually. It is 
expected that it only contains that `response` object with the illustrated 
fields.

Inside the Postman UI navigate to your HTTP request and then `Scripts > 
Post-response` to add this data.

Everything else inside this file is automatically generated by Postman when 
exporting this collection.

Your test should now be ready to run!