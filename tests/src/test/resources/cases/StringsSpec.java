package cases;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import resources.Namespace;

class StringsSpec {

  @Test
  void groovyStringIsSupported() {
    var x = 1;

    Assertions.assertEquals("2 = 2 && string-value", "" + (x + 1) + " = 2 && " + new Namespace().getStringValue());
    Assertions.assertEquals("1", "" + x);
  }

  @Test
  void doubleQuotesInStringsCorrectlyHandled() {
    var json = "{\"x\": 1, \"name\": \"Hello\"}";

    Assertions.assertEquals(json, "{\"x\": 1, \"name\": \"Hello\"}");
  }

  @Test
  void multilineStringsAreProperlyTranslated() {
    var str = "\n        {\n          \"a\" : 1,\n          \"b\" : 2\n        }\n        ";

    Assertions.assertEquals("\n        {\n          \"a\" : 1,\n          \"b\" : 2\n        }\n        ", str);
    Assertions.assertEquals("\n" + "        {\n" + "          \"a\" : 1,\n" + "          \"b\" : 2\n" + "        }\n" + "        ", str);
  }

  @Test
  void longStringsWithNlBreaksAreConvertedToMultilineStrings() {
    var str = """
    
            select t1.x, t2.y
            from table_1 t1
              join table_2 t2 on t1.t2_id = t2.id
            where t1.p1 = 'asd'
              and t2.y1 = 'dsa'
              and t1.age > 10
            order by t1.time_created desc
            limit 10

    """;

    Assertions.assertEquals("""
    
            select t1.x, t2.y
            from table_1 t1
              join table_2 t2 on t1.t2_id = t2.id
            where t1.p1 = 'asd'
              and t2.y1 = 'dsa'
              and t1.age > 10
            order by t1.time_created desc
            limit 10

    """, str);
  }
}
