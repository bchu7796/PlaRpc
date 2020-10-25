# plaRPC

## Introduction

plaRPC is a tool that makes developing RPC microservices with Java easier. Users can create a normal Java class and run it as the gRPC server simply with this tool.

## Example

Let's say we have an interface "Test":
```Java
public interface Test {
    Integer hello();
    Integer helloName(String name);
    Integer square(Integer a);
    Integer sum(Integer a, Integer b);
}
```
and it's implementation "TestImpl":
```Java
public class TestImpl implements Test{
    public Integer hello() {
        System.out.println("hello");
        return 0;
    }

    @Override
    public Integer helloString(String name) {
        System.out.println("hello " + name);
        return 0;
    }

    @Override
    public Integer square(Integer a) {
        return a * a;
    }

    @Override
    public Integer sum(Integer a, Integer b) {
        return a + b;
    }
}
```

We can now deploy the service by using the following code:
```Java
Test test = new TestImpl();
PlaRpcHandlerServerApi<Test> server = new PlaRpcHandlerServerImpl<>(test);
server.start(5000);
```

Clients can connect to the service by using the following code:
```Java
PlaRpcHandlerClientApi<Test> test = new PlaRpcHandlerClientImpl(Test.class);
test.rpc().hello();
test.rpc().helloString("world");
test.rpc().square(10);
test.rpc().sum(10, 10);
```
