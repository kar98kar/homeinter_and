package pw.bmyo.www.swiperecycler;

/**
 * Created by huang on 2016/12/31.
 */

public interface LoadCallback {
    void loadComplete();
    void loading(int progress);
}