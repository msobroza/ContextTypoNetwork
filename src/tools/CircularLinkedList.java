package tools;

import java.util.LinkedList;

public class CircularLinkedList<E> extends LinkedList<E> {

    @Override
    public E get(int i) {
        if (i < 0) {
            return super.get(i % this.size() + this.size());
        } else {
            return super.get(i % this.size());
        }
    }
}
