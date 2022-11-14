import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

class QueryRunner implements Runnable {
    // Declare socket for client access
    protected Socket socketConnection;

    public QueryRunner(Socket clientSocket) {
        this.socketConnection = clientSocket;
    }

    public void run() {
        try {
            // Reading data from client
            InputStreamReader inputStream = new InputStreamReader(socketConnection
                    .getInputStream());
            BufferedReader bufferedInput = new BufferedReader(inputStream);
            OutputStreamWriter outputStream = new OutputStreamWriter(socketConnection
                    .getOutputStream());
            BufferedWriter bufferedOutput = new BufferedWriter(outputStream);
            PrintWriter printWriter = new PrintWriter(bufferedOutput, true);
            String clientCommand = "";
            String responseQuery = "";
            // Read client query from the socket endpoint
            clientCommand = bufferedInput.readLine();

        
            // System.out.println("HELLO");

            while (!clientCommand.equals("#")) {

                /********************************************/

                while(true){

                        Connection c = null;
                        try {
                            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/project", "cs301_pro", "1234");
                            
                            String[] splited = clientCommand.split("\\s+");
        
                            int size=splited.length;
        
                            String train_number=splited[size-3];
                            String coach_type=splited[size-1];
                            String date=splited[size-2];
        
        
                            String query="select insert_ticket("+splited[0]+",'"+train_number+"','"+coach_type+"','"+date+"',array[";
        
                            for (int i=1;i<size-3;i++){
                                if( splited[i].endsWith(",")){
                                    splited[i]="'"+splited[i].substring(0,splited[i].length()-1)+"',";
                                }
                                else{
                                    splited[i]="'"+splited[i]+"'";
                                }
                                query+=splited[i];
                            }

                            query+="]);";
            
                            c.createStatement().execute("BEGIN;");
            
                            c.createStatement().execute("SET TRANSACTION ISOLATION LEVEL READ COMMITTED;");
        
                            String createTableSQL = query;
            
                            Statement statement = c.createStatement();
                            
                            ResultSet rs=statement.executeQuery(createTableSQL);
        
                            c.createStatement().execute("commit;");
        
                            c.close();
        
                            rs.next();
                            responseQuery=rs.getString(1);

                            break;
        
                        } catch (SQLException e) {
                            String a=e.getMessage();
                            if(a.contains("100E")){
                                responseQuery="TRAIN DOES NOT EXIST";
                                break;
                            }
                            else if(a.contains("200E")){
                                responseQuery="TRAIN DOES NOT RUN ON THIS DAY";
                                break;
                            }
                            else if(a.contains("300E")){
                                responseQuery="TRAIN DOES NOT HAVE REQUIRED SEATS";
                                break;
                            }
                            else{
                                System.out.println(e.getMessage());
                                continue;
                            }
                        }
                        finally{
                            try {
                                if (c != null) {
                                    c.close();
                                }
                            } catch (SQLException ex) {
                                System.out.println(ex.getMessage());
                            }
                        }
                }
                printWriter.println(responseQuery);
                clientCommand = bufferedInput.readLine();
            }

            inputStream.close();
            bufferedInput.close();
            outputStream.close();
            bufferedOutput.close();
            printWriter.close();
            socketConnection.close();
        } catch (IOException e) {
            return;
        }
    }
}

/**
 * Main Class to controll the program flow
 */
public class ServiceModule {
    // Server listens to port
    static int serverPort = 7008;
    // Max no of parallel requests the server can process
    static int numServerCores = 50;

    // ------------ Main----------------------
    public static void main(String[] args) throws IOException {
        // Creating a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numServerCores);

        try (// Creating a server socket to listen for clients
                ServerSocket serverSocket = new ServerSocket(serverPort)) {
            Socket socketConnection = null;

            // Always-ON server
            while (true) {
                System.out.println("Listening port : " + serverPort
                        + "\nWaiting for clients...");
                socketConnection = serverSocket.accept(); // Accept a connection from a client
                System.out.println("Accepted client :"
                        + socketConnection.getRemoteSocketAddress().toString()
                        + "\n");
                // Create a runnable task
                Runnable runnableTask = new QueryRunner(socketConnection);
                // Submit task for execution
                executorService.submit(runnableTask);
            }
        }
    }
}
