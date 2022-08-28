package com.yevhenii.nospock.jast;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JAnnotationUsage implements JAstNode {

  private final String name;
  private final Map<String, JAstNode> members = new LinkedHashMap<>();

  public JAnnotationUsage(String name) {
    this.name = Objects.requireNonNull(name);
  }

  public JAnnotationUsage(String name, JAstNode value) {
    this(name);
    this.members.put("value", value);
  }

  public void addMember(String name, JAstNode member) {
    this.members.put(name, member);
  }

  @Override
  public String asCode(CodeStyle style) {
    StringBuilder sb = new StringBuilder("@").append(name);
    if (members.size() == 1 && members.get("value") != null) {
      sb.append("(").append(members.get("value").asCode(style)).append(")");
    } else if (!members.isEmpty()) {
      sb.append("(");
      sb.append(
        members.entrySet()
          .stream()
          .map(mE -> mE.getKey() + " = " + mE.getValue().asCode(style))
          .collect(Collectors.joining(", "))
      );
      sb.append(")");
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JAnnotationUsage that = (JAnnotationUsage) o;
    return Objects.equals(name, that.name) && Objects.equals(members, that.members);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, members);
  }
}
