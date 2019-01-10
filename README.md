# idd

### 简介

    idd是一款分布式自增id序列生成框架，基于netty实现，不依赖任何第3方容器、中间件。
    idd实现了raft协议的选举部分，客户端请求都将转发给leader，leader采用多数派提交方式来保证一致性。
    由于场景特性，服务需要提供"当前id是多少"，不需要提供"历史上产生了哪些id"。
    所以，idd实现了顺序一致性，未实现线性一致性，不保存历史记录，同时，换来大幅度的性能提升。
    
### 请求压缩优化

    假设f(100, 1)表示：100个请求分别申请1个id。
    假设ABC3个节点的idd集群，A是leader。每个节点收到客户端f(100, 1)，BC提交给A f(1, 100)，A广播 f(1， 300)。
    即，同一时刻的ID请求，leader只需要增加计数，在一轮广播中完成落盘，后由具体节点分发。
    经请求压缩优化后，idd每次请求延时为8-10ms，TPS在1万以上，上限为物理上限。
    
    如何提供更快的速度和更严格的一致性?
    除了使用SSD硬盘外，可提供二进制的客户端，每次请求只访问leader，可以减少一次网络IO，并近似实现线性一致性。
    此时，idd集群演变为可自动切换的一主多从集群。

### 现状

    目前实现了原型，未处理边界、异常情况。
    基于raft选举、内存同步队列，还可以做其他有趣、有意义的事情。
    示例代码位于源代码eastwind.idd.test。 
    
### 示例--嵌入式

  1-创建一个3节点的idd集群

        String[] addresses = { ":18727", ":18728", ":18729" };
        String allAddressesStr = String.join(",", addresses);
	
        IddClient[] iddClients = new IddClient[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            iddClients[i] = IddApplication.create(addresses[i], allAddressesStr).getIddClient();
        }

  以下请求用任一IddClient，都返回相同结果。
  2-创建一个名称为user的sequence

        // create
        CompletableFuture<Sequence> cf = iddClients[0].create("user");
        cf.thenAccept(s -> System.out.println("create sequence: " + s.getName() + "."));
        cf.join();

        // create if exist
        cf = iddClients[1].create("user");
        cf.exceptionally(th -> {
            System.err.println(th.getMessage());
            return null;
        });
		
  3-获取下一个id

        // get id
        for (int i = 0; i < 10; i++) {
            iddClients[i % iddClients.length].next("user").thenAccept(s -> System.out.println("new id: " + s.getNextVal())).join();
        }

### 示例--HTTP

    idd支持HTTP形式调用。
    
  1-控制台
   
    Get: http://127.0.0.1:18728/
    Response:
    {
        "address" : ":18728",
        "role" : "LEADER",
        "servers" : ":18727,:18728,:18729",
        "startAt" : "2019-01-10 18:37:43",
        "currentTerm" : 1,
        "logId" : 12,
        "sequences" : [ {
            "name" : "user",
            "version" : 1,
            "logId" : 12,
            "nextVal" : 11
            } 
        ]
    }
  
  2-ID相关
  
    Put/Get/Post分别表示创建、查询、申请，如：
    Post：http://127.0.0.1:18728/user
    Response:
    {
        "name" : "user",
        "version" : 1,   // sequence的版本号，创建时的logId
        "logId" : 12,    // raft的logId
        "nextVal" : 11   // new id
    }

#### 后记

    本人近期花了3-4周写了这个玩具，由于有别的事情要做，idd开发告一段落，有时间有机会的话再把这个框架写完整。
