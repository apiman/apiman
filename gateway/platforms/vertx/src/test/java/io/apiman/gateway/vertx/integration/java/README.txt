Put your Java integration tests in here or sub directories.

To create a Java integration test in Vert.x - that's a test that runs *inside* the Vert.x container, you should
create a class like this which extends `TestVerticle`.

`TestVerticle` acts as both a Verticle which can run inside the Vert.x container and as a JUnit test.

We use a custom JUnit test runner so that when this test is run it auto-magically creates a Vert.x container
and runs it in that whilst communicating the test results back to the runner so they can be collated as
normal.

You should annotate your test methods with @Test as normal, and you can use the standard JUnit API
inside the test to assert stuff.

@Before and @After annotations currently don't work, but you can put any shared startup code that inside the
standard verticle `start()` method.

You can use the standard JUnit Assert API in your test by using the VertxAssert class