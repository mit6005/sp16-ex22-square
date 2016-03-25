package square;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SquareServerMulti is a server that squares integers passed to it.
 * It accepts requests of the form:
 *      Request ::= Number "\n"
 *      Number ::= [0-9]+
 * and for each request, returns a reply of the form:
 *      Reply ::= (Number | "err") "\n"
 * where a Number is the square of the request number,
 * or "err" is used to indicate a misformatted request.
 * SquareServerMulti can handle multiple concurrent clients.
 */
public class SquareServerMulti {
    /** Default port number where the server listens for connections. */
    public static final int SQUARE_PORT = 4949;
    
    private ServerSocket serverSocket;
    // Rep invariant: serverSocket != null
    //
    // Thread safety argument:
    //   TODO SQUARE_PORT
    //   TODO serverSocket
    //   TODO socket objects
    //   TODO readers and writers in handle()
    //   TODO data in handle()
    
    /**
     * Make a SquareServerMulti that listens for connections on port.
     * @param port port number, requires 0 <= port <= 65535
     */
    public SquareServerMulti(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
    
    /**
     * Run the server, listening for connections and handling them.
     * @throws IOException if the main server socket is broken
     */
    public void serve() throws IOException {
        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            // create a new thread to handle that client
            Thread handler = new Thread(new Runnable() {
                public void run() {
                    try {
                        try {
                            handle(socket);
                        } finally {
                            socket.close();
                        }
                    } catch (IOException ioe) {
                        // this exception wouldn't terminate serve(),
                        // since we're now on a different thread, but
                        // we still need to handle it
                        ioe.printStackTrace();
                    }
                }
            });
            // start the thread
            handler.start();
        }
    }
    
    /**
     * Handle one client connection. Returns when client disconnects.
     * @param socket socket where client is connected
     * @throws IOException if connection encounters an error
     */
    private void handle(Socket socket) throws IOException {
        System.err.println("client connected");
        
        // get the socket's input stream, and wrap converters around it
        // that convert it from a byte stream to a character stream,
        // and that buffer it so that we can read a line at a time
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // similarly, wrap character=>bytestream converter around the
        // socket output stream, and wrap a PrintWriter around that so
        // that we have more convenient ways to write Java primitive
        // types to it.
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        try {
            // each request is a single line containing a number
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                System.err.println("request: " + line);
                try {
                    int x = Integer.valueOf(line);
                    // compute answer and send back to client
                    int y = x * x;
                    System.err.println("reply: " + y);
                    out.println(y);
                } catch (NumberFormatException e) {
                    // complain about ill-formatted request
                    System.err.println("reply: err");
                    out.print("err\n");
                }
                // important! our PrintWriter is auto-flushing, but if it were not:
                // out.flush();
            }
        } finally {
            out.close();
            in.close();
        }
    }
    
    /**
     * Start a SquareServerMulti running on the default port.
     */
    public static void main(String[] args) {
        try {
            SquareServerMulti server = new SquareServerMulti(SQUARE_PORT);
            server.serve();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
