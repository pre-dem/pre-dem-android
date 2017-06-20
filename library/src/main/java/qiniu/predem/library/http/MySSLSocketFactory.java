package qiniu.predem.library.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Misty on 17/6/15.
 */

public class MySSLSocketFactory extends SSLSocketFactory {
    private static final String TAG = "MySSLSocketFactory";
    protected final String hostIp;
    public MySSLSocketFactory(String hostIp) {
        super();
        this.hostIp = hostIp;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return HttpsURLConnection.getDefaultSSLSocketFactory().getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return HttpsURLConnection.getDefaultSSLSocketFactory().getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return HttpsURLConnection.getDefaultSSLSocketFactory().createSocket(s, hostIp, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return HttpsURLConnection.getDefaultSSLSocketFactory().createSocket(hostIp, 443);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return HttpsURLConnection.getDefaultSSLSocketFactory().createSocket(hostIp, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return HttpsURLConnection.getDefaultSSLSocketFactory().createSocket(hostIp, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return HttpsURLConnection.getDefaultSSLSocketFactory().createSocket(InetAddress.getByName(hostIp), port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return HttpsURLConnection.getDefaultSSLSocketFactory().createSocket(InetAddress.getByName(hostIp), port, localAddress, localPort);
    }
}
