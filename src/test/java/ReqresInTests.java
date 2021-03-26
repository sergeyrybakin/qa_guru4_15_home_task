import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReqresInTests {

    static String delayedResponseAmount;
    static long delayedGetListTime;
    static List<String> delayedUsers = new ArrayList<>();
    static String getListAmount;
    static long ordinaryGetListTime;
    static ResponseSpecification responseSpec;
    static List<String> users = new ArrayList<>();

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "https://reqres.in";
        responseSpec = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
    }

    @Test
    public void delayedResponseTest() {
        ExtractableResponse<Response> result = get("/api/users?delay=3")
                .then()
                .spec(responseSpec)
                .body("support.url", is("https://reqres.in/#support-heading"))
                .extract();
        delayedResponseAmount = result.path("total" ).toString();
        delayedGetListTime = result.time();

        assertThat(delayedUsers.size(), equalTo(0));
        delayedUsers = result.path("data.first_name");
        assertThat(delayedUsers.size(), greaterThan(0) );

        if(ordinaryGetListTime != 0L) {
            compareResults();
        }
    }

    @Test
    public void getListUsersTest() {
        ExtractableResponse<Response> result = get("/api/users?page=2")
                .then()
                .spec(responseSpec)
                .extract();

        getListAmount = result.path("total").toString();
        ordinaryGetListTime = result.time();

        assertThat(users.size(),is(equalTo(0)));
        users = result.path("data.first_name");
        assertThat(users.size(),greaterThan(0));

        if(delayedGetListTime != 0L) {
            compareResults();
        }
    }

    @Test
    public void createNewUserTest() {
        String data = "{ \"name\":\"morpheus\", \"job\":\"leader\" }";

        ExtractableResponse<Response> result = given()
                .contentType(ContentType.JSON)
                .body(data)
        .when()
                .post("/api/users")
        .then()
                .statusCode(201)
                .extract();
        String newUser = result.path("name");

        assertTrue(data.contains(newUser));
        if(!users.isEmpty())
            assertThat(users,not(contains(newUser)));
        if (!delayedUsers.isEmpty())
            assertThat(delayedUsers,not(contains(newUser)));
    }

    @Test
    public void userNotFoundTest() {
        get("/api/users/30000")
                .then()
                .statusCode(404)
                .extract();
    }

    @Test
    public void getUserByIdTest() {
        ExtractableResponse<Response> result = get("/api/users/8")
                .then()
                .spec(responseSpec)
                .extract();

        String email = result.path("data.email");
        String firstName = result.path("data.first_name");
        String lastName = result.path("data.last_name");
        assertTrue(email.contains("lindsay.ferguson@reqres.in"));
        assertTrue(firstName.contains("Lindsay"));
        assertTrue(lastName.contains("Ferguson"));
    }

    private void compareResults()
    {
        assertThat(delayedResponseAmount,is(equalTo(getListAmount)));
        assertThat(delayedGetListTime, greaterThan(ordinaryGetListTime));
    }
}
