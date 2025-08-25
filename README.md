# Usecase Ensured

A tool to bridge the gap between exploratory API testing and structured testing.

Use your data from tools like Postman within your existing test setup. Reduce friction and shorten feedback loops in your API tests.

# Painpoint illustrated
Tools like Postman are great for exploratory testing but the insights gained are not persisted. The integration with JUnit and IDEs is also bad.

Writing API contract tests (behaviour driven tests, integration tests... call them whatever you want) is a great way to test your API. It provides good code coverage and at the same time
it does not tie you to the internal implementation details of your code. The downside is that boilerplate code is needed in order to set up the data in the tests.

Zooming out to look at the whole picture, if you use a tool like Postman and at the same time have API level tests then you end up doing very similar work in two different parts of the
project without gaining any extra benefit, it's essentially wasted time. I think that the concerns of exploratory testing and API testing should have a shared foundation
that streamlines these aspects of programming.

# How does Usecase Ensured help?
With this tool you can integrate the usecases you define/discover in tools like Postman into your JUnit based tests. 
This reduces the amount of work you need to do in order to provide tests for the API contract you expose in your program.

#### Usecase illustrated:
- Writing API contract tests increases boilerplate code, Usecase Ensured helps minimize this problem. You just need to configure an environment where your whole service is up and running.
- Quick transition from exploratory testing into "rigorous testing". Found an interesting edge case? Write a postman collection and wire it into your JUnit suite,
  push to Git and you're done.
  Technical consultants could even use this to report bugs in a more effective and reproducible manner.
- Simplify work hand over between seniors and juniors. The senior explains the task and provides some tests written with Usecase Ensured, much less effort than normal tests. The
  junior then has a more solid foundation to start off from when working on the task.

# A demonstration
An example project that relies on Usecase Ensured is located in the `dummy-api` subdirectory.
Let us take a tour of the central points, feel free to experiment with the finer details on your 
own.

Pull the Usecase Ensured dependency from:
<https://central.sonatype.com/artifact/io.github.usecase-ensured/usecase-ensured>

Below are the contents of `IntegrationTest.kt`
```kotlin

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(UsecaseEnsuredExtension::class)
class IntegrationTest(@Autowired private val controller: DummyController, ) {

    @BeforeEach
    fun teardown() {
        controller.reset()
    }

    @Test
    @Usecase("a-test.json")
    fun `can create dummy ALT`() {}

    @Test
    @Usecase("secret.json")
    fun `can call secret endpoint (2)`(){}

    @Test
    @Usecase("multi-step.json")
    fun `can retrieve dummy (2)`() {}

    @Test
    @Usecase("meta-variable.json")
    fun `can use meta variables`() {}
}
```

The `UsecaseEnsuredExtension` is needed in order to plug the library into the
lifecycle of your JUnit tests, without it the library is not "enabled".

The `SpringBootTest` annotation is also critical. Your API tests expect to be
run against a fully up and running instance of your program accessible at a predefined port.
The port needs to be known in advance because it is used 
in the definitions of our test cases **outside of any Spring Boot context**.

Every relevant test method is annotated with the `Usecase` annotation.
It accepts the file name containing the Usecase Ensured compatible test spec.

Currently only Postman style collections are supported. `Usecase`'s `type` parameter
is therefore not required. By convention, the Postman compatible specs go into the
`src/test/resources/postman` directory. The files specified above the test methods are Postman collections,
exported by a button in Postman.

On to the Postman collections themselves

Postman's variable are **not** supported, everything needs to be static.

The Postman requests need to be pointing to the URL configured for you program
in the test setup.

Simply exporting a Postman collection is sufficient to make a runnable `Usecase`.
No assertions have been defined yet though. To add assertions to a given request within
a collection a `post response` script is added through the Postman UI, 
it needs to have a proper structure. Here is an example:
```javascript
const response = {
    "response": 201,
    "content" : {
        "id": "{{any}}",
        "name": "Bob"
    }
}
```

The `"{{any}}"` meta variable can be used to ignore a field in assertions. The rest of
the example is self-explanatory.
