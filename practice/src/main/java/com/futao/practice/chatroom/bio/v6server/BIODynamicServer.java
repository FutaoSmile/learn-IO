package com.futao.practice.chatroom.bio.v6server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.futao.practice.chatroom.bio.Constants;
import com.futao.practice.chatroom.bio.v6server.servlet.Servlet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于BIO实现的静态 and 动态服务器
 *
 * @author futao
 * @date 2020/7/6
 */
public class BIODynamicServer {

    private static final Logger logger = LoggerFactory.getLogger(BIODynamicServer.class);

    /**
     * 用于处理客户端接入的线程池
     */
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);

    /**
     * 静态资源路径
     */
    private static final String STATIC_RESOURCE_PATH = System.getProperty("user.dir") + "/practice/src/main/resources/pages/";

    /**
     * Servlet的类路径
     */
    private static final String DYNAMIC_RESOURCE_CLASS_PATH = "com.futao.practice.chatroom.bio.v6server.servlet.";

    /**
     * Servlet后缀
     */
    private static final String SERVLET_SUFFIX = "Servlet";

    /**
     * Servlet缓存
     */
    private static final Map<String, Servlet> SERVLET_MAP = new HashMap<>();

    /**
     * 默认页面
     */
    private static final String DEFAULT_PAGE = STATIC_RESOURCE_PATH + "index.html";

    /**
     * 响应的基础信息
     */
    public static final String BASIC_RESPONSE = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html;charset=utf-8\r\n" +
            "Vary: Accept-Encoding\r\n";

    /**
     * 回车换行符
     */
    private static final String carriageReturn = "\r\n";


    public void start() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            logger.debug("========== 基于BIO实现的服务器，开始提供服务 ==========");
            while (true) {
                Socket socket = serverSocket.accept();
                THREAD_POOL.execute(() -> {
                    try {
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream();

                        byte[] bytes = new byte[1024];
                        int curByteLength = inputStream.read(bytes);
                        byte[] dest = new byte[curByteLength];
                        System.arraycopy(bytes, 0, dest, 0, curByteLength);
                        //请求报文
                        String request = new String(dest);
                        logger.info("接收到客户端的数据:\n{}\n{}", request, StringUtils.repeat("=", 50));
                        // 解析请求地址
                        String requestUri = BIODynamicServer.getRequestUri(request);
                        // 静态资源处理器
                        boolean staticHandler = staticHandler(requestUri, outputStream);
                        if (!staticHandler) {
                            //动态资源处理器
                            if (!dynamicHandler(requestUri, outputStream)) {
                                //动态资源不存在，响应404
                                logger.debug("资源[{}]不存在，响应404", requestUri);
                                staticHandler("404.html", outputStream);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 动态资源处理器
     *
     * @param requestUri   请求资源名
     * @param outputStream 输出流
     * @return
     * @throws IOException
     */
    private boolean dynamicHandler(String requestUri, OutputStream outputStream) throws IOException {
        //Servlet是否存在
        boolean servletExist = false;
        //Servlet
        Servlet servletInstance = null;
        //从缓存中取
        Servlet servlet = SERVLET_MAP.get(requestUri);
        if (servlet == null) {
            //缓存中不存在
            try {
                //反射获取Class
                Class<Servlet> aClass = (Class<Servlet>) Class.forName(BIODynamicServer.DYNAMIC_RESOURCE_CLASS_PATH + requestUri + BIODynamicServer.SERVLET_SUFFIX);
                //创建Servlet对象
                servletInstance = aClass.newInstance();
                //缓存
                SERVLET_MAP.put(requestUri, servletInstance);
                servletExist = true;
                logger.debug("动态资源[{}]存在", requestUri);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                return false;
            }
        } else {
            //缓存中存在
            servletInstance = servlet;
            servletExist = true;
            logger.debug("动态资源[{}]存在", requestUri);
        }
        //执行业务逻辑
        try {
            Object result = servletInstance.service();
            String resp = JSON.toJSONString(result, SerializerFeature.PrettyFormat);
            //结果写入输出流
            BIODynamicServer.writeResponse(outputStream, resp.getBytes(Constants.CHARSET));
        } catch (Exception e) {
            //响应500
            staticHandler("500.html", outputStream);
        }
        return servletExist;
    }


    /**
     * 静态资源处理器
     *
     * @return
     */
    public boolean staticHandler(String page, OutputStream outputStream) throws IOException {
        //资源的绝对路径
        String filePath = BIODynamicServer.STATIC_RESOURCE_PATH + page;
        boolean fileExist = false;
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            logger.debug("静态资源[{}]存在", page);
            fileExist = true;
            //读取文件内容
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            //写入响应
            BIODynamicServer.writeResponse(outputStream, bytes);
        }
        return fileExist;
    }

    /**
     * 写入响应
     *
     * @param outputStream 输出流
     * @param content      内容
     * @throws IOException
     */
    private static void writeResponse(OutputStream outputStream, byte[] content) throws IOException {
        //写入基础响应头
        outputStream.write(BASIC_RESPONSE.getBytes(Constants.CHARSET));
        //写入服务器信息
        outputStream.write(("Server: futaoServer/1.1" + BIODynamicServer.carriageReturn).getBytes(Constants.CHARSET));
        //写入传输的正文内容大小
        outputStream.write(("content-length: " + content.length + BIODynamicServer.carriageReturn).getBytes(Constants.CHARSET));
        //响应头与响应体之间需要空一行
        outputStream.write(BIODynamicServer.carriageReturn.getBytes(Constants.CHARSET));
        //写入响应正文
        outputStream.write(content);
        outputStream.flush();
    }

    /**
     * 获取请求的资源地址
     *
     * @param request
     * @return
     */
    private static String getRequestUri(String request) {
        //GET /index.html HTTP/1.1
        int firstBlank = request.indexOf(" ");
        String excludeMethod = request.substring(firstBlank + 2);
        return excludeMethod.substring(0, excludeMethod.indexOf(" "));
    }


    public static void main(String[] args) throws IOException {
        new BIODynamicServer().start();
    }
}
