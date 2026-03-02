package Filters;

import org.example.RecUser;

@FunctionalInterface
public interface UserFilter {
    boolean test(RecUser user);

    // Комбинируем фильтры
    default UserFilter and(UserFilter other){
        return user -> this.test(user) && other.test(user);
    }

    default UserFilter or(UserFilter other){
        return user -> this.test(user) || other.test(user);
    }
}
