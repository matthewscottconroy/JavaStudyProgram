package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class NetworkingQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.NETWORKING; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("net-mc-01", Topic.NETWORKING, 3,
                "Which class represents a TCP client connection to a server?",
                "URL", "ServerSocket", "Socket", "DatagramSocket",
                "c",
                "Socket(host, port) opens a TCP connection to a server. "
                + "Use getInputStream() and getOutputStream() to exchange data. "
                + "ServerSocket listens for incoming connections. "
                + "DatagramSocket is for connectionless UDP, not TCP."),

            mc("net-mc-02", Topic.NETWORKING, 3,
                "What does ServerSocket.accept() do?",
                "Sends an HTTP accept header",
                "Blocks until a client connects and returns a Socket for that connection",
                "Accepts all pending packets at once",
                "Validates the server's SSL certificate",
                "b",
                "ServerSocket.accept() is a blocking call. "
                + "When a client connects, it returns a Socket object for communicating with that specific client. "
                + "To handle multiple clients, call accept() in a loop and handle each returned Socket on a separate thread."),

            mc("net-mc-03", Topic.NETWORKING, 3,
                "Which class makes a simple HTTP GET request in Java 11+?",
                "URLConnection", "HttpURLConnection", "HttpClient (java.net.http)", "Socket",
                "c",
                "java.net.http.HttpClient (Java 11+) provides a modern API: "
                + "HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url)).build(), "
                + "HttpResponse.BodyHandlers.ofString()). "
                + "HttpURLConnection is the older API. HttpClient supports both sync and async requests."),

            mc("net-mc-04", Topic.NETWORKING, 3,
                "What is a port number in networking?",
                "The physical network port on the computer's motherboard",
                "A 16-bit number that identifies a specific service/process on a host",
                "The IP address suffix after the dot",
                "The maximum packet size for a connection",
                "b",
                "Port numbers (0-65535) distinguish different services on the same IP address. "
                + "Well-known ports: HTTP=80, HTTPS=443, SSH=22, SMTP=25. "
                + "When running a server, choose a port > 1024 (ports < 1024 require root/admin on most OSes)."),

            trace("net-tr-01", Topic.NETWORKING, 3,
                "After this server starts, what port is it listening on?",
                "ServerSocket server = new ServerSocket(8080);\n"
                + "System.out.println(\"Listening on port \" + server.getLocalPort());",
                "Listening on port 8080",
                "new ServerSocket(8080) binds to port 8080. getLocalPort() returns 8080. "
                + "The printed output is 'Listening on port 8080'."),

            debug("net-db-01", Topic.NETWORKING, 3,
                "This client code throws ConnectException. What is the most likely cause?",
                "Socket socket = new Socket(\"localhost\", 9090);\n"
                + "// Throws java.net.ConnectException: Connection refused",
                "localhost resolves to the wrong IP address",
                "No server is listening on port 9090 on localhost",
                "Socket requires an import from java.net.client",
                "The port 9090 is reserved and cannot be used",
                "b",
                "ConnectException: Connection refused means the OS rejected the connection — "
                + "typically because nothing is listening on that port. "
                + "Start a ServerSocket on port 9090 first, then connect the client. "
                + "Also check firewall rules if connecting to a remote host."),

            codegen("net-cg-01", Topic.NETWORKING, 3,
                "Which correctly sends the text 'hello' to a server and reads its response?",
                "Socket s = new Socket(\"localhost\", 8080); s.write(\"hello\"); String r = s.read();",
                "try (Socket s = new Socket(\"localhost\", 8080)) { PrintWriter out = new PrintWriter(s.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream())); out.println(\"hello\"); String r = in.readLine(); }",
                "new Socket(\"hello\", 8080);",
                "Socket s = new Socket(); s.send(\"localhost:8080\", \"hello\");",
                "b",
                "Wrap getOutputStream() in PrintWriter for text sending and getInputStream() in BufferedReader for line reading. "
                + "PrintWriter(stream, true) auto-flushes on println. "
                + "Use try-with-resources to close the socket. "
                + "Option A: Socket has no write()/read() methods directly."),

            mc("net-mc-05", Topic.NETWORKING, 3,
                "What is the difference between TCP and UDP?",
                "TCP is faster; UDP is more reliable",
                "TCP guarantees delivery and ordering; UDP is connectionless with no delivery guarantee but lower overhead",
                "UDP is used for web; TCP is used only for file transfer",
                "They are the same protocol with different names",
                "b",
                "TCP (Transmission Control Protocol): connection-oriented, reliable, ordered delivery, flow control. "
                + "Use for: HTTP, email, file transfer where data integrity matters. "
                + "UDP (User Datagram Protocol): no connection, no guarantee — some packets may be lost or arrive out of order. "
                + "Use for: video streaming, gaming, DNS where speed matters more than perfect delivery."),

            mc("net-mc-06", Topic.NETWORKING, 3,
                "Which class makes HTTP requests using Java's built-in client (Java 11+)?",
                "java.net.URL",
                "java.net.http.HttpClient",
                "java.net.HttpConnection",
                "javax.servlet.HttpRequest",
                "b",
                "java.net.http.HttpClient (Java 11) provides a fluent API: "
                + "HttpClient.newHttpClient().send(request, BodyHandlers.ofString()). "
                + "Supports both synchronous (send) and asynchronous (sendAsync) requests, HTTP/2, and WebSockets. "
                + "java.net.URL is the older approach."),

            mc("net-mc-07", Topic.NETWORKING, 3,
                "What is the purpose of a URL in Java networking?",
                "A special type of Socket for secure connections",
                "A class that represents a Uniform Resource Locator and provides methods to open connections to it",
                "A wrapper around a file path for network files",
                "A class that validates IP addresses",
                "b",
                "java.net.URL represents a URL (http, https, ftp, file, etc.). "
                + "url.openStream() returns an InputStream to read the resource. "
                + "url.openConnection() returns a URLConnection for more control (headers, timeouts). "
                + "getClass().getResource('/path') returns a URL to a classpath resource."),

            trace("net-tr-02", Topic.NETWORKING, 3,
                "What does this code print if the server at localhost:8080 is running and echoes the message?",
                "try (Socket s = new Socket(\"localhost\", 8080);\n"
                + "     PrintWriter out = new PrintWriter(s.getOutputStream(), true);\n"
                + "     BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {\n"
                + "    out.println(\"ping\");\n"
                + "    System.out.println(in.readLine());\n"
                + "}",
                "ping",
                "The client sends 'ping'; the echo server sends it back. readLine() reads 'ping'. println prints it."),

            trace("net-tr-03", Topic.NETWORKING, 3,
                "What does this URL parse into?",
                "URL url = new URL(\"https://example.com:8443/api/data?id=5\");\n"
                + "System.out.println(url.getHost() + \" \" + url.getPort());",
                "example.com 8443",
                "getHost() returns the hostname: 'example.com'. getPort() returns the explicit port: 8443. "
                + "If no port is in the URL, getPort() returns -1 (the protocol's default port is implied)."),

            debug("net-db-02", Topic.NETWORKING, 3,
                "The server handles only one client and then exits. What is wrong?",
                "ServerSocket server = new ServerSocket(8080);\n"
                + "Socket client = server.accept();\n"
                + "handleClient(client);\n"
                + "// Server stops after one client",
                "ServerSocket can only handle one connection total",
                "accept() is only called once — after one client, the loop ends (there is no loop)",
                "handleClient() must run on a separate thread",
                "ServerSocket must be recreated for each client",
                "b",
                "To accept multiple clients, wrap accept() in a loop: "
                + "while (true) { Socket client = server.accept(); new Thread(() -> handleClient(client)).start(); } "
                + "The loop blocks on accept() until the next client connects."),

            codegen("net-cg-02", Topic.NETWORKING, 3,
                "Which correctly fetches the content of a URL as a String using Java 11+ HttpClient?",
                "String body = new URL(\"https://api.example.com/data\").readText();",
                "HttpClient client = HttpClient.newHttpClient(); HttpRequest req = HttpRequest.newBuilder(URI.create(\"https://api.example.com/data\")).build(); HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString()); String body = resp.body();",
                "String body = Socket.get(\"https://api.example.com/data\");",
                "URLConnection conn = new URL(\"https://api.example.com/data\").openConnection(); String body = conn.read();",
                "b",
                "HttpClient → HttpRequest.newBuilder(URI) → send() with BodyHandlers.ofString() → response.body(). "
                + "This is the modern Java 11+ approach. "
                + "Option A: URL has no readText(). Option D: URLConnection has no read() method.")
        );
    }
}
