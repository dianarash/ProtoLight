package com.IFNTUOG.DRashkevych;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UDP {
    private int port;
    private String ip;
    private byte[] receiveData = new byte[1024];
    private byte[] sendData = new byte[1024];

    private boolean testApp = true;                //true for pass connection and test application

    public UDP(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public boolean isTestApp() {
        return testApp;
    }


    public void sender(String str) throws IOException {
        DatagramSocket client_socket = new DatagramSocket(port);
        InetAddress IPAddress =  InetAddress.getByName(ip);

        sendData = str.getBytes();

        DatagramPacket send_packet = new DatagramPacket(sendData,str.length(), IPAddress, port);
        client_socket.send(send_packet);
        client_socket.close();
    }

    public boolean checkConnect(String str) throws IOException {
        if(testApp) return true;
        else {
            DatagramSocket client_socket = new DatagramSocket(port);
            InetAddress IPAddress = InetAddress.getByName(ip);

            sendData = str.getBytes();

            DatagramPacket send_packet = new DatagramPacket(sendData, str.length(), IPAddress, port);
            client_socket.send(send_packet);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            client_socket.setSoTimeout(2000);     // wait 2000 ms for receive
            try {
                client_socket.receive(receivePacket);
            } catch (SocketTimeoutException e) {
                client_socket.close();
                return false;
            }
            String modifiedSentence = new String(receivePacket.getData());
            client_socket.close();
            if (modifiedSentence.contains("O"))
                return true;
            else
                return false;
        }
    }
}
