### #` ByteBuffer`

> `ByteBuffer`为NIO中的字节缓冲区，相对于BIO的Stream流只支持写入或者读取单向操作，`ByteBuffer`是双向的，支持读和写。

* 类型
    * DirectByteBuffer
        * 使用的是操作系统级别的内存DirectByteBuffer，分配比较慢，但是数据的读写比较快，因为少了一次从系统内存到JVM内存的复制过程
        * 初始化方法:
            * `ByteBuffer.allocateDirect(1024 * 4);`
    * HeapByteBuffer
        * 使用的是JVM的堆内存HeapByteBuffer，对于JVM来说，分配比较快，但是读写比较慢，因为需要将操作系统内存里的数据复制到JVM内存
        * 初始化方法:
            * `ByteBuffer.allocate(1024 * 4);`

* 核心属性
    * `capacity`
        * `ByteBuffer`的容量，这个值在`ByteBuffer`初始化的时候就确定下来了。不论是在读还是在写模式下，这个值都不变。
    * `position`
        * 写模式下：
            * 该值表示当前写到了`ByteBuffer`的哪个位置，`ByteBuffer`初始化时，这个值为0。
            * `position`的最大值为`capacity-1`。
        * 读模式下：
            * 当从写模式切换到读模式，会将`position`重置为0，即从`ByteBuffer`的起始位置开始读取数据。
    * `limit`
        * 写模式下：
            * `limit`为最大可写入的数据量，即`ByteBuffer`的最大容量，值为`capacity`
        * 读模式下：            
            * 当从写模式切换从读模式，`limit`将会被设置为读模式下的`position`值，即可读取的最大数据量。
   
* 核心方法
    * `flip()`
        * 将写模式切换为读模式
        * 会触发的对核心属性的操作:
            * 将`position`设置为`0`，即从`ByteBuffer`起始位置开始读。
            * 将`limit`设置为写模式下`position`的值，即最大可读取的数据量大小。
    * `mark()`
        * 标记当前`position`位置
    * `reset()`
        * 将`position`指向上一次`mark()`所指向的位置，可以从这个位置重复向下读取数据
    * `clear()`
        * 在逻辑上清空ByteBuffer里的数据，实际上不清空数据
        * 会触发的动作：
            * 将`limit`设置为`capacity`
            * `position`指向起始位置`0`
            * 提示：实际上数据并未清理，只是下次是从0的位置开始写入数据，效果上像是数据清空了。
            * 提示：如果`ByteBuffer`中的数据并未完全读完，调用这个方法将忽略那些未读取的数据。
    * `compact()`
        * 如果并未读取完`ByteBuffer`中的数据，调用`compact()`会将`position~limit`之间的数据拷贝到`ByteBuffer`的起始处，并且`position`为剩余数据量的大小，下次再往`ByteBuffer`中写入数据时，将在`position`位置继续往下写，不会覆盖历史数据。
    * `hasRemaining()`
        * 判断缓冲区中是否还有未读数据
        
* 将数据写入ByteBuffer的方式
    * `byteBuffer.put(x)`
    * `channel.write(byteBuffer)`
* 从ByteBuffer中读取数据的方式
    * `byteBuffer.get()`
    * `channel.read(bytebuffer)`