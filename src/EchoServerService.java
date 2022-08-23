import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class EchoServerService {
    private static String name;

    public static void handle(Socket socket, HashMap<Socket, String> clients, List<Socket> clientSockets) {
        name = String.format("user" + socket.getPort());

        try (Scanner reader = getReader(socket);
             PrintWriter writer = getWriter(socket);
             socket) {

            clients.put(socket, name);
            sendResponse("Привет, " + name, writer);

            while (true) {
                String message = reader.nextLine().strip();
                if (isEmptyMsg(message) || isQuitMsg(message)) {
                    clients.remove(socket, name);
                    clientSockets.remove(socket);
                    break;
                }
                System.out.printf("Получено от клиента %s: %s%n", socket.getPort(), message);

                if (message.contains("/name ")) {
                    changeName(message, socket, writer, clients);
                } else if (message.contains("/list")) {
                    getUsersList(writer, clients);
                } else if (message.contains("/whisper ")) {
                    privateMessage(message, socket, writer, clients);
                } else {
                    for (var c : clientSockets) {
                        if (c != socket) {
                            sendResponse(clients.get(socket) + ": " + message, getWriter(c));  //message
                        }
                    }
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("Клиент закрыл соединение!");
            clientSockets.remove(socket);
            clients.remove(socket, name);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.printf("Клиент отключен: %s%n", socket);
            clientSockets.remove(socket);
            clients.remove(socket, name);
        }
    }

    private static PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream stream = socket.getOutputStream();
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

    private static void changeName(String message, Socket socket, Writer writer, HashMap<Socket, String> clients) throws IOException {
        String newName = message.replace("/name ", "");
        for (var c : clients.values()) {
            if (c.equalsIgnoreCase(newName.replaceAll("\\s", ""))) {
                sendResponse("Не удалось сменить имя, пользователь с таким именем уже существует", writer);
                return;
            }
        }
        if (newName.matches("^[a-zA-Z0-9]{3,12}$")) {
            sendResponse("Вы теперь известны как " + newName, writer);
            for (var s : clients.keySet()) {
                if (s != socket) {
                    sendResponse("Пользователь " + clients.get(socket) + " теперь известен как " + newName, getWriter(s));
                }
            }
            clients.put(socket, newName);
        } else {
            sendResponse("Имя может содержать только буквы и цифры (от 3 до 12 символов).", writer);
        }
    }

    private static void getUsersList(Writer writer, HashMap<Socket, String> clients) throws IOException {
        sendResponse(String.valueOf(clients.values()).replace("[", "").replace("]", ""), writer);
    }

    private static void privateMessage(String message, Socket socket, Writer writer, HashMap<Socket, String> clients) throws IOException {
        String msg = message.replace("/whisper ", "");
        for (var c : clients.values()) {
            if (msg.contains(c)) {
                String msg1 = msg.replace(c + " ", "");
                for (var s : clients.entrySet()) {
                    Socket key = s.getKey();
                    if (c.equals(s.getValue())) {
                        sendResponse(clients.get(socket) + ": " + msg1, getWriter(key));
                        break;
                    }
                }
            }
        }
    }

}
