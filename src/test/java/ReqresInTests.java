import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class ReqresInTests {

    static String delayedResponseAmount;
    static long delayedGetListTime;
    static String getListAmount;
    static long ordinaryGetListTime;
    static ResponseSpecification responseSpec;

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
        if(delayedGetListTime != 0L) {
            compareResults();
        }
    }

    private void compareResults()
    {
        System.out.println("getListAmount: " + getListAmount + "  time: " + ordinaryGetListTime);
        System.out.println("delayedResponceAmount: " + delayedResponseAmount + "   time: " + delayedGetListTime);
        assertThat(delayedResponseAmount,is(equalTo(getListAmount)));
        assertThat(delayedGetListTime, greaterThan(ordinaryGetListTime));
    }
}
