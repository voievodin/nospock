package cases;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import resources.Namespace;

import static java.lang.Thread.State;
import static java.lang.Thread.State.RUNNABLE;
import static java.lang.Thread.State.TERMINATED;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

class ParameterisedSpec {

  @ParameterizedTest
  @MethodSource("aPlusBPlusCEqualsResultValuesProvider")
  void aPlusBPlusCEqualsResult(Integer a, Integer b, Integer c, Integer result) {
    var actualResult = a + b + c;

    Assertions.assertEquals(result, actualResult);
  }

  @ParameterizedTest
  @MethodSource("x1AndX2TypesDerivedCorrectlyValuesProvider")
  void x1AndX2TypesDerivedCorrectly(Namespace x1, Namespace x2) {
    Assertions.assertEquals(1, 1);
  }

  @ParameterizedTest
  @MethodSource("x1AndX2TypesDerivedCorrectlyFromUsageValuesProvider")
  void x1AndX2TypesDerivedCorrectlyFromUsage(Namespace x1, Namespace x2) {
    Assertions.assertNotNull(useX(x1));
    Assertions.assertNotNull(useX(x2));
    Assertions.assertEquals(1, 1);
  }

  @ParameterizedTest
  @MethodSource("x1PlusX2EqualsequalsResIsAPositiveIntegerValuesProvider")
  void x1PlusX2EqualsequalsResIsAPositiveInteger(Integer x1, Integer x2, Integer res) {
    Assertions.assertEquals(res, x1 + x2);
  }

  @ParameterizedTest
  @MethodSource("staticallyImportedParameterValuesGetCorrectTypesValuesProvider")
  void staticallyImportedParameterValuesGetCorrectTypes(State x1, BigInteger x2, State x3, String x4) {
    Assertions.assertEquals(1, 1);
  }

  @ParameterizedTest
  @MethodSource("emptyWhereValuesSectionIsIgnoredValuesProvider")
  void emptyWhereValuesSectionIsIgnored(Integer a, Integer b, Integer c) {
    Assertions.assertEquals(c, a + b);
  }

  @ParameterizedTest
  @MethodSource("nullIsCorrectlyPassedWhenUsingSingleElementParameterizationValuesProvider")
  void nullIsCorrectlyPassedWhenUsingSingleElementParameterization(Integer x) {
    Assertions.assertEquals(x, x);
  }

  @ParameterizedTest
  @MethodSource("collectionCanBeSupplierOfWhereExpressionValuesValuesProvider")
  void collectionCanBeSupplierOfWhereExpressionValues(Object x) {
    Assertions.assertEquals(x, x);
  }

  @ParameterizedTest
  @MethodSource("arrayCanBeSupplierOfWhereExpressionValuesValuesProvider")
  void arrayCanBeSupplierOfWhereExpressionValues(Object x) {
    Assertions.assertNotNull(x.startsWith("a"));
  }

  @ParameterizedTest
  @MethodSource("predefinedListAndValuesProvidingCollectionSourcesCanBeUsedToProvideValuesValuesProvider")
  void predefinedListAndValuesProvidingCollectionSourcesCanBeUsedToProvideValues(Integer x, Object y) {
    Assertions.assertEquals(4, x + y);
  }

  @ParameterizedTest
  @MethodSource("twoCollectionsCanBeUsedToProvideValuesValuesProvider")
  void twoCollectionsCanBeUsedToProvideValues(Object x, Object y) {
    Assertions.assertEquals(4, x + y);
  }

  @ParameterizedTest
  @MethodSource("whereCanHaveValuesDirectlyAssignedUsedValuesProvider")
  void whereCanHaveValuesDirectlyAssignedUsed(Integer x, String y, String z) {
    Assertions.assertEquals(z, x + y);
  }

  @ParameterizedTest
  @MethodSource("multipleColumnsCanFollowEmptySectionValuesProvider")
  void multipleColumnsCanFollowEmptySection(Integer a, Integer b, Integer r1, Integer r2, Integer r3) {
    Assertions.assertEquals(r1, a + b);
    Assertions.assertEquals(r2, a * b);
    Assertions.assertEquals(r3, a - b);
  }

