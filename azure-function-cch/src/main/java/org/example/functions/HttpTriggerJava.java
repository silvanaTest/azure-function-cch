package org.example.functions;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Azure Functions with HTTP Trigger.
 */
@Slf4j
public class HttpTriggerJava {

    private JdbcTemplate jdbcTemplate;

    public HttpTriggerJava() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setUrl("jdbc:sqlserver://sqldbcch03.database.windows.net:1433;database=sql-db-cch03;user=useradmin@sqldbcch03;password=Zala960205Zala960205;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
        this.jdbcTemplate = new JdbcTemplate(driverManagerDataSource);
    }

    @FunctionName("getById")
    public HttpResponseMessage runbyId(
            @HttpTrigger(name = "getById", methods = {HttpMethod.GET}
                    , route = "items/{id}", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<Item>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        String id = request.getUri().getPath().split("/")[3];
        if (id == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass an id on the query string").build();
        }

        log.info("Id: {}", id);
        Item item;
        try {
            item = jdbcTemplate.queryForObject("SELECT * FROM Item WHERE Id = ?", new Object[]{id}, (rs, rowNum) -> new Item(
                    rs.getInt("Id"),
                    rs.getString("Name"),
                    rs.getString("Description")
            ));
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("{\"message\": \"No se encontro el item\"}")
                    .build();
        }

        return request.createResponseBuilder(HttpStatus.OK).body(item).build();
    }

    @Getter
    @Setter
    public static class Item {
        private Integer Id;
        private String Name;
        private String Description;

        public Item(Integer id, String name, String description) {
            this.Id = id;
            this.Name = name;
            this.Description = description;
        }
    }
}
