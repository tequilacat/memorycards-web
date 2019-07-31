package test.tequilacat.memcard.server.services;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.util.stream.StreamSupport;

import org.assertj.core.util.Arrays;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SortOperation;

public class TestBed {

  /*
   * {
    "_id" : "01001",
    "city" : "AGAWAM",
    "loc" : [
        -72.622739,
        42.070206
    ],
    "pop" : 15338,
    "state" : "MA"
    }
   */
  public static class City {
    @Id
    private String id;
    private int pop;
    private String state;
    private String city;
    
    public City() {}
    
    public City(int pop, String state, String city) {
      super();
      this.pop = pop;
      this.state = state;
      this.city = city;
    }
    public String getId() {
      return id;
    }
    public void setId(String id) {
      this.id = id;
    }
    public int getPop() {
      return pop;
    }
    public void setPop(int pop) {
      this.pop = pop;
    }
    public String getState() {
      return state;
    }
    public void setState(String state) {
      this.state = state;
    }
    public String getCity() {
      return city;
    }
    public void setCity(String city) {
      this.city = city;
    }
    
  }
  
  
  private void fillCities(MongoTemplate mongoTemplate) {
    var coll = mongoTemplate.getCollection(mongoTemplate.getCollectionName(City.class));
    coll.drop();
    
    mongoTemplate.insert(Arrays.asList(Arrays.array(
        new City(200, "ma", "macity1"),
        new City(430, "ma", "macity4"),
        
        new City(100, "oh", "oh_city1"),
        new City(110, "oh", "oh_city2"),
        new City(120, "oh", "oh_city3"),
        new City(130, "oh", "oh_city4"),
        
        new City(10, "ny", "ny_city1"),
        new City(110, "ny", "ny_city2"),
        new City(150, "ny", "ny_city2"),
        new City(120, "ny", "ny_city3"),
        new City(1000, "ny", "ny_city4")        
        )), City.class);    
  }
  
  // @Test
  public void testFillData(@Autowired MongoTemplate mongoTemplate) {
    // var docs = new String[] {"{}"};
    fillCities(mongoTemplate);
    String collName = mongoTemplate.getCollectionName(City.class);

    
    
    var sumZips = group("state").count().as("zipCount");
    SortOperation sortByCount = sort(Direction.ASC, "zipCount");
    var groupFirstAndLast = group().first("_id").as("minZipState")
      .first("zipCount").as("minZipCount").last("_id").as("maxZipState")
      .last("zipCount").as("maxZipCount");
     
    /*
    Aggregation aggregation = newAggregation(sumZips, sortByCount, groupFirstAndLast);    
    */
    Aggregation aggregation = newAggregation(sumZips, sortByCount, groupFirstAndLast);
    
    AggregationResults<Document> result = mongoTemplate
        .aggregate(aggregation, collName, Document.class);
    
    //Document document= result.getUniqueMappedResult();
    //var resultList = result; // mongoTemplate.findAll(Document.class, mongoTemplate.getCollectionName(City.class));    
    StreamSupport.stream(result.spliterator(), false).forEach(c-> System.out.println(c));
  }
  
  
  
  
}
