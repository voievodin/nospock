package resources;

public class Namespace {

  public static final Namespace DEFAULT = new Namespace();
  
  public static Namespace create() {
    return new Namespace();
  }
  
  public static boolean isActive() {
    return true;
  }
  
  public Inner3 inner3;
  private Namespace self = this;

  public String getStringValue() {
    return "string-value";
  }
  
  public Namespace namespace() {
    return new Namespace();
  }

  public Namespace getSelf() {
    return self;
  }

  public String compute(String s) {
    return s;
  }

  public Integer compute2(int a, int b) {
    return a + b;
  }

  public static class Inner1 {
    public void accept(Long v) {

    }
  }

  public static class Inner2 {
    public Inner2(Object... args) {

    }
  }

  public static class Inner3 {
    public Long f1;

    public void setF1(Long v) {
      this.f1 = v;
    }
  }

  public static class Inner4 {
    public String f1;
    public Integer f2;
    public Long f3;

    public void setF1(String f1) {
      this.f1 = f1;
    }

    public void setF2(Integer f2) {
      this.f2 = f2;
    }

    public void setF3(Long f3) {
      this.f3 = f3;
    }
  }

  public static class InnerException extends RuntimeException {}
}
