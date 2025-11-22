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

        // Extract the filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); // skip over the method, which should be "GET"
        String fileName = tokens.nextToken();
        // Prepend a "." so that file request is within the current directory.
        fileName = "." + fileName;

        // Open the requested file.
        FileInputStream fis = null;
        boolean fileExists = true;
        try{
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e){
            fileExists = false;
        }

        // Construct the response message.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;

        if (fileExists) {
            statusLine = "HTTP/1.0 200 OK" + CRLF;
            contentTypeLine = "Content-type: " + contentType( fileName ) + CRLF;

            System.out.println("Received: " + statusLine);
        }else {
            statusLine = "HTTP/1.0 404 Not Found" + CRLF;
            contentTypeLine = "Content-Type: text/html" + CRLF;
            entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
            "<BODY>Not Found</BODY></HTML>";

            System.out.println("Received: " + statusLine);
        }

        // Send the status line.
        os.writeBytes(statusLine);
        // Send the content type line.
        os.writeBytes(contentTypeLine);
        //Send a blank line to indicate the end of the header lines.
        os.writeBytes(CRLF);

        // Send the entity body.
        if (fileExists) {
            sendBytes(fis, os); 
            fis.close();
        } else {
            os.writeBytes(entityBody);
        }

        // Close streams and socket.
        os.close();
        br.close();
        socket.close();
    }

    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        // Construct a 1K buffer to hold bytes on their way to the socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;
        // Copy requested file into the socket's output stream.
        while((bytes = fis.read(buffer)) != -1 ){
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String fileName) {
        if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if(fileName.endsWith(".gif")) {
            return "image/gif";
        }
        if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")){
            return "image/jpeg";
        }
        if (fileName.endsWith(".css")){
            return "text/css";
        }
        if (fileName.endsWith(".js")){
            return "application/javascript";
        }

        return "application/octet-stream";
    }

    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}