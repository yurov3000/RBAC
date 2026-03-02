package Filters;

public class UserFilters {
    public static UserFilter byUserName(String username){
        return user -> username.equals((user.username()));
    }

    public static UserFilter byUsernameContains(String substring){
        return user -> user.username().toLowerCase().contains(substring.toLowerCase());
    }

    public static UserFilter byEmail (String email){
        return user -> email.equals(user.email());
    }

    public static UserFilter byEmailDomain(String domain){
        return user -> user.email().endsWith(domain);
    }

    public static UserFilter byFullNameContains(String substring){
        return user -> user.fullname().toLowerCase().contains(substring.toLowerCase());
    }
}
