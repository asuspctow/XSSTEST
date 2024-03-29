package org.hamcrest.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;








public class Is<T>
  extends BaseMatcher<T>
{
  private final Matcher<T> matcher;
  
  public Is(Matcher<T> matcher)
  {
    this.matcher = matcher;
  }
  
  public boolean matches(Object arg) {
    return matcher.matches(arg);
  }
  
  public void describeTo(Description description) {
    description.appendText("is ").appendDescriptionOf(matcher);
  }
  






  @Factory
  public static <T> Matcher<T> is(Matcher<T> matcher)
  {
    return new Is(matcher);
  }
  





  @Factory
  public static <T> Matcher<T> is(T value)
  {
    return is(IsEqual.equalTo(value));
  }
  





  @Factory
  public static Matcher<Object> is(Class<?> type)
  {
    return is(IsInstanceOf.instanceOf(type));
  }
}
