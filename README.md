# PROGRESSIVE TESTING

What is progressive testing? Progressive testing is a way of testing your
application in a more continuous way.

In most applications there is a big
disconnect between exploratory testing and more structured testing (anything
you would do with a testing framework or library). There are no tools to
help you bridge the gab between your unit testing framework and the tools
you use for exploratory testing like Postman or manual interaction with the
browser.

But what if such tools _did_ exist? What if there was a more integrated 
way to move from exploratory testing to more structured testing?

Those are some quite open-ended questions so let's narrow it all down to
the JVM, RESTful APIs and API clients like Postman. 
Let's also list some concrete pain points.

## PAINPOINTS ILLUSTRATED

### Double work on tests

You write Postman collections to ease interaction with your API. These
collections give you valuable feedback about the behavior of your code, but
you don't leverage any of this! Your test suite is unaware of the data that
your Postman collections give you. It's not present in the test coverage
reports, it does not affect the outcome of a CI pipeline either.

Now, Postman does offer Newman, a CLI client you could use in your CI.
But Newman does not fully address the issue of test reports, 
it does not integrate with your existing testing workflow,
it is a fully disconnected tool.

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

The tool is used in the tests of the `dummy-api` subproject in this repo so dive straight in 
and have a look at this example. The Javadocs of the `UsecaseEnsuredExtension`
and `Usecase` offer further details.