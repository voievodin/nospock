package cases

import resources.Namespace
import spock.lang.Specification

class StringsSpec extends Specification {

  def "groovy string is supported"() {
    given:
      def x = 1
    expect:
      "${x + 1} = 2 && ${new Namespace().getStringValue()}" == "2 = 2 && string-value"
      "$x" == "1"
  }
  
  def "double quotes in strings correctly handled"() {
    given:
      def json = '{"x": 1, "name": "Hello"}'
    expect:
      '{"x": 1, "name": "Hello"}' == json
  }
  
  def "multiline strings are properly translated"() {
    given:
      def str = """
        {
          "a" : 1,
          "b" : 2
        }
        """
    expect:
      str ==
        '''
        {
          "a" : 1,
          "b" : 2
        }
        '''
      str == "\n" +
        "        {\n" +
        "          \"a\" : 1,\n" +
        "          \"b\" : 2\n" +
        "        }\n" +
        "        "
  }

  def "long strings with nl breaks are converted to multiline strings"() {
    given:
      def str = """
        select t1.x, t2.y
        from table_1 t1
          join table_2 t2 on t1.t2_id = t2.id
        where t1.p1 = 'asd'
          and t2.y1 = 'dsa'
          and t1.age > 10
        order by t1.time_created desc
        limit 10
        """
    expect:
      str == '''
        select t1.x, t2.y
        from table_1 t1
          join table_2 t2 on t1.t2_id = t2.id
        where t1.p1 = 'asd'
          and t2.y1 = 'dsa'
          and t1.age > 10
        order by t1.time_created desc
        limit 10
        '''
  }
}
