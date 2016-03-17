package tools;

import com.mysql.jdbc.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DB {

    private static final String password = "neucod";
    private static final String user = "root";
    private static final String url = "//localhost:3306/bd_mots?characterEncoding=UTF-8&useUnicode=true";
    /* private static final String password = "0439152";
     private static final String user = "msobroza";
     private static final String url = "//mysql-tp.svc.enst-bretagne.fr:3306/MSOBROZA?characterEncoding=UTF-8&useUnicode=true";
     */
    private Connection dbConnection;
    private Statement dbStatement;
    private PreparedStatement pStatement;

    public DB() {
        dbConnection = null;
    }

    public boolean open() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dbConnection = DriverManager.getConnection("jdbc:mysql:" + url, user, password);
            dbStatement = dbConnection.createStatement();
            return true;

        } catch (SQLException e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
        } catch (ClassNotFoundException e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
        } catch (InstantiationException e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
        } catch (IllegalAccessException e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    public ResultSet result(String sql) {
        try {
            ResultSet rs = this.dbStatement.executeQuery(sql);
            return rs;
        } catch (SQLException e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public boolean execQuery(String query) {
        try {
            pStatement = (PreparedStatement) dbConnection.prepareStatement(query);
            return pStatement.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

    public boolean close() {
        try {
            dbStatement.close();
            dbConnection.close();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}
