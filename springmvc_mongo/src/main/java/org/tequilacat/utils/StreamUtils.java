package org.tequilacat.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Static methods to make stream usage short and concise
 * @author avo
 */
public class StreamUtils {
  
  /**
   * Gets 1st element as optional 
   * @param items
   * @return optional wrapping first element or empty optional 
   */
  public static <T> Optional<T> getFirst(Collection<T> items) {
    Iterator<T> it = items.iterator();
    return Optional.ofNullable(it.hasNext() ? it.next() : null); 
  }
  
  /**
   * @param items
   * @param match
   * @return if collection contains item matching predicate
   */
  public static <T> boolean contains(Collection<T> items, Predicate<T> match) {
    return items.stream().filter(match).findFirst().isPresent();
  }
  
  /**
   * @param items
   * @param match
   * @return first item matching predicate or null
   */
  public static <T> T findFirst(Collection<T> items, Predicate<T> match) {
    return findFirst(items.stream(), match);
  }
  
  public static <T> T findFirst(Iterable<T> items, Predicate<T> match) {
    return findFirst(StreamSupport.stream(items.spliterator(), false), match);
  }
  
  public static <T> Optional<Integer> findFirstIndexOf(Iterable<T> items, Predicate<T> match) {
    int i = 0;

    for (var item : items) {
      if (match.test(item)) {
        return Optional.of(i);
      }
      
      i++;
    }

    return Optional.empty();
  }
  
  /**
   * @param items
   * @param match
   * @return first item not matching predicate
   */
  public static <T> T findFirstNotMatch(Collection<T> items, Predicate<T> match) {
    return findFirst(items.stream(), match.negate());
  }
  /**
   * @param itemsStream
   * @param match
   * @return first item matching predicate or null
   */
  public static <T> T findFirst(Stream<T> itemsStream, Predicate<T> match) {
    return itemsStream.filter(match).findFirst().orElse(null);
  }
  
  /**
   * @param itemsStream
   * @param match
   * @return list not matching predicate
   */
  public static <T> List<T> filterNotMatch(Stream<T> itemsStream, Predicate<T> match) {
    return itemsStream.filter(match.negate()).collect(Collectors.toList());
  }
  /**
   * @param items
   * @param valueToKey
   * @return map where values are from collection and keys are computed by valueToKey parameter
   */
  public static <K,V> Map<K,V> toMap(Collection<V> items, Function<V,K> valueToKey) {
    return toMap(items.stream(), valueToKey);
  }
  
  public static <K,MV,V> Map<K,MV> toMap(Collection<V> items, Function<V,K> toKey, Function<V,MV> toValue) {
    return items.stream().collect(Collectors.toMap(i -> toKey.apply(i), i -> toValue.apply(i)));
  }
  
  /**
   * @param items
   * @param valueToKey
   * @return map where values are from collection and keys are computed by valueToKey parameter
   */
  public static <K,V> Map<K,V> toMap(Stream<V> items, Function<V,K> valueToKey) {
    return items.collect(Collectors.toMap(i->valueToKey.apply(i), i->i));
  }
  
  /**
   * Converts items from one type to another according to mapFunction
   * @param items
   * @param mapFunction
   * @return converted list
   */
  public static <T,V> List<V> mapToList(Collection<T> items, Function<T,V> mapFunction) {
    return mapToList(items.stream(), mapFunction);
  }
  
  public static <T,V> List<V> mapToList(Iterable<T> items, Function<T,V> mapFunction) {
    return mapToList(StreamSupport.stream(items.spliterator(), false), mapFunction);
  }
  
  /**
   * Converts items from one type to another according to mapFunction
   * @param items
   * @param mapFunction
   * @return converted list
   */
  public static <T,V> List<V> mapToList(Stream<T> items, Function<T,V> mapFunction) {
    return items.map(t-> mapFunction.apply(t) ).collect(Collectors.toList());
  }
  
  /**
   * shortcut to collect stream to list
   * @param aStream
   * @return
   */
  public static <T> List<T> toList(Stream<T> aStream) {
    return aStream.collect(Collectors.toList());
  }
}