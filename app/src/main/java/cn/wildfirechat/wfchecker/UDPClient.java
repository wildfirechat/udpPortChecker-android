package cn.wildfirechat.wfchecker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
    private final String host;
    private final int port;

    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String connectAndSend(String message) {
        DatagramSocket socket = null;

        try {
            // 创建UDP套接字
            socket = new DatagramSocket();

            // 服务器地址和端口号
            InetAddress serverAddress = InetAddress.getByName(host);

            // 要发送的消息
            byte[] sendData = message.getBytes();

            // 创建数据包来发送消息
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);

            // 发送消息到服务器
            socket.send(sendPacket);

            // 接收服务器的响应
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.setSoTimeout(30000);
            socket.receive(receivePacket);

            // 解析并打印服务器的响应
            String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("服务器响应: " + serverResponse);
            return serverResponse;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        return null;
    }

    private static void printUsage() {
        System.out.println("使用方法：java UDPClient host port message");
        System.out.println("例如：java UDPClient 192.168.1.121 30000 hello");
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            printUsage();
            return;
        }
        UDPClient client = new UDPClient(args[0], Integer.parseInt(args[1]));
        client.connectAndSend(args[2]);
    }
}
