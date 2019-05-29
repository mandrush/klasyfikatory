package actors.classifiers;

import akka.actor.AbstractActor;
//todo to javowy agent dla przykladu
public class Dupa extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        String.class,
                        s -> {
                           System.out.println("Received String message: {}");
                        })
                .matchAny(o -> System.out.println("Received String message: {}"))
                .build();
    }
}
