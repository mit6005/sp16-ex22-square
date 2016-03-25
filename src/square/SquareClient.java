package square;

import java.io.*;
import java.net.Socket;

/**
 * SquareClient is a client that sends requests to the SquareServer
 * and interprets its replies.
 * A new SquareClient is "open" until the close() method is called,
 * at which point it is "closed" and may not be used further.
 */
public class SquareClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    // Rep invariant: socket, in, out != null
    
    /**
     * Make a SquareClient and connect it to a server running on
     * hostname at the specified port.
     * @throws IOException if can't connect
     */
    public SquareClient(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
    
    /**
     * Send a request to the server. Requires this is "open".
     * @param x number to square
     * @throws IOException if network or server failure
     */
    public void sendRequest(int x) throws IOException {
        out.print(x + "\n");
        out.flush(); // important! make sure x actually gets sent
    }
    
    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     * @return square of requested number
     * @throws IOException if network or server failure
     */
    public int getReply() throws IOException {
        String reply = in.readLine();
        if (reply == null) {
            throw new IOException("connection terminated unexpectedly");
        }
        
        try {
            return Integer.valueOf(reply);
        } catch (NumberFormatException nfe) {
            throw new IOException("misformatted reply: " + reply);
        }
    }

    /**
     * Closes the client's connection to the server.
     * This client is now "closed". Requires this is "open".
     * @throws IOException if close fails
     */
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
    
    
    
    
    private static final int N = 100;
    
    /**
     * Use a SquareServer to square all the integers from 1 to N.
     */
    public static void main(String[] args) {
        try {
            SquareClient client = new SquareClient("localhost", SquareServer.SQUARE_PORT);

            // send the requests to square 1...N
            for (int x = 1; x <= N; ++x) {
                client.sendRequest(x);
                System.out.println(x + "^2 = ?");
            }
            
            // collect the replies
            for (int x = 1; x <= N; ++x) {
                int y = client.getReply();
                System.out.println(x + "^2 = " + y);
            }
            
            client.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
