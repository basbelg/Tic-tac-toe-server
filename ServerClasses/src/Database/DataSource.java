package Database;

import java.util.List;

public interface DataSource {
    boolean insert(Object obj);
    boolean delete(Object obj);
    boolean update(Object obj);
    Object get(Class classType, String s);
    List<Object> list(Class classType);
    List<Object> query(Class classType, String filter);
}
