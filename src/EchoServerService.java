import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class EchoServerService {
    private static HashMap<Socket, String> clients = new HashMap<>();
    private static String name;

    public static void handle(Socket socket) {
        name = String.format("user" + socket.getPort()).replaceAll(" ", "");
        System.out.printf("Подключен клиент: %s%n", name);
//        System.out.printf("Подключен клиент: %s%n", socket);

//        if (socket.isClosed()) clients.remove(socket, name);

        try (Scanner reader = getReader(socket);
             PrintWriter writer = getWriter(socket);
             socket) {

            clients.put(socket, name);
            sendResponse("Привет, " + name, writer);  //socket

            while (true) {
                String message = reader.nextLine().strip();
                if (isEmptyMsg(message) || isQuitMsg(message)) {
                    clients.remove(socket, name);
                    break;
                }
                System.out.printf("Получено от клиента %s: %s%n", socket.getPort(), message);

                if (message.contains("/name ")) {
                    changeName(message, socket, writer);
                } else if (message.contains("/list")) {
                    getUsersList(writer);
                } else if (message.contains("/whisper ")) {
                    privateMessage(message, socket, writer);
                } else {

                    for (var c : clients.keySet()) {
                        if (c != socket) { // && !socket.isClosed()
                            sendResponse(clients.get(socket) + ": " + message.toUpperCase(), getWriter(c));  //message
                        }
                    }
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("Клиент закрыл соединение!");
            clients.remove(socket, name);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.printf("Клиент отключен: %s%n", socket);
            clients.remove(socket, name);
        }
    }

    private static PrintWriter getWriter(Socket socket) throws IOException {
//        if (socket.isClosed()) clients.remove(socket, name);
        OutputStream stream = socket.getOutputStream();
//        stream.flush();
        return new PrintWriter(stream);
    }

    private static Scanner getReader(Socket socket) throws IOException {
        InputStream stream = socket.getInputStream();
        InputStreamReader input = new InputStreamReader(stream, "UTF-8");
        return new Scanner(input);
    }

    private static boolean isQuitMsg(String message) {
        return "bye".equalsIgnoreCase(message);
    }

    private static boolean isEmptyMsg(String message) {
        return message == null || message.isBlank();
    }

    private static void sendResponse(String response, Writer writer) throws IOException {
        writer.write(response);
        writer.write(System.lineSeparator());
        writer.flush();
    }

    private static void changeName(String message, Socket socket, Writer writer) throws IOException {
        String newName = message.replace("/name ", "");

        for (var c : clients.values()) {
            if (c.equalsIgnoreCase(newName.replaceAll(" ", ""))) {
                sendResponse("Не удалось сменить имя", writer);
                return;
            }
        }
        sendResponse("Вы теперь известны как " + newName, writer);
        for (var s : clients.keySet()) {
            if (s != socket) { // && !socket.isClosed()
                sendResponse("Пользователь " + clients.get(socket) + " теперь известен как " + newName, getWriter(s));
            }
        }
        clients.put(socket, newName);
    }

    private static void getUsersList(Writer writer) throws IOException {
        sendResponse(String.valueOf(clients.values()).replace("[","").replace("]",""), writer);
    }

    private static void privateMessage(String message, Socket socket, Writer writer) throws IOException {
        for (var c : clients.values()) {
            if (message.contains(c)) {
                String msg = message.replace("/whisper ","").replace(c+" ", "");

                for(var s: clients.entrySet()){
                    if(c.equals(s.getValue())){
                        Socket key = s.getKey();
                        sendResponse(clients.get(socket) + ": " + msg, getWriter(key));
                        break; //breaking because its one to one map
                    }
                }

//                for (var s: clients.keySet()) {
//                    if(s != socket)
//                }
            }
        }
    }

//    public static String reverseString(String str) {
//        StringBuilder sb = new StringBuilder(str);
//        sb.reverse();
//        return sb.toString();
//    }

}
