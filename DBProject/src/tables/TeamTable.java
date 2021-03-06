package tables;

import db.Criteria;
import db.QueryResult;
import objects.Player;
import objects.Team;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Class to make and edit the coach table
 * @author Noah Shiotani
 */
public class TeamTable {

    /**
     * Reads a csv file for team data and adds them to the team table
     * <p>
     * the team table must already exist
     *
     * @param conn     the database connection
     * @param fileName the name of the csv file to read from
     * @throws SQLException
     */
    public static void populateTeamTableFromCSV(Connection conn, String fileName) throws SQLException {

        //ArrayList to store the teams that are later added to the table
        ArrayList<Team> teams = new ArrayList<Team>();

        //Read in the data
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                if(line.isEmpty()) {
                    continue;
                }

                String[] data = line.split(",");
                if(data[0].equals("tid")) {
                    continue;
                }
                teams.add(new Team(data));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Create an sql query to insert all the teams into the table at once
        String sql = createTeamInsertSQL(teams);

        //Create and execute the sql statement
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }

    /**
     * Create a team table if it does not exist
     * @param conn the database connection
     */
    public static void createTeamTable(Connection conn){
        try{
            String query = "CREATE TABLE IF NOT EXISTS teams("
                    + "TEAM_ID VARCHAR(3) PRIMARY KEY,"
                    + "NAME VARCHAR(255),"
                    + "LOCATION VARCHAR(255),"
                    + "LEAGUE VARCHAR(1)"
                    + ");" ;
            Statement stmt = conn.createStatement();
            stmt.execute(query);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Creates an SQL query to add teams to the team table from an array list
     * @param teams list of teams to add
     * @return the SQL query as a string
     */
    public static String createTeamInsertSQL(ArrayList<Team> teams){
        StringBuilder sb = new StringBuilder();

        //The start of the statement that tells it the order of attributes
        sb.append("INSERT INTO teams (TEAM_ID, NAME, LOCATION, LEAGUE) VALUES");

        for(int i = 0; i < teams.size(); i++){
            Team t = teams.get(i);
            sb.append(String.format("(\'%s\',\'%s\',\'%s\',\'%s\')",
                    t.getTeam_id(), t.getName(), t.getLocation(), t.getLeague()));

            //If it's the last list, add a semi-colon to end the statement
            if(i != teams.size() - 1){
                sb.append(",");
            }
            else{
                sb.append(";");
            }
        }

        return sb.toString();
    }

    public static ArrayList<QueryResult> selectTeams(Connection conn) {
        String query = "SELECT * FROM teams;";
        ArrayList<QueryResult> results = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);

            while(result.next()){
                results.add(new Team(result.getString(1), result.getString(2), result.getString(3), result.getString(4)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static ArrayList<QueryResult> selectTeams(Connection conn, ArrayList<Criteria> criterias) {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT * FROM teams WHERE ");

        String border = "";
        for(Criteria crit : criterias) {
            sb.append(border);
            sb.append(crit.getParameter() + "='" + crit.getValue() + "'");
            border = ",";
        }
        sb.append(";");

        ArrayList<QueryResult> results = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sb.toString());

            while(result.next()){
                results.add(new Team(result.getString(1), result.getString(2), result.getString(3), result.getString(4)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}
