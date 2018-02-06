package rawe.gordon.com;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import rawe.gordon.com.loader.SpiLoader;

/**
 * Created by gordon on 1/19/18.
 */

public class SpiManager {

    private static class Holder {
        private static SpiManager sInstance = new SpiManager();
    }

    public static SpiManager getInstance() {
        return Holder.sInstance;
    }

    /**
     * access one service
     */
    public <T> T getService(Class<T> clazz) {
        Iterator<T> iterator = SpiLoader.iterator(clazz);
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * access a service list
     */
    public <T> T getServices(Class<T> clazz, Class specificClazz) {
        Iterator<T> iterator = SpiLoader.iterator(clazz);
        while (iterator.hasNext()) {
            T now = iterator.next();
            if (now.getClass() == specificClazz) {
                return now;
            }
        }
        return null;
    }

    /**
     * access a service list
     */
    public <T> Set<T> getServices(Class<T> clazz) {
        Iterator<T> iterator = SpiLoader.iterator(clazz);
        Set<T> services = new LinkedHashSet<>();
        while (iterator.hasNext()) {
            services.add(iterator.next());
        }
        return services;
    }
}
