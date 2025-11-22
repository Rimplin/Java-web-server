import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer {
    public static void main(String[] args) throws Exception {
        int port = 6789;
        ServerSocket server_socket = new ServerSocket(port);
        System.out.println("Listening on port: " + port);
        while (true) {
            Socket client_socket = server_socket.accept();
            HttpRequest request = new HttpRequest(client_socket);
            Thread thread = new Thread(request);
            // Start the execution of the thread
            thread.start();
        }
    }
}

class HttpRequest implements Runnable {
    final String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    private void processRequest() throws Exception {
        // Get reference to the socket's input and output streams
        InputStream is = new DataInputStream(socket.getInputStream());
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        // Set up input stream filters
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        // Get the request line of the HTTP request message.
        String requestLine = br.readLine();
        System.out.println();
        System.out.println(requestLine);

        // Get and display the header lines.
        String headerLine = null;
        while((headerLine = br.readLine()).length() != 0){
            System.out.println(headerLine);
        }

        // Close streams and socket.
        os.close();
        br.close();
        socket.close();
    }

    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
        // Your code goes here — what should happen when the thread starts
        System.out.println("Handling HTTP request...");
    }
}