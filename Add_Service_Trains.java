
import java.io.File;
import java.util.Scanner;
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

public class Add_Service_Trains {
    public static void main(String[] args) {

        try{
            String inputfile="./Trainschedule.txt";
            File queries = new File(inputfile);
    
            Scanner queryScanner = new Scanner(queries);
            String clientcommand = queryScanner.nextLine();


            while(!clientcommand.equals("#")){
                
                Connection c = null;
                try {
                    c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/project", "cs301_pro", "1234");
                    String[] splited = clientcommand.split("\\s+");


                    String query="Select insert_service_train('"+splited[0]+"','"+splited[1]+"','"+splited[2]+"','"+splited[3]+"');";
                    System.out.println(query);                    
                } catch (Exception e) {
                    
                }
                clientcommand = queryScanner.nextLine();

            }

            queryScanner.close();
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }

        }
    }
