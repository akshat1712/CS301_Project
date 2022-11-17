import java.io.File;
import java.util.Scanner;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class EXTRA_TEAM_TRAIN_ADD {
    public static void main(String[] args) {

        try {
            String[] trains = { "12012", "12046", "12058", "12650", "12992", "19412", "22692" };
            // String[] trains = { "22692"};

            for (int i = 0; i < trains.length; i++) {
                String inputfile = "./" + trains[i] + ".txt";

                File queries = new File(inputfile);

                Scanner queryScanner = new Scanner(queries);
                String clientcommand = queryScanner.nextLine();

                
                Vector<String> station_code = new Vector<String>(); 
                Vector<String> arrival_time= new Vector<String>(); 
                Vector<String> departure_time = new Vector<String>(); 
                Vector<String> station_name = new Vector<String>(); 
                Vector<Integer> arrival_day = new Vector<Integer>(); 
                Vector<Integer> departure_day = new Vector<Integer>(); 

                while (!clientcommand.equals("#")) {


                        String[] splited = clientcommand.split("\\s+");

                        station_code.add(splited[0]);

                        String now=splited[1];
                        int day_num=0;
                        for( int j=0;j<now.length();j++){
                            if(now.charAt(j)=='*'){
                                day_num++;
                            }
                        }

                        now=now.replace("*", "");

                        arrival_time.add(now);
                        arrival_day.add(day_num);

                        now=splited[splited.length-1];
                        day_num=0;
                        for( int j=0;j<now.length();j++){
                            if(now.charAt(j)=='*'){
                                day_num++;
                            }
                        }
                        
                        now=now.replace("*", "");

                        departure_time.add(now);
                        departure_day.add(day_num);                    

                        now="";
                        
                        for ( int j =2;j<splited.length-1;j++){
                            if(j==splited.length-2){
                                now=now+splited[j];
                            }
                            else{
                                now=now+splited[j]+" ";
                            }
                        }

                        station_name.add(now);
                        clientcommand = queryScanner.nextLine();

                }
                


                try{
                    Connection c = null;
                    c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/project", "cs301_pro", "1234");

                    for( int j=0;j<station_code.size();j++){
                        for( int k=j+1;k<station_code.size();k++)
                        {
                            String Query=String.format("insert into service values('%s','%s','%s','%s',%d,'%s',%d);",
                            trains[i],station_name.get(j),station_name.get(k),departure_time.get(j),departure_day.get(j),arrival_time.get(k),arrival_day.get(k));

                            c.createStatement().execute(Query);
                        }
                    }

                    
                }catch(SQLException e){
                    System.out.println(e.getMessage());
                }
                queryScanner.close();

            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
    }
}