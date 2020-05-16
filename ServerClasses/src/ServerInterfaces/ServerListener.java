package ServerInterfaces;

import java.io.Serializable;

public interface ServerListener {
    void update(Serializable msg, Object data);
}
