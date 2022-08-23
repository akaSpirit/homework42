import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer {
    private final int port;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private static List<Socket> clientSockets = new ArrayList<>();
    private static HashMap<Socket, String> clients = new HashMap<>();

    private EchoServer(int port) {
        this.port = port;
    }

    public static EchoServer bindToPort(int port) {
        return new EchoServer(port);
    }

    public void run() {
        try (var server = new ServerSocket(port)) {
            while (!server.isClosed()) {
                Socket clientSocket = server.accept();
                checkConnection(clientSocket);
            }
        } catch (IOException e) {
            System.out.printf("Вероятнее всего порт %s занят.%n", port);
            e.printStackTrace();
        }
    }

    public void checkConnection(Socket clientSocket) {
        System.out.printf("Подключен клиент: %s%n", clientSocket.getPort());
        pool.submit(() -> {
        try {
            clientSockets.add(clientSocket);
            EchoServerService.handle(clientSocket, clients, clientSockets);
        } catch (NoSuchElementException e) {
            System.out.printf("Отключен клиент: %s%n", clientSocket.getPort());
            clientSockets.remove(clientSocket);
            clients.remove(clientSocket);
            e.printStackTrace();
        }
    });
}


}
