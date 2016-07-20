

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Alec Huang
 */
class DBManager {


    private static Logger _log = Logger.getLogger(DBManager.class.getName());
    private static DBManager instance = new DBManager();
    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/sos_usage";
    //  Database credentials
    private static final String USER = "root";
    private static final String PASSWORD = "1234567";

    static DBManager getInstance() {
        return instance;
    }

    void init(){
        try {
            if( !isTableExist() )
                createTable();
        } catch (SQLException e) {
            _log.log(Level.OFF, "Mysql has a connection problem.");
        }

    }

    private Boolean isTableExist() throws SQLException {
        Connection con = getConnection();
        DatabaseMetaData meta = con.getMetaData();
        ResultSet tables = meta.getTables(null, null, "total_records", null);
        Boolean isExist =  tables.next();
        close( con );
        return isExist;
    }

    void write(String ip, String[] httpRequest){
        String sql = "INSERT INTO total_records " +
                "VALUES (NULL, \'" + ip +"\', \'" + httpRequest[0] + "\', \'" + httpRequest[1] + "\', NULL)";
        try {
            execute( sql );
            System.out.println(" new records from: " + ip + httpRequest[0] + httpRequest[1] );
        } catch (SQLException e) {
            _log.log(Level.SEVERE, "Mysql can't insert records. SQL: " + sql);
        }
    }

    private void execute(String sql) throws SQLException {
        Connection con = getConnection();
        Statement stmt = con.createStatement();
        stmt.executeUpdate(sql);
        close( con );
    }

    private void createTable(){
        try {
            String sql = "CREATE TABLE total_records " +
                    "( id INTEGER not NULL AUTO_INCREMENT, " +
                    " ip VARCHAR(45) not NULL, " +
                    " method VARCHAR(255) not NULL, " +
                    " url VARCHAR(255) not NULL, " +
                    " ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    " PRIMARY KEY ( id ))";
            execute( sql );
        } catch (SQLException e) {
            _log.log(Level.SEVERE, "Mysql can't create Table.");
        }
    }

    private void close(Connection con) {
        try {
            con.close();
        } catch (SQLException e) {
            _log.log(Level.SEVERE, "Can't close connection properly.");
        }
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (SQLException ex) {
            _log.log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
