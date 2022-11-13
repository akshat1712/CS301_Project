
import java.io.File;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;


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

                    String query="Select insert_service_train('"+splited[0]+"','"+splited[1]+"',"+splited[2]+","+splited[3]+");";
                    System.out.println(query);                    
                    c.createStatement().execute(query);
                    c.close();
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
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
