package rawe.gordon.com.loader;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by gordon on 1/19/18.
 * SpiLoader is used when decomposing some vendor project.
 */

public class SpiLoader {

    private static final String[] DIRS = {"services/"};

    private static final Map<Class, List<CacheItem>> CLASSES = new LinkedHashMap<>();

    public static <T> Iterator<T> iterator(Class<T> clazz) {
        ClassLoader loader = clazz.getClassLoader();
        return new ConfigIterator<>(loadClasses(clazz, loader), clazz, loader);
    }

    private static <T> List<CacheItem> loadClasses(Class<T> clazz, ClassLoader loader) {
        synchronized (CLASSES) {
            List<CacheItem> classes = CLASSES.get(clazz);
            if (classes != null) {
                return classes;
            }
        }

        List<CacheItem> classNames = new LinkedList<>();
        for (String directory : DIRS) {
            Enumeration<URL> urls = getUrls(clazz, loader, directory);
            while (urls != null && urls.hasMoreElements()) {
                URL url = urls.nextElement();
                classNames.addAll(parseSafely(url));
            }

        }

        synchronized (CLASSES) {
            // 这里只保留一份的原因是保证单例服务能够保证严格单例
            if (!CLASSES.containsKey(clazz)) {
                CLASSES.put(clazz, classNames);
            } else {
                classNames = CLASSES.get(clazz);
            }
        }

        return classNames;
    }

    private static Enumeration<URL> getUrls(Class clazz, ClassLoader loader, String directory) {
        String fullName = directory + clazz.getName();
        if (loader == null) {
            try {
                return ClassLoader.getSystemResources(fullName);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            try {
                return loader.getResources(fullName);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private static List<CacheItem> parseSafely(URL url) {
        int maxTry = 3;
        for (int i = 0; i < maxTry; i++) {
            try {
                return parse(url);
            } catch (ConcurrentModificationException e) {
                if (i < maxTry - 1) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                } else {
                    throw e;
                }
            }
        }

        return new LinkedList<>();
    }

    private static List<CacheItem> parse(URL url) {
        List<CacheItem> classNames = new LinkedList<>();

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {

            is = url.openStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            String line = br.readLine();
            while (line != null) {
                CacheItem cacheItem = CacheItem.fromConfig(line);
                if (cacheItem != null) {
                    classNames.add(cacheItem);
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(br);
            closeQuietly(isr);
            closeQuietly(is);
        }
        return classNames;
    }

    private static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class CacheItem {
        final String className;
        final boolean persistence;
        Object cacheService;

        CacheItem(String className, boolean persistence) {
            this.className = className;
            this.persistence = persistence;
        }

        static CacheItem fromConfig(String line) {
            if (!TextUtils.isEmpty(line)) {
                String[] segments = line.split(":");
                String className = segments[0];
                boolean persistence = segments.length > 1 && Boolean.parseBoolean(segments[1]);
                return new CacheItem(className, persistence);
            } else {
                return null;
            }
        }
    }

    private static class ConfigIterator<T> implements Iterator<T> {

        private List<CacheItem> mClasses;
        private ClassLoader mClassLoader;
        private Class mClass;
        private int mIndex = 0;

        ConfigIterator(List<CacheItem> classes, Class<T> clazz, ClassLoader loader) {
            mClassLoader = loader;
            mClass = clazz;
            mClasses = classes;
        }

        @Override
        public boolean hasNext() {
            return mClasses != null && mIndex < mClasses.size();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            CacheItem cacheItem = mClasses.get(mIndex);
            mIndex++;

            if (cacheItem.persistence && cacheItem.cacheService != null) {
                return (T) cacheItem.cacheService;
            }

            // 如果对象是可缓存的，并且没有缓存对象，创建新对象缓存并返回
            if (cacheItem.persistence) {
                cacheItem.cacheService = newService(cacheItem);
                return (T) cacheItem.cacheService;
            }

            // 非缓存对象，每次都创建新的
            return newService(cacheItem);

        }

        @SuppressWarnings("unchecked")
        private T newService(CacheItem cacheItem) {
            Class clazz;
            try {
                clazz = Class.forName(cacheItem.className, false, mClassLoader);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (!mClass.isAssignableFrom(clazz)) {
                String msg = "source: " + clazz.getName() + ", dest: " + mClass.getName();
                ClassCastException cce = new ClassCastException(msg);
                throw new RuntimeException(cce);
            }

            try {
                Constructor<T> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
