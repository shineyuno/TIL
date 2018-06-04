package user.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by leeyunho on 18. 6. 4..
 */
public interface ConnectionMaker {
    public Connection makeConnection() throws ClassNotFoundException,SQLException;
}
