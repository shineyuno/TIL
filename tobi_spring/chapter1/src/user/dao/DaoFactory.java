package user.dao;

/**
 * Created by leeyunho on 18. 6. 6..
 */
public class DaoFactory {
    public UserDao userDao(){
        return new UserDao(connectionMaker());
    }

    public ConnectionMaker connectionMaker(){
        return new DConnectionMaker();
    }
}