  @ParameterizedTest
  @MethodSource("allColumnsCanBeSplitUsingDoublePipeValuesProvider")
  void allColumnsCanBeSplitUsingDoublePipe(Integer a, Integer b, Integer r) {
    Assertions.assertEquals(r, a + b);
  }

  public String useX(Namespace x) {
    return "used x" + x;
  }

  private static Stream<Arguments> aPlusBPlusCEqualsResultValuesProvider() {
    return Stream.of(Arguments.of(1, 1, 1, 3), Arguments.of(2, 2, 3, 7));
  }

  private static Stream<Arguments> x1AndX2TypesDerivedCorrectlyValuesProvider() {
    return Stream.of(
      Arguments.of(new Namespace(), Namespace.create()),
      Arguments.of(Namespace.create(), new Namespace()),
      Arguments.of(Namespace.DEFAULT, Namespace.DEFAULT)
    );
  }

  private static Stream<Arguments> x1AndX2TypesDerivedCorrectlyFromUsageValuesProvider() {
    return Stream.of(Arguments.of(((Callable<Namespace>) () -> new Namespace()).call(), ((Callable<Namespace>) () -> new Namespace()).call()));
  }

  private static Stream<Arguments> x1PlusX2EqualsequalsResIsAPositiveIntegerValuesProvider() {
    return Stream.of(
      Arguments.of(1, 1, 2),
      Arguments.of(2, 1, 3),
      Arguments.of(3, 1, 4)
    );
  }

  private static Stream<Arguments> staticallyImportedParameterValuesGetCorrectTypesValuesProvider() {
    return Stream.of(Arguments.of(
      RUNNABLE,
      ONE,
      State.NEW,
      Thread.State.NEW.name()
    ), Arguments.of(
      TERMINATED,
      TWO,
      State.WAITING,
      Thread.State.WAITING.name()
    ));
  }

  private static Stream<Arguments> emptyWhereValuesSectionIsIgnoredValuesProvider() {
    return Stream.of(Arguments.of(1, 2, 3), Arguments.of(2, 3, 5));
  }

  private static Stream<Arguments> nullIsCorrectlyPassedWhenUsingSingleElementParameterizationValuesProvider() {
    return Stream.of(
      Arguments.of(new Object[] {null}),
      Arguments.of(1),
      Arguments.of(2),
      Arguments.of(3),
      Arguments.of(4)
    );
  }

  private static Object collectionCanBeSupplierOfWhereExpressionValuesValuesProvider() {
    return Set.of(1, 2, 3, 4, 5);
  }

  private static Object arrayCanBeSupplierOfWhereExpressionValuesValuesProvider() {
    return new String[] {"aa", "ab", "ac"};
  }

  private static List<Arguments> predefinedListAndValuesProvidingCollectionSourcesCanBeUsedToProvideValuesValuesProvider() {
    var x = List.of(1, 2, 3).iterator();
    var y = Stream.of(3, 2, 1).collect(Collectors.toList()).iterator();
    var provider = new ArrayList<Arguments>();
    while (x.hasNext()) {
      provider.add(Arguments.of(x.next(), y.next()));
    }
    return provider;
  }

  private static List<Arguments> twoCollectionsCanBeUsedToProvideValuesValuesProvider() {
    var x = List.of(1, 2, 3).iterator();
    var y = Stream.of(3, 2, 1).collect(Collectors.toList()).iterator();
    var provider = new ArrayList<Arguments>();
    while (x.hasNext()) {
      provider.add(Arguments.of(x.next(), y.next()));
    }
    return provider;
  }

  private static Stream<Arguments> whereCanHaveValuesDirectlyAssignedUsedValuesProvider() {
    return Stream.of(Arguments.of(1, "2", "12"));
  }

  private static Stream<Arguments> multipleColumnsCanFollowEmptySectionValuesProvider() {
    return Stream.of(Arguments.of(1, 2, 3, 2, -1), Arguments.of(2, 3, 5, 6, -1));
  }

  private static Stream<Arguments> allColumnsCanBeSplitUsingDoublePipeValuesProvider() {
    return Stream.of(Arguments.of(1, 2, 3), Arguments.of(2, 3, 5));
  }
}
