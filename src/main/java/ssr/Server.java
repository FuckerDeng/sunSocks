/**
 * 
 */
/**
 * @author Administrator
 *
 */
package ssr;


import ssr.com.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
1.����һ��ServerSocket����
2.����ServerSocket�����accept�������ȴ����ӣ����ӳɹ��᷵��һ��Socket���󣬷���һֱ�����ȴ���
3.��Socket�����л�ȡInputStream��OutputStream�ֽ��������������ֱ��Ӧrequest�����response��Ӧ��
4.�������󣺶�ȡInputStream�ֽ�����Ϣ��ת���ַ�����ʽ��������������Ľ����Ƚϼ򵥣�������ȡuri(ͳһ��Դ��ʶ��)��Ϣ;
5.������Ӧ�����ݽ���������uri��Ϣ����WEB_ROOTĿ¼��Ѱ���������Դ��Դ�ļ�, ��ȡ��Դ�ļ���������д�뵽OutputStream�ֽ����У�
6.�ر�Socket����
7.ת������2�������ȴ���������
*/

public class Server {

    static final int listenPort=8888;

    static final String listenIP="0.0.0.0";

    public static void main(String[] args) {

        // �����ͻ��˴��������
        Server server = new Server();

        // �ȴ���������
        server.await();
    }
    
    public void await() {
        // ����һ��ServerSocket����
        ServerSocket serverSocket = null;

        try {
            //�������׽��ֶ���
            serverSocket = new ServerSocket(listenPort, 1, InetAddress.getByName(listenIP));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // ѭ���ȴ�һ������
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                socket.setKeepAlive(true);

                //���������б��ȴ�����
                ServerProxy sp = new ServerProxy(socket);
                Thread t = new Thread(sp);
                t.start();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}