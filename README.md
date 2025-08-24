# Usecase Ensured

A tool to bridge the gap between exploratory API testing and structured testing.

Use your data from tools like Postman within your existing test setup. Reduce friction and shorten feedback loops in your API tests.

# Painpoint illustrated
Tools like Postman are great for exploratory testing but all the assurances and insights they produce are gone in an instant or trapped within the ecosystem of 
the given tool. There is very little integration with the Java testing ecosystem. Sure, Postman has the functionality to run tests, -- even within CI pipelines -- with its CLI tool but
this solution is poorly integrated with the Java testing ecosystem. There is no code coverage report, hitting your breakpoints while debugging is a more involved process because
the app has to be started and then the Postman tool has to be triggered. Triggering breakpoints by running a JUnit test is simpler in comparison.

This was the train of thought starting at the exploratory testing side of the problem, now let us start from the opposite end!

Writing API contract tests (behaviour driven tests, integration tests... call them whatever you want) is a great way to test your API. It provides good code coverage and at the same time
it does not tie you to the internal implementation details of your code. The users of your code only care about the API you expose, every internal method you directly invoke within your
test code ties you down to minutia that are not part of the contract your user cares about.

That being said, API contract tests require a lot of boilerplate code, instantiating your models in code is cumbersome, setting up the testing environment just right with configs and
tooling requires effort. All of this adds code into your project and the more code you add, the more you end up thinking about how to organise your code. This sounds like the beginning
of a vicious cicle.

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

## Releasing a new version

All actions are performed in the `usecase-ensured` directory.

Run the command
`mvn deploy -s settings.xml -DUNAME="$USERNAME" -DPWD="$PASSWORD" -Dgpg.passphrase="$PASSPHRASE"`

How the above environment variables are provided is a separate topic,
it is important that their values do not show up in the bash history.

Instead of a global `settings.xml` a local `settings.xml` is used
in combination with the `UNAME`and`PWD`variables.
This way the values can be stored securely in a password manager like 1Password
and accessed programmatically on demand.

The `gpg.passphrase` is used in order to sign the artifacts to be publish on
<https://central.sonatype.com>

## Installing the library locally during development

Run the command
`mvn install -Dgpg.passphrase=$PASSPHRASE`. The gpg keys are expected to be in
your local gpg keychain. Currently looking for a better solution.
