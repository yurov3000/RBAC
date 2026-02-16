package org.example;

import java.util.List;

class Main {
    static void main(String[] args) {
        List<String[]> testCases = List.of(
                new String[]{"john_doe", "John Doe", "johndoe@example.com"},
                new String[]{"invalid@username", "Test User", "test@example.com"},
                new String[]{"usr", "Short Username", "short@user.com"},
                new String[]{"very_long_username_with_more_than_twenty_characters", "Long Username", "long@example.com"},
                new String[]{"valid_user", "", "valid@email.com"},
                new String[]{"valid_user", "Valid Fullname", ""},
                new String[]{"valid_user", "Valid Fullname", "not_an_email"}
        );

        for (var caseData : testCases){
            try{
                var user = User.validate(caseData[0], caseData[1], caseData[2]);
                System.out.println(user.format());
            } catch (IllegalArgumentException e){
                System.err.println(e.getMessage());
            }
        }
    }
}
