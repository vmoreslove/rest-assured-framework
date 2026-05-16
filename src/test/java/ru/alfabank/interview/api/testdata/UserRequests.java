package ru.alfabank.interview.api.testdata;

import ru.alfabank.interview.api.dto.CreateUserRequest;

public final class UserRequests {
    private UserRequests() {
    }

    public static CreateUserRequest validUser() {
        return new CreateUserRequest("Oleg", "Kopylov", "oleg.kopylov@example.com");
    }

    public static CreateUserRequest userWithoutEmail() {
        return new CreateUserRequest("Oleg", "Kopylov", null);
    }
}
