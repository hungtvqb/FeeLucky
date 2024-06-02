package mono.soap;


import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * This class accepts client connection on given port. When the connection
 * is accepted, the listener creates an instance of <code>ServerSession</code>,
 * generates new <code>PDUProcessor</code> using object derived from
 * <code>PDUProcessorFactory</code>, passes the processor to the smsc session
 * and starts the session as a standalone thread.
 */
public class HttpListener implements Runnable {

    public static int RUNNING = 1;
    public static int FINISH = 0;
    private ServerSocket[] serverSockets;
    private long acceptTimeout = 1000;
    private final Object lock;
    private int status;
    private final int port[];
    private boolean isReceiving;
    private final boolean isFirst;
    

    /**
     * Constructor with control if the listener starts as a separate thread.
     * If <code>asynchronous</code> is true, then the listener is started
     * as a separate thread, i.e. the creating thread can continue after
     * calling of method <code>enable</code>. If it's false, then the
     * caller blocks while the listener does it's work, i.e. listening.
     *
     * @param port   list of listener port     
     * @see #start()
     */
    public HttpListener(int port) {
        this.lock = new Object();
        this.port = new int[]{port};
        this.isFirst = true;
    }

    /**
     * Starts the listening. If the listener is asynchronous (reccomended),
     * then new thread is created which listens on the port and the
     * <code>enable</code> method returns to the caller. Otherwise
     * the caller is blocked in the enable method.
     *
     * @see #start()
     */
    public void start() {
        if (isFirst) {
            instance(port);
        }

        if (serverSockets == null) {
            return;
        }
        synchronized (lock) {
            for (int i = 0; i < serverSockets.length; i++) {
                if (serverSockets[i] == null) {
                    continue;
                }
                if (serverSockets[i].isClosed()) {
                    int p = serverSockets[i].getLocalPort();
                    try {
                        serverSockets[i] = createSocket(p);
                    } catch (IOException e) {
                    }
                }
            }
        }
        status = RUNNING;
        isReceiving = true;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Signals the listener that it should disable listening and wait
     * until the listener stops. Note that based on the timeout settings
     * it can take some time befor this method is finished -- the listener
     * can be blocked on i/o operation and only after exiting i/o
     * it can detect that it should disable.
     *
     * @see #start()
     */
    public void stop() {
        try {
            //noinspection SynchronizeOnNonFinalField
            synchronized (lock) {
                isReceiving = false;
                while (status == RUNNING) {
                    Thread.yield();
                }
                for (ServerSocket serverSocket : serverSockets) {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                }
            }
        } catch (IOException e) {
//             lo
        }
    }

    /**
     * The actual listening code which is run either from the thread
     * (for async listener) or called from <code>enable</code> method
     * (for sync listener). The method can be exited by calling of method
     * <code>disable</code>.
     *
     * @see #start()
     * @see #stop()
     */
    @Override
    public void run() {
        if (status != RUNNING) {
            return;
        }
        try {
            while (isReceiving) {
                listen();
                Thread.yield();
            }
        } finally {
            status = FINISH;
        }
    }

    /**
     * The "one" listen attempt called from <code>run</code> method.
     * The listening is atomicised to allow contoled stopping of the listening.
     * The length of the single listen attempt
     * is defined by <code>acceptTimeout</code>.
     * If a connection is accepted, then new session is created on this
     * connection, new PDU processor is generated using PDU processor factory
     * and the new session is started in separate thread.
     *
     * @see #run()
     */
    private void listen() {
        synchronized (lock) {
            for (ServerSocket serverSocket : serverSockets) {
                try {
                    if (serverSocket == null || serverSocket.isClosed()) {
                        continue;
                    }
                    Socket socket = serverSocket.accept();
                    if (socket == null) {
                        Thread.sleep(1);
                        continue;
                    }
                    HttpSession httpSession = new HttpSession(socket);
                    httpSession.start();
                }catch (Exception e) {
                    
                }
            }
        }
    }

    /**
     * Sets new timeout for accepting new connection.
     * The listening blocks the for maximum this time, then it
     * exits regardless the connection was acctepted or not.
     *
     * @param value the new value for accept timeout
     */
    public void setAcceptTimeout(int value) {
        acceptTimeout = value;
    }

    /**
     * Returns the current setting of accept timeout.
     *
     * @return the current accept timeout
     * @see #setAcceptTimeout(int)
     */
    public long getAcceptTimeout() {
        return acceptTimeout;
    }

    private ServerSocket createSocket(int port) throws IOException {
        ServerSocket socket = new ServerSocket(port);
        socket.setSoTimeout((int) getAcceptTimeout());
        return socket;
    }

    protected void instance(int port[]) {
        if (port == null) {
            return;
        }
        int max = port.length;
        serverSockets = new ServerSocket[max];
        for (int i = 0; i < max; i++) {
            try {
                serverSockets[i] = createSocket(port[i]);
            } catch (IOException e) {
                
            }
        }
    }

    /**
     * check listen port
     *
     * @param port port value will be search server socket
     * @return ServerSocket
     */
    private int findServerSocket(int port) {
        if (serverSockets == null) {
            return -1;
        }
        synchronized (lock) {
            for (int i = 0; i < serverSockets.length; i++) {
                if (serverSockets[i] != null && serverSockets[i].getLocalPort() == port) {
                    return i;
                }
            }
        }
        return -1;
    }


    /**
     * do reload listen port
     *
     * @param port the list of listener port
     */
    public void reloadConfig(int port[]) {
        //noinspection SynchronizeOnNonFinalField
        synchronized (lock) {
            if (port == null) {
                return;
            }
            ServerSocket newSocket[] = new ServerSocket[port.length];
            int movSocket[] = new int[serverSockets.length];
            for (int i = 0; i < movSocket.length; i++) {
                movSocket[i] = -1;
            }
            for (int i = 0; i < port.length; i++) {
                int indexOfPort = findServerSocket(port[i]);
                if (indexOfPort == -1) {
                    try {
                        newSocket[i] = createSocket(port[i]);
                    } catch (IOException e) {
                        
                    }
                } else {
                    newSocket[i] = serverSockets[indexOfPort];
                    movSocket[indexOfPort] = 1;
                }
            }
            //close unused socket
            for (int i = 0; i < movSocket.length; i++) {
                if (movSocket[i] == 1) {
                    continue;
                }
                try {
                    int _port = serverSockets[i].getLocalPort();
                    serverSockets[i].close();
                } catch (IOException e) {
                }
            }
            serverSockets = newSocket;
        }
        String log = "VASPServer listen on Port: ";
        for (int i = 0; i < serverSockets.length; i++) {
            if (serverSockets[i] != null) {
                log += serverSockets[i].getLocalPort();
            }
            if (i != serverSockets.length - 1) {
                log += ":";
            }
        }
    }
}

