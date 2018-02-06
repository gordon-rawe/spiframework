package rawe.gordon.com.services;

import rawe.gordon.com.framework.IDemoService;

/**
 * Created by gordon on 1/19/18.
 */

public class DemoService implements IDemoService {
    @Override
    public String tellMeWhoAreYou() {
        return "this is from apple module";
    }
}
