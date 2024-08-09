package test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {

    static final List<Unit> units = Stream.of(new Rat(), new Cheese(), new Poison()).collect(Collectors.toList());

    public static void start() {
        //units.forEach(unit -> units.stream().filter(target -> !target.equals(unit)).forEach(unit::compare));
        units.getFirst().z();
        //units.getFirst().h();
        units.forEach(System.out::println);
    }

    public static void main(String[] args) {
        start();
    }

}