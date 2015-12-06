import java.io.*;
import java.text.*;
import java.net.*;
import java.lang.Object;
import java.util.regex.*;
import java.sql.*;

public class Recorder{

    public static void main (String argv []) throws IOException
    {
        try
        {
          // create our mysql database connection
          Class.forName ("com.mysql.jdbc.Driver").newInstance();
          Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/recorder", "root", "password");
           
          // our SQL SELECT query. 
          // if you only need a few columns, specify them by name instead of using "*"
          String query = "SELECT * FROM userdata";
     
          // create the java statement
          Statement st = conn.createStatement();
           
          // execute the query, and get a java resultset
          ResultSet rs = st.executeQuery(query);
           
          // iterate through the java resultset
          while (rs.next())
          {
            String name = rs.getString("name");
            String region = rs.getString("region");
            String line = name + ":" + region;
            new ThreadTest(line).start();   //Create new thread for each username we are searching for
          }
          st.close();
        }
        catch (Exception e)
        {
          System.err.println("Got an exception! ");
          System.err.println(e.getMessage());
        }
    }
}

class ThreadTest extends Thread {
    public ThreadTest(String str) {
        super(str);
    }
 
    public void run() {
        String userName = "";
        String region = "";
        String [] input = getName().split(":");
        userName = input[0];
        region = input[1];
        String answer = "";
        try{
            answer = getInGameInfo(userName, region);
        }
        catch (Exception e){
            System.out.println("IO Error");
        }
        String gameId = "";
        if (answer.contains("RequestRecording")){
            Pattern findUrl = Pattern.compile("id=\\\"");
            gameId = answer.substring(answer.indexOf("id=") + 3, answer.indexOf("\"",answer.indexOf("id=")+3));
            System.out.println("Requesting recording for " + userName + " with game Id " + gameId);
            try {
                requestRecording(userName,region,gameId);
            }
            catch (Exception e){
                System.out.println("IO Error");
            }
        }
        else{
            System.out.println("Summoner " + userName + " is not in a game");
        }
    }

    public void requestRecording(String username, String region, String Id) throws IOException{
        URL url = new URL("http://" + region + ".op.gg/summoner/ajax/requestRecording.json/gameId=" + Id);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("Origin", "http://www.op.gg");
        connection.setRequestProperty("Referer", "http://" + region + "op.gg/summoner/userName=" + username);
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while((line = rd.readLine()) != null) {
          response.append(line);
          response.append('\r');
        }
        rd.close();
        String answer = response.toString();
    }
    
    public String getInGameInfo(String username, String region) throws IOException{
        URL url = new URL("http://" + region + ".op.gg/summoner/ajax/spectator/");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Referer", "http://" + region + "op.gg/summoner/userName=" + username);
        connection.setRequestProperty("Accept-Language", "en-US");
        connection.setRequestProperty("Origin", "http://www.op.gg");
        connection.setDoOutput(true);
        //Send request
        DataOutputStream wr = new DataOutputStream (
            connection.getOutputStream());
        wr.writeBytes("userName=" + username + "&force=true");
        wr.close();

        //Get Response  
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while((line = rd.readLine()) != null) {
          response.append(line);
          response.append('\r');
        }
        rd.close();
        return response.toString();
    }
}