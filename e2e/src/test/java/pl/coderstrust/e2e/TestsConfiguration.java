package pl.coderstrust.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.mapper.factory.Jackson2ObjectMapperFactory;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TestsConfiguration {

  public final static String BASE_PATH = "";
  public final static String BASE_URI = "http://localhost";
  public final static int BASE_PORT = 8080;
  public final static int TEST_INVOICES_COUNT = 10;
  public final static int DEFAULT_ENTRIES_COUNT = 5;
  public final static int DEFAULT_PAYMENTS_COUNT = 5;
  public final static int SERVER_OK_STATUS_CODE = 200;


  public final static int SERVER_ENTRY_NOT_EXIST_STATUS_CODE = 500;
  public final static String INT_FROM_STRING_REGEX_PATTERN = "([0-9])+";
  public final static int DEFAULT_TEST_INVOICE_NUMBER = 1;
  public final static int DEFAULT_PRODUCT_QUANTITY = 1;
  public final static BigDecimal WRONG_NET_VALUE = new BigDecimal(-1);


  public TestsConfiguration() {
    RestAssured.baseURI = BASE_URI;
    RestAssured.basePath = BASE_PATH;
    PreemptiveBasicAuthScheme authScheme = new PreemptiveBasicAuthScheme();
    authScheme.setUserName("admin");
    authScheme.setPassword("admin");
    RestAssured.authentication = authScheme;

    String port = System.getProperty("server.port");
    if (port == null) {
      RestAssured.port = BASE_PORT;
    } else {
      RestAssured.port = Integer.valueOf(port);
    }

    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
        ObjectMapperConfig.objectMapperConfig()
            .jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory() {
              @Override
              public ObjectMapper create(Class cls, String charset) {
                ObjectMapper jsonMapper = new ObjectMapper();
                jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                jsonMapper.registerModule(new JavaTimeModule());
                return jsonMapper;
              }
            })
    );
  }
}
