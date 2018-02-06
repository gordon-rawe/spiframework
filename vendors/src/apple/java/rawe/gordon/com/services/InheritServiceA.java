package rawe.gordon.com.services;

import rawe.gordon.com.framework.IInheritService;

/**
 * Created by gordon on 1/19/18.
 */

public class InheritServiceA implements IInheritService {
    @Override
    public String showIdentity() {
        return "InheritServiceA from apple";
    }
}
