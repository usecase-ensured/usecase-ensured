# License
MIT License: <https://opensource.org/licenses/MIT>

Applies to everything in this repository.
# Usecase Ensured
Here is the picture, you have a RESTful API and a GUI HTTP client like Postman.
You are using the client during development for exploratory purposes. The things you define in 
Postman remain inside that walled garden, your actual testing logic does not benefit from that data.
There is a gap between exploratory testing and automated testing.

So here is the question:
What if the integration tests in your language of choice and the exploration in the HTTP client 
used the exact same syntax?

- Going from feature request to tested implementation would take less effort.
- There would be less boilerplate code to maintain in the testing logic.
- Less-technical team members can file more precise bug reports and feature requests.

The Usecase Ensured library, together with a companion project for the HTTP client, 
aims to bridge this gap between exploration and automated testing.

Assisted by free and open source
tools, developers will find more joy in defining their RESTful APIs and ensuring their correctness.

# An example
Code snippets are taken from the `dummy-api` subproject of this repository.
### The usecase spec:
```json
{
  "name": "create and fetch with custom syntax",
  "given": {
    "baseUrl": "http://localhost:8080/dummy",
    "name": "Bob"
  },
  "steps": [
    {
      "name": "create",
      "do": {
        "method": "POST",
        "url": "{{given.baseUrl}}",
        "body": {
          "name": "{{given.name}}"
        },
        "saved": {
          "id": "{{id}}"
        }
      },
      "then": {
        "statusCode": 201,
        "body": {
          "name": "Bob",
          "id": "{{saved.id}}"
        }
      }
    },
    {
      "name": "fetch",
      "do": {
        "method": "GET",
        "url": "{{given.baseUrl}}/{{saved.id}}"
      },
      "then": {
        "statusCode": 200,
        "body": {
          "name": "Bob",
          "id": 0
        }
      }
    }
  ]
}
```
`given` meta variables are defined once in the `given` section at the top of the file and can be 
used anywhere through the `"{{given.xxx}}"` meta variable reference.

Each `step` can define more variables within the `saved` section inside the `do` section. These 
meta variables are referenced as `"{{saved.xxx}}"` and can be overwritten by subsequent steps.

### The JUnit test class that utilises the usecase spec
```kotlin
    @Nested
    inner class GivenFeatureTestSuite {
        @Test
        @Usecase(value = "given-feature/create-and-fetch.json")
        fun `define meta variables outside of test steps and use them throughout the usecase`() {
        }
    }
```

# Setup
- This library needs an actual HTTP port to hit, your test suite needs to spin up the 
  app on a predefined port to make this happen. I recommend using the `SpringBootTest` 
  annotation and testcontainers. Docker compose can also be used for local development.
- Pull the dependency from 
  <https://central.sonatype.com/artifact/io.github.usecase-ensured/usecase-ensured> and get testing!

# Doctrine
1. Optimize for programmer happiness
2. Free & open-source
3. Plug into existing systems, do not build walled gardens
4. Minimal scope, maximal simplicity, always feature-driven

# Roadmap
1. Build Java library that plugs into JUnit ✅
2. Build an HTTP client, 
   the bridge between exploration and automated testing is complete. 
3. Build extensions compatible with testing libraries in other languages, based on curiosity or 
   popular demand.
4. Explore supporting formats other than JSON/HTTP.*

*: some microservices only listen to Kafka topics, setting up integration tests in this case
can be tricky and repetitive. A library could improve programmer happiness. The same thought
process can also be applied to other formats.