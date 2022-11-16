
import java.io.File;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;


public class add_trains_extra {
    public static void main(String[] args) {

        try{
            String[] trains = {"12012", "12046", "12058", "12650", "12992", "19412", "22692"};
            for (int i=0;i<trains.length;i++)
            {
                String inputfile = "./ExtraPart/" + trains[i] + ".txt";
                // String inputfile="./Trainschedule.txt";
                File queries = new File(inputfile);
        
                Scanner queryScanner = new Scanner(queries);
                String clientcommand = queryScanner.nextLine();
    
                while(!clientcommand.equals("#")){
                    
                    Connection c = null;
                    try {
                        c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/project", "cs301_pro", "1234");
                        String[] splited = clientcommand.split("\\s+");

                        String station_code = splited[0];
                        String arrival_time = splited[1];
                        String departure_time = splited[splited.length-1];
                        String station_name = "";
                        int day_num = 0;
                        for(int j=2;j<splited.length-1;j++)
                        {
                            station_name += splited[j];
                        }
                        while(departure_time.charAt(0)=='*')
                        {
                            day_num++;
                            departure_time = departure_time.substring(1, departure_time.length()-1);
                        }
                        
                        String query="Select insert_service_extra('"+trains[i]+"', '"+station_code+"','"+arrival_time+"','"+station_name+"','"+departure_time+"', "+day_num+");";
                                       
                        c.createStatement().execute(query);
                        c.close();
                        System.out.println(query);
                    } catch (Exception e) {
                        System.err.println("Error: " + e.getMessage());
                    }
                    clientcommand = queryScanner.nextLine();
    
                }
    
                queryScanner.close();
            }
            
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }

        }
    }
