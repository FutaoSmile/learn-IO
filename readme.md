### # 使用ServerSocket与Socket实现消息收发
* **服务端**: [ServerSocket.java](src/main/java/com/futao/learn/imooc/chatroom/bio/threads/netty/MyServerSocket.java)
* **客户端**: [Socket.java](src/main/java/com/futao/learn/imooc/chatroom/bio/threads/netty/MyClientSocket.java)
    * 读取用户在控制台输入的文本，发送给服务端。
    * 服务端接收到消息后，响应相同的文本给客户端。


> 字节流：讲究Stream；字符流：讲究Reader，Writer

### # 基于BIO(Blocking I/O)的多人群发聊天室
* **服务端**: [ChatServer.java](src/main/java/com/futao/learn/imooc/chatroom/bio/ChatServer.java)
    * 创建`ServerSocket`实例，监听`8888`端口。
    * 阻塞等待客户端接入`serverSocket.accept()`。while循环继续阻塞等待下一个客户端socket的接入
    * 当客户端接入，获取到客户端的socket连接。
        * 创建子线程完成下面的工作，主线程继续阻塞，等待新的socket连接注册。
        * 通过客户端socket的port标识每一个客户端。
        * 当客户端接入ServerSocket，把客户端的socket保存在Map<port,socket>中，以便后续向连接的socket客户端发送消息。
        * 通过while循环等待客户端发送消息(readLine()每次读取到`\n`)。
        * 将客户端发送的消息转发到注册的其他socket，即Map<port,socket>中的客户端。
        * *这样其实每个客户端在服务端都对应有一个线程在循环等待客户端的数据传输。*
        * 如果socket客户端发送的文本是`quit`，说明该客户端准备退出
            * 则将该socket从Map中remove。
            * 关闭socket连接。
            * 结束等待传输消息的while循环
            * 回收线程
       
![image.png](https://upload-images.jianshu.io/upload_images/1846623-d02e8b83dfb861d6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![image.png](https://upload-images.jianshu.io/upload_images/1846623-f4c36956f31c9bab.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![image.png](https://upload-images.jianshu.io/upload_images/1846623-ca834d2ccb22dbe9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

* **客户端**: [Client.java](src/main/java/com/futao/learn/imooc/chatroom/bio/Client.java)
    * 启动Socket，连接ServerSocket服务器。
    * 连接成功后，开启一个新的线程来循环等待服务端发送的消息。（读线程与写线程分离，保证Socket客户端专门有一个单独的线程来读取服务端的消息，不会被写时间阻塞）
    * 在`main`线程中循环获取用户输入的数据，并将数据通过socket发送到服务器。
        * 因为服务器对每个客户端都有一个线程，所以服务器在接收到消息后，可以将消息转发给其他的Socket客户端。
    * 当用户输入的数据为`quit`时，
        * 服务端接收到内容为`quit`的数据，则关闭对应的socket连接。
        * 客户端关闭socket连接。
        
> ServerSocket.accept() 会一直阻塞，直到有客户端接入。
> InputStream.read()与OutputStream.write()也都会一直阻塞一个线程，直到输入和输出事件的发生。

### # NIO `NEW IO` OR `Non-Blocking IO`
* 核心概念&参数：
    * ByteBuffer 缓冲区
    * 写模式/读模式
    * position
    * limit
    * capacity
    * flip()
* 过程:
    1. 写模式下position指针刚开始指向position=0的位置，limit=capacity=缓冲区大小。
    2. 向缓冲区写入数据，position向下移动指针。
    3. 调用flip()，将缓冲区从写模式变更成读模式，同时position=0，limit指向写入数据的末尾。
    4. 开始读取数据，position指针随着数据的读取向下移动，直到到达limit。
    5. clear()/compact()。
        * clear()会清空整个缓冲区的数据
        * 而compact()只会清空
    


### # 其他
* 网络编程的本质是进程间的通信。传输字节010101
* Java io包
    * 字节流: 
        * *程序从其他地方*输入: `OutputStream`
        * *程序*输出*到其他地方*: `InputStream`
    * 字符流:
        * *程序从其他地方*输入: `Reader`
        * *程序*输出*到其他地方*: `Writer`