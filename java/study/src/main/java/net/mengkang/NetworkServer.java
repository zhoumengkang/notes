package net.mengkang;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhoumengkang on 15/12/15.
 */
public class NetworkServer implements Runnable {

    private final ServerSocket serverSocket;
    private final ExecutorService pool;

    public NetworkServer(int port, int poolSize) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.pool = Executors.newFixedThreadPool(poolSize);
    }

    public void run() {
        try {
            for (;;) {
                pool.execute(new Handler(serverSocket.accept()));
            }
        } catch (IOException e) {
            pool.shutdown();
        }
    }

    class Handler implements Runnable {
        private final Socket socket;
        Handler(Socket socket) {
            this.socket = socket;
        }
        public void run() {

        }
    }

    void shutdownAndAwaitTermination(ExecutorService pool){
        pool.shutdown();
        try {
            if (pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();

                if (pool.awaitTermination(60,TimeUnit.SECONDS)) {
                    System.out.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
