/**
 * 
 */
/**
 * @author Administrator
 *
 */
package ssr;


import com.ice.jni.registry.RegistryException;
import ssr.com.*;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
1.����һ��ServerSocket����
2.����ServerSocket�����accept�������ȴ����ӣ����ӳɹ��᷵��һ��Socket���󣬷���һֱ�����ȴ���
3.��Socket�����л�ȡInputStream��OutputStream�ֽ��������������ֱ��Ӧrequest�����response��Ӧ��
4.�������󣺶�ȡInputStream�ֽ�����Ϣ��ת���ַ�����ʽ��������������Ľ����Ƚϼ򵥣�������ȡuri(ͳһ��Դ��ʶ��)��Ϣ;
5.������Ӧ�����ݽ���������uri��Ϣ����WEB_ROOTĿ¼��Ѱ���������Դ��Դ�ļ�, ��ȡ��Դ�ļ���������д�뵽OutputStream�ֽ����У�
6.�ر�Socket����
7.ת������2�������ȴ���������
*/

public class Client implements ActionListener{

    //Specify the look and feel to use.  Valid values:  s
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    final static String LOOKANDFEEL = "System";
    JButton jbutton;
    JLabel lblServer;
    JLabel lblPort;
    JLabel lblPassword;
    JTextField txtServer;
    JTextField txtPort;
    JTextField txtPassword;
    Boolean blnStart = false;

    static final int workerNumber = 4;//�̳߳ر���������������Ϊ8��cpu�����ʵ�����Ӧ��С��8

    static final int maxPoolSize=256;//����߳�����������󲢷���

    static final int maxWorkerInQueue = 2500;// �������������

    static final int waitTime = 10;// ��ʱ�ȴ�ʱ��

    static final int listenPort=8788;

    static final String listenIP="127.0.0.1";

    static final String foreignIP="45.32.77.143";

    static final ThreadPoolExecutor tpe = new ThreadPoolExecutor(workerNumber,
            maxPoolSize, waitTime, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(maxWorkerInQueue));

    public Client(){
        doShutDownWork();
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Client client = new Client();
                client.createAndShowGUI();
            }
        });
    }

    private void doShutDownWork() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Proxy proxy = new Proxy();
                try {
                    // ���ô��������
                    proxy.disableProxy();
                } catch (RegistryException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (!blnStart)
        {
            jbutton.setText("Stop Proxy");
            blnStart = true;

            ThreadClient threadClient = new ThreadClient();
            threadClient.start();
        }else{
            // �˳�
            System.exit(1);
        }
    }

    class ThreadClient extends Thread{
        @Override
        public void run() {
            try {
                // ���ô��������
                Proxy proxy = new Proxy();
                // IE���������
                proxy.changeProxy(listenIP, listenPort);
            } catch (Exception ex) {
                System.out.println("PC Proxy Server Setting Error:" + ex.getMessage());
            }

            try {
                // �������ش��������
                Client client = new Client();

                // �ȴ���������
                client.await(txtServer.getText(), txtPort.getText());
            } catch (Exception ex) {
                System.out.println("Proxy Client Error:" + ex.getMessage());
            }
        }
    }

    private static void initLookAndFeel() {
        String lookAndFeel = null;

        if (LOOKANDFEEL != null) {
            if (LOOKANDFEEL.equals("Metal")) {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("System")) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("Motif")) {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            } else if (LOOKANDFEEL.equals("GTK+")) { //new in 1.4.2
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            } else {
                System.err.println("Unexpected value of LOOKANDFEEL specified: "
                        + LOOKANDFEEL);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }

            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:"
                        + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel ("
                        + lookAndFeel
                        + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (Exception e) {
                System.err.println("Couldn't get specified look and feel ("
                        + lookAndFeel
                        + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI() {
        //Set the look and feel.---������ۣ����Ժ���
        initLookAndFeel();

        //Make sure we have nice window decorations.
        //����Ϊfalse�Ļ�����Ϊ���ı����
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //----------------------Pannel Components---------------------------
        Panel pn = new Panel(null);
        pn.setSize(800, 600);

        lblServer = new JLabel("Server");
        lblServer.setBounds(50, 30, 100, 30);
        pn.add(lblServer);

        txtServer = new JTextField(listenIP);
        txtServer.setBounds(150, 30, 100, 30);
        pn.add(txtServer);

        JLabel lblForeignIP = new JLabel(foreignIP);
        lblForeignIP.setBounds(300, 30, 100, 30);
        pn.add(lblForeignIP);

        lblPort = new JLabel("Port");
        lblPort.setBounds(50, 80, 100, 30);
        pn.add(lblPort);

        txtPort = new JTextField("8888");
        txtPort.setBounds(150, 80, 100, 30);
        pn.add(txtPort);

        lblPassword = new JLabel("Password");
        lblPassword.setBounds(50, 130, 100, 30);
        pn.add(lblPassword);

        txtPassword = new JTextField("sun");
        txtPassword.setBounds(150, 130, 100, 30);
        pn.add(txtPassword);

        jbutton = new JButton("Start Proxy");
        jbutton.setMnemonic(KeyEvent.VK_I);
        jbutton.addActionListener(this);
        jbutton.setBounds(100, 180, 200, 30);
        pn.add(jbutton);
        //----------------------Pannel Components---------------------------

        //Display the window.
        frame.add(pn);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(400, 280);
    }
    
    public void await(String serverIP, String serverPort) throws IOException {
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
                ClinetProxy cp = new ClinetProxy(socket, serverIP, serverPort);
                Thread t = new Thread(cp);
                t.start();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}